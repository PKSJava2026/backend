package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.ChangeRequestStatus;
import com.beta.expedition.model.ChangeRequestType;
import com.beta.expedition.model.ContractChangeRequest;
import com.beta.expedition.model.ContractKind;
import com.beta.expedition.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContractChangeRequestRepository {

    private static final String COLUMNS = "id, customer_contract_id, carrier_contract_id, type, initiated_by, "
            + "proposed_price, proposed_terms, proposed_start_date, proposed_end_date, status, created_at, "
            + "resolved_at";

    private final DatabaseManager db;

    public ContractChangeRequestRepository(DatabaseManager db) {
        this.db = db;
    }

    public ContractChangeRequest save(ContractChangeRequest request) {
        String sql = "INSERT INTO contract_change_requests (" + contractColumn(request.getKind()) + ", type, "
                + "initiated_by, proposed_price, proposed_terms, proposed_start_date, proposed_end_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, request.getContractId());
            statement.setString(2, request.getType().name());
            statement.setLong(3, request.getInitiatedBy());
            statement.setBigDecimal(4, request.getProposedPrice());
            statement.setString(5, request.getProposedTerms());
            statement.setObject(6, request.getProposedStartDate(), Types.DATE);
            statement.setObject(7, request.getProposedEndDate(), Types.DATE);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                request.setId(rs.getLong("id"));
                request.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            }
            return request;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить запрос: " + e.getMessage(), e);
        }
    }

    public boolean resolve(long id, ChangeRequestStatus status) {
        String sql = "UPDATE contract_change_requests SET status = ?, resolved_at = now() WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось изменить запрос: " + e.getMessage(), e);
        }
    }

    public Optional<ContractChangeRequest> findById(long id) {
        String sql = "SELECT " + COLUMNS + " FROM contract_change_requests WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось найти запрос: " + e.getMessage(), e);
        }
    }

    public Optional<ContractChangeRequest> findPendingByContract(ContractKind kind, long contractId) {
        String sql = "SELECT " + COLUMNS + " FROM contract_change_requests WHERE " + contractColumn(kind)
                + " = ? AND status = 'PENDING'";
        List<ContractChangeRequest> found = findList(sql, contractId);
        return found.stream().findFirst();
    }

    public List<ContractChangeRequest> findPendingByCounterparty(ContractKind kind, long counterpartyId) {
        String sql = "SELECT r.* FROM contract_change_requests r JOIN " + contractTable(kind) + " c ON c.id = r."
                + contractColumn(kind) + " WHERE r.status = 'PENDING' AND c." + counterpartyColumn(kind)
                + " = ? ORDER BY r.id";
        return findList(sql, counterpartyId);
    }

    public List<ContractChangeRequest> findPending(ContractKind kind) {
        String sql = "SELECT " + COLUMNS + " FROM contract_change_requests WHERE " + contractColumn(kind)
                + " IS NOT NULL AND status = 'PENDING' ORDER BY id";
        return findList(sql, null);
    }

    private List<ContractChangeRequest> findList(String sql, Long parameter) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setLong(1, parameter);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<ContractChangeRequest> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить запросы: " + e.getMessage(), e);
        }
    }

    private ContractChangeRequest map(ResultSet rs) throws SQLException {
        ContractChangeRequest request = new ContractChangeRequest();
        request.setId(rs.getLong("id"));
        long customerContractId = rs.getLong("customer_contract_id");
        if (rs.wasNull()) {
            request.setKind(ContractKind.CARRIER);
            request.setContractId(rs.getLong("carrier_contract_id"));
        } else {
            request.setKind(ContractKind.CUSTOMER);
            request.setContractId(customerContractId);
        }
        request.setType(ChangeRequestType.valueOf(rs.getString("type")));
        request.setInitiatedBy(rs.getLong("initiated_by"));
        request.setProposedPrice(rs.getBigDecimal("proposed_price"));
        request.setProposedTerms(rs.getString("proposed_terms"));
        request.setProposedStartDate(rs.getObject("proposed_start_date", LocalDate.class));
        request.setProposedEndDate(rs.getObject("proposed_end_date", LocalDate.class));
        request.setStatus(ChangeRequestStatus.valueOf(rs.getString("status")));
        request.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        request.setResolvedAt(rs.getObject("resolved_at", OffsetDateTime.class));
        return request;
    }

    private String contractColumn(ContractKind kind) {
        return kind == ContractKind.CUSTOMER ? "customer_contract_id" : "carrier_contract_id";
    }

    private String contractTable(ContractKind kind) {
        return kind == ContractKind.CUSTOMER ? "customer_contracts" : "carrier_contracts";
    }

    private String counterpartyColumn(ContractKind kind) {
        return kind == ContractKind.CUSTOMER ? "customer_id" : "carrier_id";
    }
}

package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.AbstractContract;
import com.beta.expedition.model.ContractStatus;
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

public abstract class ContractRepository<T extends AbstractContract> {

    private final DatabaseManager db;

    protected ContractRepository(DatabaseManager db) {
        this.db = db;
    }

    protected abstract String table();

    protected abstract String counterpartyColumn();

    protected abstract long counterpartyId(T contract);

    protected abstract T map(ResultSet rs) throws SQLException;

    protected void fillCommon(AbstractContract contract, ResultSet rs) throws SQLException {
        contract.setId(rs.getLong("id"));
        contract.setOrderId(rs.getLong("order_id"));
        contract.setForwarderId(rs.getLong("forwarder_id"));
        contract.setPrice(rs.getBigDecimal("price"));
        contract.setTerms(rs.getString("terms"));
        contract.setStartDate(rs.getObject("start_date", LocalDate.class));
        contract.setEndDate(rs.getObject("end_date", LocalDate.class));
        contract.setStatus(ContractStatus.valueOf(rs.getString("status")));
        contract.setCreatedBy(rs.getLong("created_by"));
        contract.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
    }

    public T save(T contract) {
        String sql = "INSERT INTO " + table() + " (order_id, " + counterpartyColumn() + ", forwarder_id, price, "
                + "terms, start_date, end_date, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING id, created_at";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, contract.getOrderId());
            statement.setLong(2, counterpartyId(contract));
            statement.setLong(3, contract.getForwarderId());
            statement.setBigDecimal(4, contract.getPrice());
            statement.setString(5, contract.getTerms());
            statement.setObject(6, contract.getStartDate(), Types.DATE);
            statement.setObject(7, contract.getEndDate(), Types.DATE);
            statement.setString(8, contract.getStatus().name());
            statement.setLong(9, contract.getCreatedBy());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                contract.setId(rs.getLong("id"));
                contract.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            }
            return contract;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить договор: " + e.getMessage(), e);
        }
    }

    public boolean update(T contract) {
        String sql = "UPDATE " + table() + " SET price = ?, terms = ?, start_date = ?, end_date = ?, status = ? "
                + "WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, contract.getPrice());
            statement.setString(2, contract.getTerms());
            statement.setObject(3, contract.getStartDate(), Types.DATE);
            statement.setObject(4, contract.getEndDate(), Types.DATE);
            statement.setString(5, contract.getStatus().name());
            statement.setLong(6, contract.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось изменить договор: " + e.getMessage(), e);
        }
    }

    public Optional<T> findById(long id) {
        String sql = "SELECT * FROM " + table() + " WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось найти договор: " + e.getMessage(), e);
        }
    }

    public List<T> findAll() {
        return findList("SELECT * FROM " + table() + " ORDER BY id", null);
    }

    public List<T> findByStatus(ContractStatus status) {
        return findList("SELECT * FROM " + table() + " WHERE status = ? ORDER BY id", status.name());
    }

    private List<T> findList(String sql, String parameter) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setString(1, parameter);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить договоры: " + e.getMessage(), e);
        }
    }
}

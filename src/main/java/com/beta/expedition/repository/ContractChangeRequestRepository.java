package com.beta.expedition.repository;

import com.beta.expedition.model.ContractChangeRequest;
import com.beta.expedition.model.enums.RequestStatus;
import com.beta.expedition.model.enums.RequestType;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class ContractChangeRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    public ContractChangeRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ContractChangeRequest> mapper = (rs, rowNum) -> {
        ContractChangeRequest r = new ContractChangeRequest();
        r.setId(UUID.fromString(rs.getString("request_id")));
        String customerContractId = rs.getString("customer_contract_id");
        r.setCustomerContractId(customerContractId != null ? UUID.fromString(customerContractId) : null);
        String carrierContractId = rs.getString("carrier_contract_id");
        r.setCarrierContractId(carrierContractId != null ? UUID.fromString(carrierContractId) : null);
        r.setType(RequestType.valueOf(rs.getString("type")));
        r.setInitiatedById(UUID.fromString(rs.getString("initiated_by")));
        r.setProposedChangesJson(rs.getString("proposed_changes"));
        r.setPreviousValuesJson(rs.getString("previous_values"));
        r.setStatus(RequestStatus.valueOf(rs.getString("status")));
        r.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        r.setResolvedAt(rs.getTimestamp("resolved_at") != null ? rs.getTimestamp("resolved_at").toInstant() : null);
        return r;
    };

    private PGobject toJsonb(String json) throws SQLException {
        PGobject o = new PGobject();
        o.setType("jsonb");
        o.setValue(json);
        return o;
    }

    public List<ContractChangeRequest> findPendingForUser(UUID userId) {
        return jdbcTemplate.query(
            "SELECT r.* FROM contract_change_requests r " +
            "LEFT JOIN customer_contracts cc ON r.customer_contract_id = cc.contract_id " +
            "LEFT JOIN carrier_contracts crc ON r.carrier_contract_id = crc.contract_id " +
            "WHERE r.status = 'PENDING' AND (cc.forwarder_id = ? OR cc.customer_id = ? OR crc.forwarder_id = ? OR crc.carrier_id = ?) " +
            "AND r.initiated_by != ?",
            mapper, userId, userId, userId, userId, userId
        );
    }

    public ContractChangeRequest save(ContractChangeRequest r) {
        try {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                "INSERT INTO contract_change_requests (request_id, customer_contract_id, carrier_contract_id, type, initiated_by, proposed_changes, previous_values, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, now())",
                id, r.getCustomerContractId(), r.getCarrierContractId(), r.getType().name(), r.getInitiatedById(),
                toJsonb(r.getProposedChangesJson()), toJsonb(r.getPreviousValuesJson()), r.getStatus().name()
            );
            r.setId(id);
            return r;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения JSON", e);
        }
    }

    public void updateStatus(UUID id, RequestStatus status) {
        jdbcTemplate.update(
            "UPDATE contract_change_requests SET status=?, resolved_at=now() WHERE request_id=?",
            status.name(), id
        );
    }
}
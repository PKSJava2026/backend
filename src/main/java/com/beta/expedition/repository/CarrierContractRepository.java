package com.beta.expedition.repository;

import com.beta.expedition.model.CarrierContract;
import com.beta.expedition.model.enums.ContractStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CarrierContractRepository {

    private final JdbcTemplate jdbcTemplate;

    public CarrierContractRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CarrierContract> mapper = (rs, rowNum) -> {
        String applicationId = rs.getString("application_id");
        return new CarrierContract(
                UUID.fromString(rs.getString("contract_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("forwarder_id")),
                UUID.fromString(rs.getString("carrier_id")),
                applicationId != null ? UUID.fromString(applicationId) : null,
                rs.getBigDecimal("price"),
                rs.getString("terms"),
                rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                ContractStatus.valueOf(rs.getString("status")),
                UUID.fromString(rs.getString("created_by")),
                rs.getTimestamp("created_at").toInstant()
        );
    };

    public List<CarrierContract> findAll() {
        return jdbcTemplate.query("SELECT * FROM carrier_contracts ORDER BY created_at DESC", mapper);
    }

    public List<CarrierContract> findByStatus(ContractStatus status) {
        return jdbcTemplate.query(
                "SELECT * FROM carrier_contracts WHERE status = ? ORDER BY created_at DESC",
                mapper,
                status.name()
        );
    }

    public Optional<CarrierContract> findById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM carrier_contracts WHERE contract_id = ?", mapper, id)
                .stream()
                .findFirst();
    }

    public CarrierContract save(CarrierContract c) {
        if (c.getId() == null) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO carrier_contracts (contract_id, order_id, application_id, carrier_id, forwarder_id, price, terms, start_date, end_date, status, created_by, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())",
                    id, c.getOrderId(), c.getApplicationId(), c.getCarrierId(), c.getForwarderId(),
                    c.getPrice(), c.getTerms(), c.getStartDate(), c.getEndDate(),
                    c.getStatus().name(), c.getCreatedById()
            );
            c.setId(id);
        } else {
            jdbcTemplate.update(
                    "UPDATE carrier_contracts SET price=?, terms=?, start_date=?, end_date=?, status=? WHERE contract_id=?",
                    c.getPrice(), c.getTerms(), c.getStartDate(), c.getEndDate(), c.getStatus().name(), c.getId()
            );
        }
        return c;
    }

    public void deleteById(UUID id) {
        jdbcTemplate.update("DELETE FROM carrier_contracts WHERE contract_id = ?", id);
    }
}

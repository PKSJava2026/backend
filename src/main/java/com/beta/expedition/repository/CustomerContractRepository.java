package com.beta.expedition.repository;

import com.beta.expedition.model.CustomerContract;
import com.beta.expedition.model.enums.ContractStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerContractRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerContractRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CustomerContract> mapper = (rs, rowNum) -> new CustomerContract(
        UUID.fromString(rs.getString("contract_id")),
        UUID.fromString(rs.getString("order_id")),
        UUID.fromString(rs.getString("forwarder_id")),
        UUID.fromString(rs.getString("customer_id")),
        rs.getBigDecimal("price"),
        rs.getString("terms"),
        rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
        rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
        ContractStatus.valueOf(rs.getString("status")),
        UUID.fromString(rs.getString("created_by")),
        rs.getTimestamp("created_at").toInstant()
    );

    public List<CustomerContract> findAll() {
        return jdbcTemplate.query("SELECT * FROM customer_contracts ORDER BY created_at DESC", mapper);
    }

    public List<CustomerContract> findByStatus(ContractStatus status) {
        return jdbcTemplate.query("SELECT * FROM customer_contracts WHERE status = ? ORDER BY created_at DESC", mapper, status.name());
    }

    public Optional<CustomerContract> findById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM customer_contracts WHERE contract_id = ?", mapper, id)
                .stream().findFirst();
    }

    // поиск по дате заключения — пункт ТЗ "поиск по id/дате"
    public List<CustomerContract> findByCreatedDate(java.time.LocalDate date) {
        return jdbcTemplate.query(
            "SELECT * FROM customer_contracts WHERE created_at::date = ? ORDER BY created_at DESC",
            mapper, date
        );
    }

    public CustomerContract save(CustomerContract c) {
        if (c.getId() == null) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                "INSERT INTO customer_contracts (contract_id, order_id, forwarder_id, customer_id, price, terms, start_date, end_date, status, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())",
                id, c.getOrderId(), c.getForwarderId(), c.getCustomerId(), c.getPrice(), c.getTerms(),
                c.getStartDate(), c.getEndDate(), c.getStatus().name(), c.getCreatedById()
            );
            c.setId(id);
        } else {
            jdbcTemplate.update(
                "UPDATE customer_contracts SET price=?, terms=?, start_date=?, end_date=?, status=? WHERE contract_id=?",
                c.getPrice(), c.getTerms(), c.getStartDate(), c.getEndDate(), c.getStatus().name(), c.getId()
            );
        }
        return c;
    }

    public void deleteById(UUID id) {
        jdbcTemplate.update("DELETE FROM customer_contracts WHERE contract_id = ?", id);
    }
}
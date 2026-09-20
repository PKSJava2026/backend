package com.beta.expedition.repository;

import com.beta.expedition.model.Order;
import com.beta.expedition.model.enums.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> mapper = (rs, rowNum) -> new Order(
        UUID.fromString(rs.getString("order_id")),
        UUID.fromString(rs.getString("customer_id")),
        rs.getString("cargo_description"),
        rs.getBigDecimal("weight_kg"),
        rs.getBigDecimal("volume_m3"),
        rs.getString("origin"),
        rs.getString("destination"),
        rs.getDate("desired_date") != null ? rs.getDate("desired_date").toLocalDate() : null,
        OrderStatus.valueOf(rs.getString("status")),
        rs.getTimestamp("created_at").toInstant()
    );

    public List<Order> findAll() {
        return jdbcTemplate.query("SELECT * FROM orders ORDER BY created_at DESC", mapper);
    }

    public List<Order> findByStatus(OrderStatus status) {
        return jdbcTemplate.query("SELECT * FROM orders WHERE status = ? ORDER BY created_at DESC", mapper, status.name());
    }

    public Optional<Order> findById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM orders WHERE order_id = ?", mapper, id)
                .stream().findFirst();
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                "INSERT INTO orders (order_id, customer_id, cargo_description, weight_kg, volume_m3, origin, destination, desired_date, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, now())",
                id, order.getCustomerId(), order.getCargoDescription(), order.getWeightKg(), order.getVolumeM3(),
                order.getOrigin(), order.getDestination(), order.getDesiredDate(), order.getStatus().name()
            );
            order.setId(id);
        } else {
            jdbcTemplate.update(
                "UPDATE orders SET cargo_description=?, weight_kg=?, volume_m3=?, origin=?, destination=?, desired_date=?, status=? WHERE order_id=?",
                order.getCargoDescription(), order.getWeightKg(), order.getVolumeM3(),
                order.getOrigin(), order.getDestination(), order.getDesiredDate(), order.getStatus().name(), order.getId()
            );
        }
        return order;
    }

    public void deleteById(UUID id) {
        jdbcTemplate.update("DELETE FROM orders WHERE order_id = ?", id);
    }
}
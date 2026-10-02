package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.Order;
import com.beta.expedition.model.OrderStatus;
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

public class OrderRepository {

    private static final String COLUMNS = "id, customer_id, cargo_description, weight_kg, volume_m3, "
            + "origin, destination, desired_date, status, created_at";

    private final DatabaseManager db;

    public OrderRepository(DatabaseManager db) {
        this.db = db;
    }

    public Order save(Order order) {
        String sql = "INSERT INTO orders (customer_id, cargo_description, weight_kg, volume_m3, origin, "
                + "destination, desired_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, order.getCustomerId());
            statement.setString(2, order.getCargoDescription());
            statement.setBigDecimal(3, order.getWeightKg());
            statement.setBigDecimal(4, order.getVolumeM3());
            statement.setString(5, order.getOrigin());
            statement.setString(6, order.getDestination());
            statement.setObject(7, order.getDesiredDate(), Types.DATE);
            statement.setString(8, order.getStatus().name());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                order.setId(rs.getLong("id"));
                order.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            }
            return order;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить заявку: " + e.getMessage(), e);
        }
    }

    /** @return false, если заявки с таким id нет */
    public boolean update(Order order) {
        String sql = "UPDATE orders SET cargo_description = ?, weight_kg = ?, volume_m3 = ?, origin = ?, "
                + "destination = ?, desired_date = ?, status = ? WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, order.getCargoDescription());
            statement.setBigDecimal(2, order.getWeightKg());
            statement.setBigDecimal(3, order.getVolumeM3());
            statement.setString(4, order.getOrigin());
            statement.setString(5, order.getDestination());
            statement.setObject(6, order.getDesiredDate(), Types.DATE);
            statement.setString(7, order.getStatus().name());
            statement.setLong(8, order.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось изменить заявку: " + e.getMessage(), e);
        }
    }

    /** @return false, если заявки с таким id нет */
    public boolean delete(long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось удалить заявку: " + e.getMessage(), e);
        }
    }

    public Optional<Order> findById(long id) {
        String sql = "SELECT " + COLUMNS + " FROM orders WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось найти заявку: " + e.getMessage(), e);
        }
    }

    public List<Order> findAll() {
        return findList("SELECT " + COLUMNS + " FROM orders ORDER BY id", null);
    }

    public List<Order> findByCustomerId(long customerId) {
        return findList("SELECT " + COLUMNS + " FROM orders WHERE customer_id = ? ORDER BY id", customerId);
    }

    private List<Order> findList(String sql, Long parameter) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setLong(1, parameter);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<Order> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить заявки: " + e.getMessage(), e);
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setCustomerId(rs.getLong("customer_id"));
        order.setCargoDescription(rs.getString("cargo_description"));
        order.setWeightKg(rs.getBigDecimal("weight_kg"));
        order.setVolumeM3(rs.getBigDecimal("volume_m3"));
        order.setOrigin(rs.getString("origin"));
        order.setDestination(rs.getString("destination"));
        order.setDesiredDate(rs.getObject("desired_date", LocalDate.class));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return order;
    }
}

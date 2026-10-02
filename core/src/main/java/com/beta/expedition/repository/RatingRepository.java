package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.Rating;
import com.beta.expedition.model.RatingSummary;
import com.beta.expedition.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class RatingRepository {

    private final DatabaseManager db;

    public RatingRepository(DatabaseManager db) {
        this.db = db;
    }

    public Rating save(Rating rating) {
        String sql = "INSERT INTO ratings (order_id, from_user_id, to_user_id, score, comment) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, rating.getOrderId());
            statement.setLong(2, rating.getFromUserId());
            statement.setLong(3, rating.getToUserId());
            statement.setInt(4, rating.getScore());
            statement.setString(5, rating.getComment());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                rating.setId(rs.getLong("id"));
                rating.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            }
            return rating;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить оценку: " + e.getMessage(), e);
        }
    }

    public boolean exists(long orderId, long fromUserId) {
        String sql = "SELECT 1 FROM ratings WHERE order_id = ? AND from_user_id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setLong(2, fromUserId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось проверить оценку: " + e.getMessage(), e);
        }
    }

    public List<Rating> findByToUserId(long userId) {
        String sql = "SELECT id, order_id, from_user_id, to_user_id, score, comment, created_at FROM ratings "
                + "WHERE to_user_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Rating> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить оценки: " + e.getMessage(), e);
        }
    }

    public RatingSummary summaryAll() {
        String sql = "SELECT count(*), coalesce(avg(score), 0) FROM ratings";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return new RatingSummary(rs.getInt(1), rs.getDouble(2));
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось посчитать оценки: " + e.getMessage(), e);
        }
    }

    private Rating map(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getLong("id"));
        rating.setOrderId(rs.getLong("order_id"));
        rating.setFromUserId(rs.getLong("from_user_id"));
        rating.setToUserId(rs.getLong("to_user_id"));
        rating.setScore(rs.getInt("score"));
        rating.setComment(rs.getString("comment"));
        rating.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return rating;
    }
}

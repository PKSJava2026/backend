package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.Notification;
import com.beta.expedition.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private final DatabaseManager db;

    public NotificationRepository(DatabaseManager db) {
        this.db = db;
    }

    public Notification save(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, change_request_id, message) VALUES (?, ?, ?) "
                + "RETURNING id, created_at";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, notification.getUserId());
            statement.setObject(2, notification.getChangeRequestId(), Types.BIGINT);
            statement.setString(3, notification.getMessage());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                notification.setId(rs.getLong("id"));
                notification.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            }
            return notification;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить уведомление: " + e.getMessage(), e);
        }
    }

    /** Уведомления пользователя, новые сверху. */
    public List<Notification> findByUserId(long userId) {
        String sql = "SELECT id, user_id, change_request_id, message, is_read, created_at FROM notifications "
                + "WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Notification> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить уведомления: " + e.getMessage(), e);
        }
    }

    public int countUnread(long userId) {
        String sql = "SELECT count(*) FROM notifications WHERE user_id = ? AND NOT is_read";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось посчитать уведомления: " + e.getMessage(), e);
        }
    }

    /** @return false, если у пользователя нет уведомления с таким id */
    public boolean markRead(long id, long userId) {
        return update("UPDATE notifications SET is_read = true WHERE id = ? AND user_id = ?", id, userId) > 0;
    }

    public int markAllRead(long userId) {
        return update("UPDATE notifications SET is_read = true WHERE user_id = ? AND NOT is_read", userId);
    }

    private int update(String sql, long... parameters) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setLong(i + 1, parameters[i]);
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось изменить уведомления: " + e.getMessage(), e);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getLong("id"));
        notification.setUserId(rs.getLong("user_id"));
        long requestId = rs.getLong("change_request_id");
        notification.setChangeRequestId(rs.wasNull() ? null : requestId);
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return notification;
    }
}

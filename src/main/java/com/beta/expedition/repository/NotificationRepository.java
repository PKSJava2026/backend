package com.beta.expedition.repository;

import com.beta.expedition.model.Notification;
import com.beta.expedition.model.enums.NotificationType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> mapper = (rs, rowNum) -> new Notification(
        UUID.fromString(rs.getString("notification_id")),
        UUID.fromString(rs.getString("user_id")),
        NotificationType.valueOf(rs.getString("type")),
        rs.getString("request_id") != null ? UUID.fromString(rs.getString("request_id")) : null,
        rs.getString("message"),
        rs.getBoolean("is_read"),
        rs.getTimestamp("created_at").toInstant()
    );

    public List<Notification> findUnreadByUser(UUID userId) {
        return jdbcTemplate.query(
            "SELECT * FROM notifications WHERE user_id = ? AND is_read = false ORDER BY created_at DESC",
            mapper, userId
        );
    }

    public void save(Notification n) {
        jdbcTemplate.update(
            "INSERT INTO notifications (notification_id, user_id, type, request_id, message, is_read, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, now())",
            UUID.randomUUID(), n.getUserId(), n.getType().name(), n.getRequestId(), n.getMessage(), n.isRead()
        );
    }

    public void markRead(UUID id) {
        jdbcTemplate.update("UPDATE notifications SET is_read = true WHERE notification_id = ?", id);
    }
}
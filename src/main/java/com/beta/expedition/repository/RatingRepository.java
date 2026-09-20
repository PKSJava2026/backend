package com.beta.expedition.repository;

import com.beta.expedition.model.Rating;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class RatingRepository {

    private final JdbcTemplate jdbcTemplate;

    public RatingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Rating> mapper = (rs, rowNum) -> new Rating(
        UUID.fromString(rs.getString("rating_id")),
        UUID.fromString(rs.getString("order_id")),
        UUID.fromString(rs.getString("from_user_id")),
        UUID.fromString(rs.getString("to_user_id")),
        rs.getShort("score"),
        rs.getString("comment"),
        rs.getTimestamp("created_at").toInstant()
    );

    public List<Rating> findByToUser(UUID userId) {
        return jdbcTemplate.query("SELECT * FROM ratings WHERE to_user_id = ?", mapper, userId);
    }

    public void save(Rating r) {
        jdbcTemplate.update(
            "INSERT INTO ratings (rating_id, order_id, from_user_id, to_user_id, score, comment, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, now())",
            UUID.randomUUID(), r.getOrderId(), r.getFromUserId(), r.getToUserId(), r.getScore(), r.getComment()
        );
    }
}
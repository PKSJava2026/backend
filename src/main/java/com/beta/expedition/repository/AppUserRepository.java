package com.beta.expedition.repository;

import com.beta.expedition.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AppUser> mapper = (rs, rowNum) -> new AppUser(
        UUID.fromString(rs.getString("user_id")),
        rs.getShort("role_id"),
        rs.getString("email"),
        rs.getString("password_hash"),
        rs.getString("full_name"),
        rs.getString("phone"),
        rs.getString("company_name"),
        rs.getBoolean("is_active"),
        rs.getTimestamp("created_at").toInstant()
    );

    public List<AppUser> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY created_at DESC", mapper);
    }

    public Optional<AppUser> findById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE user_id = ?", mapper, id)
                .stream().findFirst();
    }

    public Optional<AppUser> findByFullNameIgnoreCase(String fullName) {
        return jdbcTemplate.query(
                "SELECT * FROM users WHERE LOWER(full_name) = LOWER(?)", mapper, fullName
        ).stream().findFirst();
    }

    public boolean existsByFullNameIgnoreCase(String fullName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(full_name) = LOWER(?)", Integer.class, fullName
        );
        return count != null && count > 0;
    }

    public boolean existsByEmailIgnoreCase(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)", Integer.class, email
        );
        return count != null && count > 0;
    }

    public AppUser save(AppUser user) {
        if (user.getId() == null) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                "INSERT INTO users (user_id, role_id, email, password_hash, full_name, phone, company_name, is_active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, now())",
                id, user.getRoleId(), user.getEmail(), user.getPasswordHash(),
                user.getFullName(), user.getPhone(), user.getCompanyName(), user.isActive()
            );
            user.setId(id);
        } else {
            jdbcTemplate.update(
                "UPDATE users SET role_id=?, email=?, password_hash=?, full_name=?, phone=?, company_name=?, is_active=? WHERE user_id=?",
                user.getRoleId(), user.getEmail(), user.getPasswordHash(),
                user.getFullName(), user.getPhone(), user.getCompanyName(), user.isActive(), user.getId()
            );
        }
        return user;
    }
}
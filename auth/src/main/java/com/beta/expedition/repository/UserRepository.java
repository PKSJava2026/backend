package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.Role;
import com.beta.expedition.model.User;
import com.beta.expedition.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private static final String COLUMNS = "id, nickname, email, password_hash, role, is_active, created_at";

    private final DatabaseManager db;

    public UserRepository(DatabaseManager db) {
        this.db = db;
    }

    /** Вставляет пользователя и заполняет сгенерированные id и createdAt. */
    public User save(User user) {
        String sql = "INSERT INTO users (nickname, email, password_hash, role) VALUES (?, ?, ?, ?) "
                + "RETURNING id, created_at";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getNickname());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                user.setId(rs.getLong("id"));
                user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить пользователя: " + e.getMessage(), e);
        }
    }

    public Optional<User> findById(long id) {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", id);
    }

    public Optional<User> findByNickname(String nickname) {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE lower(nickname) = lower(?)", nickname);
    }

    public Optional<User> findByEmail(String email) {
        return findOne("SELECT " + COLUMNS + " FROM users WHERE email = ?", email);
    }

    public List<User> findByRole(Role role) {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE role = ? ORDER BY id";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            try (ResultSet rs = statement.executeQuery()) {
                List<User> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить пользователей: " + e.getMessage(), e);
        }
    }

    /** @return false, если пользователя с таким id нет */
    public boolean setActive(long id, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось изменить пользователя: " + e.getMessage(), e);
        }
    }

    private Optional<User> findOne(String sql, Object parameter) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось найти пользователя: " + e.getMessage(), e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setNickname(rs.getString("nickname"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return user;
    }
}

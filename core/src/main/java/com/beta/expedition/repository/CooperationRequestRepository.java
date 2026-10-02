package com.beta.expedition.repository;

import com.beta.expedition.exception.DatabaseException;
import com.beta.expedition.model.CooperationRequest;
import com.beta.expedition.model.CooperationStatus;
import com.beta.expedition.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CooperationRequestRepository {

    private static final String COLUMNS = "id, carrier_id, description, vehicle_info, status, created_at";

    private final DatabaseManager db;

    public CooperationRequestRepository(DatabaseManager db) {
        this.db = db;
    }

    public CooperationRequest save(CooperationRequest request) {
        String sql = "INSERT INTO cooperation_requests (carrier_id, description, vehicle_info, status) "
                + "VALUES (?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, request.getCarrierId());
            statement.setString(2, request.getDescription());
            statement.setString(3, request.getVehicleInfo());
            statement.setString(4, request.getStatus().name());
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                request.setId(rs.getLong("id"));
                request.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            }
            return request;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось сохранить заявку о сотрудничестве: " + e.getMessage(), e);
        }
    }

    /** @return false, если заявки с таким id нет */
    public boolean updateStatus(long id, CooperationStatus status) {
        String sql = "UPDATE cooperation_requests SET status = ? WHERE id = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось изменить заявку о сотрудничестве: " + e.getMessage(), e);
        }
    }

    public Optional<CooperationRequest> findById(long id) {
        List<CooperationRequest> found = findList(
                "SELECT " + COLUMNS + " FROM cooperation_requests WHERE id = ?", id);
        return found.stream().findFirst();
    }

    public List<CooperationRequest> findByCarrierId(long carrierId) {
        return findList("SELECT " + COLUMNS + " FROM cooperation_requests WHERE carrier_id = ? ORDER BY id",
                carrierId);
    }

    public List<CooperationRequest> findByStatus(CooperationStatus status) {
        return findList("SELECT " + COLUMNS + " FROM cooperation_requests WHERE status = ? ORDER BY id",
                status.name());
    }

    private List<CooperationRequest> findList(String sql, Object parameter) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet rs = statement.executeQuery()) {
                List<CooperationRequest> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось получить заявки о сотрудничестве: " + e.getMessage(), e);
        }
    }

    private CooperationRequest map(ResultSet rs) throws SQLException {
        CooperationRequest request = new CooperationRequest();
        request.setId(rs.getLong("id"));
        request.setCarrierId(rs.getLong("carrier_id"));
        request.setDescription(rs.getString("description"));
        request.setVehicleInfo(rs.getString("vehicle_info"));
        request.setStatus(CooperationStatus.valueOf(rs.getString("status")));
        request.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return request;
    }
}

package com.beta.expedition.repository;

import com.beta.expedition.model.CarrierApplication;
import com.beta.expedition.model.enums.ApplicationStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CarrierApplicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public CarrierApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CarrierApplication> mapper = (rs, rowNum) -> new CarrierApplication(
        UUID.fromString(rs.getString("application_id")),
        UUID.fromString(rs.getString("carrier_id")),
        rs.getString("description"),
        rs.getString("vehicle_info"),
        ApplicationStatus.valueOf(rs.getString("status")),
        rs.getTimestamp("created_at").toInstant()
    );

    public List<CarrierApplication> findAll() {
        return jdbcTemplate.query("SELECT * FROM carrier_applications ORDER BY created_at DESC", mapper);
    }

    public CarrierApplication save(CarrierApplication a) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO carrier_applications (application_id, carrier_id, description, vehicle_info, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, now())",
            id, a.getCarrierId(), a.getDescription(), a.getVehicleInfo(), a.getStatus().name()
        );
        a.setId(id);
        return a;
    }
}
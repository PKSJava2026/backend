package com.beta.expedition.repository;

import com.beta.expedition.model.Role;
import com.beta.expedition.model.enums.RoleName;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Role> mapper = (rs, rowNum) -> new Role(
        rs.getShort("role_id"),
        RoleName.valueOf(rs.getString("name"))
    );

    public List<Role> findAll() {
        return jdbcTemplate.query("SELECT * FROM roles", mapper);
    }

    public Optional<Role> findByName(RoleName name) {
        return jdbcTemplate.query("SELECT * FROM roles WHERE name = ?", mapper, name.name())
                .stream().findFirst();
    }
}
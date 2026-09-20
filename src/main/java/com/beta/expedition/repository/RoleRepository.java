package com.beta.expedition.repository;

import com.beta.expedition.model.Role;
import com.beta.expedition.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {

    Optional<Role> findByName(RoleName name);
}
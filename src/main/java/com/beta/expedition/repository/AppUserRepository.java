package com.beta.expedition.repository;

import com.beta.expedition.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    @EntityGraph(attributePaths = "role")
    Optional<AppUser> findByFullNameIgnoreCase(String fullName);

    boolean existsByFullNameIgnoreCase(String fullName);

    boolean existsByEmailIgnoreCase(String email);
}
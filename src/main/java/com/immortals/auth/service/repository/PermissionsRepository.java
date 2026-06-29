package com.immortals.auth.service.repository;

import com.immortals.auth.service.domain.entity.Permissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionsRepository extends JpaRepository<Permissions, Long> {
    Optional<Permissions> findByName(String name);

    boolean existsByName(String name);
}

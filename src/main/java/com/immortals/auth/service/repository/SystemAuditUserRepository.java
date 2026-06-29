package com.immortals.auth.service.repository;

import com.immortals.auth.service.domain.entity.SystemAuditUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemAuditUserRepository extends JpaRepository<SystemAuditUser, Long> {
    @Query("SELECT u FROM SystemAuditUser u WHERE u.active = true")
    Optional<SystemAuditUser> findActiveSystemUser();
}

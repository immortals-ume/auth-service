package com.immortals.auth.service.repository;

import com.immortals.auth.service.audit.AuditStatus;
import com.immortals.auth.service.audit.AuditType;
import com.immortals.auth.service.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUsernameOrderByCreatedDateDesc(String username, Pageable pageable);

    Page<AuditLog> findByAuditTypeOrderByCreatedDateDesc(AuditType auditType, Pageable pageable);

    Page<AuditLog> findByStatusOrderByCreatedDateDesc(AuditStatus status, Pageable pageable);

    List<AuditLog> findByRequestId(String requestId);

    Page<AuditLog> findByCreatedDateBetweenOrderByCreatedDateDesc(Instant from, Instant to, Pageable pageable);

    Page<AuditLog> findByResourceTypeAndResourceIdOrderByCreatedDateDesc(
            String resourceType, String resourceId, Pageable pageable);
}


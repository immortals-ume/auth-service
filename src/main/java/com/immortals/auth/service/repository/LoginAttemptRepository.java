package com.immortals.auth.service.repository;

import com.immortals.auth.service.domain.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, String> {
}
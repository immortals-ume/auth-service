package com.immortals.auth.service.repository;

import com.immortals.auth.service.domain.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByUsername(String username);

    void deleteByUsername(String username);
}

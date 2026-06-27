package com.immortals.auth.service.repository;

import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenAndType(String token, VerificationToken.TokenType type);

    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user AND vt.type = :type")
    void deleteByUserAndType(@Param("user") User user, @Param("type") VerificationToken.TokenType type);
}

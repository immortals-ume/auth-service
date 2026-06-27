package com.immortals.auth.service.repository;


import com.immortals.auth.service.domain.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"}, type = EntityGraph.EntityGraphType.LOAD)
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select user from User user where user.activeInd = true and (user.userName = :userNameOrEmailOrPhone or user.email = :userNameOrEmailOrPhone or user.contactNumber = :userNameOrEmailOrPhone)")
    Optional<User> findUser(@Param("userNameOrEmailOrPhone") String userNameOrEmailOrPhone);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Boolean existsByUserName(String username);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Boolean existsByEmail(String email);
}
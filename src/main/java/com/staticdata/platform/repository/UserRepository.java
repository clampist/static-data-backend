package com.staticdata.platform.repository;

import com.staticdata.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User data access layer
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find byUsernameFindUser
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Check if username exists
     */
    Boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    Boolean existsByEmail(String email);

    /**
     * Find enabled user by username
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findActiveByUsername(@Param("username") String username);

    /**
     * Find enabled user by email
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
    Optional<User> findActiveByEmail(@Param("email") String email);
}
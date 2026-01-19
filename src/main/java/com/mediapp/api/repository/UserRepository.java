package com.mediapp.api.repository;

import com.mediapp.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.workspace WHERE u.email = :email")
    Optional<User> findByEmailWithWorkspace(@Param("email") String email);

    boolean existsByEmail(String email);
}


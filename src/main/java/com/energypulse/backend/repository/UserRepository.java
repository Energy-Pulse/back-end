package com.energypulse.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.energypulse.backend.model.User;

@Repository 
public interface UserRepository extends JpaRepository<User, Long>{

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    User findByUserId(UUID userId);

    Optional<User> findByUsername(String username);

}

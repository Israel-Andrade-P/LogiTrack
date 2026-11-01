package com.zeldev.auth_service.repository;

import com.zeldev.auth_service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    Optional<UserEntity> findUserByEmail(String email);
}

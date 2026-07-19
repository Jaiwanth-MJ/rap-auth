package com.rapauth.rapauth.repository;

import com.rapauth.rapauth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRegisterNumber(String registerNumber);

    boolean existsByEmail(String email);

    boolean existsByRegisterNumber(String registerNumber);
}
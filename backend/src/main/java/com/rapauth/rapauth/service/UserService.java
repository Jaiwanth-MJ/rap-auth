package com.rapauth.rapauth.service;

import com.rapauth.rapauth.dto.user.UserRequest;
import com.rapauth.rapauth.dto.user.UserResponse;
import com.rapauth.rapauth.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User save(User user);

    UserResponse createUser(UserRequest request);

    Optional<User> findByEmail(String email);

    Optional<User> findByRegisterNumber(String registerNumber);

    List<User> findAll();

    Optional<User> findById(Long id);
}
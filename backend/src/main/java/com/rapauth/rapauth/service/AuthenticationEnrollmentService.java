package com.rapauth.rapauth.service;

import com.rapauth.rapauth.entity.auth.AuthenticationMethod;
import com.rapauth.rapauth.entity.user.User;

import java.util.List;

public interface AuthenticationEnrollmentService {

    void enrollMethods(User user, List<AuthenticationMethod> methods);
}
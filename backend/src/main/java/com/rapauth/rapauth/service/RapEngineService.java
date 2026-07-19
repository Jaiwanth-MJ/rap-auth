package com.rapauth.rapauth.service;

import com.rapauth.rapauth.entity.auth.LoginSession;
import com.rapauth.rapauth.entity.user.User;

public interface RapEngineService {

    LoginSession createLoginSession(User user);

    LoginSession regenerateAuthentication(Long sessionId);

}
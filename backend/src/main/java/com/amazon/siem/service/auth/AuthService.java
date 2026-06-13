package com.amazon.siem.service.auth;

import com.amazon.siem.dto.JwtResponse;
import com.amazon.siem.dto.LoginRequest;
import com.amazon.siem.dto.SignupRequest;

public interface AuthService {
    void registerUser(SignupRequest signupRequest);
    JwtResponse authenticateUser(LoginRequest loginRequest);
}

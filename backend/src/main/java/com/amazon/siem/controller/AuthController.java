package com.amazon.siem.controller;

import com.amazon.siem.dto.JwtResponse;
import com.amazon.siem.dto.LoginRequest;
import com.amazon.siem.dto.MessageResponse;
import com.amazon.siem.dto.SignupRequest;
import com.amazon.siem.service.auth.AuthService;
import com.amazon.siem.service.audit.AuditLogService;
import com.amazon.siem.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private RateLimiter rateLimiter;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        try {
            authService.registerUser(signupRequest);
            auditLogService.logAction(
                    signupRequest.getUsername(),
                    "USER_REGISTRATION",
                    "User",
                    "SUCCESS",
                    ipAddress,
                    "Successfully registered account: " + signupRequest.getEmail()
            );
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            auditLogService.logAction(
                    signupRequest.getUsername(),
                    "USER_REGISTRATION",
                    "User",
                    "FAILED",
                    ipAddress,
                    "Failed registration attempt: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();

        // Apply endpoint rate limiting
        if (!rateLimiter.isAllowed(ipAddress)) {
            auditLogService.logAction(
                    loginRequest.getUsername(),
                    "USER_LOGIN",
                    "AuthenticationService",
                    "BLOCKED",
                    ipAddress,
                    "Login request blocked due to rate limit threshold."
            );
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many requests. Please try again later."));
        }

        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            auditLogService.logAction(
                    loginRequest.getUsername(),
                    "USER_LOGIN",
                    "AuthenticationService",
                    "SUCCESS",
                    ipAddress,
                    "User successfully logged in."
            );
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            auditLogService.logAction(
                    loginRequest.getUsername(),
                    "USER_LOGIN",
                    "AuthenticationService",
                    "FAILED",
                    ipAddress,
                    "Failed login attempt: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid username or password."));
        }
    }
}

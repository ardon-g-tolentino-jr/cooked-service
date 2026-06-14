package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.AuthResponse;
import com.humanworkstream.cooked.dto.ForgotPasswordRequest;
import com.humanworkstream.cooked.dto.GoogleLoginRequest;
import com.humanworkstream.cooked.dto.LoginRequest;
import com.humanworkstream.cooked.dto.RegisterRequest;
import com.humanworkstream.cooked.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest req) {
        appUserService.register(req);
        // No session is returned: the user receives a temporary password by email and signs in with it.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Account created. A temporary password has been sent to your email."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(appUserService.login(req));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest req) {
        return ResponseEntity.ok(appUserService.loginWithGoogle(req.idToken()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        appUserService.forgotPassword(req.email());
        // Always 200 with the same message — never reveal whether the email is registered.
        return ResponseEntity.ok(Map.of("message", "If that email is registered, a temporary password has been sent."));
    }
}
package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.AuthResponse;
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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appUserService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(appUserService.login(req));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest req) {
        return ResponseEntity.ok(appUserService.loginWithGoogle(req.idToken()));
    }
}
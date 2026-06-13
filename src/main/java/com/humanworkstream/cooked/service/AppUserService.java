package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.AuthResponse;
import com.humanworkstream.cooked.dto.LoginRequest;
import com.humanworkstream.cooked.dto.RegisterRequest;
import com.humanworkstream.cooked.dto.UserPatchRequest;
import com.humanworkstream.cooked.dto.UserResponse;
import com.humanworkstream.cooked.entity.AppUser;
import com.humanworkstream.cooked.repository.AppUserRepository;
import com.humanworkstream.cooked.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionGateService subscriptionGate;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (appUserRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        // Gate: validate + redeem the registration code on the subscription service before
        // creating the local account. Throws (aborting signup) if the code is missing/invalid.
        subscriptionGate.provisionViaCode(req.displayName(), req.email(), req.password(), req.registrationCode());
        AppUser user = new AppUser();
        user.setEmail(req.email());
        user.setDisplayName(req.displayName());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user = appUserRepository.save(user);
        log.info("[AppUserService] Registered userId={}", user.getId());
        String token = jwtUtil.generate(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        AppUser user = appUserRepository.findOneByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        // Gate: only users with active Cooked access (redeemed code or subscription) may sign in.
        subscriptionGate.assertActiveAccess(user.getEmail());
        log.info("[AppUserService] Login userId={}", user.getId());
        String token = jwtUtil.generate(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName());
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return UserResponse.from(findById(userId));
    }

    @Transactional
    public UserResponse patchMe(Long userId, UserPatchRequest req) {
        AppUser user = findById(userId);
        if (req.displayName() != null) user.setDisplayName(req.displayName());
        if (req.handle() != null) user.setHandle(req.handle());
        return UserResponse.from(appUserRepository.save(user));
    }

    private AppUser findById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.AuthResponse;
import com.humanworkstream.cooked.dto.LoginRequest;
import com.humanworkstream.cooked.dto.RegisterRequest;
import com.humanworkstream.cooked.dto.UserPatchRequest;
import com.humanworkstream.cooked.dto.UserResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.humanworkstream.cooked.entity.AppUser;
import com.humanworkstream.cooked.repository.AppUserRepository;
import com.humanworkstream.cooked.security.GoogleTokenVerifier;
import com.humanworkstream.cooked.security.JwtUtil;
import com.humanworkstream.cooked.security.PasswordGenerator;
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
    private final GoogleTokenVerifier googleTokenVerifier;
    private final EmailService emailService;

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
        // TRIAL tier: the registration code itself names the trial.
        user.setTrial(req.registrationCode() != null && req.registrationCode().toUpperCase().contains("TRIAL"));
        user = appUserRepository.save(user);
        log.info("[AppUserService] Registered userId={} trial={}", user.getId(), user.isTrial());
        return authResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        AppUser user = appUserRepository.findOneByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        // Gate: only users with active Cooked access (redeemed code or subscription) may sign in.
        // Returns whether that access is a TRIAL code; refresh the stored flag each login.
        user.setTrial(subscriptionGate.assertActiveAccess(user.getEmail()));
        appUserRepository.save(user);
        log.info("[AppUserService] Login userId={} trial={}", user.getId(), user.isTrial());
        return authResponse(user);
    }

    /**
     * Sign in with a Google ID token. Verifies the token, enforces the same Cooked access
     * gate as password login, then finds-or-creates the local user (matched by email; SSO
     * accounts have no local password).
     */
    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        GoogleIdToken.Payload payload;
        try {
            payload = googleTokenVerifier.verify(idToken);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google sign-in");
        }
        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account has no email");
        }
        String name = payload.get("name") != null ? String.valueOf(payload.get("name")) : email;

        // Gate: only emails with active Cooked access may sign in (same rule as password login).
        // Checked before creating the local user so blocked accounts leave no orphan row.
        boolean trial = subscriptionGate.assertActiveAccess(email);

        AppUser user = appUserRepository.findOneByEmail(email).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setEmail(email);
            u.setDisplayName(name);
            // SSO account — password_hash stays null
            return u;
        });
        user.setTrial(trial);
        user = appUserRepository.save(user);
        log.info("[AppUserService] Google login userId={} trial={}", user.getId(), user.isTrial());
        return authResponse(user);
    }

    /** Reset to a system-generated temp password and email it. Silent if the email is unknown or SSO-only. */
    @Transactional
    public void forgotPassword(String email) {
        appUserRepository.findOneByEmail(email).ifPresent(user -> {
            if (user.getPasswordHash() == null) {
                log.info("[AppUserService] forgot-password for SSO-only userId={} — skipped", user.getId());
                return;
            }
            String temp = PasswordGenerator.generate(12);
            user.setPasswordHash(passwordEncoder.encode(temp));
            user.setPasswordTemporary(true);
            appUserRepository.save(user);
            log.info("[AppUserService] issued temp password for userId={}", user.getId());
            emailService.send(user.getEmail(), "Your Cooked temporary password",
                    "Hi " + user.getDisplayName() + ",\n\n" +
                    "We received a request to reset your Cooked password. Sign in with this temporary password:\n\n" +
                    "    " + temp + "\n\n" +
                    "You'll be asked to set a new password right after signing in.\n\n" +
                    "If you didn't request this, you can ignore this email.\n\n— Cooked");
        });
        // Always return normally — never reveal whether the email is registered.
    }

    /** Change the signed-in user's password (verifying the current one) and clear the temp flag. */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        AppUser user = findById(userId);
        if (user.getPasswordHash() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This account uses Google sign-in and has no password.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordTemporary(false);
        appUserRepository.save(user);
        log.info("[AppUserService] changed password for userId={}", user.getId());
    }

    private AuthResponse authResponse(AppUser user) {
        String token = jwtUtil.generate(user.getEmail(), user.getId(), user.getRole().name(), user.isTrial());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName(),
                user.getRole().name(), user.isTrial(), user.isPasswordTemporary());
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
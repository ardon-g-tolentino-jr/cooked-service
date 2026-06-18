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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

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

    /** Length of a trial account's full-access window before trial limits apply. */
    @Value("${cooked.trial.full-access-days}")
    private long fullAccessDays;

    @Transactional
    public void register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        String displayName = (req.displayName() != null && !req.displayName().isBlank())
                ? req.displayName().trim() : email;

        // A local account may already exist from a prior (possibly partial) signup, or the email
        // may already be a client of the subscription service from another Human Workstream app.
        // A *claimed* account — one whose owner has already set their own password — is protected:
        // re-registering must not reset it. An *unclaimed* account (still on a temporary password,
        // never used) is reconciled below: we re-provision access if needed and re-issue a fresh
        // temporary password so a user who never received/used the first one — or who simply shares
        // an email already known to the subscription service — can still complete onboarding.
        AppUser existing = findByEmailFlexible(req.email().trim()).orElse(null);
        if (existing != null && !existing.isPasswordTemporary()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This email is already registered for Cooked. Please sign in, or use \"Forgot password\" to reset it.");
        }

        // Registration does not take a user-chosen password: we generate a temporary one,
        // store it (flagged temporary), and email it. The user sets their own on first sign-in.
        String temp = PasswordGenerator.generate(12);
        // Gate: validate + redeem the registration code on the subscription service before
        // creating the local account. Throws (aborting signup) if the code is missing/invalid.
        // Idempotent: if the email already has active Cooked access (a prior attempt, or another
        // app), skip redemption and just create/reconcile the local account.
        if (subscriptionGate.hasActiveAccess(email)) {
            log.info("[AppUserService] {} already has active Cooked access — skipping code redemption", email);
        } else {
            subscriptionGate.provisionViaCode(displayName, email, temp, req.registrationCode());
        }
        AppUser user = existing != null ? existing : new AppUser();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode(temp));
        user.setPasswordTemporary(true);
        // TRIAL tier: the registration code itself names the trial.
        user.setTrial(req.registrationCode() != null && req.registrationCode().toUpperCase().contains("TRIAL"));
        ensureTrialWindow(user);
        user = appUserRepository.save(user);
        log.info("[AppUserService] {} userId={} trial={} fullAccessUntil={} (temp password issued)",
                existing == null ? "Registered" : "Reconciled unclaimed signup for",
                user.getId(), user.isTrial(), user.getTrialFullAccessUntil());
        emailService.sendWelcomeEmail(user.getEmail(), user.getDisplayName(), temp);
    }

    /** Look up a user by email, tolerant of stored case (new accounts are lowercased; legacy ones
     *  may be mixed-case). Tries the value as-is, then a lowercased variant. */
    private java.util.Optional<AppUser> findByEmailFlexible(String email) {
        return appUserRepository.findOneByEmail(email)
                .or(() -> appUserRepository.findOneByEmail(email.trim().toLowerCase()));
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        AppUser user = findByEmailFlexible(req.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        // Gate: only users with active Cooked access (redeemed code or subscription) may sign in.
        // Returns the trial flag + active plan names; refresh trial + resolved tier each login.
        SubscriptionGateService.GateResult gate = subscriptionGate.assertActiveAccess(user.getEmail());
        user.setTrial(gate.trial());
        ensureTrialWindow(user);
        user.setTier(effectiveTier(user, gate.tier()));
        appUserRepository.save(user);
        log.info("[AppUserService] Login userId={} trial={} tier={} fullAccessUntil={}",
                user.getId(), user.isTrial(), user.getTier(), user.getTrialFullAccessUntil());
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
        String rawEmail = payload.getEmail();
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account has no email");
        }
        String email = rawEmail.trim().toLowerCase();
        String name = payload.get("name") != null ? String.valueOf(payload.get("name")) : email;

        // Gate: only emails with active Cooked access may sign in (same rule as password login).
        // Checked before creating the local user so blocked accounts leave no orphan row.
        SubscriptionGateService.GateResult gate = subscriptionGate.assertActiveAccess(email);

        AppUser user = findByEmailFlexible(email).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setEmail(email);
            u.setDisplayName(name);
            // SSO account — password_hash stays null
            return u;
        });
        user.setTrial(gate.trial());
        ensureTrialWindow(user);
        user.setTier(effectiveTier(user, gate.tier()));
        user = appUserRepository.save(user);
        log.info("[AppUserService] Google login userId={} trial={} tier={} fullAccessUntil={}",
                user.getId(), user.isTrial(), user.getTier(), user.getTrialFullAccessUntil());
        return authResponse(user);
    }

    /** Reset to a system-generated temp password and email it. Silent if the email is unknown or SSO-only. */
    @Transactional
    public void forgotPassword(String email) {
        findByEmailFlexible(email.trim()).ifPresent(user -> {
            if (user.getPasswordHash() == null) {
                log.info("[AppUserService] forgot-password for SSO-only userId={} — skipped", user.getId());
                return;
            }
            String temp = PasswordGenerator.generate(12);
            user.setPasswordHash(passwordEncoder.encode(temp));
            user.setPasswordTemporary(true);
            appUserRepository.save(user);
            log.info("[AppUserService] issued temp password for userId={}", user.getId());
            emailService.sendPasswordResetEmail(user.getEmail(), user.getDisplayName(), temp);
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

    /**
     * In-app registration-code change for the signed-in user. Asks the subscription service to
     * switch this email onto the new code's plan — it revokes the current Cooked access and
     * redeems the new code in one transaction (a rejected code leaves the old access intact).
     * Then re-resolves trial + tier (same as login) and returns a refreshed session so the new
     * plan's limits/features apply immediately — no re-login required.
     */
    @Transactional
    public AuthResponse changeRegistrationCode(Long userId, String registrationCode) {
        AppUser user = findById(userId);
        subscriptionGate.upgradeViaCode(user.getEmail(), registrationCode);
        SubscriptionGateService.GateResult gate = subscriptionGate.assertActiveAccess(user.getEmail());
        user.setTrial(gate.trial());
        ensureTrialWindow(user);
        user.setTier(effectiveTier(user, gate.tier()));
        appUserRepository.save(user);
        log.info("[AppUserService] Changed registration code for userId={} trial={} tier={}",
                user.getId(), user.isTrial(), user.getTier());
        return authResponse(user);
    }

    /**
     * Freeze the trial full-access window the first time a user is known to be a trial
     * account. Anchored to created_at (≈ now for brand-new users) + the configured days,
     * so a later config change doesn't move an existing user's window.
     */
    private void ensureTrialWindow(AppUser user) {
        if (user.isTrial() && user.getTrialFullAccessUntil() == null) {
            OffsetDateTime anchor = user.getCreatedAt() != null ? user.getCreatedAt() : OffsetDateTime.now();
            user.setTrialFullAccessUntil(anchor.plusDays(fullAccessDays));
        }
    }

    /**
     * Resolve the tier carried in the JWT.
     * <ul>
     *   <li>Active trial (within its window): the built-in TRIAL TIER applies (all features on,
     *       recipe count capped by {@link TrialLimitService}) — no subscription tier, so null.</li>
     *   <li>Expired trial: degrade to the FREE subscription tier (the plan named "Free"), whose
     *       admin-defined {@code tier_limit} restrictions then apply.</li>
     *   <li>Otherwise: the user's own subscription plan tier.</li>
     * </ul>
     * Call after {@link #ensureTrialWindow} so the window is set for brand-new trials.
     */
    private String effectiveTier(AppUser user, String planTier) {
        if (user.isTrial()) {
            OffsetDateTime until = user.getTrialFullAccessUntil();
            boolean active = until != null && OffsetDateTime.now().isBefore(until);
            return active ? null : subscriptionGate.freeTierName();
        }
        return planTier;
    }

    private AuthResponse authResponse(AppUser user) {
        // Only carry the full-access window for trial accounts.
        OffsetDateTime until = user.isTrial() ? user.getTrialFullAccessUntil() : null;
        Long untilMs = until != null ? until.toInstant().toEpochMilli() : null;
        String untilIso = until != null ? until.toString() : null;
        String token = jwtUtil.generate(user.getEmail(), user.getId(), user.getRole().name(), user.isTrial(), untilMs, user.getTier());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName(),
                user.getRole().name(), user.isTrial(), untilIso, user.getTier(), user.isPasswordTemporary());
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
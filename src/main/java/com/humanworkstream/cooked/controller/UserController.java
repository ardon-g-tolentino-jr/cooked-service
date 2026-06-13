package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.ChangePasswordRequest;
import com.humanworkstream.cooked.dto.IngredientResponse;
import com.humanworkstream.cooked.dto.UserIngredientEditRequest;
import com.humanworkstream.cooked.dto.UserPatchRequest;
import com.humanworkstream.cooked.dto.UserResponse;
import com.humanworkstream.cooked.dto.UserSettingsPatchRequest;
import com.humanworkstream.cooked.dto.UserSettingsResponse;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.AppUserService;
import com.humanworkstream.cooked.service.IngredientService;
import com.humanworkstream.cooked.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserService appUserService;
    private final UserSettingsService settingsService;
    private final IngredientService ingredientService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        return ResponseEntity.ok(appUserService.getMe(securityUtils.getCurrentUserId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> patchMe(@RequestBody UserPatchRequest req) {
        return ResponseEntity.ok(appUserService.patchMe(securityUtils.getCurrentUserId(), req));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        appUserService.changePassword(securityUtils.getCurrentUserId(), req.currentPassword(), req.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated."));
    }

    @GetMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.get(securityUtils.getCurrentUserId()));
    }

    @PatchMapping("/me/settings")
    public ResponseEntity<UserSettingsResponse> patchSettings(@RequestBody UserSettingsPatchRequest req) {
        return ResponseEntity.ok(settingsService.patch(securityUtils.getCurrentUserId(), req));
    }

    @GetMapping("/me/ingredients")
    public ResponseEntity<List<IngredientResponse>> listIngredients() {
        return ResponseEntity.ok(ingredientService.listForUser(securityUtils.getCurrentUserId()));
    }

    @PutMapping("/me/ingredients/{ingredientId}/edit")
    public ResponseEntity<IngredientResponse> putIngredientEdit(
            @PathVariable Long ingredientId,
            @RequestBody UserIngredientEditRequest req) {
        return ResponseEntity.ok(ingredientService.putEdit(
                securityUtils.getCurrentUserId(), ingredientId, req));
    }

    @DeleteMapping("/me/ingredients/{ingredientId}/edit")
    public ResponseEntity<Void> deleteIngredientEdit(@PathVariable Long ingredientId) {
        ingredientService.deleteEdit(securityUtils.getCurrentUserId(), ingredientId);
        return ResponseEntity.noContent().build();
    }
}
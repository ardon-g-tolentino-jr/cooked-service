package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.SavedRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/saved-recipes")
@RequiredArgsConstructor
public class SavedRecipeController {

    private final SavedRecipeService savedRecipeService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Long>> list() {
        return ResponseEntity.ok(savedRecipeService.listSavedIds(securityUtils.getCurrentUserId()));
    }

    @PutMapping("/{recipeId}")
    public ResponseEntity<Void> save(@PathVariable Long recipeId) {
        savedRecipeService.save(securityUtils.getCurrentUserId(), recipeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> unsave(@PathVariable Long recipeId) {
        savedRecipeService.unsave(securityUtils.getCurrentUserId(), recipeId);
        return ResponseEntity.noContent().build();
    }
}
package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.entity.Recipe;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Recipe>> getMine() {
        return ResponseEntity.ok(recipeService.findByUserId(securityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getById(@PathVariable Integer id) {
        return recipeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Recipe> create(@RequestBody Recipe recipe) {
        // Ownership always comes from the JWT, never from the request body
        recipe.setUserId(securityUtils.getCurrentUserId());
        return ResponseEntity.ok(recipeService.create(recipe));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Recipe> patch(@PathVariable Integer id, @RequestBody Recipe recipe) {
        return recipeService.patch(id, recipe)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
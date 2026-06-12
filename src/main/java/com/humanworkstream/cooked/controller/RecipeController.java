package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.RecipeCreateRequest;
import com.humanworkstream.cooked.dto.RecipeDetailResponse;
import com.humanworkstream.cooked.dto.RecipeIngredientRequest;
import com.humanworkstream.cooked.dto.RecipeIngredientResponse;
import com.humanworkstream.cooked.dto.RecipeInstructionRequest;
import com.humanworkstream.cooked.dto.RecipeInstructionResponse;
import com.humanworkstream.cooked.dto.RecipeMoodsRequest;
import com.humanworkstream.cooked.dto.RecipePatchRequest;
import com.humanworkstream.cooked.dto.RecipeSummaryResponse;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<RecipeSummaryResponse>> list(
            @RequestParam(defaultValue = "visible") String scope) {
        Long userId = securityUtils.getCurrentUserId();
        List<RecipeSummaryResponse> result = "mine".equals(scope)
                ? recipeService.listMine(userId)
                : recipeService.listVisible(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getDetail(id, securityUtils.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<RecipeDetailResponse> create(@Valid @RequestBody RecipeCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipeService.create(securityUtils.getCurrentUserId(), req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RecipeDetailResponse> patch(
            @PathVariable Long id, @RequestBody RecipePatchRequest req) {
        return ResponseEntity.ok(recipeService.patch(id, securityUtils.getCurrentUserId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recipeService.delete(id, securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/moods")
    public ResponseEntity<List<String>> putMoods(
            @PathVariable Long id, @Valid @RequestBody RecipeMoodsRequest req) {
        return ResponseEntity.ok(recipeService.putMoods(id, securityUtils.getCurrentUserId(), req));
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<List<RecipeIngredientResponse>> putIngredients(
            @PathVariable Long id, @RequestBody List<RecipeIngredientRequest> req) {
        return ResponseEntity.ok(recipeService.putIngredients(id, securityUtils.getCurrentUserId(), req));
    }

    @PutMapping("/{id}/instructions")
    public ResponseEntity<List<RecipeInstructionResponse>> putInstructions(
            @PathVariable Long id, @RequestBody List<RecipeInstructionRequest> req) {
        return ResponseEntity.ok(recipeService.putInstructions(id, securityUtils.getCurrentUserId(), req));
    }
}
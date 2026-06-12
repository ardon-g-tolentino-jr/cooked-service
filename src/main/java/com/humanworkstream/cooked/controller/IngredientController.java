package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public ResponseEntity<List<Ingredient>> getByRecipeId(@RequestParam Integer recipeId) {
        return ResponseEntity.ok(ingredientService.findByRecipeId(recipeId));
    }

    @PostMapping
    public ResponseEntity<Ingredient> create(@RequestBody Ingredient ingredient) {
        return ResponseEntity.ok(ingredientService.create(ingredient));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Ingredient> patch(@PathVariable Integer id, @RequestBody Ingredient ingredient) {
        return ingredientService.patch(id, ingredient)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

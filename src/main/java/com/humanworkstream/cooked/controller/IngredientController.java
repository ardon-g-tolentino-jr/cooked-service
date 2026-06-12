package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.IngredientCreateRequest;
import com.humanworkstream.cooked.dto.IngredientResponse;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> list() {
        return ResponseEntity.ok(ingredientService.listForUser(securityUtils.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<IngredientResponse> create(@Valid @RequestBody IngredientCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ingredientService.create(securityUtils.getCurrentUserId(), req));
    }
}
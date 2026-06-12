package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.ShoppingItemAddRequest;
import com.humanworkstream.cooked.dto.ShoppingItemResponse;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.ShoppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shopping")
@RequiredArgsConstructor
public class ShoppingController {

    private final ShoppingService shoppingService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<ShoppingItemResponse>> list() {
        return ResponseEntity.ok(shoppingService.list(securityUtils.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<ShoppingItemResponse> add(@Valid @RequestBody ShoppingItemAddRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shoppingService.add(securityUtils.getCurrentUserId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shoppingService.delete(securityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAll() {
        shoppingService.clearAll(securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
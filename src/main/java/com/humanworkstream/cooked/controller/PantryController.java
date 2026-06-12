package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.PantryItemCreateRequest;
import com.humanworkstream.cooked.dto.PantryItemPatchRequest;
import com.humanworkstream.cooked.dto.PantryItemResponse;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.PantryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pantry")
@RequiredArgsConstructor
public class PantryController {

    private final PantryService pantryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<PantryItemResponse>> list() {
        return ResponseEntity.ok(pantryService.list(securityUtils.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<PantryItemResponse> add(@Valid @RequestBody PantryItemCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pantryService.add(securityUtils.getCurrentUserId(), req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PantryItemResponse> patch(
            @PathVariable Long id, @RequestBody PantryItemPatchRequest req) {
        return ResponseEntity.ok(pantryService.patch(securityUtils.getCurrentUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pantryService.delete(securityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
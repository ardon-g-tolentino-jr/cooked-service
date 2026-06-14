package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.PantryTemplateCreateRequest;
import com.humanworkstream.cooked.dto.PantryTemplateResponse;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.PantryTemplateService;
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
@RequestMapping("/pantry-templates")
@RequiredArgsConstructor
public class PantryTemplateController {

    private final PantryTemplateService pantryTemplateService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<PantryTemplateResponse>> list() {
        return ResponseEntity.ok(pantryTemplateService.list(securityUtils.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<PantryTemplateResponse> create(@Valid @RequestBody PantryTemplateCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pantryTemplateService.create(securityUtils.getCurrentUserId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pantryTemplateService.delete(securityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}

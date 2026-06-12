package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.CookHistoryResponse;
import com.humanworkstream.cooked.dto.CookRequest;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.CookHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cook-history")
@RequiredArgsConstructor
public class CookHistoryController {

    private final CookHistoryService cookHistoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<CookHistoryResponse>> list() {
        return ResponseEntity.ok(cookHistoryService.list(securityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CookHistoryResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(cookHistoryService.getOne(securityUtils.getCurrentUserId(), id));
    }

    @PostMapping
    public ResponseEntity<CookHistoryResponse> cook(@Valid @RequestBody CookRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cookHistoryService.cook(securityUtils.getCurrentUserId(), req));
    }
}
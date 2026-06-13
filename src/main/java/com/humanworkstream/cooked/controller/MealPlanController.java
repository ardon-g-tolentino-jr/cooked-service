package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.MealPlanAddRequest;
import com.humanworkstream.cooked.dto.MealPlanEntryResponse;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/meal-plan")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<MealPlanEntryResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(mealPlanService.list(securityUtils.getCurrentUserId(), from, to));
    }

    @PostMapping
    public ResponseEntity<MealPlanEntryResponse> add(@Valid @RequestBody MealPlanAddRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mealPlanService.add(securityUtils.getCurrentUserId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mealPlanService.delete(securityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}

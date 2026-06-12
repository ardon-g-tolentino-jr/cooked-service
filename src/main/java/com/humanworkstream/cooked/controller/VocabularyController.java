package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.service.CuisineService;
import com.humanworkstream.cooked.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final MoodService moodService;
    private final CuisineService cuisineService;

    @GetMapping("/moods")
    public ResponseEntity<List<String>> moods() {
        return ResponseEntity.ok(moodService.listAll());
    }

    @GetMapping("/cuisines")
    public ResponseEntity<List<String>> cuisines() {
        return ResponseEntity.ok(cuisineService.listAll());
    }
}
package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.Mood;
import com.humanworkstream.cooked.repository.MoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodRepository moodRepository;

    @Transactional(readOnly = true)
    public List<String> listAll() {
        return moodRepository.findAll().stream().map(Mood::getName).sorted().toList();
    }
}
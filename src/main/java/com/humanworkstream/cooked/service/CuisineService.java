package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.Cuisine;
import com.humanworkstream.cooked.repository.CuisineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuisineService {

    private final CuisineRepository cuisineRepository;

    @Transactional(readOnly = true)
    public List<String> listAll() {
        return cuisineRepository.findAll().stream().map(Cuisine::getName).sorted().toList();
    }
}
package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.PantryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PantryTemplateRepository extends JpaRepository<PantryTemplate, Long> {

    List<PantryTemplate> findByUserIdOrderByCreatedAtDesc(Long userId);
}

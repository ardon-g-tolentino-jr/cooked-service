package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.CookHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CookHistoryRepository extends JpaRepository<CookHistory, Long> {

    List<CookHistory> findByUserIdOrderByCookedAtDesc(Long userId);
}
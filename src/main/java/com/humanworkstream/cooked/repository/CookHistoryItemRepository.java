package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.CookHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CookHistoryItemRepository extends JpaRepository<CookHistoryItem, Long> {

    List<CookHistoryItem> findByCookHistoryId(Long cookHistoryId);
}
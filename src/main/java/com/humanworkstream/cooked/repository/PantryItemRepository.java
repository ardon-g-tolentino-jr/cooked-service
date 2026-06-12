package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUserIdOrderByExpiresOnAscCreatedAtAsc(Long userId);

    // FIFO deduction order: earliest expiry first, nulls last, then creation order
    @Query(value = "SELECT * FROM pantry_item WHERE user_id = :userId AND ingredient_id = :ingredientId " +
                   "ORDER BY expires_on ASC NULLS LAST, created_at ASC", nativeQuery = true)
    List<PantryItem> findForDeduction(@Param("userId") Long userId,
                                      @Param("ingredientId") Long ingredientId);
}
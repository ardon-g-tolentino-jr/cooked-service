package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {

    List<ShoppingItem> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<ShoppingItem> findByUserIdAndIngredientId(Long userId, Long ingredientId);

    void deleteByUserId(Long userId);
}
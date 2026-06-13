package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.ShoppingItemAddRequest;
import com.humanworkstream.cooked.dto.ShoppingItemResponse;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.entity.ShoppingItem;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.ShoppingItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final IngredientRepository ingredientRepository;
    private final TrialLimitService trialLimits;

    @Transactional(readOnly = true)
    public List<ShoppingItemResponse> list(Long userId) {
        List<ShoppingItem> items = shoppingItemRepository.findByUserIdOrderByCreatedAtAsc(userId);
        Map<Long, Ingredient> ingMap = ingredientRepository
                .findAllById(items.stream().map(ShoppingItem::getIngredientId).toList())
                .stream().collect(Collectors.toMap(Ingredient::getId, i -> i));
        return items.stream().map(s -> ShoppingItemResponse.from(s,
                ingMap.getOrDefault(s.getIngredientId(), unknown()).getName())).toList();
    }

    @Transactional
    public ShoppingItemResponse add(Long userId, ShoppingItemAddRequest req) {
        trialLimits.assertEnabled(TrialLimitService.SHOPPING);
        ingredientRepository.findById(req.ingredientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown ingredient"));
        ShoppingItem item = shoppingItemRepository
                .findByUserIdAndIngredientId(userId, req.ingredientId())
                .map(existing -> {
                    existing.setGrams(existing.getGrams().add(req.grams()));
                    return existing;
                })
                .orElseGet(() -> new ShoppingItem(null, userId, req.ingredientId(), req.grams(), null));
        item = shoppingItemRepository.save(item);
        String name = ingredientRepository.findById(item.getIngredientId())
                .map(Ingredient::getName).orElse("Unknown");
        log.info("[ShoppingService] Upserted shoppingItemId={} userId={}", item.getId(), userId);
        return ShoppingItemResponse.from(item, name);
    }

    @Transactional
    public void delete(Long userId, Long itemId) {
        ShoppingItem item = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping item not found"));
        if (!userId.equals(item.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        shoppingItemRepository.deleteById(itemId);
    }

    @Transactional
    public void clearAll(Long userId) {
        shoppingItemRepository.deleteByUserId(userId);
        log.info("[ShoppingService] Cleared shopping list userId={}", userId);
    }

    private Ingredient unknown() {
        Ingredient i = new Ingredient();
        i.setName("Unknown");
        return i;
    }
}
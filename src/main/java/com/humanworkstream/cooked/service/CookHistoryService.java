package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.CookHistoryItemResponse;
import com.humanworkstream.cooked.dto.CookHistoryResponse;
import com.humanworkstream.cooked.dto.CookRequest;
import com.humanworkstream.cooked.entity.CookHistory;
import com.humanworkstream.cooked.entity.CookHistoryItem;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.entity.UserIngredientEdit;
import com.humanworkstream.cooked.entity.id.UserIngredientEditId;
import com.humanworkstream.cooked.repository.CookHistoryItemRepository;
import com.humanworkstream.cooked.repository.CookHistoryRepository;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.UserIngredientEditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookHistoryService {

    private final CookHistoryRepository cookHistoryRepository;
    private final CookHistoryItemRepository cookHistoryItemRepository;
    private final IngredientRepository ingredientRepository;
    private final UserIngredientEditRepository editRepository;
    private final PantryService pantryService;

    @Transactional(readOnly = true)
    public List<CookHistoryResponse> list(Long userId) {
        return cookHistoryRepository.findByUserIdOrderByCookedAtDesc(userId).stream()
                .map(h -> {
                    List<CookHistoryItemResponse> items = cookHistoryItemRepository
                            .findByCookHistoryId(h.getId()).stream()
                            .map(CookHistoryItemResponse::from).toList();
                    return CookHistoryResponse.from(h, items);
                }).toList();
    }

    @Transactional(readOnly = true)
    public CookHistoryResponse getOne(Long userId, Long historyId) {
        CookHistory h = cookHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cook history not found"));
        if (!userId.equals(h.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        List<CookHistoryItemResponse> items = cookHistoryItemRepository
                .findByCookHistoryId(h.getId()).stream()
                .map(CookHistoryItemResponse::from).toList();
        return CookHistoryResponse.from(h, items);
    }

    @Transactional
    public CookHistoryResponse cook(Long userId, CookRequest req) {
        List<Long> ingredientIds = req.ingredients().stream()
                .map(CookRequest.IngredientEntry::ingredientId).toList();
        Map<Long, Ingredient> ingMap = ingredientRepository.findAllById(ingredientIds)
                .stream().collect(Collectors.toMap(Ingredient::getId, i -> i));
        for (Long id : ingredientIds) {
            if (!ingMap.containsKey(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown ingredient: " + id);
            }
        }
        Map<Long, UserIngredientEdit> edits = editRepository.findByIdUserId(userId)
                .stream().collect(Collectors.toMap(e -> e.getId().getIngredientId(), e -> e));

        BigDecimal totalKcal = BigDecimal.ZERO;
        List<CookHistoryItem> snapshotItems = new ArrayList<>();

        for (CookRequest.IngredientEntry entry : req.ingredients()) {
            Ingredient ing = ingMap.get(entry.ingredientId());
            BigDecimal kcalPerGram = Optional.ofNullable(edits.get(ing.getId()))
                    .map(UserIngredientEdit::getKcalPerGram)
                    .filter(v -> v != null)
                    .orElse(ing.getKcalPerGram());
            BigDecimal kcal = entry.grams().multiply(kcalPerGram);
            totalKcal = totalKcal.add(kcal);
            snapshotItems.add(new CookHistoryItem(null, null, ing.getName(), entry.grams(), kcal));
        }

        CookHistory history = new CookHistory(null, userId, req.recipeId(),
                req.recipeName(), req.scale(), BigDecimal.valueOf(req.servings()),
                totalKcal, null);
        history = cookHistoryRepository.save(history);
        final Long historyId = history.getId();

        List<CookHistoryItem> saved = new ArrayList<>();
        for (CookHistoryItem item : snapshotItems) {
            item.setCookHistoryId(historyId);
            saved.add(cookHistoryItemRepository.save(item));
        }

        // FIFO pantry deduction (best-effort, won't rollback cook if pantry is short)
        for (CookRequest.IngredientEntry entry : req.ingredients()) {
            Ingredient ing = ingMap.get(entry.ingredientId());
            pantryService.deductFifo(userId, ing.getId(), entry.grams(), ing);
        }

        log.info("[CookHistoryService] Cooked historyId={} userId={} totalKcal={}", historyId, userId, totalKcal);
        return CookHistoryResponse.from(history, saved.stream().map(CookHistoryItemResponse::from).toList());
    }
}
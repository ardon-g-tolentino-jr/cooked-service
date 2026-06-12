package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.PantryItemCreateRequest;
import com.humanworkstream.cooked.dto.PantryItemPatchRequest;
import com.humanworkstream.cooked.dto.PantryItemResponse;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.entity.PantryItem;
import com.humanworkstream.cooked.enumeration.UnitType;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.PantryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PantryService {

    private final PantryItemRepository pantryItemRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public List<PantryItemResponse> list(Long userId) {
        List<PantryItem> items = pantryItemRepository.findByUserIdOrderByExpiresOnAscCreatedAtAsc(userId);
        Map<Long, Ingredient> ingMap = loadIngredients(items.stream().map(PantryItem::getIngredientId).toList());
        return items.stream().map(p -> PantryItemResponse.from(p,
                ingMap.getOrDefault(p.getIngredientId(), unknownIngredient()).getName())).toList();
    }

    @Transactional
    public PantryItemResponse add(Long userId, PantryItemCreateRequest req) {
        Ingredient ing = ingredientRepository.findById(req.ingredientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown ingredient"));
        if (req.unit() == UnitType.PCS && ing.getGramsPerPiece() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ingredient has no gramsPerPiece, cannot use PCS unit");
        }
        PantryItem item = new PantryItem(null, userId, req.ingredientId(),
                req.qty(), req.unit(), req.expiresOn(), null);
        item = pantryItemRepository.save(item);
        log.info("[PantryService] Added pantryItemId={} userId={}", item.getId(), userId);
        return PantryItemResponse.from(item, ing.getName());
    }

    @Transactional
    public PantryItemResponse patch(Long userId, Long itemId, PantryItemPatchRequest req) {
        PantryItem item = findOwned(userId, itemId);
        if (req.qty() != null) item.setQty(req.qty());
        if (req.unit() != null) item.setUnit(req.unit());
        if (req.expiresOn() != null) item.setExpiresOn(req.expiresOn());
        item = pantryItemRepository.save(item);
        String name = ingredientRepository.findById(item.getIngredientId())
                .map(Ingredient::getName).orElse("Unknown");
        return PantryItemResponse.from(item, name);
    }

    @Transactional
    public void delete(Long userId, Long itemId) {
        findOwned(userId, itemId);
        pantryItemRepository.deleteById(itemId);
        log.info("[PantryService] Deleted pantryItemId={} userId={}", itemId, userId);
    }

    /**
     * FIFO deduction: deducts gramsNeeded for the given ingredient from the user's pantry.
     * Converts pantry unit quantities to grams before deducting.
     * Called within the cook transaction.
     */
    public void deductFifo(Long userId, Long ingredientId, BigDecimal gramsNeeded,
                            Ingredient ingredient) {
        List<PantryItem> lots = pantryItemRepository.findForDeduction(userId, ingredientId);
        BigDecimal remaining = gramsNeeded;
        for (PantryItem lot : lots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal lotGrams = toGrams(lot.getQty(), lot.getUnit(), ingredient.getGramsPerPiece());
            if (lotGrams.compareTo(BigDecimal.ZERO) <= 0) continue;
            if (lotGrams.compareTo(remaining) <= 0) {
                remaining = remaining.subtract(lotGrams);
                pantryItemRepository.delete(lot);
            } else {
                BigDecimal newGrams = lotGrams.subtract(remaining);
                lot.setQty(fromGrams(newGrams, lot.getUnit(), ingredient.getGramsPerPiece()));
                pantryItemRepository.save(lot);
                remaining = BigDecimal.ZERO;
            }
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("[PantryService] Partial pantry deduction for ingredientId={}: {}g short", ingredientId, remaining);
        }
    }

    private BigDecimal toGrams(BigDecimal qty, UnitType unit, BigDecimal gramsPerPiece) {
        return switch (unit) {
            case G -> qty;
            case KG -> qty.multiply(BigDecimal.valueOf(1000));
            case ML -> qty;
            case L -> qty.multiply(BigDecimal.valueOf(1000));
            case PCS -> gramsPerPiece != null ? qty.multiply(gramsPerPiece) : BigDecimal.ZERO;
        };
    }

    private BigDecimal fromGrams(BigDecimal grams, UnitType unit, BigDecimal gramsPerPiece) {
        return switch (unit) {
            case G -> grams;
            case KG -> grams.divide(BigDecimal.valueOf(1000), 6, java.math.RoundingMode.HALF_UP);
            case ML -> grams;
            case L -> grams.divide(BigDecimal.valueOf(1000), 6, java.math.RoundingMode.HALF_UP);
            case PCS -> gramsPerPiece != null && gramsPerPiece.compareTo(BigDecimal.ZERO) > 0
                    ? grams.divide(gramsPerPiece, 6, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        };
    }

    private PantryItem findOwned(Long userId, Long itemId) {
        PantryItem item = pantryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pantry item not found"));
        if (!userId.equals(item.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return item;
    }

    private Map<Long, Ingredient> loadIngredients(List<Long> ids) {
        return ingredientRepository.findAllById(ids)
                .stream().collect(Collectors.toMap(Ingredient::getId, i -> i));
    }

    private Ingredient unknownIngredient() {
        Ingredient i = new Ingredient();
        i.setName("Unknown");
        return i;
    }
}

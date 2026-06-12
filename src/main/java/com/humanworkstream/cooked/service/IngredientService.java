package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.IngredientCreateRequest;
import com.humanworkstream.cooked.dto.IngredientResponse;
import com.humanworkstream.cooked.dto.UserIngredientEditRequest;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.entity.UserIngredientEdit;
import com.humanworkstream.cooked.entity.id.UserIngredientEditId;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.UserIngredientEditRepository;
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
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final UserIngredientEditRepository editRepository;

    @Transactional(readOnly = true)
    public List<IngredientResponse> listForUser(Long userId) {
        List<Ingredient> ingredients = ingredientRepository.findByIsBuiltinTrueOrCreatedByOrderByNameAsc(userId);
        Map<Long, UserIngredientEdit> edits = editRepository.findByIdUserId(userId)
                .stream().collect(Collectors.toMap(e -> e.getId().getIngredientId(), e -> e));
        return ingredients.stream().map(i -> {
            UserIngredientEdit edit = edits.get(i.getId());
            if (edit == null) return IngredientResponse.from(i);
            var kcal = edit.getKcalPerGram() != null ? edit.getKcalPerGram() : i.getKcalPerGram();
            var piece = edit.getGramsPerPiece() != null ? edit.getGramsPerPiece() : i.getGramsPerPiece();
            return IngredientResponse.from(i, kcal, piece);
        }).toList();
    }

    @Transactional
    public IngredientResponse create(Long userId, IngredientCreateRequest req) {
        if (ingredientRepository.existsByNameIgnoreCase(req.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingredient name already exists");
        }
        Ingredient ing = new Ingredient();
        ing.setName(req.name());
        ing.setCategory(req.category());
        ing.setKcalPerGram(req.kcalPerGram());
        ing.setGramsPerPiece(req.gramsPerPiece());
        ing.setIsBuiltin(false);
        ing.setCreatedBy(userId);
        ing = ingredientRepository.save(ing);
        log.info("[IngredientService] Created ingredientId={} userId={}", ing.getId(), userId);
        return IngredientResponse.from(ing);
    }

    @Transactional
    public IngredientResponse putEdit(Long userId, Long ingredientId, UserIngredientEditRequest req) {
        Ingredient ing = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));
        UserIngredientEditId editId = new UserIngredientEditId(userId, ingredientId);
        UserIngredientEdit edit = editRepository.findById(editId)
                .orElse(new UserIngredientEdit(editId, null, null));
        if (req.kcalPerGram() != null) edit.setKcalPerGram(req.kcalPerGram());
        if (req.gramsPerPiece() != null) edit.setGramsPerPiece(req.gramsPerPiece());
        editRepository.save(edit);
        var kcal = edit.getKcalPerGram() != null ? edit.getKcalPerGram() : ing.getKcalPerGram();
        var piece = edit.getGramsPerPiece() != null ? edit.getGramsPerPiece() : ing.getGramsPerPiece();
        return IngredientResponse.from(ing, kcal, piece);
    }

    @Transactional
    public void deleteEdit(Long userId, Long ingredientId) {
        editRepository.deleteById(new UserIngredientEditId(userId, ingredientId));
    }

    public Ingredient findByIdOrThrow(Long ingredientId) {
        return ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ingredient not found: " + ingredientId));
    }
}
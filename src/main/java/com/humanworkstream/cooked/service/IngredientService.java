package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.IngredientCreateRequest;
import com.humanworkstream.cooked.dto.IngredientResponse;
import com.humanworkstream.cooked.dto.IngredientUpdateRequest;
import com.humanworkstream.cooked.dto.UserIngredientEditRequest;
import com.humanworkstream.cooked.entity.AppUser;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.entity.UserIngredientEdit;
import com.humanworkstream.cooked.entity.id.UserIngredientEditId;
import com.humanworkstream.cooked.repository.AppUserRepository;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.UserIngredientEditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final AppUserRepository appUserRepository;
    private final TrialLimitService trialLimits;

    @Transactional(readOnly = true)
    public List<IngredientResponse> listForUser(Long userId) {
        List<Ingredient> ingredients = ingredientRepository.findByIsBuiltinTrueOrCreatedByOrderByNameAsc(userId);
        Map<Long, UserIngredientEdit> edits = editRepository.findByIdUserId(userId)
                .stream().collect(Collectors.toMap(e -> e.getId().getIngredientId(), e -> e));
        Map<Long, String> creatorNames = appUserRepository.findAllById(
                        ingredients.stream().map(Ingredient::getCreatedBy).filter(id -> id != null).distinct().toList())
                .stream().collect(Collectors.toMap(AppUser::getId, AppUser::getDisplayName));
        return ingredients.stream().map(i -> {
            String creator = i.getCreatedBy() != null ? creatorNames.get(i.getCreatedBy()) : null;
            UserIngredientEdit edit = edits.get(i.getId());
            if (edit == null) return IngredientResponse.from(i, creator);
            var kcal = edit.getKcalPerGram() != null ? edit.getKcalPerGram() : i.getKcalPerGram();
            var piece = edit.getGramsPerPiece() != null ? edit.getGramsPerPiece() : i.getGramsPerPiece();
            return IngredientResponse.from(i, kcal, piece, creator);
        }).toList();
    }

    @Transactional
    public IngredientResponse create(Long userId, boolean isAdmin, IngredientCreateRequest req) {
        // TRIAL tier: trial users can't add custom ingredients when the component is disabled.
        if (!isAdmin) trialLimits.assertEnabled(TrialLimitService.INGREDIENTS);
        if (ingredientRepository.existsByNameIgnoreCase(req.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingredient name already exists");
        }
        Ingredient ing = new Ingredient();
        ing.setName(req.name());
        ing.setCategory(req.category());
        ing.setKcalPerGram(req.kcalPerGram());
        ing.setGramsPerPiece(req.gramsPerPiece());
        // Admins add System (built-in) ingredients; regular users add private custom ones.
        ing.setIsBuiltin(isAdmin);
        ing.setCreatedBy(isAdmin ? null : userId);
        ing = ingredientRepository.save(ing);
        log.info("[IngredientService] Created ingredientId={} builtin={} userId={}", ing.getId(), isAdmin, userId);
        return IngredientResponse.from(ing, creatorName(ing));
    }

    @Transactional
    public IngredientResponse update(Long userId, boolean isAdmin, Long id, IngredientUpdateRequest req) {
        Ingredient ing = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));
        requireManage(userId, isAdmin, ing);
        if (!ing.getName().equalsIgnoreCase(req.name()) && ingredientRepository.existsByNameIgnoreCase(req.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingredient name already exists");
        }
        ing.setName(req.name());
        ing.setCategory(req.category());
        ing.setKcalPerGram(req.kcalPerGram());
        ing.setGramsPerPiece(req.gramsPerPiece());
        ing = ingredientRepository.save(ing);
        log.info("[IngredientService] Updated ingredientId={} userId={}", id, userId);
        return IngredientResponse.from(ing, creatorName(ing));
    }

    @Transactional
    public void delete(Long userId, boolean isAdmin, Long id) {
        Ingredient ing = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));
        requireManage(userId, isAdmin, ing);
        try {
            ingredientRepository.delete(ing);
            ingredientRepository.flush(); // force the FK check now so we can map it to 409
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ingredient is in use by recipes, pantry or shopping and can't be deleted");
        }
        log.info("[IngredientService] Deleted ingredientId={} userId={}", id, userId);
    }

    // System (built-in) ingredients are admin-managed; custom ones belong to their creator.
    private void requireManage(Long userId, boolean isAdmin, Ingredient ing) {
        boolean allowed = Boolean.TRUE.equals(ing.getIsBuiltin())
                ? isAdmin
                : (isAdmin || userId.equals(ing.getCreatedBy()));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this ingredient");
        }
    }

    private String creatorName(Ingredient ing) {
        return ing.getCreatedBy() != null
                ? appUserRepository.findById(ing.getCreatedBy()).map(AppUser::getDisplayName).orElse(null)
                : null;
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
        String creator = ing.getCreatedBy() != null
                ? appUserRepository.findById(ing.getCreatedBy()).map(AppUser::getDisplayName).orElse(null) : null;
        return IngredientResponse.from(ing, kcal, piece, creator);
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
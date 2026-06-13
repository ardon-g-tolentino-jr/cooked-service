package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.RecipeCreateRequest;
import com.humanworkstream.cooked.dto.RecipeDetailResponse;
import com.humanworkstream.cooked.dto.RecipeIngredientRequest;
import com.humanworkstream.cooked.dto.RecipeIngredientResponse;
import com.humanworkstream.cooked.dto.RecipeInstructionRequest;
import com.humanworkstream.cooked.dto.RecipeInstructionResponse;
import com.humanworkstream.cooked.dto.RecipeMoodsRequest;
import com.humanworkstream.cooked.dto.RecipePatchRequest;
import com.humanworkstream.cooked.dto.RecipeSummaryResponse;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.entity.Recipe;
import com.humanworkstream.cooked.entity.RecipeIngredient;
import com.humanworkstream.cooked.entity.RecipeInstruction;
import com.humanworkstream.cooked.entity.RecipeMood;
import com.humanworkstream.cooked.entity.RecipeRating;
import com.humanworkstream.cooked.entity.id.RecipeIngredientId;
import com.humanworkstream.cooked.entity.id.RecipeInstructionId;
import com.humanworkstream.cooked.entity.id.RecipeMoodId;
import com.humanworkstream.cooked.repository.CuisineRepository;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.RecipeIngredientRepository;
import com.humanworkstream.cooked.repository.RecipeInstructionRepository;
import com.humanworkstream.cooked.repository.RecipeMoodRepository;
import com.humanworkstream.cooked.repository.RecipeRatingRepository;
import com.humanworkstream.cooked.repository.RecipeRepository;
import com.humanworkstream.cooked.repository.CookHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMoodRepository moodRepository;
    private final RecipeIngredientRepository ingredientLinkRepo;
    private final RecipeInstructionRepository instructionRepo;
    private final IngredientRepository ingredientRepository;
    private final CuisineRepository cuisineRepository;
    private final TrialLimitService trialLimits;
    private final RecipeRatingRepository recipeRatingRepository;
    private final CookHistoryRepository cookHistoryRepository;

    @Transactional(readOnly = true)
    public List<RecipeSummaryResponse> listVisible(Long userId) {
        return recipeRepository.findVisibleToUser(userId).stream()
                .map(r -> RecipeSummaryResponse.from(r, moodsFor(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeSummaryResponse> listMine(Long userId) {
        return recipeRepository.findByOwnerUserIdOrderByNameAsc(userId).stream()
                .map(r -> RecipeSummaryResponse.from(r, moodsFor(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponse getDetail(Long recipeId, Long userId) {
        Recipe r = findVisible(recipeId, userId);
        return buildDetail(r, userId);
    }

    @Transactional
    public RecipeDetailResponse create(Long userId, RecipeCreateRequest req) {
        // TRIAL tier: cap the number of recipes a trial user may own.
        trialLimits.assertEnabled(TrialLimitService.RECIPES);
        trialLimits.assertUnderLimit(TrialLimitService.RECIPES, recipeRepository.countByOwnerUserId(userId));
        validateCuisine(req.cuisine());
        Recipe r = new Recipe();
        r.setName(req.name());
        r.setOwnerUserId(userId);
        r.setCuisine(req.cuisine());
        r.setPrepTimeMin(req.prepTimeMin());
        r.setServings(req.servings());
        r = recipeRepository.save(r);
        Long recipeId = r.getId();
        saveMoods(recipeId, req.moods());
        saveIngredients(recipeId, req.ingredients());
        saveInstructions(recipeId, req.instructions());
        log.info("[RecipeService] Created recipeId={} userId={}", recipeId, userId);
        return buildDetail(r, userId);
    }

    @Transactional
    public RecipeDetailResponse patch(Long recipeId, Long userId, RecipePatchRequest req) {
        Recipe r = findOwned(recipeId, userId);
        if (req.name() != null) r.setName(req.name());
        if (req.cuisine() != null) { validateCuisine(req.cuisine()); r.setCuisine(req.cuisine()); }
        if (req.prepTimeMin() != null) r.setPrepTimeMin(req.prepTimeMin());
        if (req.servings() != null) r.setServings(req.servings());
        if (req.isShared() != null) r.setIsShared(req.isShared());
        return buildDetail(recipeRepository.save(r), userId);
    }

    @Transactional
    public void delete(Long recipeId, Long userId) {
        findOwned(recipeId, userId);
        moodRepository.deleteByIdRecipeId(recipeId);
        ingredientLinkRepo.deleteByIdRecipeId(recipeId);
        instructionRepo.deleteByIdRecipeId(recipeId);
        recipeRepository.deleteById(recipeId);
        log.info("[RecipeService] Deleted recipeId={} userId={}", recipeId, userId);
    }

    @Transactional
    public List<String> putMoods(Long recipeId, Long userId, RecipeMoodsRequest req) {
        findOwned(recipeId, userId);
        moodRepository.deleteByIdRecipeId(recipeId);
        saveMoods(recipeId, req.moods());
        return moodsFor(recipeId);
    }

    @Transactional
    public List<RecipeIngredientResponse> putIngredients(Long recipeId, Long userId,
                                                          List<RecipeIngredientRequest> reqs) {
        findOwned(recipeId, userId);
        ingredientLinkRepo.deleteByIdRecipeId(recipeId);
        saveIngredients(recipeId, reqs);
        return ingredientsFor(recipeId);
    }

    @Transactional
    public List<RecipeInstructionResponse> putInstructions(Long recipeId, Long userId,
                                                            List<RecipeInstructionRequest> reqs) {
        findOwned(recipeId, userId);
        instructionRepo.deleteByIdRecipeId(recipeId);
        saveInstructions(recipeId, reqs);
        return instructionsFor(recipeId);
    }

    // --- helpers ---

    private Recipe findVisible(Long recipeId, Long userId) {
        Recipe r = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!r.getIsCommunity() && !userId.equals(r.getOwnerUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return r;
    }

    private Recipe findOwned(Long recipeId, Long userId) {
        Recipe r = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!userId.equals(r.getOwnerUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not recipe owner");
        }
        return r;
    }

    private void validateCuisine(String cuisine) {
        if (!cuisineRepository.existsById(cuisine)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown cuisine: " + cuisine);
        }
    }

    private void saveMoods(Long recipeId, List<String> moods) {
        if (moods == null) return;
        moods.forEach(m -> moodRepository.save(new RecipeMood(new RecipeMoodId(recipeId, m))));
    }

    private void saveIngredients(Long recipeId, List<RecipeIngredientRequest> reqs) {
        if (reqs == null) return;
        reqs.forEach(req -> {
            if (!ingredientRepository.existsById(req.ingredientId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown ingredient: " + req.ingredientId());
            }
            ingredientLinkRepo.save(new RecipeIngredient(
                    new RecipeIngredientId(recipeId, req.ingredientId()), req.grams()));
        });
    }

    private void saveInstructions(Long recipeId, List<RecipeInstructionRequest> reqs) {
        if (reqs == null) return;
        reqs.forEach(req -> instructionRepo.save(new RecipeInstruction(
                new RecipeInstructionId(recipeId, req.stepNo()), req.instruction())));
    }

    private List<String> moodsFor(Long recipeId) {
        return moodRepository.findByIdRecipeId(recipeId)
                .stream().map(m -> m.getId().getMood()).toList();
    }

    private List<RecipeIngredientResponse> ingredientsFor(Long recipeId) {
        List<RecipeIngredient> links = ingredientLinkRepo.findByIdRecipeId(recipeId);
        if (links.isEmpty()) return Collections.emptyList();
        Map<Long, Ingredient> ingMap = ingredientRepository
                .findAllById(links.stream().map(l -> l.getId().getIngredientId()).toList())
                .stream().collect(Collectors.toMap(Ingredient::getId, i -> i));
        return links.stream().map(l -> {
            Ingredient ing = ingMap.get(l.getId().getIngredientId());
            return new RecipeIngredientResponse(
                    ing.getId(), ing.getName(), ing.getCategory(),
                    l.getGrams(), ing.getKcalPerGram());
        }).toList();
    }

    private List<RecipeInstructionResponse> instructionsFor(Long recipeId) {
        return instructionRepo.findByIdRecipeIdOrderByIdStepNoAsc(recipeId)
                .stream().map(i -> new RecipeInstructionResponse(
                        i.getId().getStepNo(), i.getInstruction()))
                .toList();
    }

    private RecipeDetailResponse buildDetail(Recipe r, Long userId) {
        Double avg = recipeRatingRepository.avgByRecipeId(r.getId());
        long count = recipeRatingRepository.countByRecipeId(r.getId());
        Integer mine = recipeRatingRepository.findByRecipeIdAndUserId(r.getId(), userId)
                .map(rr -> rr.getStars().intValue()).orElse(null);
        return RecipeDetailResponse.from(r, moodsFor(r.getId()),
                ingredientsFor(r.getId()), instructionsFor(r.getId()), avg, count, mine);
    }

    /** Rate a community recipe (1–5). Only allowed after the user has cooked it at least once. */
    @Transactional
    public RecipeDetailResponse rate(Long userId, Long recipeId, int stars) {
        Recipe r = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        if (!Boolean.TRUE.equals(r.getIsCommunity())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only community recipes can be rated");
        }
        if (!cookHistoryRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cook this recipe at least once before rating it");
        }
        RecipeRating rr = recipeRatingRepository.findByRecipeIdAndUserId(recipeId, userId)
                .orElseGet(() -> {
                    RecipeRating n = new RecipeRating();
                    n.setRecipeId(recipeId);
                    n.setUserId(userId);
                    return n;
                });
        rr.setStars((short) stars);
        recipeRatingRepository.save(rr);
        log.info("[RecipeService] user={} rated recipe={} stars={}", userId, recipeId, stars);
        return buildDetail(r, userId);
    }
}
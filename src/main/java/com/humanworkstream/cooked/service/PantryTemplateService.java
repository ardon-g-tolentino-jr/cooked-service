package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.PantryTemplateCreateRequest;
import com.humanworkstream.cooked.dto.PantryTemplateItemResponse;
import com.humanworkstream.cooked.dto.PantryTemplateResponse;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.entity.PantryTemplate;
import com.humanworkstream.cooked.entity.PantryTemplateItem;
import com.humanworkstream.cooked.entity.id.PantryTemplateItemId;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.PantryTemplateItemRepository;
import com.humanworkstream.cooked.repository.PantryTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PantryTemplateService {

    private final PantryTemplateRepository templateRepository;
    private final PantryTemplateItemRepository itemRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public List<PantryTemplateResponse> list(Long userId) {
        List<PantryTemplate> templates = templateRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (templates.isEmpty()) return List.of();

        List<PantryTemplateItem> items = itemRepository.findByIdTemplateIdIn(
                templates.stream().map(PantryTemplate::getId).toList());
        Map<Long, String> names = ingredientNameMap(
                items.stream().map(i -> i.getId().getIngredientId()).toList());

        Map<Long, List<PantryTemplateItemResponse>> itemsByTemplate = items.stream().collect(Collectors.groupingBy(
                i -> i.getId().getTemplateId(),
                Collectors.mapping(i -> new PantryTemplateItemResponse(
                        i.getId().getIngredientId(),
                        names.getOrDefault(i.getId().getIngredientId(), "Unknown")), Collectors.toList())));

        return templates.stream()
                .map(t -> PantryTemplateResponse.from(t, itemsByTemplate.getOrDefault(t.getId(), List.of())))
                .toList();
    }

    @Transactional
    public PantryTemplateResponse create(Long userId, PantryTemplateCreateRequest req) {
        // distinct ingredient ids, validated against the catalog
        List<Long> ids = new LinkedHashSet<>(req.ingredientIds()).stream().toList();
        Map<Long, Ingredient> ingredients = ingredientRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ingredient::getId, i -> i));
        if (ingredients.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown ingredient in template");
        }

        PantryTemplate template = templateRepository.save(
                new PantryTemplate(null, userId, req.name(), null));
        itemRepository.saveAll(ids.stream()
                .map(id -> new PantryTemplateItem(new PantryTemplateItemId(template.getId(), id)))
                .toList());
        log.info("[PantryTemplateService] Created pantryTemplateId={} items={} userId={}",
                template.getId(), ids.size(), userId);

        List<PantryTemplateItemResponse> itemResponses = ids.stream()
                .map(id -> new PantryTemplateItemResponse(id, ingredients.get(id).getName()))
                .toList();
        return PantryTemplateResponse.from(template, itemResponses);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        PantryTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pantry template not found"));
        if (!userId.equals(template.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        templateRepository.deleteById(id); // DB FK cascades to pantry_template_item
        log.info("[PantryTemplateService] Deleted pantryTemplateId={} userId={}", id, userId);
    }

    private Map<Long, String> ingredientNameMap(List<Long> ids) {
        return ingredientRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ingredient::getId, Ingredient::getName));
    }
}

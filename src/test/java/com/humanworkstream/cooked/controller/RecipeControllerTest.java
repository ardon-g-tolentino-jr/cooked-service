package com.humanworkstream.cooked.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanworkstream.cooked.dto.RecipeCreateRequest;
import com.humanworkstream.cooked.dto.RecipeDetailResponse;
import com.humanworkstream.cooked.dto.RecipeSummaryResponse;
import com.humanworkstream.cooked.security.JwtUtil;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RecipeController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class }
)
class RecipeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RecipeService recipeService;
    @MockBean SecurityUtils securityUtils;
    @MockBean JwtUtil jwtUtil;

    private RecipeSummaryResponse stubSummary(long id) {
        return new RecipeSummaryResponse(id, "Chicken Adobo", 1L, null,
                "Filipino", 15, 4, false, false, List.of("comfort"), OffsetDateTime.now());
    }

    private RecipeDetailResponse stubDetail(long id) {
        return new RecipeDetailResponse(id, "Chicken Adobo", 1L, null,
                "Filipino", 15, 4, false, false,
                List.of("comfort"), Collections.emptyList(), Collections.emptyList(),
                OffsetDateTime.now(), null, 0L, null);
    }

    @Test
    void list_visibleScope_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(recipeService.listVisible(1L)).thenReturn(List.of(stubSummary(1L)));

        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Chicken Adobo"));
    }

    @Test
    void list_mineScope_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(recipeService.listMine(1L)).thenReturn(List.of(stubSummary(1L)));

        mockMvc.perform(get("/recipes").param("scope", "mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getDetail_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(recipeService.getDetail(eq(1L), eq(1L))).thenReturn(stubDetail(1L));

        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cuisine").value("Filipino"));
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(recipeService.create(eq(1L), any(RecipeCreateRequest.class))).thenReturn(stubDetail(1L));

        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RecipeCreateRequest("Chicken Adobo", "Filipino",
                                        15, 4, List.of("comfort"),
                                        Collections.emptyList(), Collections.emptyList()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void delete_returns204() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);

        mockMvc.perform(delete("/recipes/1"))
                .andExpect(status().isNoContent());

        verify(recipeService).delete(1L, 1L);
    }
}
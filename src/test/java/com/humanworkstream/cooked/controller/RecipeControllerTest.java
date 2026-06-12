package com.humanworkstream.cooked.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanworkstream.cooked.entity.Recipe;
import com.humanworkstream.cooked.enumeration.Difficulty;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private Recipe stub(int id) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setUserId(1);
        r.setName("Chicken Adobo");
        r.setCuisine("Filipino");
        r.setDifficulty(Difficulty.EASY);
        r.setPrepMinutes(15);
        r.setCookMinutes(45);
        r.setServings(4);
        return r;
    }

    @Test
    void getMine_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1);
        when(recipeService.findByUserId(1)).thenReturn(List.of(stub(1)));

        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Chicken Adobo"))
                .andExpect(jsonPath("$[0].difficulty").value("easy"));
    }

    @Test
    void getById_found_returns200() throws Exception {
        when(recipeService.findById(1)).thenReturn(Optional.of(stub(1)));

        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(recipeService.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/recipes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_setsOwnerFromJwt_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1);
        when(recipeService.create(any(Recipe.class))).thenReturn(stub(1));

        Recipe body = stub(0);
        body.setUserId(99); // must be overridden by the JWT user

        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(recipeService).create(org.mockito.ArgumentMatchers.argThat(r -> r.getUserId() == 1));
    }

    @Test
    void patch_found_returns200() throws Exception {
        when(recipeService.patch(eq(1), any(Recipe.class))).thenReturn(Optional.of(stub(1)));

        mockMvc.perform(patch("/recipes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patch_notFound_returns404() throws Exception {
        when(recipeService.patch(eq(999), any(Recipe.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/recipes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/recipes/1"))
                .andExpect(status().isNoContent());

        verify(recipeService).delete(1);
    }
}
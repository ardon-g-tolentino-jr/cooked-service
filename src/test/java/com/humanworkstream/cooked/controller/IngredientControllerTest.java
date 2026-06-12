package com.humanworkstream.cooked.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.security.JwtUtil;
import com.humanworkstream.cooked.service.IngredientService;
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
        controllers = IngredientController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class }
)
class IngredientControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean IngredientService ingredientService;
    @MockBean JwtUtil jwtUtil;

    private Ingredient stub(int id) {
        Ingredient i = new Ingredient();
        i.setId(id);
        i.setRecipeId(1);
        i.setName("Soy sauce");
        i.setQuantity("120");
        i.setUnit("ml");
        i.setPosition(1);
        return i;
    }

    @Test
    void getByRecipeId_returns200() throws Exception {
        when(ingredientService.findByRecipeId(1)).thenReturn(List.of(stub(1)));

        mockMvc.perform(get("/ingredients").param("recipeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Soy sauce"));
    }

    @Test
    void create_returns200() throws Exception {
        when(ingredientService.create(any(Ingredient.class))).thenReturn(stub(1));

        mockMvc.perform(post("/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stub(0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patch_found_returns200() throws Exception {
        when(ingredientService.patch(eq(1), any(Ingredient.class))).thenReturn(Optional.of(stub(1)));

        mockMvc.perform(patch("/ingredients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patch_notFound_returns404() throws Exception {
        when(ingredientService.patch(eq(999), any(Ingredient.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/ingredients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/ingredients/1"))
                .andExpect(status().isNoContent());

        verify(ingredientService).delete(1);
    }
}
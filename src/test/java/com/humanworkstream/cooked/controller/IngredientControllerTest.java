package com.humanworkstream.cooked.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanworkstream.cooked.dto.IngredientCreateRequest;
import com.humanworkstream.cooked.dto.IngredientResponse;
import com.humanworkstream.cooked.security.JwtUtil;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.IngredientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = IngredientController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class }
)
class IngredientControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean IngredientService ingredientService;
    @MockBean SecurityUtils securityUtils;
    @MockBean JwtUtil jwtUtil;

    private IngredientResponse stub(long id) {
        return new IngredientResponse(id, "Soy Sauce", "Condiment",
                BigDecimal.valueOf(0.6), null, true, null, null,
                BigDecimal.valueOf(0.6), null);
    }

    @Test
    void list_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(ingredientService.listForUser(1L)).thenReturn(List.of(stub(1L)));

        mockMvc.perform(get("/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Soy Sauce"));
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(ingredientService.create(eq(1L), any(IngredientCreateRequest.class))).thenReturn(stub(2L));

        mockMvc.perform(post("/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new IngredientCreateRequest("Soy Sauce", "Condiment",
                                        BigDecimal.valueOf(0.6), null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void create_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
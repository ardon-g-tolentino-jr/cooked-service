package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.security.JwtUtil;
import com.humanworkstream.cooked.service.DBHealthCheckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = HealthCheckController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class }
)
class HealthCheckControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean DBHealthCheckService dbHealthCheckService;
    @MockBean JwtUtil jwtUtil;

    @Test
    void healthcheck_returns200() throws Exception {
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void dbHealthcheck_up_returns200() throws Exception {
        when(dbHealthCheckService.isDatabaseHealthy()).thenReturn(true);

        mockMvc.perform(get("/db/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void dbHealthcheck_down_returns503() throws Exception {
        when(dbHealthCheckService.isDatabaseHealthy()).thenReturn(false);

        mockMvc.perform(get("/db/healthcheck"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    void schemaHealthcheck_up_returns200() throws Exception {
        when(dbHealthCheckService.isSchemaHealthy()).thenReturn(true);

        mockMvc.perform(get("/db/schema/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}

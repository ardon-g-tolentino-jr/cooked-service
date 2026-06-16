package com.humanworkstream.cooked.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * Allowed browser origins. Credentials are enabled, so origins must be an explicit
     * allow-list (never "*"). Only TLS origins are permitted in production; localhost is
     * kept for local dev. Override the whole list via the CORS_ALLOWED_ORIGINS env var
     * (comma-separated) without a redeploy.
     */
    @Value("${cors.allowed-origins:http://localhost:*,https://humanworkstream.com,https://cooked.humanworkstream.com,https://cookedapi.humanworkstream.com}")
    private List<String> allowedOriginPatterns;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(allowedOriginPatterns);
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

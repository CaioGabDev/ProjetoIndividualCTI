package com.example.Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Libera o front-end do Vite (http://localhost:5173) para chamar a API.
 * O bean e consumido pelo SecurityConfig, que aplica o CORS antes dos
 * filtros de seguranca — e assim o preflight OPTIONS tambem passa.
 */
@Configuration
public class CorsConfig {

    private static final List<String> ORIGENS_LIBERADAS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173");

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ORIGENS_LIBERADAS);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L); // o navegador guarda o preflight por 1h

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", config);
        return fonte;
    }
}

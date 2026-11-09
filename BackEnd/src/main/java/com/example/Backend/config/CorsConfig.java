package com.example.Backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Quais sites podem chamar a API pelo navegador.
 *
 * O Vite local (localhost:5173) sempre entra. O endereco publicado (Vercel)
 * vem da variavel CORS_ORIGINS - sem precisar mudar codigo a cada deploy.
 * Mais de um endereco: separe por virgula. Aceita curinga, por exemplo
 * https://cti-front-*.vercel.app para liberar as previas da Vercel.
 *
 * O bean e consumido pelo SecurityConfig, que aplica o CORS antes dos
 * filtros de seguranca — e assim o preflight OPTIONS tambem passa.
 */
@Configuration
public class CorsConfig {

    private static final List<String> ORIGENS_LOCAIS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173");

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${cors.origins:}") String origensExtras) {
        List<String> origens = new ArrayList<>(ORIGENS_LOCAIS);

        Arrays.stream(origensExtras.split(","))
                .map(String::trim)
                .map(origem -> origem.endsWith("/") ? origem.substring(0, origem.length() - 1) : origem)
                .filter(origem -> !origem.isEmpty())
                .forEach(origens::add);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origens);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L); // o navegador guarda o preflight por 1h

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", config);
        return fonte;
    }
}

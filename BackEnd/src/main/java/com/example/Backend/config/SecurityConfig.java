package com.example.Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * O projeto tem spring-boot-starter-security no pom: sem esta classe o Spring
 * bloquearia todos os endpoints com tela de login. Aqui a API fica aberta,
 * que e o combinado para o ambiente de desenvolvimento.
 *
 * CSRF desligado porque a API e stateless (sem sessao e sem cookie): o token
 * de CSRF nao teria onde ser guardado e so quebraria POST/PUT/DELETE.
 * Quando entrar login de verdade, trocar o permitAll por regras por rota.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())   // usa o bean do CorsConfig
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req.anyRequest().permitAll())
                .build();
    }

    /** Usado pelo UsuarioService: a senha vai para o banco em hash, nunca em texto puro. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

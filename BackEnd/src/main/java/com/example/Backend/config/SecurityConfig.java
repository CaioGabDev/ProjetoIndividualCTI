package com.example.Backend.config;

import com.example.Backend.security.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Painel de controle da seguranca: quem entra onde.
 *
 * A API e stateless — nao existe sessao nem cookie, a identidade viaja no
 * token a cada pedido. Isso tem duas consequencias diretas aqui embaixo:
 * o CSRF fica desligado (sem cookie de sessao nao ha o que falsificar) e a
 * politica de sessao e STATELESS (o servidor nao guarda nada entre pedidos).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityFilter securityFilter) throws Exception {
        return http
                .cors(Customizer.withDefaults())   // usa o bean do CorsConfig
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        // O navegador manda um OPTIONS antes do pedido real (preflight
                        // do CORS). Ele vem sem token, entao precisa passar sempre.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // As unicas rotas publicas: sem elas ninguem conseguiria
                        // criar a primeira conta nem fazer o primeiro login.
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()

                        // Excluir conta e acao de administrador.
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")

                        // Todo o resto (clientes, contratos, upload, dashboard...) exige token.
                        .anyRequest().authenticated())

                // Sem este trecho o Spring responderia 403 para quem chega sem token.
                // O codigo certo e 401 (nao autenticado) — e e o 401 que o front usa
                // para limpar a sessao e devolver a pessoa ao login.
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // Nosso filtro roda antes do filtro de login padrao do Spring.
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Quem confere e-mail + senha no login.
     *
     * Montado a mao (DaoAuthenticationProvider + ProviderManager) para deixar
     * visivel o que ele junta: o AutenticacaoService, que acha o usuario pelo
     * e-mail, e o PasswordEncoder, que compara a senha digitada com o hash.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /** Usado no cadastro (gera o hash) e no login (confere o hash). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

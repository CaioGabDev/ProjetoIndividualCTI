package com.example.Backend.security;

import com.example.Backend.repository.UsuarioRepository;
import com.example.Backend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * O porteiro que olha TODO pedido antes de ele chegar ao controller.
 *
 * Le o header Authorization, valida o token e, se ele prestar, marca a
 * requisicao como autenticada. Repare que o filtro nunca bloqueia nada: ele
 * so diz "esta pessoa e fulano" ou nao diz nada. Quem bloqueia e o
 * SecurityConfig, olhando depois se a rota exigia login.
 *
 * Buscamos o usuario no banco em vez de confiar so no conteudo do token:
 * assim, uma conta excluida ou com perfil rebaixado para de valer na hora,
 * sem esperar o token vencer.
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository repository) {
        this.tokenService = tokenService;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(PREFIXO)) {
            tokenService.validarToken(header.substring(PREFIXO.length()))
                    .flatMap(repository::findByEmailIgnoreCase)
                    .ifPresent(usuario -> {
                        var auth = new UsernamePasswordAuthenticationToken(
                                usuario, null, usuario.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }

        chain.doFilter(request, response);
    }
}

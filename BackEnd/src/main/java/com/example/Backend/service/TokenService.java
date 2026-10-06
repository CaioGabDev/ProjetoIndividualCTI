package com.example.Backend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.Backend.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Fabrica e confere o cracha digital (JWT) do usuario logado.
 *
 * O token tem 3 partes: cabecalho, dados e assinatura. Os dados sao apenas
 * codificados em Base64 — qualquer um consegue ler. Por isso o token guarda
 * e-mail e perfil, nunca a senha. Quem impede alguem de FORJAR um token
 * (dizendo, por exemplo, que e ADMIN) e a assinatura HMAC256 feita com o
 * segredo: sem o segredo, a assinatura nao fecha e o token e recusado.
 */
@Service
public class TokenService {

    /** Quem emitiu o token. Conferido na validacao para nao aceitar token de outro sistema. */
    private static final String EMISSOR = "ancora-cti-insights";

    @Value("${api.security.token.secret}")
    private String segredo;

    @Value("${api.security.token.expiration-minutes}")
    private long minutosDeValidade;

    /** Chamado depois de o login ser aprovado. */
    public String gerarToken(Usuario usuario) {
        return JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(usuario.getEmail())                       // dono do token
                .withClaim("perfil", usuario.getPerfil().name())       // so informativo para o front
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(minutosDeValidade, ChronoUnit.MINUTES))
                .sign(Algorithm.HMAC256(segredo));
    }

    /**
     * Confere assinatura, emissor e validade.
     *
     * Devolve Optional: cheio com o e-mail do dono se o token presta, vazio se
     * foi adulterado, veio de outra chave ou ja venceu. O chamador (SecurityFilter)
     * so precisa saber "da para confiar?", sem tratar excecao.
     */
    public Optional<String> validarToken(String token) {
        try {
            String email = JWT.require(Algorithm.HMAC256(segredo))
                    .withIssuer(EMISSOR)
                    .build()
                    .verify(token)
                    .getSubject();
            return Optional.ofNullable(email);
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    /** Usado na resposta do login para o front saber quando a sessao cai. */
    public long getMinutosDeValidade() {
        return minutosDeValidade;
    }
}

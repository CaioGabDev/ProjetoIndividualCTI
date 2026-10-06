package com.example.Backend.dto;

/**
 * Resposta de POST /api/auth/login.
 *
 * Alem do token, devolve o usuario para o front preencher a topbar sem
 * precisar de uma segunda chamada logo apos o login.
 *
 * "tipo" e "expiraEmMinutos" nao sao decorativos: o front monta o header
 * "Bearer <token>" a partir do tipo e sabe quando a sessao vai cair.
 */
public record TokenDTO(
        String token,
        String tipo,
        long expiraEmMinutos,
        UsuarioRespostaDTO usuario) {

    public static TokenDTO de(String token, long expiraEmMinutos, UsuarioRespostaDTO usuario) {
        return new TokenDTO(token, "Bearer", expiraEmMinutos, usuario);
    }
}

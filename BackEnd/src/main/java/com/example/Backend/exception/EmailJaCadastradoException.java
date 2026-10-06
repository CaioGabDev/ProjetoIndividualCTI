package com.example.Backend.exception;

/**
 * Erro proprio do sistema: tentaram cadastrar um e-mail que ja existe.
 *
 * Virar uma excecao com nome proprio (em vez de um 400 generico) e o que
 * permite ao GlobalExceptionHandler responder 409 Conflict, que e o codigo
 * certo para "o recurso ja existe" e o que o front usa para dar a mensagem.
 */
public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Ja existe um usuario com o e-mail " + email + ".");
    }
}

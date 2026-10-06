package com.example.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Dados que chegam em POST /api/auth/login. */
public record LoginDTO(

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail invalido")
        String email,

        // Sem @Pattern de propósito: aqui so queremos saber se o campo veio.
        // Quem decide se a senha confere e o BCrypt, comparando com o hash.
        @NotBlank(message = "Informe a senha")
        String senha) {
}

package com.example.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados que chegam em POST /api/auth/register (e em POST /api/usuarios).
 *
 * E um record, nao a entidade: o cliente so consegue mandar estes quatro
 * campos. Se recebessemos Usuario direto, alguem poderia enviar "id",
 * "perfil" ou "criadoEm" no JSON e sobrescrever o que nao devia.
 */
public record CadastroDTO(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
        String email,

        // Opcional: se vier vazio, o service usa "Gestor comercial".
        @Size(max = 100, message = "O cargo deve ter no maximo 100 caracteres")
        String cargo,

        // O teto de 72 nao e capricho: o BCrypt so considera os 72 primeiros
        // bytes da senha, entao aceitar mais do que isso daria falsa seguranca.
        @NotBlank(message = "A senha e obrigatoria")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\d).{8,72}$",
                 message = "A senha deve ter de 8 a 72 caracteres, com letras e numeros")
        String senha) {
}

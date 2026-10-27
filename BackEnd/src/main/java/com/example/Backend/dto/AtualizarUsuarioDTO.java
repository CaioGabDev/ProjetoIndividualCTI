package com.example.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados que chegam em PUT /api/usuarios/{id}.
 *
 * Diferente do CadastroDTO em um ponto: a senha e OPCIONAL. Vir vazia
 * significa "nao quero trocar a senha", e o service mantem o hash atual.
 * Se vier preenchida, precisa passar na mesma regra do cadastro.
 */
public record AtualizarUsuarioDTO(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 255, message = "O e-mail deve ter no maximo 255 caracteres")
        String email,

        @Size(max = 100, message = "O cargo deve ter no maximo 100 caracteres")
        String cargo,

        // O "|^$" no fim do padrao e o que permite mandar a senha em branco.
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$|^$",
                 message = "A senha deve ter de 8 a 72 caracteres, com letras e numeros")
        String senha) {
}

package com.example.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Uma linha da planilha, do jeito que o uploadStore do front entrega.
 * E um DTO, nao a entidade: o front manda o consultor por nome (texto) e
 * nao conhece o id do banco.
 */
public record ClienteUploadDTO(

        @NotBlank(message = "O nome do cliente e obrigatorio")
        @Size(max = 150)
        String nome,

        @Size(max = 18)
        String cnpj,

        @Email(message = "E-mail invalido")
        @Size(max = 255)
        String email,

        @Size(max = 100)
        String segmento,

        @Pattern(regexp = "^[ABCabc]?$", message = "O nivel deve ser A, B ou C")
        String nivel,

        @PositiveOrZero(message = "O faturamento nao pode ser negativo")
        BigDecimal faturamento,

        /** Nome do consultor responsavel; o service acha ou cria o cadastro. */
        @Size(max = 150)
        String consultor,

        @Size(max = 100)
        String cidade) {
}

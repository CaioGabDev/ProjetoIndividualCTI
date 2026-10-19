package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Uma linha da aba upload_clientes, do jeito que o Pinia envia para
 * POST /api/clientes/validar.
 *
 * Aqui ficam so as regras simples e declarativas (campo obrigatorio, formato,
 * numero positivo, data valida). O que depende de comparar linhas ou de mexer
 * no conteudo — codigo repetido, padronizacao — fica no ClienteValidacaoService.
 *
 * O Pinia ja confere tudo isso na tela; repetimos aqui porque a API tambem
 * recebe pedidos que nao passaram pela tela (Thunder Client, Postman, outro sistema).
 */
public record ClienteDTO(

        @NotBlank(message = "O codigo do cliente e obrigatorio")
        String codigoCliente,           // coluna codigo_cliente

        @NotBlank(message = "O nome do cliente e obrigatorio")
        String nomeCliente,             // coluna nome_cliente

        @NotBlank(message = "O consultor e obrigatorio")
        String consultor,               // coluna consultor

        @NotBlank(message = "O segmento e obrigatorio")
        String segmento,                // coluna segmento (padronizado no service)

        // Categoria fechada: so a letra, maiuscula.
        @NotBlank(message = "O nivel do cliente e obrigatorio")
        @Pattern(regexp = "[ABC]", message = "O nivel deve ser A, B ou C")
        String nivelCliente,            // coluna nivel_cliente

        // @NotNull porque @Positive sozinho deixa passar null.
        @NotNull(message = "O faturamento anual e obrigatorio")
        @Positive(message = "O faturamento deve ser maior que zero")
        Double faturamentoAnual,        // coluna faturamento_anual

        @NotBlank(message = "Informe os servicos contratados")
        String servicosContratados,     // coluna servicos_contratados ("A;B;C")

        // Chega como "2025-01-15". Data impossivel (2025-02-30) nem vira
        // LocalDate: falha antes, na leitura do JSON (ver GlobalExceptionHandler).
        @NotNull(message = "A data de contratacao e obrigatoria")
        LocalDate dataContratacao,      // coluna data_contratacao

        String cidade,                  // opcional

        // Opcional, mas se vier preenchida precisa ter exatamente 2 letras.
        @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "A UF deve possuir duas letras")
        String uf) {
}

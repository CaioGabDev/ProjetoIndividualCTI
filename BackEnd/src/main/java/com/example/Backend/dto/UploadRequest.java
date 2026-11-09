package com.example.Backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Corpo do POST /api/upload.
 *
 * O front le a planilha, aponta os erros e manda so as linhas aprovadas, no
 * mesmo formato do POST /api/clientes/validar (ClienteDTO). "recebidas" e o
 * total de linhas da planilha, para o relatorio do lote mostrar quantas
 * ficaram pelo caminho.
 */
public record UploadRequest(
        @NotBlank(message = "Informe o nome do arquivo")
        @Size(max = 255, message = "O nome do arquivo deve ter no maximo 255 caracteres")
        String arquivo,

        @NotNull(message = "Informe quantas linhas a planilha tinha")
        @PositiveOrZero(message = "A quantidade de linhas nao pode ser negativa")
        Integer recebidas,

        @NotEmpty(message = "Envie ao menos um cliente")
        @Size(max = 5000, message = "Envie no maximo 5000 clientes por requisicao")
        List<@Valid @NotNull ClienteDTO> clientes) {
}

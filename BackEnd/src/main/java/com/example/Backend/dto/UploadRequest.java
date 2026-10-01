package com.example.Backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Corpo do POST /api/upload: a lista de clientes que o front ja tratou.
 * O @Valid na lista faz o Bean Validation descer em cada item.
 */
public record UploadRequest(

        /** Nome do arquivo ou tela de origem — vai para a Telemetria. */
        @Size(max = 100)
        String origem,

        @NotEmpty(message = "Envie ao menos um cliente")
        @Size(max = 5000, message = "Envie no maximo 5000 clientes por requisicao")
        @Valid
        List<ClienteUploadDTO> clientes) {
}

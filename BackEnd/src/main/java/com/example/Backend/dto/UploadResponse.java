package com.example.Backend.dto;

/** Resumo devolvido pelo POST /api/upload para a tela mostrar o resultado. */
public record UploadResponse(
        int recebidos,
        int inseridos,
        int atualizados,
        int consultoresCriados,
        Long telemetriaId,
        String mensagem) {
}

package com.example.Backend.dto;

import com.example.Backend.model.UploadLote;

import java.time.LocalDateTime;

/** Um envio de planilha, como o front mostra no historico. */
public record LoteDTO(
        Long id,
        String arquivo,
        String status,
        String mensagem,
        Integer linhasRecebidas,
        Integer linhasValidas,
        String enviadoPor,
        LocalDateTime enviadoEm) {

    public static LoteDTO de(UploadLote lote) {
        return new LoteDTO(
                lote.getId(),
                lote.getArquivo(),
                lote.getStatus(),
                lote.getMensagem(),
                lote.getLinhasRecebidas(),
                lote.getLinhasValidas(),
                lote.getEnviadoPor(),
                lote.getEnviadoEm());
    }
}

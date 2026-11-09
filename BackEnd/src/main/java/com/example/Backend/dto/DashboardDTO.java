package com.example.Backend.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Resposta do GET /api/dashboard.
 *
 * "resultado" e o JSON que o Python gravou em estatistica_lote (resumo,
 * porSegmento, porNivel, porServico, porMes, boxplot, histograma, insights...).
 * O @JsonRawValue manda o texto como JSON de verdade, sem o Java precisar
 * conhecer cada campo: se o Python ganhar uma estatistica nova, ela ja chega
 * no front sem mexer aqui.
 *
 * Os dois campos vem null quando nenhuma planilha foi processada ainda.
 */
public record DashboardDTO(
        LoteDTO lote,
        @JsonRawValue String resultado) {
}

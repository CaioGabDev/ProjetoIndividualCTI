package com.example.Backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Uma linha da tela de Clientes: o cliente ja com consultor e servicos resolvidos. */
public record ClienteResumoDTO(
        Long id,
        String codigo,
        String nome,
        String consultor,
        String segmento,
        String nivel,
        BigDecimal faturamento,
        String cidade,
        String uf,
        List<String> servicos,
        LocalDate dataContratacao) {
}

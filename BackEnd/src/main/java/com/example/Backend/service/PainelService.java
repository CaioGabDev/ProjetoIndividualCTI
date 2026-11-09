package com.example.Backend.service;

import com.example.Backend.dto.DashboardDTO;
import com.example.Backend.dto.LoteDTO;
import com.example.Backend.model.EstatisticaLote;
import com.example.Backend.model.UploadLote;
import com.example.Backend.repository.EstatisticaLoteRepository;
import com.example.Backend.repository.UploadLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dados do dashboard: o ultimo lote processado e as estatisticas que o Python
 * calculou para ele. O Java nao recalcula nada - so entrega.
 */
@Service
public class PainelService {

    private final UploadLoteRepository loteRepository;
    private final EstatisticaLoteRepository estatisticaRepository;

    public PainelService(UploadLoteRepository loteRepository, EstatisticaLoteRepository estatisticaRepository) {
        this.loteRepository = loteRepository;
        this.estatisticaRepository = estatisticaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDTO painel() {
        return loteRepository.findFirstByStatusOrderByIdDesc(UploadLote.PROCESSADO)
                .map(lote -> new DashboardDTO(
                        LoteDTO.de(lote),
                        estatisticaRepository.findFirstByLoteIdOrderByIdDesc(lote.getId())
                                .map(EstatisticaLote::getResultado)
                                .orElse(null)))
                .orElse(new DashboardDTO(null, null));
    }
}

package com.example.Backend.repository;

import com.example.Backend.model.UploadLote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UploadLoteRepository extends JpaRepository<UploadLote, Long> {

    /** O lote que alimenta o painel: o ultimo que o Python terminou de processar. */
    Optional<UploadLote> findFirstByStatusOrderByIdDesc(String status);

    /** Historico da tela de envio e do dashboard. */
    List<UploadLote> findTop20ByOrderByIdDesc();
}

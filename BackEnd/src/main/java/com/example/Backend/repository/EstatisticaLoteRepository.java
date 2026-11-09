package com.example.Backend.repository;

import com.example.Backend.model.EstatisticaLote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstatisticaLoteRepository extends JpaRepository<EstatisticaLote, Long> {

    Optional<EstatisticaLote> findFirstByLoteIdOrderByIdDesc(Long loteId);
}

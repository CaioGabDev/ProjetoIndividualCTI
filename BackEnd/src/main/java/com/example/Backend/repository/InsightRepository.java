package com.example.Backend.repository;

import com.example.Backend.model.Insight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso ao banco para Insight. */
public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);

    boolean existsByContratoId(Long contratoId);
}

package com.example.Backend.repository;

import com.example.Backend.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso ao banco para Contrato. */
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByClienteId(Long clienteId);

    /** Barra o DELETE de um Servico que ainda tem contrato apontando para ele. */
    boolean existsByServicoId(Long servicoId);

    boolean existsByClienteId(Long clienteId);
}

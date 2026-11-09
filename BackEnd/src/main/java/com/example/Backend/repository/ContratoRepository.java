package com.example.Backend.repository;

import com.example.Backend.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByClienteId(Long clienteId);

    boolean existsByServicoId(Long servicoId);

    boolean existsByClienteId(Long clienteId);

    /** Todos os contratos (com o servico) dos clientes de um lote, numa consulta so. */
    @Query("select ct from Contrato ct join fetch ct.servico join fetch ct.cliente c where c.lote.id = :loteId")
    List<Contrato> findDoLote(@Param("loteId") Long loteId);
}

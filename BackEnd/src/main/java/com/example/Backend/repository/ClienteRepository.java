package com.example.Backend.repository;

import com.example.Backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Clientes de um lote, com o consultor ja carregado (evita uma consulta por linha). */
    @Query("select c from Cliente c left join fetch c.consultor where c.lote.id = :loteId order by c.faturamentoAnual desc")
    List<Cliente> findDoLote(@Param("loteId") Long loteId);

    List<Cliente> findByConsultorId(Long consultorId);
}

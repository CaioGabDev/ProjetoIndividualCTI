package com.example.Backend.repository;

import com.example.Backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acesso ao banco para Cliente. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Chave usada pelo upload para decidir entre inserir e atualizar. */
    Optional<Cliente> findFirstByCnpj(String cnpj);

    Optional<Cliente> findFirstByNomeIgnoreCase(String nome);

    List<Cliente> findByConsultorId(Long consultorId);
}

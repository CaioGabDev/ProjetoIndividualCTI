package com.example.Backend.repository;

import com.example.Backend.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso ao banco para Servico. */
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Optional<Servico> findFirstByNomeIgnoreCase(String nome);
}

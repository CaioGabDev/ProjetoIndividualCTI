package com.example.Backend.repository;

import com.example.Backend.model.Consultor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso ao banco para Consultor. O Spring Data gera a implementacao. */
public interface ConsultorRepository extends JpaRepository<Consultor, Long> {

    /** Usado no upload: reaproveita o consultor em vez de duplicar o cadastro. */
    Optional<Consultor> findFirstByNomeIgnoreCase(String nome);

    /** Usado para checar matricula duplicada ignorando o proprio registro. */
    Optional<Consultor> findFirstByMatriculaIgnoreCase(String matricula);
}

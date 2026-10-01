package com.example.Backend.repository;

import com.example.Backend.model.Telemetria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso ao banco para Telemetria (log de eventos). */
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {

    /** Os eventos mais recentes primeiro — e o que a tela de historico mostra. */
    List<Telemetria> findAllByOrderByDataHoraDesc();
}

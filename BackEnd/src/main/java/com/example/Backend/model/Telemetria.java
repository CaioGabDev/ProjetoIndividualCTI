package com.example.Backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registro dos eventos de processamento: uploads de planilha, logins, chamadas da API.
 * O material nao define cardinalidade para esta entidade porque ela e um log.
 */
@Entity
@Table(name = "telemetrias")
public class Telemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ex.: UPLOAD, LOGIN, PYTHON, API. */
    @Column(nullable = false, length = 50)
    private String evento;

    @Column(length = 100)
    private String origem;

    @Column(length = 500)
    private String mensagem;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    public Telemetria() {
    }

    public Telemetria(String evento, String origem, String mensagem) {
        this.evento = evento;
        this.origem = origem;
        this.mensagem = mensagem;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}

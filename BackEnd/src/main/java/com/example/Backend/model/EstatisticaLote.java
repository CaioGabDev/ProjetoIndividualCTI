package com.example.Backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Resultado da analise do Python para um lote: media, mediana, boxplot,
 * histograma, agrupamentos e as frases de insight, tudo num JSON so.
 *
 * Quem grava e o Python (banco.py). O Java so le e devolve o JSON como veio
 * para o dashboard - por isso o campo e texto, e nao um objeto mapeado.
 */
@Entity
@Table(name = "estatistica_lote")
public class EstatisticaLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UploadLote lote;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    @Column(nullable = false, columnDefinition = "text")
    private String resultado;

    public EstatisticaLote() {
    }

    public Long getId() {
        return id;
    }

    public UploadLote getLote() {
        return lote;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public String getResultado() {
        return resultado;
    }
}

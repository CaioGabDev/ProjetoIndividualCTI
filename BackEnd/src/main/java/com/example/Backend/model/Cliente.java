package com.example.Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente da carteira — e o centro do dominio do CTI Insights.
 * N Clientes -> 1 Consultor | 1 Cliente -> N Contratos | N Clientes -> 1 Lote.
 *
 * Quem grava os clientes e o ETL em Python (analytics-python/banco.py), depois
 * de limpar a planilha. Por isso a tabela e as colunas seguem o SQL de la:
 * o Hibernate cria a estrutura, o Python preenche.
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reprocessar ou apagar um lote apaga os clientes dele junto (ON DELETE CASCADE).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private UploadLote lote;

    @Column(name = "codigo_cti", nullable = false, length = 60)
    private String codigoCti;

    @Column(nullable = false, length = 255)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "consultor_id")
    private Consultor consultor;

    @Column(length = 40)
    private String segmento;

    /** Classificacao da carteira: A, B ou C. */
    @Column(length = 1)
    private String nivel;

    @Column(name = "faturamento_anual", precision = 14, scale = 2)
    private BigDecimal faturamentoAnual;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @OneToMany(mappedBy = "cliente")
    @JsonIgnore
    private List<Contrato> contratos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    @JsonIgnore
    private List<Insight> insights = new ArrayList<>();

    public Cliente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UploadLote getLote() {
        return lote;
    }

    public String getCodigoCti() {
        return codigoCti;
    }

    public String getNome() {
        return nome;
    }

    public Consultor getConsultor() {
        return consultor;
    }

    public String getSegmento() {
        return segmento;
    }

    public String getNivel() {
        return nivel;
    }

    public BigDecimal getFaturamentoAnual() {
        return faturamentoAnual;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public List<Contrato> getContratos() {
        return contratos;
    }

    public List<Insight> getInsights() {
        return insights;
    }
}

package com.example.Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Servico contratado pelos clientes (link, fibra, cloud...).
 * Relacionamento do projeto: 1 Servico -> N Contratos.
 */
@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do servico e obrigatorio")
    @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @Size(max = 100, message = "A categoria deve ter no maximo 100 caracteres")
    @Column(length = 100)
    private String categoria;

    @OneToMany(mappedBy = "servico")
    @JsonIgnore
    private List<Contrato> contratos = new ArrayList<>();

    public Servico() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public List<Contrato> getContratos() {
        return contratos;
    }

    public void setContratos(List<Contrato> contratos) {
        this.contratos = contratos;
    }
}

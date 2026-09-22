package com.example.Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Consultor responsavel por uma carteira de clientes.
 * Relacionamento do projeto: 1 Consultor -> N Clientes.
 */
@Entity
@Table(name = "consultores")
public class Consultor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(unique = true, length = 30)
    private String matricula;

    // A chave estrangeira consultor_id fica do lado de Cliente (mappedBy).
    // @JsonIgnore evita recursao infinita ao converter para JSON.
    @OneToMany(mappedBy = "consultor")
    @JsonIgnore
    private List<Cliente> clientes = new ArrayList<>();

    public Consultor() {
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

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }
}

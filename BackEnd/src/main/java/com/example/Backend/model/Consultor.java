package com.example.Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    @NotBlank(message = "O nome do consultor e obrigatorio")
    @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @Size(max = 30, message = "A matricula deve ter no maximo 30 caracteres")
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

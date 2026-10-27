package com.example.Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Usuario que acessa o painel (tela de Login do front-end).
 *
 * Implementa UserDetails: e o formato de usuario que o Spring Security entende.
 * Com isso o proprio Spring consegue conferir a senha no login (BCrypt) e saber
 * quais permissoes a pessoa tem, sem a gente escrever essa comparacao na mao.
 *
 * O campo senha guarda SEMPRE o hash BCrypt (60 caracteres), nunca o texto
 * digitado. Ele entra no POST/PUT e nunca sai na resposta JSON (WRITE_ONLY).
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "O cargo e obrigatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String cargo = "Gestor comercial";

    // Sem @NotBlank: no PUT, senha vazia significa "manter a atual".
    // A obrigatoriedade na criacao e checada no UsuarioService.
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    // Nivel de acesso. Gravado como texto (USER / ADMIN) para o banco ficar legivel.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil = Perfil.USER;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Usuario() {
    }

    public Usuario(String nome, String email, String cargo, String senhaHash, Perfil perfil) {
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.senha = senhaHash;
        this.perfil = perfil;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    // ------------------------------------------------------------------
    // Contrato do UserDetails (o que o Spring Security pergunta a classe).
    // @JsonIgnore em todos: sao detalhes internos da seguranca, nao fazem
    // parte da resposta da API (quem responde o JSON e o UsuarioRespostaDTO).
    // ------------------------------------------------------------------

    /** Permissoes da pessoa. O prefixo ROLE_ e o que hasRole("ADMIN") procura. */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    /** Diz ao Spring onde esta a senha (o hash) para ele comparar no login. */
    @Override
    @JsonIgnore
    public String getPassword() {
        return senha;
    }

    /** Diz ao Spring qual campo e o login. Aqui, o e-mail. */
    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }
}

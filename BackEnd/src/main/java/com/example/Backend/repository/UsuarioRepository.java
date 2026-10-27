package com.example.Backend.repository;

import com.example.Backend.model.Perfil;
import com.example.Backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acesso ao banco para Usuario. O Spring gera o SQL pelo nome do metodo. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Usado para nao deixar o sistema ficar sem nenhum administrador. */
    long countByPerfil(Perfil perfil);
}

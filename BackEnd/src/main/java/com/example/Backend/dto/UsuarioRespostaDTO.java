package com.example.Backend.dto;

import com.example.Backend.model.Usuario;

import java.time.LocalDateTime;

/**
 * O que a API DEVOLVE sobre um usuario.
 *
 * A entidade Usuario nunca sai no JSON: ela carrega o hash da senha e os
 * metodos do UserDetails. Este record tem so os campos seguros de expor.
 */
public record UsuarioRespostaDTO(
        Long id,
        String nome,
        String email,
        String cargo,
        String perfil,
        LocalDateTime criadoEm) {

    /** Converte a entidade na resposta, copiando so o que pode sair. */
    public static UsuarioRespostaDTO de(Usuario u) {
        return new UsuarioRespostaDTO(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getCargo(),
                u.getPerfil().name(),
                u.getCriadoEm());
    }
}

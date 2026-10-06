package com.example.Backend.service;

import com.example.Backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Ensina o Spring Security a achar um usuario no NOSSO banco.
 *
 * O Spring sabe comparar senha com hash, mas nao conhece a tabela usuarios.
 * Esta classe e a ponte: recebe o e-mail digitado no login e devolve o
 * Usuario (que implementa UserDetails). Dai o Spring pega o hash sozinho,
 * compara com a senha digitada pelo BCrypt e aprova ou recusa.
 */
@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository repository;

    public AutenticacaoService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
    }
}

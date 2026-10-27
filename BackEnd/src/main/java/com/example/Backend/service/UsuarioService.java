package com.example.Backend.service;

import com.example.Backend.dto.AtualizarUsuarioDTO;
import com.example.Backend.dto.CadastroDTO;
import com.example.Backend.exception.EmailJaCadastradoException;
import com.example.Backend.model.Perfil;
import com.example.Backend.model.Usuario;
import com.example.Backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Regras do cadastro e do CRUD de Usuario (quem acessa o painel). */
@Service
public class UsuarioService {

    private static final String CARGO_PADRAO = "Gestor comercial";

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + id + " nao encontrado(a)."));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return repository.findByEmailIgnoreCase(normalizar(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado."));
    }

    /**
     * Cria a conta a partir do formulario de cadastro.
     *
     * O primeiro usuario do sistema vira ADMIN: alguem precisa poder
     * administrar contas, e nao ha como pedir permissao a um admin que ainda
     * nao existe. Do segundo em diante, todo mundo entra como USER.
     */
    @Transactional
    public Usuario cadastrar(CadastroDTO dto) {
        String email = normalizar(dto.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new EmailJaCadastradoException(email);
        }

        Perfil perfil = repository.count() == 0 ? Perfil.ADMIN : Perfil.USER;

        // A senha nunca e gravada em texto puro: o encode gera o hash BCrypt.
        return repository.save(new Usuario(
                dto.nome().trim(),
                email,
                cargoOuPadrao(dto.cargo()),
                encoder.encode(dto.senha()),
                perfil));
    }

    @Transactional
    public Usuario atualizar(Long id, AtualizarUsuarioDTO dto) {
        Usuario atual = buscar(id);
        String email = normalizar(dto.email());

        // So reclama se o e-mail pertencer a OUTRA pessoa (manter o proprio e valido).
        repository.findByEmailIgnoreCase(email)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new EmailJaCadastradoException(email);
                });

        atual.setNome(dto.nome().trim());
        atual.setEmail(email);
        atual.setCargo(cargoOuPadrao(dto.cargo()));

        // Senha em branco no PUT significa "mantem a atual".
        if (dto.senha() != null && !dto.senha().isBlank()) {
            atual.setSenha(encoder.encode(dto.senha()));
        }

        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario alvo = buscar(id);

        // Trava de seguranca: sem nenhum ADMIN ninguem mais administra contas.
        if (alvo.getPerfil() == Perfil.ADMIN && repository.countByPerfil(Perfil.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este e o unico administrador: promova outro antes de excluir.");
        }

        repository.delete(alvo);
    }

    /** E-mail e identificacao: guardar sempre igual evita duplicata por maiuscula ou espaco. */
    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String cargoOuPadrao(String cargo) {
        return (cargo == null || cargo.isBlank()) ? CARGO_PADRAO : cargo.trim();
    }
}

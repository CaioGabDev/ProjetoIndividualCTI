package com.example.Backend.service;

import com.example.Backend.model.Usuario;
import com.example.Backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Regras do CRUD de Usuario (quem acessa o painel). */
@Service
public class UsuarioService {

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

    @Transactional
    public Usuario criar(Usuario novo) {
        novo.setId(null);
        novo.setEmail(novo.getEmail().trim().toLowerCase());
        validarEmail(novo.getEmail(), null);
        validarSenha(novo.getSenha());

        // A senha nunca e gravada em texto puro.
        novo.setSenha(encoder.encode(novo.getSenha()));
        return repository.save(novo);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dados) {
        Usuario atual = buscar(id);
        String email = dados.getEmail().trim().toLowerCase();
        validarEmail(email, id);

        atual.setNome(dados.getNome());
        atual.setEmail(email);
        atual.setCargo(dados.getCargo());

        // Senha em branco no PUT significa "mantem a atual".
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            atual.setSenha(encoder.encode(dados.getSenha()));
        }

        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    /** Na criacao a senha e obrigatoria (no PUT ela pode vir vazia). */
    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha e obrigatoria.");
        }
        if (senha.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter ao menos 6 caracteres.");
        }
    }

    /** O e-mail e a identificacao do usuario, entao nao pode repetir. */
    private void validarEmail(String email, Long idIgnorado) {
        repository.findByEmailIgnoreCase(email)
                .filter(u -> !u.getId().equals(idIgnorado))
                .ifPresent(u -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ja existe um usuario com o e-mail " + email + ".");
                });
    }
}

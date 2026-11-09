package com.example.Backend.config;

import com.example.Backend.model.Perfil;
import com.example.Backend.model.Usuario;
import com.example.Backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o administrador na primeira vez que a API sobe.
 *
 * Os dados vem das variaveis ADMIN_EMAIL e ADMIN_PASSWORD (na Azure, em
 * "Variaveis de ambiente"). Assim o banco novo da nuvem ja nasce com alguem
 * capaz de entrar, sem senha escrita no codigo. Se o e-mail ja existe, nada
 * muda - reiniciar o app nao troca a senha de ninguem.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);
    private static final String SENHA_VALIDA = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final String email;
    private final String senha;
    private final String nome;

    public AdminInicial(UsuarioRepository repository,
                        PasswordEncoder encoder,
                        @Value("${admin.email:}") String email,
                        @Value("${admin.password:}") String senha,
                        @Value("${admin.nome:Administrador}") String nome) {
        this.repository = repository;
        this.encoder = encoder;
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.senha = senha == null ? "" : senha;
        this.nome = nome;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || senha.isEmpty()) {
            return;   // sem ADMIN_EMAIL/ADMIN_PASSWORD: o primeiro cadastro vira ADMIN (UsuarioService)
        }
        if (!senha.matches(SENHA_VALIDA)) {
            log.warn("ADMIN_PASSWORD ignorada: precisa de 8 a 72 caracteres, com letras e numeros.");
            return;
        }
        if (repository.existsByEmailIgnoreCase(email)) {
            return;
        }

        repository.save(new Usuario(nome, email, "Administrador", encoder.encode(senha), Perfil.ADMIN));
        log.info("Administrador inicial criado: {}", email);
    }
}

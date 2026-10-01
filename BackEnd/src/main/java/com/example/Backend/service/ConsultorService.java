package com.example.Backend.service;

import com.example.Backend.model.Consultor;
import com.example.Backend.repository.ClienteRepository;
import com.example.Backend.repository.ConsultorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Regras do CRUD de Consultor. */
@Service
public class ConsultorService {

    private final ConsultorRepository repository;
    private final ClienteRepository clienteRepository;

    public ConsultorService(ConsultorRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<Consultor> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Consultor buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultor " + id + " nao encontrado(a)."));
    }

    @Transactional
    public Consultor criar(Consultor novo) {
        novo.setId(null);                 // id vem do banco, nunca do cliente HTTP
        validarMatricula(novo, null);
        return repository.save(novo);
    }

    @Transactional
    public Consultor atualizar(Long id, Consultor dados) {
        Consultor atual = buscar(id);
        validarMatricula(dados, id);

        atual.setNome(dados.getNome());
        atual.setMatricula(dados.getMatricula());
        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        Consultor atual = buscar(id);

        // Nao apaga quem ainda tem carteira: o cliente ficaria sem responsavel.
        if (!clienteRepository.findByConsultorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O consultor tem clientes vinculados. Troque o consultor desses clientes antes de excluir.");
        }

        repository.delete(atual);
    }

    /** A matricula e unica no banco; checar antes da gravacao da um erro mais claro. */
    private void validarMatricula(Consultor consultor, Long idIgnorado) {
        String matricula = consultor.getMatricula();
        if (matricula == null || matricula.isBlank()) {
            consultor.setMatricula(null);   // vazio vira null para nao colidir no unique
            return;
        }

        repository.findFirstByMatriculaIgnoreCase(matricula)
                .filter(c -> !c.getId().equals(idIgnorado))
                .ifPresent(c -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Ja existe um consultor com a matricula " + matricula + ".");
                });
    }
}

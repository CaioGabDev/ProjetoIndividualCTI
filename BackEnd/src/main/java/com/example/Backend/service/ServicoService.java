package com.example.Backend.service;

import com.example.Backend.model.Servico;
import com.example.Backend.repository.ContratoRepository;
import com.example.Backend.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Regras do CRUD de Servico. */
@Service
public class ServicoService {

    private final ServicoRepository repository;
    private final ContratoRepository contratoRepository;

    public ServicoService(ServicoRepository repository, ContratoRepository contratoRepository) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
    }

    @Transactional(readOnly = true)
    public List<Servico> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Servico buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servico " + id + " nao encontrado(a)."));
    }

    @Transactional
    public Servico criar(Servico novo) {
        novo.setId(null);
        return repository.save(novo);
    }

    @Transactional
    public Servico atualizar(Long id, Servico dados) {
        Servico atual = buscar(id);
        atual.setNome(dados.getNome());
        atual.setCategoria(dados.getCategoria());
        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        Servico atual = buscar(id);

        // Apagar deixaria contratos orfaos de servico.
        if (contratoRepository.existsByServicoId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O servico esta em uso por contratos. Exclua ou altere esses contratos antes.");
        }

        repository.delete(atual);
    }
}

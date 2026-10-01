package com.example.Backend.service;

import com.example.Backend.model.Cliente;
import com.example.Backend.model.Contrato;
import com.example.Backend.model.Insight;
import com.example.Backend.repository.ClienteRepository;
import com.example.Backend.repository.ContratoRepository;
import com.example.Backend.repository.InsightRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/** Regras do CRUD de Insight. */
@Service
public class InsightService {

    private final InsightRepository repository;
    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;

    public InsightService(InsightRepository repository,
                          ClienteRepository clienteRepository,
                          ContratoRepository contratoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.contratoRepository = contratoRepository;
    }

    @Transactional(readOnly = true)
    public List<Insight> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Insight buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insight " + id + " nao encontrado(a)."));
    }

    @Transactional
    public Insight criar(Insight novo) {
        novo.setId(null);
        novo.setCliente(resolverCliente(novo.getCliente()));
        novo.setContrato(resolverContrato(novo.getContrato()));
        validarVinculo(novo);

        if (novo.getGeradoEm() == null) {
            novo.setGeradoEm(LocalDateTime.now());
        }

        return repository.save(novo);
    }

    @Transactional
    public Insight atualizar(Long id, Insight dados) {
        Insight atual = buscar(id);

        atual.setTipo(dados.getTipo());
        atual.setDescricao(dados.getDescricao());
        atual.setCliente(resolverCliente(dados.getCliente()));
        atual.setContrato(resolverContrato(dados.getContrato()));
        validarVinculo(atual);

        if (dados.getGeradoEm() != null) {
            atual.setGeradoEm(dados.getGeradoEm());
        }

        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    /** Um insight solto nao serve para nada: precisa de cliente ou de contrato. */
    private void validarVinculo(Insight insight) {
        if (insight.getCliente() == null && insight.getContrato() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O insight precisa estar ligado a um cliente ou a um contrato.");
        }
    }

    private Cliente resolverCliente(Cliente referencia) {
        if (referencia == null || referencia.getId() == null) {
            return null;
        }

        return clienteRepository.findById(referencia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cliente " + referencia.getId() + " nao existe."));
    }

    private Contrato resolverContrato(Contrato referencia) {
        if (referencia == null || referencia.getId() == null) {
            return null;
        }

        return contratoRepository.findById(referencia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Contrato " + referencia.getId() + " nao existe."));
    }
}

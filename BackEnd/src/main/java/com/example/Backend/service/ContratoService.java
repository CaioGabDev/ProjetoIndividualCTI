package com.example.Backend.service;

import com.example.Backend.model.Cliente;
import com.example.Backend.model.Contrato;
import com.example.Backend.model.Servico;
import com.example.Backend.repository.ClienteRepository;
import com.example.Backend.repository.ContratoRepository;
import com.example.Backend.repository.InsightRepository;
import com.example.Backend.repository.ServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Regras do CRUD de Contrato — a entidade com mais validacoes do projeto. */
@Service
public class ContratoService {

    private final ContratoRepository repository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final InsightRepository insightRepository;

    public ContratoService(ContratoRepository repository,
                           ClienteRepository clienteRepository,
                           ServicoRepository servicoRepository,
                           InsightRepository insightRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.insightRepository = insightRepository;
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Contrato buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato " + id + " nao encontrado(a)."));
    }

    @Transactional
    public Contrato criar(Contrato novo) {
        novo.setId(null);
        novo.setCliente(resolverCliente(novo.getCliente()));
        novo.setServico(resolverServico(novo.getServico()));
        validarDatas(novo);

        if (novo.getAtivo() == null) {
            novo.setAtivo(Boolean.TRUE);
        }

        return repository.save(novo);
    }

    @Transactional
    public Contrato atualizar(Long id, Contrato dados) {
        Contrato atual = buscar(id);

        atual.setDataInicio(dados.getDataInicio());
        atual.setDataFim(dados.getDataFim());
        atual.setValor(dados.getValor());
        atual.setAtivo(dados.getAtivo() == null ? Boolean.TRUE : dados.getAtivo());
        atual.setCliente(resolverCliente(dados.getCliente()));
        atual.setServico(resolverServico(dados.getServico()));
        validarDatas(atual);

        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        Contrato atual = buscar(id);

        if (insightRepository.existsByContratoId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O contrato tem insights vinculados. Exclua os insights antes.");
        }

        repository.delete(atual);
    }

    /** Contrato sem cliente nao existe: a coluna cliente_id e obrigatoria. */
    private Cliente resolverCliente(Cliente referencia) {
        if (referencia == null || referencia.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o cliente do contrato (campo cliente.id).");
        }

        return clienteRepository.findById(referencia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cliente " + referencia.getId() + " nao existe."));
    }

    /** O servico tambem e exigido: a regra do projeto e 1 Servico -> N Contratos. */
    private Servico resolverServico(Servico referencia) {
        if (referencia == null || referencia.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o servico do contrato (campo servico.id).");
        }

        return servicoRepository.findById(referencia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Servico " + referencia.getId() + " nao existe."));
    }

    private void validarDatas(Contrato contrato) {
        if (contrato.getDataFim() != null
                && contrato.getDataFim().isBefore(contrato.getDataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de fim nao pode ser anterior a data de inicio.");
        }
    }
}

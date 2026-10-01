package com.example.Backend.service;

import com.example.Backend.model.Telemetria;
import com.example.Backend.repository.TelemetriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/** Regras do CRUD de Telemetria e o registro de eventos usado pelo upload. */
@Service
public class TelemetriaService {

    private final TelemetriaRepository repository;

    public TelemetriaService(TelemetriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Telemetria> listar() {
        return repository.findAllByOrderByDataHoraDesc();
    }

    @Transactional(readOnly = true)
    public Telemetria buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telemetria " + id + " nao encontrado(a)."));
    }

    @Transactional
    public Telemetria criar(Telemetria nova) {
        nova.setId(null);
        nova.setEvento(nova.getEvento().trim().toUpperCase());

        if (nova.getDataHora() == null) {
            nova.setDataHora(LocalDateTime.now());
        }

        return repository.save(nova);
    }

    @Transactional
    public Telemetria atualizar(Long id, Telemetria dados) {
        Telemetria atual = buscar(id);

        atual.setEvento(dados.getEvento().trim().toUpperCase());
        atual.setOrigem(dados.getOrigem());
        atual.setMensagem(dados.getMensagem());

        if (dados.getDataHora() != null) {
            atual.setDataHora(dados.getDataHora());
        }

        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    /**
     * Atalho usado por outros services (upload, por exemplo) para deixar
     * o evento gravado sem precisar montar a entidade na mao.
     */
    @Transactional
    public Telemetria registrar(String evento, String origem, String mensagem) {
        return repository.save(new Telemetria(evento, origem, mensagem));
    }
}

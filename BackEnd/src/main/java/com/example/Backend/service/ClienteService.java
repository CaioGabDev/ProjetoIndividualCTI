package com.example.Backend.service;

import com.example.Backend.dto.ClienteResumoDTO;
import com.example.Backend.model.Cliente;
import com.example.Backend.model.Contrato;
import com.example.Backend.model.UploadLote;
import com.example.Backend.repository.ClienteRepository;
import com.example.Backend.repository.ContratoRepository;
import com.example.Backend.repository.UploadLoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Carteira de clientes. Os clientes sao gravados pelo ETL em Python, um
 * conjunto por lote; a tela mostra a "base atual", que e o ultimo lote
 * processado - o mesmo que alimenta o dashboard.
 */
@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final ContratoRepository contratoRepository;
    private final UploadLoteRepository loteRepository;

    public ClienteService(ClienteRepository repository,
                          ContratoRepository contratoRepository,
                          UploadLoteRepository loteRepository) {
        this.repository = repository;
        this.contratoRepository = contratoRepository;
        this.loteRepository = loteRepository;
    }

    /** Duas consultas no total (clientes + contratos), e nao uma por cliente. */
    @Transactional(readOnly = true)
    public List<ClienteResumoDTO> listarBaseAtual() {
        return loteRepository.findFirstByStatusOrderByIdDesc(UploadLote.PROCESSADO)
                .map(lote -> listarDoLote(lote.getId()))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente " + id + " nao encontrado(a)."));
    }

    /** Os contratos e insights do cliente saem junto (ON DELETE CASCADE no banco). */
    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    private List<ClienteResumoDTO> listarDoLote(Long loteId) {
        Map<Long, List<Contrato>> contratosPorCliente = contratoRepository.findDoLote(loteId).stream()
                .collect(Collectors.groupingBy(ct -> ct.getCliente().getId()));

        return repository.findDoLote(loteId).stream()
                .map(cliente -> {
                    List<Contrato> contratos = contratosPorCliente.getOrDefault(cliente.getId(), List.of());

                    List<String> servicos = contratos.stream()
                            .map(ct -> ct.getServico().getNome())
                            .distinct()
                            .sorted()
                            .toList();

                    LocalDate contratacao = contratos.stream()
                            .map(Contrato::getDataContratacao)
                            .filter(Objects::nonNull)
                            .min(Comparator.naturalOrder())
                            .orElse(null);

                    return new ClienteResumoDTO(
                            cliente.getId(),
                            cliente.getCodigoCti(),
                            cliente.getNome(),
                            cliente.getConsultor() == null ? null : cliente.getConsultor().getNome(),
                            cliente.getSegmento(),
                            cliente.getNivel(),
                            cliente.getFaturamentoAnual(),
                            cliente.getCidade(),
                            cliente.getUf(),
                            servicos,
                            contratacao);
                })
                .toList();
    }
}

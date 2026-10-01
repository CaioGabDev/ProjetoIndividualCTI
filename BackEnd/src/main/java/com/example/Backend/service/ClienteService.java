package com.example.Backend.service;

import com.example.Backend.model.Cliente;
import com.example.Backend.model.Consultor;
import com.example.Backend.repository.ClienteRepository;
import com.example.Backend.repository.ConsultorRepository;
import com.example.Backend.repository.ContratoRepository;
import com.example.Backend.repository.InsightRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Regras do CRUD de Cliente. */
@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final ConsultorRepository consultorRepository;
    private final ContratoRepository contratoRepository;
    private final InsightRepository insightRepository;

    public ClienteService(ClienteRepository repository,
                          ConsultorRepository consultorRepository,
                          ContratoRepository contratoRepository,
                          InsightRepository insightRepository) {
        this.repository = repository;
        this.consultorRepository = consultorRepository;
        this.contratoRepository = contratoRepository;
        this.insightRepository = insightRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente " + id + " nao encontrado(a)."));
    }

    @Transactional
    public Cliente criar(Cliente novo) {
        novo.setId(null);                 // id vem do banco, nunca do cliente HTTP
        normalizar(novo);
        novo.setConsultor(resolverConsultor(novo.getConsultor()));
        return repository.save(novo);
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente dados) {
        Cliente atual = buscar(id);
        normalizar(dados);

        atual.setNome(dados.getNome());
        atual.setEmail(dados.getEmail());
        atual.setSegmento(dados.getSegmento());
        atual.setCidade(dados.getCidade());
        atual.setCnpj(dados.getCnpj());
        atual.setNivel(dados.getNivel());
        atual.setFaturamento(dados.getFaturamento());
        atual.setConsultor(resolverConsultor(dados.getConsultor()));

        return repository.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente atual = buscar(id);

        if (contratoRepository.existsByClienteId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O cliente tem contratos vinculados. Exclua os contratos antes.");
        }
        if (insightRepository.existsByClienteId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O cliente tem insights vinculados. Exclua os insights antes.");
        }

        repository.delete(atual);
    }

    /**
     * O front envia o relacionamento como consultor: { id: 3 }.
     * Aqui a referencia e trocada pela entidade real do banco — sem isso o JPA
     * tentaria gravar um consultor solto, e um id invalido passaria batido.
     */
    private Consultor resolverConsultor(Consultor referencia) {
        if (referencia == null || referencia.getId() == null) {
            return null;                  // cliente sem consultor e permitido
        }

        return consultorRepository.findById(referencia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Consultor " + referencia.getId() + " nao existe."));
    }

    /**
     * Padroniza os campos antes de gravar. Precisa usar as MESMAS regras do
     * UploadService: se o CRUD gravasse "12.345.678/0001-99" e o upload
     * "12345678000199", o mesmo cliente entraria duas vezes no banco.
     */
    private void normalizar(Cliente cliente) {
        cliente.setNome(limpar(cliente.getNome()));
        cliente.setSegmento(limpar(cliente.getSegmento()));
        cliente.setCidade(limpar(cliente.getCidade()));

        String email = limpar(cliente.getEmail());
        cliente.setEmail(email == null ? null : email.toLowerCase());

        cliente.setCnpj(somenteDigitos(cliente.getCnpj()));

        String nivel = limpar(cliente.getNivel());
        cliente.setNivel(nivel == null ? null : nivel.toUpperCase());
    }

    private String limpar(String texto) {
        if (texto == null) return null;
        String limpo = texto.trim().replaceAll("\\s{2,}", " ");
        return limpo.isEmpty() ? null : limpo;
    }

    /** CNPJ guardado so com digitos, igual ao UploadService. */
    private String somenteDigitos(String texto) {
        String limpo = limpar(texto);
        if (limpo == null) return null;
        String digitos = limpo.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }
}

package com.example.Backend.controller;

import com.example.Backend.dto.ClienteResumoDTO;
import com.example.Backend.model.Cliente;
import com.example.Backend.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Carteira de clientes em /api/clientes.
 *
 * Os clientes entram pelo envio de planilha (POST /api/upload -> ETL em Python),
 * por isso aqui nao ha POST/PUT: a tela so consulta e remove.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    /** 200 com os clientes da base atual (ultimo lote processado). */
    @GetMapping
    public List<ClienteResumoDTO> listar() {
        return service.listarBaseAtual();
    }

    /** 200 com o registro, ou 404 se o id nao existir. */
    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** 204 sem corpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

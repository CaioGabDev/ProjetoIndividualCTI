package com.example.Backend.controller;

import com.example.Backend.model.Cliente;
import com.example.Backend.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** CRUD de Cliente em /api/clientes. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    /** 200 com a lista completa. */
    @GetMapping
    public List<Cliente> listar() {
        return service.listar();
    }

    /** 200 com o registro, ou 404 se o id nao existir. */
    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** 201 com o header Location apontando para o novo registro. */
    @PostMapping
    public ResponseEntity<Cliente> criar(@RequestBody @Valid Cliente corpo) {
        Cliente salvo = service.criar(corpo);
        return ResponseEntity.created(URI.create("/api/clientes/" + salvo.getId())).body(salvo);
    }

    /** 200 com o registro atualizado. */
    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Long id, @RequestBody @Valid Cliente corpo) {
        return service.atualizar(id, corpo);
    }

    /** 204 sem corpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

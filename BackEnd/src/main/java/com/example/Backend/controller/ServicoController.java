package com.example.Backend.controller;

import com.example.Backend.model.Servico;
import com.example.Backend.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** CRUD de Servico em /api/servicos. */
@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    /** 200 com a lista completa. */
    @GetMapping
    public List<Servico> listar() {
        return service.listar();
    }

    /** 200 com o registro, ou 404 se o id nao existir. */
    @GetMapping("/{id}")
    public Servico buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** 201 com o header Location apontando para o novo registro. */
    @PostMapping
    public ResponseEntity<Servico> criar(@RequestBody @Valid Servico corpo) {
        Servico salvo = service.criar(corpo);
        return ResponseEntity.created(URI.create("/api/servicos/" + salvo.getId())).body(salvo);
    }

    /** 200 com o registro atualizado. */
    @PutMapping("/{id}")
    public Servico atualizar(@PathVariable Long id, @RequestBody @Valid Servico corpo) {
        return service.atualizar(id, corpo);
    }

    /** 204 sem corpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

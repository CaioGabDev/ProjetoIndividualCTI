package com.example.Backend.controller;

import com.example.Backend.model.Insight;
import com.example.Backend.service.InsightService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** CRUD de Insight em /api/insights. */
@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final InsightService service;

    public InsightController(InsightService service) {
        this.service = service;
    }

    /** 200 com a lista completa. */
    @GetMapping
    public List<Insight> listar() {
        return service.listar();
    }

    /** 200 com o registro, ou 404 se o id nao existir. */
    @GetMapping("/{id}")
    public Insight buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** 201 com o header Location apontando para o novo registro. */
    @PostMapping
    public ResponseEntity<Insight> criar(@RequestBody @Valid Insight corpo) {
        Insight salvo = service.criar(corpo);
        return ResponseEntity.created(URI.create("/api/insights/" + salvo.getId())).body(salvo);
    }

    /** 200 com o registro atualizado. */
    @PutMapping("/{id}")
    public Insight atualizar(@PathVariable Long id, @RequestBody @Valid Insight corpo) {
        return service.atualizar(id, corpo);
    }

    /** 204 sem corpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.Backend.controller;

import com.example.Backend.model.Consultor;
import com.example.Backend.service.ConsultorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** CRUD de Consultor em /api/consultores. */
@RestController
@RequestMapping("/api/consultores")
public class ConsultorController {

    private final ConsultorService service;

    public ConsultorController(ConsultorService service) {
        this.service = service;
    }

    /** 200 com a lista completa. */
    @GetMapping
    public List<Consultor> listar() {
        return service.listar();
    }

    /** 200 com o registro, ou 404 se o id nao existir. */
    @GetMapping("/{id}")
    public Consultor buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** 201 com o header Location apontando para o novo registro. */
    @PostMapping
    public ResponseEntity<Consultor> criar(@RequestBody @Valid Consultor corpo) {
        Consultor salvo = service.criar(corpo);
        return ResponseEntity.created(URI.create("/api/consultores/" + salvo.getId())).body(salvo);
    }

    /** 200 com o registro atualizado. */
    @PutMapping("/{id}")
    public Consultor atualizar(@PathVariable Long id, @RequestBody @Valid Consultor corpo) {
        return service.atualizar(id, corpo);
    }

    /** 204 sem corpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

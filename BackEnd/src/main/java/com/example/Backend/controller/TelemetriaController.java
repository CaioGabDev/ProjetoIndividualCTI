package com.example.Backend.controller;

import com.example.Backend.model.Telemetria;
import com.example.Backend.service.TelemetriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** CRUD de Telemetria em /api/telemetrias. */
@RestController
@RequestMapping("/api/telemetrias")
public class TelemetriaController {

    private final TelemetriaService service;

    public TelemetriaController(TelemetriaService service) {
        this.service = service;
    }

    /** 200 com a lista completa. */
    @GetMapping
    public List<Telemetria> listar() {
        return service.listar();
    }

    /** 200 com o registro, ou 404 se o id nao existir. */
    @GetMapping("/{id}")
    public Telemetria buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** 201 com o header Location apontando para o novo registro. */
    @PostMapping
    public ResponseEntity<Telemetria> criar(@RequestBody @Valid Telemetria corpo) {
        Telemetria salvo = service.criar(corpo);
        return ResponseEntity.created(URI.create("/api/telemetrias/" + salvo.getId())).body(salvo);
    }

    /** 200 com o registro atualizado. */
    @PutMapping("/{id}")
    public Telemetria atualizar(@PathVariable Long id, @RequestBody @Valid Telemetria corpo) {
        return service.atualizar(id, corpo);
    }

    /** 204 sem corpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

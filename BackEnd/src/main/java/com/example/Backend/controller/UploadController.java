package com.example.Backend.controller;

import com.example.Backend.dto.UploadRequest;
import com.example.Backend.dto.UploadResponse;
import com.example.Backend.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe em JSON os clientes que o front-end ja tratou e grava no banco.
 * Nao faz upload de arquivo: a planilha e lida e validada no navegador
 * (uploadStore.js) e so os dados limpos chegam aqui.
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }

    /** 201 com o resumo do que foi gravado; 400 se algum cliente vier invalido. */
    @PostMapping
    public ResponseEntity<UploadResponse> enviar(@RequestBody @Valid UploadRequest corpo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.gravar(corpo));
    }
}

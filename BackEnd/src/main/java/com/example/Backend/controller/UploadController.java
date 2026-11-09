package com.example.Backend.controller;

import com.example.Backend.dto.LoteDTO;
import com.example.Backend.dto.UploadRequest;
import com.example.Backend.model.UploadLote;
import com.example.Backend.model.Usuario;
import com.example.Backend.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Envio da carteira para a base, em /api/upload.
 *
 *   POST /api/upload   recebe as linhas ja tratadas no front, cria o lote e
 *                      dispara o ETL em Python
 *   GET  /api/upload   historico dos ultimos envios
 *
 * A planilha e lida e validada no navegador (uploadStore.js); aqui chega JSON.
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }

    /**
     * 201 quando o Python terminou e o lote esta PROCESSADO.
     * 202 quando o Python ainda esta processando (demorou mais que o timeout).
     * 400/422/502/503 com {"mensagem"} quando algo deu errado (ver UploadService).
     */
    @PostMapping
    public ResponseEntity<LoteDTO> enviar(@RequestBody @Valid UploadRequest corpo,
                                          @AuthenticationPrincipal Usuario usuario) {
        LoteDTO lote = service.enviar(corpo, usuario == null ? null : usuario.getEmail());
        HttpStatus status = UploadLote.PROCESSADO.equals(lote.status()) ? HttpStatus.CREATED : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(lote);
    }

    @GetMapping
    public List<LoteDTO> historico() {
        return service.historico();
    }
}

package com.example.Backend.controller;

import com.example.Backend.dto.DashboardDTO;
import com.example.Backend.service.PainelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /api/dashboard - estatisticas do ultimo lote processado (exige token).
 * Com a base vazia responde 200 com {"lote": null, "resultado": null}.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PainelService service;

    public DashboardController(PainelService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardDTO painel() {
        return service.painel();
    }
}

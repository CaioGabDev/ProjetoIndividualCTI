package com.example.Backend.controller;

import com.example.Backend.dto.ClienteDTO;
import com.example.Backend.service.ClienteValidacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Segunda validacao da planilha, agora no servidor.
 *
 *   POST /api/clientes/validar   (exige token, como toda rota fora de /api/auth)
 *
 * Recebe a lista que o Pinia aprovou, valida de novo e devolve a lista
 * padronizada. Sem banco nesta etapa — nao ha Repository nem Entity aqui.
 *
 * Fica num controller separado do ClienteController para deixar claro que
 * este endpoint nao grava nada.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteValidacaoController {

    private final ClienteValidacaoService service;

    public ClienteValidacaoController(ClienteValidacaoService service) {
        this.service = service;
    }

    /**
     * 200 com a lista padronizada; 400 se algum item reprovar.
     *
     * O @Valid fica DENTRO do List<> para o Spring validar cada cliente. Nesse
     * formato o erro chega como HandlerMethodValidationException (e nao
     * MethodArgumentNotValidException) — o GlobalExceptionHandler trata os dois.
     */
    @PostMapping("/validar")
    public ResponseEntity<List<ClienteDTO>> validar(
            @RequestBody List<@Valid @NotNull ClienteDTO> clientes) {
        return ResponseEntity.ok(service.validarEPreparar(clientes));
    }
}

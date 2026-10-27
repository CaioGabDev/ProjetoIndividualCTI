package com.example.Backend.controller;

import com.example.Backend.dto.AtualizarUsuarioDTO;
import com.example.Backend.dto.CadastroDTO;
import com.example.Backend.dto.UsuarioRespostaDTO;
import com.example.Backend.model.Usuario;
import com.example.Backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * CRUD de Usuario em /api/usuarios. Todas as rotas exigem token
 * (ver SecurityConfig); o DELETE exige perfil ADMIN.
 *
 * Entra e sai DTO, nunca a entidade: na entrada isso impede que alguem mande
 * "id" ou "perfil" no JSON; na saida, garante que o hash da senha nao vaze.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    /** 200 com a lista completa. */
    @GetMapping
    public List<UsuarioRespostaDTO> listar() {
        return service.listar().stream().map(UsuarioRespostaDTO::de).toList();
    }

    /** 200 com o registro, ou 404 se o id nao existir. */
    @GetMapping("/{id}")
    public UsuarioRespostaDTO buscar(@PathVariable Long id) {
        return UsuarioRespostaDTO.de(service.buscar(id));
    }

    /** 201 com o header Location apontando para o novo registro. */
    @PostMapping
    public ResponseEntity<UsuarioRespostaDTO> criar(@RequestBody @Valid CadastroDTO corpo) {
        Usuario salvo = service.cadastrar(corpo);
        return ResponseEntity.created(URI.create("/api/usuarios/" + salvo.getId()))
                .body(UsuarioRespostaDTO.de(salvo));
    }

    /** 200 com o registro atualizado. Senha em branco mantem a atual. */
    @PutMapping("/{id}")
    public UsuarioRespostaDTO atualizar(@PathVariable Long id, @RequestBody @Valid AtualizarUsuarioDTO corpo) {
        return UsuarioRespostaDTO.de(service.atualizar(id, corpo));
    }

    /** 204 sem corpo. Exige perfil ADMIN. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

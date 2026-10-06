package com.example.Backend.controller;

import com.example.Backend.dto.CadastroDTO;
import com.example.Backend.dto.LoginDTO;
import com.example.Backend.dto.TokenDTO;
import com.example.Backend.dto.UsuarioRespostaDTO;
import com.example.Backend.model.Usuario;
import com.example.Backend.service.TokenService;
import com.example.Backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Porta de entrada da autenticacao, em /api/auth.
 *
 *   POST /api/auth/register  cria a conta (publica)
 *   POST /api/auth/login     troca e-mail+senha por um token (publica)
 *   GET  /api/auth/me        diz quem esta logado (exige token)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                          TokenService tokenService,
                          UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
    }

    /** 201 com os dados da conta criada (sem a senha). 409 se o e-mail ja existe. */
    @PostMapping("/register")
    public ResponseEntity<UsuarioRespostaDTO> register(@RequestBody @Valid CadastroDTO dto) {
        Usuario usuario = usuarioService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioRespostaDTO.de(usuario));
    }

    /**
     * 200 com o token. 401 se o e-mail nao existe OU a senha esta errada.
     *
     * Nao comparamos senha aqui: entregamos as credenciais ao
     * authenticationManager, que busca o usuario pelo AutenticacaoService e
     * deixa o BCrypt decidir. Se ele recusar, lanca AuthenticationException e
     * o GlobalExceptionHandler transforma no 401 generico.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO dto) {
        var credenciais = new UsernamePasswordAuthenticationToken(
                dto.email().trim().toLowerCase(), dto.senha());

        Authentication auth = authenticationManager.authenticate(credenciais);
        Usuario usuario = (Usuario) auth.getPrincipal();

        return ResponseEntity.ok(TokenDTO.de(
                tokenService.gerarToken(usuario),
                tokenService.getMinutosDeValidade(),
                UsuarioRespostaDTO.de(usuario)));
    }

    /**
     * 200 com os dados de quem esta logado; 401 sem token valido.
     *
     * O @AuthenticationPrincipal pega o Usuario que o SecurityFilter colocou
     * no contexto — nao ha busca extra no banco nem id vindo da URL, entao
     * ninguem consegue pedir o perfil de outra pessoa por aqui.
     */
    @GetMapping("/me")
    public UsuarioRespostaDTO me(@AuthenticationPrincipal Usuario usuario) {
        return UsuarioRespostaDTO.de(usuario);
    }
}

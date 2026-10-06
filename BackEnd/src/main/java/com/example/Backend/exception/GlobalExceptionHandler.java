package com.example.Backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Um lugar so para transformar excecao em resposta HTTP.
 *
 * Sem isso, um e-mail repetido viraria 500 e o front nao teria como
 * diferenciar "dados invalidos" de "ja existe" de "senha errada". O corpo
 * sempre tem a chave "mensagem"; nos erros de validacao vem tambem "campos".
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400: o @Valid reprovou algum campo. Devolve qual campo e por que. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", "Dados invalidos");
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    /**
     * 409: e-mail repetido. Pegamos as duas excecoes porque a checagem no
     * service pode perder uma corrida entre dois cadastros simultaneos — nesse
     * caso quem barra e a constraint UNIQUE do banco, que chega aqui como
     * DataIntegrityViolationException.
     */
    @ExceptionHandler({EmailJaCadastradoException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, String>> conflito(RuntimeException ex) {
        String mensagem = (ex instanceof EmailJaCadastradoException)
                ? ex.getMessage()
                : "Registro em conflito com outro ja existente.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem", mensagem));
    }

    /**
     * 401: e-mail inexistente OU senha errada.
     * A mensagem e a MESMA nos dois casos de proposito: dizer "esse e-mail nao
     * existe" entregaria a quem ataca a lista de quem tem conta no sistema.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> credenciais() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("mensagem", "E-mail ou senha invalidos"));
    }

    /** 403: esta logado, mas o perfil nao permite essa acao. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> semPermissao() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensagem", "Voce nao tem permissao para esta acao."));
    }

    /** Mantem o status escolhido no service e padroniza o corpo em "mensagem". */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> statusEscolhido(ResponseStatusException ex) {
        String mensagem = ex.getReason() != null ? ex.getReason() : "Nao foi possivel concluir a operacao.";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("mensagem", mensagem));
    }
}

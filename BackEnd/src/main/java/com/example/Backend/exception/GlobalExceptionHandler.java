package com.example.Backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
     * 400: algum item de uma LISTA reprovou (POST /api/clientes/validar).
     *
     * Com List<@Valid ClienteDTO> o Spring valida pelo "method validation" e
     * lanca esta excecao em vez da MethodArgumentNotValidException acima.
     * Devolvemos todos os erros, cada um com o indice do item na lista (0 = o
     * primeiro), para o front apontar a linha certa da planilha. "campo" e
     * "erro" repetem o primeiro problema, para quem so quer uma frase.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> validacaoDeLista(HandlerMethodValidationException ex) {
        List<Map<String, Object>> erros = new ArrayList<>();

        for (ParameterErrors item : ex.getBeanResults()) {
            for (FieldError f : item.getFieldErrors()) {
                Map<String, Object> erro = new LinkedHashMap<>();
                erro.put("indice", item.getContainerIndex());
                erro.put("campo", f.getField());
                erro.put("erro", f.getDefaultMessage());
                erros.add(erro);
            }
        }

        // Item null na lista ([null]) nao tem campo: cai aqui, em valueResults.
        ex.getValueResults().forEach(item -> item.getResolvableErrors().forEach(e -> {
            Map<String, Object> erro = new LinkedHashMap<>();
            erro.put("indice", item.getContainerIndex());
            erro.put("campo", "");
            erro.put("erro", "Item vazio na lista");
            erros.add(erro);
        }));

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", "Dados invalidos");
        if (!erros.isEmpty()) {
            corpo.put("campo", erros.get(0).get("campo"));
            corpo.put("erro", erros.get(0).get("erro"));
        }
        corpo.put("erros", erros);
        return ResponseEntity.badRequest().body(corpo);
    }

    /**
     * 400: o JSON nem chegou a virar objeto — texto em campo numerico
     * ("faturamentoAnual": "abc"), data fora do formato ou impossivel
     * ("2025-02-30"), virgula sobrando. Sem isto o Spring devolveria um 400
     * sem a chave "mensagem" que o front espera.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonIlegivel() {
        return ResponseEntity.badRequest().body(Map.of("mensagem",
                "JSON invalido: confira numeros e datas (formato aaaa-mm-dd)."));
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

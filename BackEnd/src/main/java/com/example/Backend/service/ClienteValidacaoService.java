package com.example.Backend.service;

import com.example.Backend.dto.ClienteDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validacoes que o DTO nao consegue fazer sozinho (POST /api/clientes/validar).
 *
 * O DTO enxerga uma linha por vez; este service enxerga o lote inteiro, entao
 * e aqui que se descobre codigo repetido. Tambem padroniza o texto para a
 * proxima etapa (Ciencia de Dados).
 *
 * Nada e gravado no banco nesta etapa: entra a lista, sai a lista preparada.
 */
@Service
public class ClienteValidacaoService {

    private static final int LIMITE_DO_LOTE = 5000;

    /**
     * Variacoes que aparecem na planilha -> nome unico. A chave ja esta sem
     * acento, sem ponto e minuscula (ver chave()), entao "IND.", "Industria"
     * e "INDÚSTRIA" caem todos em "ind" / "industria".
     */
    private static final Map<String, String> SEGMENTOS = Map.ofEntries(
            Map.entry("ind", "Indústria"),
            Map.entry("industria", "Indústria"),
            Map.entry("com", "Comércio"),
            Map.entry("comercio", "Comércio"),
            Map.entry("serv", "Serviços"),
            Map.entry("servicos", "Serviços"),
            Map.entry("varejo", "Varejo"),
            Map.entry("saude", "Saúde"),
            Map.entry("agro", "Agro"),
            Map.entry("agronegocio", "Agro"),
            Map.entry("educacao", "Educação"),
            Map.entry("tecnologia", "Tecnologia"),
            Map.entry("ti", "Tecnologia"));

    public List<ClienteDTO> validarEPreparar(List<ClienteDTO> clientes) {
        if (clientes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie ao menos um cliente.");
        }
        if (clientes.size() > LIMITE_DO_LOTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Envie no maximo " + LIMITE_DO_LOTE + " clientes por requisicao.");
        }

        // Junta TODOS os repetidos antes de recusar: devolver so o primeiro
        // obrigaria a pessoa a corrigir e reenviar uma vez por codigo.
        Set<String> vistos = new HashSet<>();
        Set<String> repetidos = new LinkedHashSet<>();
        for (ClienteDTO cliente : clientes) {
            String codigo = cliente.codigoCliente().trim().toUpperCase(Locale.ROOT);
            if (!vistos.add(codigo)) {
                repetidos.add(codigo);
            }
        }

        if (!repetidos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Codigo repetido na planilha: " + String.join(", ", repetidos));
        }

        return clientes.stream()
                .map(this::padronizar)
                .toList();
    }

    /** Devolve um DTO novo (record e imutavel) com o texto ja limpo. */
    private ClienteDTO padronizar(ClienteDTO c) {
        return new ClienteDTO(
                c.codigoCliente().trim().toUpperCase(Locale.ROOT),
                limpar(c.nomeCliente()),
                formatarNome(c.consultor()),
                padronizarSegmento(c.segmento()),
                c.nivelCliente().trim().toUpperCase(Locale.ROOT),
                c.faturamentoAnual(),
                padronizarServicos(c.servicosContratados()),
                c.dataContratacao(),
                c.cidade() == null ? "" : limpar(c.cidade()),
                c.uf() == null ? "" : c.uf().trim().toUpperCase(Locale.ROOT));
    }

    /** Segmento desconhecido nao e erro: so sai sem espacos sobrando. */
    private String padronizarSegmento(String segmento) {
        return SEGMENTOS.getOrDefault(chave(segmento), limpar(segmento));
    }

    /** "ANA SOUZA" e "ana  souza" viram "Ana Souza". */
    private String formatarNome(String nome) {
        return Arrays.stream(limpar(nome).toLowerCase(Locale.ROOT).split(" "))
                .map(p -> p.substring(0, 1).toUpperCase(Locale.ROOT) + p.substring(1))
                .collect(Collectors.joining(" "));
    }

    /** "Internet Dedicada ; Firewall;" vira "Internet Dedicada;Firewall". */
    private String padronizarServicos(String servicos) {
        return Arrays.stream(servicos.split(";"))
                .map(this::limpar)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(";"));
    }

    /** Tira espaco da borda e espaco duplo entre palavras. */
    private String limpar(String texto) {
        return texto.trim().replaceAll("\\s{2,}", " ");
    }

    /** "IND." -> "ind", "Indústria" -> "industria": sem acento, sem pontuacao. */
    private String chave(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }
}

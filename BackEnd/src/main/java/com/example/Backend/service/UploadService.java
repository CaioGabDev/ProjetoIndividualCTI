package com.example.Backend.service;

import com.example.Backend.dto.ClienteDTO;
import com.example.Backend.dto.LoteDTO;
import com.example.Backend.dto.UploadRequest;
import com.example.Backend.model.UploadLote;
import com.example.Backend.repository.UploadLoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fluxo do POST /api/upload:
 *
 *   1. valida e padroniza de novo as linhas (ClienteValidacaoService);
 *   2. cria o lote como AGUARDANDO_ETL e ja grava (commit) - o Python le e
 *      atualiza essa linha por outra conexao, entao ela precisa existir;
 *   3. chama o Python (EtlClient), que limpa, calcula e grava os clientes;
 *   4. le o lote de novo para devolver o status que o Python deixou.
 *
 * De proposito este metodo NAO e @Transactional: se fosse, o lote do passo 2
 * so seria gravado no fim, e o Python nao o encontraria.
 */
@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private final ClienteValidacaoService validacao;
    private final UploadLoteRepository loteRepository;
    private final EtlClient etl;
    private final TelemetriaService telemetria;

    public UploadService(ClienteValidacaoService validacao,
                         UploadLoteRepository loteRepository,
                         EtlClient etl,
                         TelemetriaService telemetria) {
        this.validacao = validacao;
        this.loteRepository = loteRepository;
        this.etl = etl;
        this.telemetria = telemetria;
    }

    public LoteDTO enviar(UploadRequest pedido, String usuario) {
        List<ClienteDTO> clientes = validacao.validarEPreparar(pedido.clientes());
        int recebidas = Math.max(pedido.recebidas(), clientes.size());

        UploadLote lote = new UploadLote();
        lote.setArquivo(pedido.arquivo().trim());
        lote.setLinhasRecebidas(recebidas);
        lote.setLinhasValidas(clientes.size());
        lote.setEnviadoPor(usuario);
        lote.setStatus(UploadLote.AGUARDANDO_ETL);
        lote.setMensagem("Aguardando o ETL em Python.");
        Long loteId = loteRepository.save(lote).getId();

        try {
            etl.processar(loteId, recebidas, paraLinhasDoEtl(clientes));
        } catch (RestClientResponseException e) {
            log.warn("ETL respondeu {} para o lote {}: {}", e.getStatusCode(), loteId, e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 401) {
                return falhar(loteId, HttpStatus.BAD_GATEWAY,
                        "O Python recusou a chave do ETL: o ETL_KEY do Java precisa ser igual ao ETL_API_KEY do Python.");
            }
            // Quando o Python trata o erro, o motivo ja fica gravado no lote; senao,
            // usamos o "detail" que ele devolveu para a tela dizer o que houve.
            return falhar(loteId, HttpStatus.UNPROCESSABLE_CONTENT,
                    "O Python nao conseguiu processar a planilha (HTTP " + e.getStatusCode().value()
                            + detalheDoPython(e.getResponseBodyAsString()) + ").");
        } catch (ResourceAccessException e) {
            log.warn("Sem resposta do ETL em {} para o lote {}: {}", etl.getUrl(), loteId, e.getMessage());
            return falhar(loteId, HttpStatus.SERVICE_UNAVAILABLE,
                    "Nao foi possivel falar com o servico de analise (Python). Confira o ETL_URL e se o app Python esta ligado.");
        }

        UploadLote atualizado = buscar(loteId);
        telemetria.registrar("UPLOAD", origem(usuario), "Lote " + loteId + ": " + atualizado.getMensagem());
        return LoteDTO.de(atualizado);
    }

    @Transactional(readOnly = true)
    public List<LoteDTO> historico() {
        return loteRepository.findTop20ByOrderByIdDesc().stream().map(LoteDTO::de).toList();
    }

    /**
     * Fecha o lote como ERRO (se o Python ja nao tiver fechado) e devolve o erro
     * para o front. Excecao: se o Python ainda estiver processando (o timeout
     * estourou antes dele terminar), o lote volta como esta e a tela mostra
     * "processando".
     */
    private LoteDTO falhar(Long loteId, HttpStatus status, String motivo) {
        UploadLote lote = buscar(loteId);

        if (UploadLote.PROCESSADO.equals(lote.getStatus()) || UploadLote.PROCESSANDO.equals(lote.getStatus())) {
            return LoteDTO.de(lote);
        }

        if (!UploadLote.ERRO.equals(lote.getStatus())) {
            lote.setStatus(UploadLote.ERRO);
            lote.setMensagem(motivo);
            loteRepository.save(lote);
        }

        telemetria.registrar("UPLOAD_ERRO", origem(lote.getEnviadoPor()), "Lote " + loteId + ": " + lote.getMensagem());
        throw new ResponseStatusException(status, lote.getMensagem());
    }

    private UploadLote buscar(Long loteId) {
        return loteRepository.findById(loteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lote " + loteId + " nao encontrado."));
    }

    /**
     * ClienteDTO (camelCase do Java) -> linha no formato da planilha, que e o
     * que o limpeza.py le: codigo_cliente, nome_cliente, nivel_cliente...
     */
    private List<Map<String, Object>> paraLinhasDoEtl(List<ClienteDTO> clientes) {
        List<Map<String, Object>> linhas = new ArrayList<>(clientes.size());

        for (ClienteDTO c : clientes) {
            Map<String, Object> linha = new LinkedHashMap<>();   // aceita null (Map.of nao)
            linha.put("codigo_cliente", c.codigoCliente());
            linha.put("nome_cliente", c.nomeCliente());
            linha.put("consultor", c.consultor());
            linha.put("segmento", c.segmento());
            linha.put("nivel_cliente", c.nivelCliente());
            linha.put("faturamento_anual", c.faturamentoAnual());
            linha.put("servicos_contratados", c.servicosContratados());
            linha.put("data_contratacao", c.dataContratacao() == null ? null : c.dataContratacao().toString());
            linha.put("cidade", c.cidade());
            linha.put("uf", c.uf());
            linhas.add(linha);
        }

        return linhas;
    }

    /** Pega o campo "detail" da resposta de erro do FastAPI: {"detail": "..."}. */
    private String detalheDoPython(String corpo) {
        if (corpo == null || corpo.isBlank()) return "";
        Matcher m = Pattern.compile("\"detail\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(corpo);
        String detalhe = m.find() ? m.group(1) : corpo.strip();
        if (detalhe.length() > 200) detalhe = detalhe.substring(0, 200);
        return ": " + detalhe;
    }

    /** A coluna origem da telemetria aceita 100 caracteres. */
    private String origem(String usuario) {
        if (usuario == null || usuario.isBlank()) return "front-end";
        return usuario.length() <= 100 ? usuario : usuario.substring(0, 100);
    }
}

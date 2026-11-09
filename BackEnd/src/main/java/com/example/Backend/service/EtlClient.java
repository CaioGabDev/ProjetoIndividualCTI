package com.example.Backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Quem conversa com o analytics-python (FastAPI).
 *
 *   POST {ETL_URL}   header X-ETL-Key: {ETL_KEY}
 *   corpo: { "loteId": 1, "recebidas": 33, "linhas": [ {...}, ... ] }
 *
 * A chave precisa ser igual ao ETL_API_KEY do Python, senao ele responde 401.
 * O timeout de leitura e longo de proposito: no plano gratis da Azure o Python
 * pode estar "dormindo" e leva alguns segundos para acordar na primeira chamada.
 */
@Component
public class EtlClient {

    private final RestClient http;
    private final String url;
    private final String chave;

    public EtlClient(@Value("${etl.url}") String url,
                     @Value("${etl.key:}") String chave,
                     @Value("${etl.timeout-seconds:180}") long timeoutSegundos) {
        HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));

        this.http = RestClient.builder().requestFactory(fabrica).build();
        this.url = url;
        this.chave = chave == null ? "" : chave.trim();
    }

    public String getUrl() {
        return url;
    }

    /**
     * Manda o lote e espera o Python terminar. Lanca RestClientResponseException
     * se o Python responder erro (401, 422, 500) e ResourceAccessException se
     * nem der para conectar (URL errada, app parado, timeout).
     */
    public void processar(Long loteId, int recebidas, List<Map<String, Object>> linhas) {
        http.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    if (!chave.isEmpty()) {
                        h.set("X-ETL-Key", chave);
                    }
                })
                .body(Map.of("loteId", loteId, "recebidas", recebidas, "linhas", linhas))
                .retrieve()
                .toBodilessEntity();
    }
}

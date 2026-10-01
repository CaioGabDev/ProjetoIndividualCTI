package com.example.Backend.service;

import com.example.Backend.dto.ClienteUploadDTO;
import com.example.Backend.dto.UploadRequest;
import com.example.Backend.dto.UploadResponse;
import com.example.Backend.model.Cliente;
import com.example.Backend.model.Consultor;
import com.example.Backend.model.Telemetria;
import com.example.Backend.repository.ClienteRepository;
import com.example.Backend.repository.ConsultorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Grava no banco a carteira que o front-end ja tratou e validou (POST /api/upload).
 *
 * O trabalho de limpar e padronizar a planilha fica no uploadStore do Vue; aqui
 * so decidimos entre inserir e atualizar, resolvemos o consultor e registramos
 * o evento na Telemetria. Tudo numa unica transacao: se uma linha estourar, o
 * lote inteiro volta atras e o banco nao fica meio gravado.
 */
@Service
public class UploadService {

    private final ClienteRepository clienteRepository;
    private final ConsultorRepository consultorRepository;
    private final TelemetriaService telemetriaService;

    public UploadService(ClienteRepository clienteRepository,
                         ConsultorRepository consultorRepository,
                         TelemetriaService telemetriaService) {
        this.clienteRepository = clienteRepository;
        this.consultorRepository = consultorRepository;
        this.telemetriaService = telemetriaService;
    }

    @Transactional
    public UploadResponse gravar(UploadRequest requisicao) {
        int inseridos = 0;
        int atualizados = 0;

        // Cache por nome: a planilha repete o mesmo consultor em dezenas de
        // linhas e sem isso seria uma consulta ao banco por linha.
        Map<String, Consultor> consultoresEmCache = new HashMap<>();
        int consultoresCriados = 0;

        for (ClienteUploadDTO linha : requisicao.clientes()) {
            String nome = limpar(linha.nome());
            String cnpj = somenteDigitos(linha.cnpj());

            Optional<Cliente> existente = localizar(cnpj, nome);
            Cliente cliente = existente.orElseGet(Cliente::new);

            // Campo que nao veio na planilha nao apaga o que ja esta gravado:
            // uma planilha sem a coluna "email" nao deve zerar os e-mails do banco.
            cliente.setNome(nome);
            cliente.setCnpj(ouAtual(cnpj, cliente.getCnpj()));
            cliente.setEmail(ouAtual(minusculo(linha.email()), cliente.getEmail()));
            cliente.setSegmento(ouAtual(limpar(linha.segmento()), cliente.getSegmento()));
            cliente.setCidade(ouAtual(limpar(linha.cidade()), cliente.getCidade()));
            cliente.setNivel(ouAtual(maiusculo(linha.nivel()), cliente.getNivel()));

            if (linha.faturamento() != null) {
                cliente.setFaturamento(linha.faturamento());
            }

            String nomeConsultor = limpar(linha.consultor());
            if (nomeConsultor != null) {
                String chave = nomeConsultor.toLowerCase();
                Consultor consultor = consultoresEmCache.get(chave);

                if (consultor == null) {
                    Optional<Consultor> achado =
                            consultorRepository.findFirstByNomeIgnoreCase(nomeConsultor);

                    if (achado.isPresent()) {
                        consultor = achado.get();
                    } else {
                        // Consultor que aparece na planilha mas nao esta cadastrado.
                        Consultor novo = new Consultor();
                        novo.setNome(nomeConsultor);
                        consultor = consultorRepository.save(novo);
                        consultoresCriados++;
                    }

                    consultoresEmCache.put(chave, consultor);
                }

                cliente.setConsultor(consultor);
            }

            clienteRepository.save(cliente);

            if (existente.isPresent()) {
                atualizados++;
            } else {
                inseridos++;
            }
        }

        int recebidos = requisicao.clientes().size();
        String origem = limpar(requisicao.origem());
        String mensagem = inseridos + " cliente(s) inserido(s) e " + atualizados + " atualizado(s).";

        Telemetria evento = telemetriaService.registrar("UPLOAD",
                origem == null ? "front-end" : origem, mensagem);

        return new UploadResponse(recebidos, inseridos, atualizados,
                consultoresCriados, evento.getId(), mensagem);
    }

    /** CNPJ e a chave boa; sem ele, cai para o nome (e o que a planilha garante). */
    private Optional<Cliente> localizar(String cnpj, String nome) {
        if (cnpj != null) {
            Optional<Cliente> porCnpj = clienteRepository.findFirstByCnpj(cnpj);
            if (porCnpj.isPresent()) {
                return porCnpj;
            }
        }

        return clienteRepository.findFirstByNomeIgnoreCase(nome);
    }

    /** Fica com o valor novo quando ele veio; senao mantem o que esta no banco. */
    private String ouAtual(String novo, String atual) {
        return novo != null ? novo : atual;
    }

    private String limpar(String texto) {
        if (texto == null) return null;
        String limpo = texto.trim().replaceAll("\\s{2,}", " ");
        return limpo.isEmpty() ? null : limpo;
    }

    private String minusculo(String texto) {
        String limpo = limpar(texto);
        return limpo == null ? null : limpo.toLowerCase();
    }

    private String maiusculo(String texto) {
        String limpo = limpar(texto);
        return limpo == null ? null : limpo.toUpperCase();
    }

    /** Guarda o CNPJ so com digitos: "12.345.678/0001-99" e o mesmo cliente que "12345678000199". */
    private String somenteDigitos(String texto) {
        String limpo = limpar(texto);
        if (limpo == null) return null;
        String digitos = limpo.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }
}

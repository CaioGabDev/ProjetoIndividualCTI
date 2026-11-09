package com.example.Backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Cada planilha enviada vira um lote.
 *
 * O Java cria o lote como AGUARDANDO_ETL e chama o Python. O Python troca o
 * status para PROCESSANDO, grava os clientes e termina em PROCESSADO (ou ERRO,
 * com o motivo na mensagem). Os nomes da tabela e das colunas sao os mesmos que
 * o analytics-python/banco.py usa no SQL - mudar aqui exige mudar la.
 */
@Entity
@Table(name = "upload_lote")
public class UploadLote {

    public static final String AGUARDANDO_ETL = "AGUARDANDO_ETL";
    public static final String PROCESSANDO = "PROCESSANDO";
    public static final String PROCESSADO = "PROCESSADO";
    public static final String ERRO = "ERRO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String arquivo;

    @Column(nullable = false, length = 30)
    private String status = AGUARDANDO_ETL;

    // O Python corta a mensagem em 480 caracteres antes de gravar.
    @Column(length = 480)
    private String mensagem;

    @Column(name = "linhas_recebidas", nullable = false)
    private Integer linhasRecebidas;

    @Column(name = "linhas_validas", nullable = false)
    private Integer linhasValidas;

    @Column(name = "enviado_por", length = 255)
    private String enviadoPor;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm = LocalDateTime.now();

    public UploadLote() {
    }

    public Long getId() {
        return id;
    }

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem == null || mensagem.length() <= 480 ? mensagem : mensagem.substring(0, 480);
    }

    public Integer getLinhasRecebidas() {
        return linhasRecebidas;
    }

    public void setLinhasRecebidas(Integer linhasRecebidas) {
        this.linhasRecebidas = linhasRecebidas;
    }

    public Integer getLinhasValidas() {
        return linhasValidas;
    }

    public void setLinhasValidas(Integer linhasValidas) {
        this.linhasValidas = linhasValidas;
    }

    public String getEnviadoPor() {
        return enviadoPor;
    }

    public void setEnviadoPor(String enviadoPor) {
        this.enviadoPor = enviadoPor;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }
}

package com.mvc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Movimentacao {
    private int idCarteira;
    private int idMovimento;
    private LocalDate dataOperacao;
    private TipoOperacao tipoOperacao;
    private BigDecimal quantidadeMovimentada;

    public Movimentacao(int idCarteira, int idMovimento, LocalDate dataOperacao, TipoOperacao tipoOperacao, BigDecimal quantidadeMovimentada) {
        this.idCarteira = idCarteira;
        this.idMovimento = idMovimento;
        this.dataOperacao = dataOperacao;
        this.tipoOperacao = tipoOperacao;
        this.quantidadeMovimentada = quantidadeMovimentada;
    }

    // Getters e Setters
    public int getIdCarteira() { return idCarteira; }
    public int getIdMovimento() { return idMovimento; }
    public LocalDate getDataOperacao() { return dataOperacao; }
    public TipoOperacao getTipoOperacao() { return tipoOperacao; }
    public BigDecimal getQuantidadeMovimentada() { return quantidadeMovimentada; }
}
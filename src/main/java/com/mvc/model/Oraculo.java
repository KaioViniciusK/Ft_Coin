package com.mvc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Oraculo {
    private LocalDate data;
    private BigDecimal cotacao;

    public Oraculo(LocalDate data, BigDecimal cotacao) {
        this.data = data;
        this.cotacao = cotacao;
    }

    public LocalDate getData() { return data; }
    public BigDecimal getCotacao() { return cotacao; }
}
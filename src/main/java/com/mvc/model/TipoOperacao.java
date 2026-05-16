package com.mvc.model;

public enum TipoOperacao {
    COMPRA('C'),
    VENDA('V');

    private final char codigo;

    TipoOperacao(char codigo) {
        this.codigo = codigo;
    }

    public char getCodigo() {
        return codigo;
    }
}
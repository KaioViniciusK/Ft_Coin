package ftcoin.model;

/** Tipo de uma movimentação; o código de um caractere é o valor gravado no banco ('C' ou 'V'). */
public enum TipoOperacao {
    COMPRA('C'),
    VENDA('V');

    private final char codigo;

    TipoOperacao(char codigo) {
        this.codigo = codigo;
    }

    /** @return o caractere gravado no banco para este tipo */
    public char getCodigo() {
        return codigo;
    }

    /**
     * Converte o caractere do banco no tipo correspondente.
     *
     * @param codigo 'C' ou 'V'
     * @return o tipo de operação
     * @throws IllegalArgumentException se o código não for conhecido
     */
    public static TipoOperacao fromCodigo(char codigo) {
        for (TipoOperacao tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de operação desconhecido: " + codigo);
    }
}

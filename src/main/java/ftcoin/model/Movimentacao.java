package ftcoin.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

/** Compra ou venda de FTCoins em uma carteira. Imutável; a quantidade usa BigDecimal. */
public final class Movimentacao {
    /** Ordem usada em todos os cálculos: por data e, no mesmo dia, pela ordem de registro. */
    public static final Comparator<Movimentacao> ORDEM_CRONOLOGICA =
            Comparator.comparing(Movimentacao::getData).thenComparingInt(Movimentacao::getId);

    private final int id;
    private final int idCarteira;
    private final LocalDate data;
    private final TipoOperacao tipo;
    private final BigDecimal quantidade;

    /**
     * @param id         identificador do movimento (positivo)
     * @param idCarteira identificador da carteira (positivo)
     * @param data       data da operação
     * @param tipo       compra ou venda
     * @param quantidade quantidade de FTCoins, maior que zero
     * @throws IllegalArgumentException se algum valor for inválido
     */
    public Movimentacao(int id, int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal quantidade) {
        if (id <= 0 || idCarteira <= 0) {
            throw new IllegalArgumentException("Identificadores devem ser positivos");
        }
        if (data == null || tipo == null || quantidade == null) {
            throw new IllegalArgumentException("Data, tipo e quantidade são obrigatórios");
        }
        if (quantidade.signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
        this.id = id;
        this.idCarteira = idCarteira;
        this.data = data;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public int getId() {
        return id;
    }

    public int getIdCarteira() {
        return idCarteira;
    }

    public LocalDate getData() {
        return data;
    }

    public TipoOperacao getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }
}

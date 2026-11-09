package ftcoin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Resultado calculado de uma carteira (nunca persistido): quantidades e valores em reais,
 * por custo médio ponderado e a mercado.
 */
public final class ResultadoFinanceiroDTO {
    private final int idCarteira;
    private final BigDecimal quantidadeSaldo;
    private final BigDecimal totalComprado;
    private final BigDecimal totalVendido;
    private final BigDecimal custoMedio;
    private final BigDecimal saldoCustoMedio;
    private final BigDecimal resultadoCustoMedio;
    private final BigDecimal cotacaoMercado;
    private final BigDecimal saldoMercado;
    private final BigDecimal resultadoMercado;

    ResultadoFinanceiroDTO(int idCarteira, BigDecimal quantidadeSaldo, BigDecimal totalComprado,
            BigDecimal totalVendido, BigDecimal custoMedio, BigDecimal saldoCustoMedio,
            BigDecimal resultadoCustoMedio, BigDecimal cotacaoMercado, BigDecimal saldoMercado,
            BigDecimal resultadoMercado) {
        this.idCarteira = idCarteira;
        this.quantidadeSaldo = quantidadeSaldo;
        this.totalComprado = totalComprado;
        this.totalVendido = totalVendido;
        this.custoMedio = custoMedio;
        this.saldoCustoMedio = saldoCustoMedio;
        this.resultadoCustoMedio = resultadoCustoMedio;
        this.cotacaoMercado = cotacaoMercado;
        this.saldoMercado = saldoMercado;
        this.resultadoMercado = resultadoMercado;
    }

    static ResultadoFinanceiroDTO vazio(int idCarteira) {
        BigDecimal z = BigDecimal.ZERO;
        return new ResultadoFinanceiroDTO(idCarteira, z, z, z, z, z, z, null, z, z);
    }

    public int getIdCarteira() {
        return idCarteira;
    }

    /** @return FTCoins em carteira */
    public BigDecimal getQuantidadeSaldo() {
        return quantidadeSaldo;
    }

    /** @return soma, em reais, de tudo o que foi comprado */
    public BigDecimal getTotalComprado() {
        return totalComprado;
    }

    /** @return soma, em reais, de tudo o que foi vendido */
    public BigDecimal getTotalVendido() {
        return totalVendido;
    }

    /** @return custo médio ponderado por FTCoin do saldo atual */
    public BigDecimal getCustoMedio() {
        return custoMedio;
    }

    /** @return valor do saldo atual ao custo médio */
    public BigDecimal getSaldoCustoMedio() {
        return saldoCustoMedio;
    }

    /** @return ganho ou perda das vendas já realizadas, por custo médio */
    public BigDecimal getResultadoCustoMedio() {
        return resultadoCustoMedio;
    }

    /** @return cotação usada para o mercado, ou null se não há saldo para valorar */
    public BigDecimal getCotacaoMercado() {
        return cotacaoMercado;
    }

    /** @return valor do saldo atual à cotação de mercado */
    public BigDecimal getSaldoMercado() {
        return saldoMercado;
    }

    /** @return ganho ou perda se o saldo fosse vendido à cotação de mercado */
    public BigDecimal getResultadoMercado() {
        return resultadoMercado;
    }

    /** @return resultado a custo médio sobre o total comprado, em percentual; zero sem compras */
    public BigDecimal getPercentualCustoMedio() {
        return percentual(resultadoCustoMedio);
    }

    /** @return resultado a mercado sobre o total comprado, em percentual; zero sem compras */
    public BigDecimal getPercentualMercado() {
        return percentual(resultadoMercado);
    }

    private BigDecimal percentual(BigDecimal valor) {
        if (totalComprado.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return valor.multiply(BigDecimal.valueOf(100)).divide(totalComprado, 4, RoundingMode.HALF_EVEN);
    }
}

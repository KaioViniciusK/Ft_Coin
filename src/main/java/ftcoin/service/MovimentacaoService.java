package ftcoin.service;

import ftcoin.dao.CarteiraDAO;
import ftcoin.dao.MovimentacaoDAO;
import ftcoin.model.Cotacao;
import ftcoin.model.Movimentacao;
import ftcoin.model.TipoOperacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compra e venda, validação de saldo e apuração financeira (custo médio ponderado e mercado).
 * Saldo, custo médio e resultado são calculados a cada consulta e nunca persistidos.
 */
public final class MovimentacaoService {
    private static final Logger LOG = Logger.getLogger(MovimentacaoService.class.getName());
    private static final int ESCALA = 10;
    /** Limites da coluna Quantidade do banco, DECIMAL(10,3). */
    private static final int ESCALA_BANCO = 3;
    private static final int DIGITOS_INTEIROS_BANCO = 7;

    private final MovimentacaoDAO movimentacaoDAO;
    private final CarteiraDAO carteiraDAO;
    private final OraculoService oraculo;
    private final Clock relogio;

    public MovimentacaoService(MovimentacaoDAO movimentacaoDAO, CarteiraDAO carteiraDAO, OraculoService oraculo,
            Clock relogio) {
        this.movimentacaoDAO = movimentacaoDAO;
        this.carteiraDAO = carteiraDAO;
        this.oraculo = oraculo;
        this.relogio = relogio;
    }

    /**
     * Registra uma compra ou venda. Vendas são validadas contra o histórico inteiro, inclusive
     * movimentos de datas posteriores, para que o saldo nunca fique negativo.
     *
     * @return a movimentação gravada
     * @throws NegocioException se a carteira não existir, a quantidade ou a data forem inválidas
     *                          ou o saldo for insuficiente
     */
    public Movimentacao registrar(int idCarteira, TipoOperacao tipo, LocalDate data, BigDecimal quantidade) {
        exigirCarteira(idCarteira);
        if (data == null) {
            throw new NegocioException("erro.data.invalida");
        }
        if (data.isAfter(LocalDate.now(relogio))) {
            throw new NegocioException("erro.data.futura", data);
        }
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new NegocioException("erro.quantidade.invalida");
        }
        if (quantidade.stripTrailingZeros().scale() > ESCALA_BANCO || quantidade.precision() - quantidade.scale() > DIGITOS_INTEIROS_BANCO) {
            throw new NegocioException("erro.quantidade.precisao");
        }
        Movimentacao nova = new Movimentacao(movimentacaoDAO.proximoId(), idCarteira, data, tipo, quantidade);
        List<Movimentacao> todas = new ArrayList<>(movimentacaoDAO.listarPorCarteira(idCarteira));
        todas.add(nova);
        todas.sort(Movimentacao.ORDEM_CRONOLOGICA);
        validarSaldo(todas);
        movimentacaoDAO.inserir(nova);
        LOG.log(Level.INFO, "Auditoria: {0} de {1} FTCoins em {2} na carteira {3}",
                new Object[] {tipo, quantidade.toPlainString(), data, idCarteira});
        return nova;
    }

    /**
     * @return o histórico da carteira em ordem cronológica
     * @throws NegocioException se a carteira não existir
     */
    public List<Movimentacao> historico(int idCarteira) {
        exigirCarteira(idCarteira);
        return movimentacaoDAO.listarPorCarteira(idCarteira);
    }

    /**
     * Apura o resultado da carteira por custo médio ponderado e a mercado. As cotações das datas
     * dos movimentos são obtidas em uma só consulta; a cotação de mercado só é buscada se houver saldo.
     *
     * @return o resultado financeiro
     * @throws NegocioException se a carteira não existir ou uma cotação necessária não puder ser obtida
     */
    public ResultadoFinanceiroDTO apurar(int idCarteira) {
        exigirCarteira(idCarteira);
        List<Movimentacao> movimentos = new ArrayList<>(movimentacaoDAO.listarPorCarteira(idCarteira));
        if (movimentos.isEmpty()) {
            return ResultadoFinanceiroDTO.vazio(idCarteira);
        }
        movimentos.sort(Movimentacao.ORDEM_CRONOLOGICA);
        Set<LocalDate> datas = new HashSet<>();
        for (Movimentacao m : movimentos) {
            datas.add(m.getData());
        }
        Map<LocalDate, Cotacao> cotacoes = oraculo.cotacoes(datas);

        BigDecimal saldo = BigDecimal.ZERO;
        BigDecimal custoTotal = BigDecimal.ZERO;
        BigDecimal comprado = BigDecimal.ZERO;
        BigDecimal vendido = BigDecimal.ZERO;
        BigDecimal realizado = BigDecimal.ZERO;
        for (Movimentacao m : movimentos) {
            BigDecimal valor = m.getQuantidade().multiply(cotacoes.get(m.getData()).getValor());
            if (m.getTipo() == TipoOperacao.COMPRA) {
                comprado = comprado.add(valor);
                custoTotal = custoTotal.add(valor);
                saldo = saldo.add(m.getQuantidade());
            } else {
                if (saldo.compareTo(m.getQuantidade()) < 0) {
                    throw new NegocioException("erro.saldo.insuficiente", m.getData());
                }
                BigDecimal custoSaida = custoTotal.multiply(m.getQuantidade()).divide(saldo, ESCALA, RoundingMode.HALF_EVEN);
                vendido = vendido.add(valor);
                realizado = realizado.add(valor.subtract(custoSaida));
                custoTotal = custoTotal.subtract(custoSaida);
                saldo = saldo.subtract(m.getQuantidade());
            }
        }
        BigDecimal custoMedio = saldo.signum() > 0 ? custoTotal.divide(saldo, ESCALA, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
        BigDecimal cotacaoMercado = null;
        BigDecimal saldoMercado = BigDecimal.ZERO;
        if (saldo.signum() > 0) {
            cotacaoMercado = oraculo.ultimaCotacaoAte(LocalDate.now(relogio)).getValor();
            saldoMercado = saldo.multiply(cotacaoMercado);
        }
        BigDecimal resultadoMercado = realizado.add(saldoMercado).subtract(custoTotal);
        return new ResultadoFinanceiroDTO(idCarteira, saldo, comprado, vendido, custoMedio, custoTotal, realizado,
                cotacaoMercado, saldoMercado, resultadoMercado);
    }

    private void exigirCarteira(int idCarteira) {
        if (carteiraDAO.buscar(idCarteira).isEmpty()) {
            throw new NegocioException("erro.carteira.inexistente", idCarteira);
        }
    }

    private void validarSaldo(List<Movimentacao> ordenadas) {
        BigDecimal saldo = BigDecimal.ZERO;
        for (Movimentacao m : ordenadas) {
            saldo = m.getTipo() == TipoOperacao.COMPRA ? saldo.add(m.getQuantidade()) : saldo.subtract(m.getQuantidade());
            if (saldo.signum() < 0) {
                throw new NegocioException("erro.saldo.insuficiente", m.getData());
            }
        }
    }
}

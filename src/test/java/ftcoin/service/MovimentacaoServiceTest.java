package ftcoin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ftcoin.dao.CarteiraMemoryDAO;
import ftcoin.dao.MovimentacaoMemoryDAO;
import ftcoin.model.Carteira;
import ftcoin.model.TipoOperacao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MovimentacaoServiceTest {
    static final LocalDate HOJE = LocalDate.of(2026, 11, 21);
    static final LocalDate D5 = LocalDate.of(2026, 1, 5);
    static final LocalDate D6 = LocalDate.of(2026, 1, 6);
    static final LocalDate D7 = LocalDate.of(2026, 1, 7);
    static final LocalDate D8 = LocalDate.of(2026, 1, 8);
    static final LocalDate D10 = LocalDate.of(2026, 1, 10);
    static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneId.of("UTC"));

    /** Serviço montado com DAOs em memória, relógio fixo em 21/11/2026 e uma carteira de id 1. */
    private static final class Cenario {
        final OraculoFalso cliente = new OraculoFalso();
        final OraculoService oraculo = new OraculoService(cliente, RELOGIO);
        final MovimentacaoService service;

        Cenario() {
            CarteiraMemoryDAO carteiras = new CarteiraMemoryDAO();
            carteiras.inserir(new Carteira(1, "Titular", "Corretora"));
            service = new MovimentacaoService(new MovimentacaoMemoryDAO(), carteiras, oraculo, RELOGIO);
        }

        void comprar(LocalDate data, String qtd) {
            service.registrar(1, TipoOperacao.COMPRA, data, new BigDecimal(qtd));
        }

        void vender(LocalDate data, String qtd) {
            service.registrar(1, TipoOperacao.VENDA, data, new BigDecimal(qtd));
        }
    }

    private static void igual(String esperado, BigDecimal real) {
        assertTrue(new BigDecimal(esperado).compareTo(real) == 0, "esperado " + esperado + " mas foi " + real);
    }

    private static String chave(Runnable acao) {
        return assertThrows(NegocioException.class, acao::run).getChave();
    }

    @Test
    void apuraCustoMedioPonderadoEMercado() {
        Cenario c = new Cenario();
        c.cliente.com(D5, "10.00").com(D6, "14.00").com(D7, "15.00").com(HOJE, "20.00");
        c.comprar(D5, "10");
        c.comprar(D6, "10");
        c.vender(D7, "5");
        ResultadoFinanceiroDTO r = c.service.apurar(1);
        igual("15", r.getQuantidadeSaldo());
        igual("240", r.getTotalComprado());
        igual("75", r.getTotalVendido());
        igual("12", r.getCustoMedio());
        igual("180", r.getSaldoCustoMedio());
        igual("15", r.getResultadoCustoMedio());
        igual("20", r.getCotacaoMercado());
        igual("300", r.getSaldoMercado());
        igual("135", r.getResultadoMercado());
        igual("6.25", r.getPercentualCustoMedio());
        igual("56.25", r.getPercentualMercado());
    }

    @Test
    void liquidacaoCompletaFechaSemSobraDeCentavos() {
        Cenario c = new Cenario();
        c.cliente.com(D5, "10.00").com(D6, "10.01").com(D7, "11.00").com(D8, "11.00");
        c.comprar(D5, "1");
        c.comprar(D6, "2");
        c.vender(D7, "1");
        c.vender(D8, "2");
        ResultadoFinanceiroDTO r = c.service.apurar(1);
        igual("0", r.getQuantidadeSaldo());
        igual("0", r.getCustoMedio());
        igual("0", r.getSaldoCustoMedio());
        igual("30.02", r.getTotalComprado());
        igual("33", r.getTotalVendido());
        igual("2.98", r.getResultadoCustoMedio());
        igual("2.98", r.getResultadoMercado());
        assertNull(r.getCotacaoMercado());
    }

    @Test
    void semSaldoNaoConsultaACotacaoDeMercado() {
        Cenario c = new Cenario();
        c.cliente.com(D5, "10.00").com(D7, "15.00");
        c.comprar(D5, "10");
        c.vender(D7, "10");
        ResultadoFinanceiroDTO r = c.service.apurar(1);
        igual("50", r.getResultadoMercado());
        assertFalse(c.cliente.consultouAte(HOJE));
    }

    @Test
    void comSaldoERedeForaAApuracaoFalhaEmVezDeUsarPrecoAntigo() {
        Cenario c = new Cenario();
        c.cliente.com(D5, "10.00").com(D6, "14.00").com(D7, "15.00").com(HOJE.minusDays(1), "19.00");
        c.comprar(D5, "10");
        c.comprar(D6, "10");
        c.vender(D7, "5");
        c.oraculo.cotacoes(Set.of(D5, D6, D7));
        c.oraculo.cotar(HOJE.minusDays(1));
        c.cliente.redeFora = true;
        assertThrows(CotacaoIndisponivelException.class, () -> c.service.apurar(1));
    }

    @Test
    void comCotacaoDeHojeEmCacheARedeForaNaoImpedeAApuracao() {
        Cenario c = new Cenario();
        c.cliente.com(D5, "10.00").com(D6, "14.00").com(D7, "15.00").com(HOJE, "20.00");
        c.comprar(D5, "10");
        c.comprar(D6, "10");
        c.vender(D7, "5");
        c.service.apurar(1);
        int consultas = c.cliente.consultas.size();
        c.cliente.redeFora = true;
        igual("135", c.service.apurar(1).getResultadoMercado());
        assertEquals(consultas, c.cliente.consultas.size());
    }

    @Test
    void dataDeMovimentoSemCotacaoImpedeAApuracao() {
        Cenario c = new Cenario();
        c.cliente.com(D5, "10.00");
        c.comprar(D5, "10");
        c.comprar(D6, "1");
        assertThrows(CotacaoIndisponivelException.class, () -> c.service.apurar(1));
    }

    @Test
    void carteiraSemMovimentosTemResultadoZeroSemConsultarOraculo() {
        Cenario c = new Cenario();
        ResultadoFinanceiroDTO r = c.service.apurar(1);
        igual("0", r.getQuantidadeSaldo());
        igual("0", r.getResultadoMercado());
        igual("0", r.getPercentualMercado());
        assertNull(r.getCotacaoMercado());
        assertTrue(c.cliente.consultas.isEmpty());
    }

    @Test
    void vendaAcimaDoSaldoERejeitada() {
        Cenario c = new Cenario();
        c.comprar(D5, "10");
        assertEquals("erro.saldo.insuficiente", chave(() -> c.vender(D6, "10.001")));
    }

    @Test
    void vendaRetroativaQueDeixariaVendaPosteriorSemSaldoERejeitada() {
        Cenario c = new Cenario();
        c.comprar(D5, "10");
        c.vender(D10, "10");
        assertEquals("erro.saldo.insuficiente", chave(() -> c.vender(D7, "1")));
        assertEquals(2, c.service.historico(1).size());
    }

    @Test
    void vendaNoMesmoDiaDeveSerRegistradaDepoisDaCompra() {
        Cenario c = new Cenario();
        assertEquals("erro.saldo.insuficiente", chave(() -> c.vender(D5, "5")));
        c.comprar(D5, "5");
        c.vender(D5, "5");
        assertEquals(2, c.service.historico(1).size());
    }

    @Test
    void dataFuturaERejeitada() {
        Cenario c = new Cenario();
        assertEquals("erro.data.futura", chave(() -> c.comprar(HOJE.plusDays(1), "1")));
        c.comprar(HOJE, "1");
    }

    @Test
    void quantidadeAceitaAteTresCasasDecimais() {
        Cenario c = new Cenario();
        c.comprar(D5, "0.001");
        c.comprar(D5, "1.500");
        assertEquals("erro.quantidade.precisao", chave(() -> c.comprar(D5, "0.0001")));
        assertEquals("erro.quantidade.precisao", chave(() -> c.comprar(D5, "10000000")));
        assertEquals("erro.quantidade.invalida", chave(() -> c.comprar(D5, "0")));
        assertEquals("erro.quantidade.invalida", chave(() -> c.comprar(D5, "-1")));
    }

    @Test
    void carteiraInexistenteERejeitadaEmTodasAsOperacoes() {
        Cenario c = new Cenario();
        assertEquals("erro.carteira.inexistente", chave(() -> c.service.apurar(99)));
        assertEquals("erro.carteira.inexistente", chave(() -> c.service.historico(99)));
        assertEquals("erro.carteira.inexistente",
                chave(() -> c.service.registrar(99, TipoOperacao.COMPRA, D5, BigDecimal.ONE)));
    }
}

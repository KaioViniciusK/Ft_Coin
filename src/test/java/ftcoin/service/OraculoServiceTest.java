package ftcoin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ftcoin.model.Cotacao;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OraculoServiceTest {
    static final LocalDate HOJE = LocalDate.of(2026, 11, 21);
    static final LocalDate ONTEM = HOJE.minusDays(1);
    static final LocalDate D5 = LocalDate.of(2026, 1, 5);
    static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-21T12:00:00Z"), ZoneId.of("UTC"));

    private static OraculoService servico(OraculoFalso cliente) {
        return new OraculoService(cliente, RELOGIO);
    }

    @Test
    void segundaConsultaDaMesmaDataVemDoCache() {
        OraculoFalso cliente = new OraculoFalso().com(D5, "10.00");
        OraculoService oraculo = servico(cliente);
        assertEquals(0, new BigDecimal("10.00").compareTo(oraculo.cotar(D5).getValor()));
        oraculo.cotar(D5);
        assertEquals(1, cliente.consultas.size());
        assertEquals(1, oraculo.getRequisicoes());
        assertEquals(1, oraculo.getAcertos());
        assertEquals(1, oraculo.getFaltas());
        assertEquals(0, oraculo.getErros());
    }

    @Test
    void faltasViramUmaRequisicaoComAMenorFaixa() {
        OraculoFalso cliente = new OraculoFalso().com(D5, "10.00").com(D5.plusDays(4), "11.00");
        servico(cliente).cotacoes(Set.of(D5, D5.plusDays(4)));
        assertEquals(1, cliente.consultas.size());
        assertEquals(D5, cliente.consultas.get(0)[0]);
        assertEquals(D5.plusDays(4), cliente.consultas.get(0)[1]);
    }

    @Test
    void falhaDeRedeNaoGravaNadaEOServicoSeRecupera() {
        OraculoFalso cliente = new OraculoFalso().com(D5, "10.00");
        OraculoService oraculo = servico(cliente);
        cliente.redeFora = true;
        assertThrows(CotacaoIndisponivelException.class, () -> oraculo.cotar(D5));
        assertEquals(1, oraculo.getErros());
        cliente.redeFora = false;
        assertEquals(0, new BigDecimal("10.00").compareTo(oraculo.cotar(D5).getValor()));
        assertEquals(2, cliente.consultas.size());
    }

    @Test
    void ausenciaDeDataPassadaEMemorizada() {
        OraculoFalso cliente = new OraculoFalso();
        OraculoService oraculo = servico(cliente);
        assertThrows(CotacaoIndisponivelException.class, () -> oraculo.cotar(D5));
        assertThrows(CotacaoIndisponivelException.class, () -> oraculo.cotar(D5));
        assertEquals(1, cliente.consultas.size());
    }

    @Test
    void mercadoComRedeForaNaoUsaCotacaoDeOutroDia() {
        OraculoFalso cliente = new OraculoFalso().com(ONTEM, "15.00");
        OraculoService oraculo = servico(cliente);
        oraculo.cotar(ONTEM);
        cliente.redeFora = true;
        assertThrows(CotacaoIndisponivelException.class, () -> oraculo.cotacaoDeMercado(HOJE));
    }

    @Test
    void mercadoUsaACotacaoDeHojeQueJaEstaNoCacheMesmoComRedeFora() {
        OraculoFalso cliente = new OraculoFalso().com(HOJE, "20.00");
        OraculoService oraculo = servico(cliente);
        oraculo.cotar(HOJE);
        cliente.redeFora = true;
        assertEquals(0, new BigDecimal("20.00").compareTo(oraculo.cotacaoDeMercado(HOJE).getValor()));
        assertEquals(1, cliente.consultas.size());
    }

    @Test
    void mercadoRecuaSoQuandoOServidorConfirmaQueHojeNaoTemCotacao() {
        OraculoFalso cliente = new OraculoFalso().com(ONTEM, "14.00").com(HOJE.minusDays(2), "13.00");
        Cotacao cotacao = servico(cliente).cotacaoDeMercado(HOJE);
        assertEquals(ONTEM, cotacao.getData());
        assertEquals(0, new BigDecimal("14.00").compareTo(cotacao.getValor()));
    }

    @Test
    void ausenciaDeHojeNaoEMemorizadaEACotacaoPublicadaDepoisEUsada() {
        OraculoFalso cliente = new OraculoFalso().com(ONTEM, "14.00");
        OraculoService oraculo = servico(cliente);
        assertEquals(ONTEM, oraculo.cotacaoDeMercado(HOJE).getData());
        cliente.com(HOJE, "16.00");
        assertEquals(HOJE, oraculo.cotacaoDeMercado(HOJE).getData());
    }

    @Test
    void mercadoSemNenhumaCotacaoNaJanelaFalha() {
        OraculoFalso cliente = new OraculoFalso().com(HOJE.minusDays(7), "9.00");
        assertThrows(CotacaoIndisponivelException.class, () -> servico(cliente).cotacaoDeMercado(HOJE));
    }

    @Test
    void cotacaoForaDaFaixaPedidaEDescartada() {
        OraculoFalso cliente = new OraculoFalso();
        cliente.respostaFixa = List.of(new Cotacao(D5, new BigDecimal("10.00")),
                new Cotacao(D5.plusDays(15), new BigDecimal("99.00")));
        OraculoService oraculo = servico(cliente);
        oraculo.cotar(D5);
        oraculo.cotar(D5.plusDays(15));
        assertEquals(2, cliente.consultas.size());
    }

    @Test
    void respostaComValoresDiferentesParaAMesmaDataEIgnoradaPorInteiro() {
        OraculoFalso cliente = new OraculoFalso().com(D5, "10.00");
        OraculoService oraculo = servico(cliente);
        cliente.respostaFixa = List.of(new Cotacao(D5, new BigDecimal("10.00")),
                new Cotacao(D5, new BigDecimal("11.00")));
        assertThrows(CotacaoIndisponivelException.class, () -> oraculo.cotar(D5));
        cliente.respostaFixa = null;
        assertEquals(0, new BigDecimal("10.00").compareTo(oraculo.cotar(D5).getValor()));
        assertEquals(2, cliente.consultas.size());
    }

    @Test
    void repeticaoIgualNaRespostaEAceita() {
        OraculoFalso cliente = new OraculoFalso();
        cliente.respostaFixa = List.of(new Cotacao(D5, new BigDecimal("10.00")),
                new Cotacao(D5, new BigDecimal("10.0")));
        assertEquals(0, new BigDecimal("10").compareTo(servico(cliente).cotar(D5).getValor()));
    }

    @Test
    void respostaSemListaEErroDeConsulta() {
        OraculoFalso cliente = new OraculoFalso();
        cliente.respostaNula = true;
        OraculoService oraculo = servico(cliente);
        assertThrows(CotacaoIndisponivelException.class, () -> oraculo.cotar(D5));
        assertTrue(oraculo.getErros() == 1);
    }
}

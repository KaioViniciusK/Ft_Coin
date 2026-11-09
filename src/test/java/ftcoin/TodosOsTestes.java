package ftcoin;

import static ftcoin.Verificacoes.*;

import ftcoin.dao.CarteiraDAO;
import ftcoin.dao.CarteiraMemoryDAO;
import ftcoin.dao.DAOFactory;
import ftcoin.dao.MemoryDAOFactory;
import ftcoin.dao.MovimentacaoDAO;
import ftcoin.dao.MovimentacaoMemoryDAO;
import ftcoin.i18n.ConfiguracaoIdioma;
import ftcoin.model.Carteira;
import ftcoin.model.FonteDados;
import ftcoin.model.Idioma;
import ftcoin.model.Movimentacao;
import ftcoin.model.TipoOperacao;
import ftcoin.config.ConfigAplicacao;
import ftcoin.oraculo.OraculoException;
import ftcoin.oraculo.OraculoSimuladoClient;
import ftcoin.oraculo.OraculoSocketClient;
import ftcoin.presenter.CarteiraPresenter;
import ftcoin.presenter.MenuPrincipalPresenter;
import ftcoin.service.CarteiraService;
import ftcoin.service.CotacaoIndisponivelException;
import ftcoin.service.MovimentacaoService;
import ftcoin.service.NegocioException;
import ftcoin.service.OraculoService;
import ftcoin.service.ResultadoFinanceiroDTO;
import ftcoin.view.CarteiraDados;
import ftcoin.view.DadosMovimentacao;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

/** Todos os testes do projeto; execute com {@code java -cp out:test-out ftcoin.TodosOsTestes}. */
public final class TodosOsTestes {
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-08T12:00:00Z"), ZoneOffset.UTC);

    /** View falsa que guarda o que o Presenter mandou mostrar e devolve respostas programadas. */
    static final class ViewFalsa implements ftcoin.view.MenuPrincipalView, ftcoin.view.CarteiraView {
        String idCarteira;
        DadosMovimentacao movimentacao;
        String data;
        CarteiraDados dadosCarteira;
        boolean confirma = true;
        String texto = "";
        String mensagem = "";
        String erro = "";
        int atualizacoes;
        boolean encerrada;

        public String solicitarIdCarteira(String t) { return idCarteira; }
        public DadosMovimentacao solicitarMovimentacao(String t, String p) { return movimentacao; }
        public String solicitarData(String t, String p) { return data; }
        public void exibirTexto(String t, String corpo) { texto = corpo; }
        public void exibirMensagem(String m) { mensagem = m; }
        public void exibirErro(String m) { erro = m; }
        public void atualizarTextos() { atualizacoes++; }
        public void encerrar() { encerrada = true; }
        public CarteiraDados solicitarCarteira(String t, CarteiraDados i, boolean e) { return dadosCarteira; }
        public String solicitarId(String t) { return idCarteira; }
        public boolean confirmar(String t, String p) { return confirma; }
    }

    private static LocalDate dia(int n) {
        return LocalDate.of(2026, 1, n);
    }

    /** Operações da planilha FTCoinApuracaoResultado: {dia, C/V, quantidade, financeiro}. */
    private static final Object[][] PLANILHA = {
        {1, 'C', 1000, "12287.94"}, {5, 'V', 500, "5278.46"}, {6, 'C', 1000, "13606.09"},
        {9, 'V', 500, "6216.09"}, {10, 'V', 500, "6918.97"}, {11, 'C', 1000, "14209.33"},
        {13, 'V', 500, "5735.94"}, {15, 'V', 500, "6282.02"}, {16, 'C', 1000, "12602.06"},
        {18, 'V', 500, "7306.61"}, {20, 'V', 500, "5779.49"}, {21, 'C', 1000, "12060.16"},
        {23, 'V', 500, "5459.83"}, {25, 'V', 500, "6512.94"}, {26, 'C', 1000, "12032.91"},
        {28, 'V', 500, "6017.55"}};

    /** Monta o sistema com DAOs em memória e um cliente falso. */
    static final class Cenario {
        final ClienteFalso cliente = new ClienteFalso();
        final CarteiraDAO carteiraDAO = new CarteiraMemoryDAO();
        final MovimentacaoDAO movimentacaoDAO = new MovimentacaoMemoryDAO();
        final OraculoService oraculo = new OraculoService(cliente);
        final CarteiraService carteiras = new CarteiraService(carteiraDAO, movimentacaoDAO);
        final MovimentacaoService movimentos = new MovimentacaoService(movimentacaoDAO, carteiraDAO, oraculo, RELOGIO);

        Cenario() {
            carteiras.incluir(1, "Ana", "Corretora A");
        }
    }

    public static void main(String[] args) throws Exception {
        testarModelo();
        testarApuracao();
        testarRegrasDeSaldo();
        testarOraculoService();
        testarClienteSocket();
        testarDaosMemoria();
        testarServicoDeCarteira();
        testarIdiomaEConfig();
        testarPresenters();
        Verificacoes.resumo();
    }

    private static void testarModelo() {
        caso("TipoOperacao: ida e volta de C e V", () -> {
            igual(TipoOperacao.COMPRA, TipoOperacao.fromCodigo('C'), "C");
            igual(TipoOperacao.VENDA, TipoOperacao.fromCodigo(TipoOperacao.VENDA.getCodigo()), "V");
        });
        caso("TipoOperacao: código inválido é rejeitado", () ->
                lanca(IllegalArgumentException.class, () -> TipoOperacao.fromCodigo('X'), "fromCodigo"));
        caso("Movimentacao: quantidade zero ou negativa é rejeitada", () -> {
            lanca(IllegalArgumentException.class, () -> new Movimentacao(1, 1, dia(1), TipoOperacao.COMPRA, BigDecimal.ZERO), "zero");
            lanca(IllegalArgumentException.class, () -> new Movimentacao(1, 1, dia(1), TipoOperacao.COMPRA, BigDecimal.valueOf(-1)), "negativa");
        });
        caso("Carteira: titular vazio é rejeitado", () ->
                lanca(IllegalArgumentException.class, () -> new Carteira(1, " ", "X"), "titular"));
        caso("FonteDados: padrão é memória; valor desconhecido é rejeitado", () -> {
            igual(FonteDados.MEMORIA, FonteDados.deArgumento(null), "padrão");
            igual(FonteDados.MARIADB, FonteDados.deArgumento("MariaDB"), "mariadb");
            lanca(IllegalArgumentException.class, () -> FonteDados.deArgumento("xml"), "xml");
        });
        caso("DAOFactory: memória devolve DAOs em memória", () -> {
            DAOFactory f = DAOFactory.obter(FonteDados.MEMORIA, new ConfigAplicacao(new Properties()));
            verdadeiro(f instanceof MemoryDAOFactory, "fábrica");
            verdadeiro(f.criarCarteiraDAO() instanceof CarteiraMemoryDAO, "carteira dao");
            verdadeiro(f.criarMovimentacaoDAO() instanceof MovimentacaoMemoryDAO, "mov dao");
        });
    }

    private static Cenario cenarioPlanilha() {
        Cenario c = new Cenario();
        for (Object[] op : PLANILHA) {
            int d = (Integer) op[0];
            c.cliente.valores.put(dia(d), new BigDecimal((String) op[3]).divide(BigDecimal.valueOf((Integer) op[2]), 5, java.math.RoundingMode.HALF_UP));
        }
        return c;
    }

    private static void testarApuracao() {
        caso("Apuração: resultado confere com a planilha do professor (30 dias, 16 operações)", () -> {
            Cenario c = cenarioPlanilha();
            c.cliente.valores.put(dia(30), new BigDecimal("14.65506"));
            // a planilha termina em 30/01/2026; o relógio de teste está em 08/11/2026, então a cotação
            // de mercado é a mais recente dentro da janela de 7 dias: usamos um relógio em 30/01.
            Clock fim = Clock.fixed(Instant.parse("2026-01-30T12:00:00Z"), ZoneOffset.UTC);
            MovimentacaoService s = new MovimentacaoService(c.movimentacaoDAO, c.carteiraDAO, c.oraculo, fim);
            for (Object[] op : PLANILHA) {
                s.registrar(1, op[1].equals('C') ? TipoOperacao.COMPRA : TipoOperacao.VENDA, dia((Integer) op[0]),
                        BigDecimal.valueOf((Integer) op[2]));
            }
            ResultadoFinanceiroDTO r = s.apurar(1);
            proximo(1000, r.getQuantidadeSaldo(), 0.0001, "saldo em FTCoins");
            proximo(76798.50, r.getTotalComprado(), 0.10, "total comprado");
            proximo(61507.89, r.getTotalVendido(), 0.10, "total vendido");
            proximo(12.15, r.getCustoMedio(), 0.01, "custo médio");
            proximo(12148.86, r.getSaldoCustoMedio(), 0.10, "saldo a custo médio");
            proximo(-3141.75, r.getResultadoCustoMedio(), 0.10, "resultado acumulado");
            proximo(14655.06, r.getSaldoMercado(), 0.10, "saldo a mercado");
            proximo(-635.55, r.getResultadoMercado(), 0.10, "resultado a mercado");
            proximo(-4.1, r.getPercentualCustoMedio(), 0.05, "percentual a custo médio");
        });
        caso("Apuração: uma só consulta à API cobre todas as datas das operações", () -> {
            Cenario c = cenarioPlanilha();
            c.cliente.valores.put(dia(30), new BigDecimal("14.65506"));
            Clock fim = Clock.fixed(Instant.parse("2026-01-30T12:00:00Z"), ZoneOffset.UTC);
            MovimentacaoService s = new MovimentacaoService(c.movimentacaoDAO, c.carteiraDAO, c.oraculo, fim);
            for (Object[] op : PLANILHA) {
                s.registrar(1, op[1].equals('C') ? TipoOperacao.COMPRA : TipoOperacao.VENDA, dia((Integer) op[0]),
                        BigDecimal.valueOf((Integer) op[2]));
            }
            s.apurar(1);
            s.apurar(1);
            igual(2, c.cliente.consultas.size(), "consultas (operações + janela de mercado); a repetição usa o cache");
        });
        caso("Apuração: carteira sem movimentos não consulta a API", () -> {
            Cenario c = new Cenario();
            ResultadoFinanceiroDTO r = c.movimentos.apurar(1);
            proximo(0, r.getResultadoMercado(), 0.0, "resultado");
            igual(0, c.cliente.consultas.size(), "consultas");
        });
        caso("Apuração: posição zerada não busca cotação de mercado", () -> {
            Cenario c = new Cenario();
            c.cliente.valores.put(dia(1), new BigDecimal("10"));
            c.cliente.valores.put(dia(2), new BigDecimal("12"));
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), new BigDecimal("4"));
            c.movimentos.registrar(1, TipoOperacao.VENDA, dia(2), new BigDecimal("4"));
            ResultadoFinanceiroDTO r = c.movimentos.apurar(1);
            proximo(8, r.getResultadoCustoMedio(), 0.0001, "ganho de 4 x (12 - 10)");
            proximo(0, r.getCustoMedio(), 0.0, "custo médio zerado");
            verdadeiro(r.getCotacaoMercado() == null, "sem cotação de mercado");
            igual(1, c.cliente.consultas.size(), "uma consulta");
        });
    }

    private static void testarRegrasDeSaldo() {
        caso("Venda acima do saldo é recusada", () -> {
            Cenario c = new Cenario();
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), new BigDecimal("5"));
            NegocioException e = lanca(NegocioException.class,
                    () -> c.movimentos.registrar(1, TipoOperacao.VENDA, dia(2), new BigDecimal("5.01")), "venda");
            igual("erro.saldo.insuficiente", e.getChave(), "chave");
            igual(1, c.movimentos.historico(1).size(), "nada foi gravado");
        });
        caso("Venda igual ao saldo é aceita", () -> {
            Cenario c = new Cenario();
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), new BigDecimal("5"));
            c.movimentos.registrar(1, TipoOperacao.VENDA, dia(2), new BigDecimal("5"));
            igual(2, c.movimentos.historico(1).size(), "histórico");
        });
        caso("Venda com data anterior à compra é recusada", () -> {
            Cenario c = new Cenario();
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(5), new BigDecimal("5"));
            lanca(NegocioException.class, () -> c.movimentos.registrar(1, TipoOperacao.VENDA, dia(2), new BigDecimal("1")), "retroativa");
        });
        caso("Venda retroativa que deixaria uma venda posterior sem saldo é recusada", () -> {
            Cenario c = new Cenario();
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), new BigDecimal("5"));
            c.movimentos.registrar(1, TipoOperacao.VENDA, dia(10), new BigDecimal("5"));
            lanca(NegocioException.class, () -> c.movimentos.registrar(1, TipoOperacao.VENDA, dia(5), new BigDecimal("1")),
                    "a venda de 1 no dia 5 faria a venda do dia 10 ficar sem saldo");
        });
        caso("Quantidade zero ou negativa é recusada no serviço", () -> {
            Cenario c = new Cenario();
            lanca(NegocioException.class, () -> c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), BigDecimal.ZERO), "zero");
            lanca(NegocioException.class, () -> c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), new BigDecimal("-1")), "negativa");
        });
        caso("Quantidade além da precisão do banco (3 casas) é recusada; 3 casas passa", () -> {
            Cenario c = new Cenario();
            igual("erro.quantidade.precisao", lanca(NegocioException.class,
                    () -> c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), new BigDecimal("1.0001")), "4 casas").getChave(), "chave");
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), new BigDecimal("1.500"));
        });
        caso("Data futura e carteira inexistente são recusadas", () -> {
            Cenario c = new Cenario();
            igual("erro.data.futura", lanca(NegocioException.class,
                    () -> c.movimentos.registrar(1, TipoOperacao.COMPRA, LocalDate.of(2026, 11, 9), BigDecimal.ONE), "futura").getChave(), "chave");
            igual("erro.carteira.inexistente", lanca(NegocioException.class,
                    () -> c.movimentos.registrar(99, TipoOperacao.COMPRA, dia(1), BigDecimal.ONE), "carteira").getChave(), "chave");
        });
        caso("Histórico sai em ordem cronológica", () -> {
            Cenario c = new Cenario();
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(9), BigDecimal.TEN);
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(3), BigDecimal.TEN);
            igual(dia(3), c.movimentos.historico(1).get(0).getData(), "primeiro");
        });
    }

    private static void testarOraculoService() {
        caso("OraculoService: falta no cache consulta a rede; acerto não consulta", () -> {
            ClienteFalso f = new ClienteFalso();
            f.valores.put(dia(3), new BigDecimal("11.5"));
            OraculoService o = new OraculoService(f);
            igual(new BigDecimal("11.5"), o.cotar(dia(3)).getValor(), "valor");
            o.cotar(dia(3));
            igual(1, f.consultas.size(), "consultas");
            igual(1, o.getAcertos(), "acertos");
            igual(1, o.getFaltas(), "faltas");
        });
        caso("OraculoService: várias datas faltantes viram uma só requisição pela menor faixa", () -> {
            ClienteFalso f = new ClienteFalso();
            for (int d = 1; d <= 10; d++) {
                f.valores.put(dia(d), BigDecimal.valueOf(d));
            }
            OraculoService o = new OraculoService(f);
            o.cotacoes(Set.of(dia(2), dia(9), dia(5)));
            igual(1, f.consultas.size(), "requisições");
            igual(dia(2), f.consultas.get(0)[0], "início da faixa");
            igual(dia(9), f.consultas.get(0)[1], "fim da faixa");
        });
        caso("OraculoService: data sem cotação gera exceção e não é consultada de novo", () -> {
            ClienteFalso f = new ClienteFalso();
            OraculoService o = new OraculoService(f);
            lanca(CotacaoIndisponivelException.class, () -> o.cotar(dia(4)), "sem cotação");
            lanca(CotacaoIndisponivelException.class, () -> o.cotar(dia(4)), "de novo");
            igual(1, f.consultas.size(), "consultas");
        });
        caso("OraculoService: rede fora sem cache gera exceção com causa preservada", () -> {
            ClienteFalso f = new ClienteFalso();
            f.redeFora = true;
            OraculoService o = new OraculoService(f);
            CotacaoIndisponivelException e = lanca(CotacaoIndisponivelException.class, () -> o.cotar(dia(4)), "rede");
            verdadeiro(e.getCause() instanceof OraculoException, "causa");
            igual(1, o.getErros(), "erros");
        });
        caso("OraculoService: rede fora com a data já no cache funciona", () -> {
            ClienteFalso f = new ClienteFalso();
            f.valores.put(dia(4), BigDecimal.ONE);
            OraculoService o = new OraculoService(f);
            o.cotar(dia(4));
            f.redeFora = true;
            igual(BigDecimal.ONE, o.cotar(dia(4)).getValor(), "valor do cache");
        });
        caso("OraculoService: cotação de mercado usa o dia mais recente da janela; rede fora usa o cache", () -> {
            ClienteFalso f = new ClienteFalso();
            f.valores.put(dia(8), new BigDecimal("9"));
            f.valores.put(dia(10), new BigDecimal("11"));
            OraculoService o = new OraculoService(f);
            igual(new BigDecimal("11"), o.ultimaCotacaoAte(dia(10)).getValor(), "mais recente");
            f.redeFora = true;
            igual(new BigDecimal("9"), o.ultimaCotacaoAte(dia(9)).getValor(), "janela de 9 usa o dia 8 do cache");
        });
        caso("OraculoSimuladoClient: valores determinísticos entre 10 e 15", () -> {
            var a = new OraculoSimuladoClient().obter(dia(1), dia(3));
            var b = new OraculoSimuladoClient().obter(dia(1), dia(3));
            igual(3, a.size(), "quantidade");
            igual(a.get(1).getValor(), b.get(1).getValor(), "determinístico");
            verdadeiro(a.get(0).getValor().compareTo(BigDecimal.TEN) >= 0 && a.get(0).getValor().compareTo(new BigDecimal("15")) < 0, "faixa");
        });
    }

    /** Servidor local que atende uma conexão com a resposta dada e devolve o que recebeu. */
    private static NetworkRequest[] servir(ServerSocket servidor, NetworkResponse resposta, boolean mudo) {
        NetworkRequest[] recebida = new NetworkRequest[1];
        Thread t = new Thread(() -> {
            try (Socket s = servidor.accept()) {
                if (mudo) {
                    Thread.sleep(1500);
                    return;
                }
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                recebida[0] = (NetworkRequest) in.readObject();
                out.writeObject(resposta);
                out.flush();
            } catch (Exception e) {
                // o teste do cliente verifica o efeito; falha aqui aparece como erro lá
            }
        });
        t.setDaemon(true);
        t.start();
        return recebida;
    }

    private static NetworkResponse resposta(String erro, ExchangeRate... taxas) {
        return new NetworkResponse(new ArrayList<>(Arrays.asList(taxas)), erro);
    }

    private static void testarClienteSocket() {
        caso("Cliente da API: envia credenciais e datas e mapeia ExchangeRate em Cotacao", () -> {
            try (ServerSocket srv = new ServerSocket(0)) {
                NetworkRequest[] recebida = servir(srv, resposta(null, new ExchangeRate(dia(3), new BigDecimal("12.5")),
                        new ExchangeRate(dia(4), new BigDecimal("13.5"))), false);
                var cotacoes = new OraculoSocketClient("localhost", srv.getLocalPort(), "G1", "segredo", 2000).obter(dia(3), dia(4));
                igual(2, cotacoes.size(), "quantidade");
                igual(new BigDecimal("13.5"), cotacoes.get(1).getValor(), "valor");
                igual("G1", recebida[0].getGroupName(), "grupo enviado");
                igual(dia(3), recebida[0].getFirstDate(), "primeira data enviada");
            }
        });
        caso("Cliente da API: lista vazia vira lista vazia", () -> {
            try (ServerSocket srv = new ServerSocket(0)) {
                servir(srv, resposta(null), false);
                igual(0, new OraculoSocketClient("localhost", srv.getLocalPort(), "G", "S", 2000).obter(dia(1), dia(1)).size(), "vazia");
            }
        });
        caso("Cliente da API: errorMessage do servidor vira OraculoException", () -> {
            try (ServerSocket srv = new ServerSocket(0)) {
                servir(srv, resposta("credencial recusada"), false);
                OraculoException e = lanca(OraculoException.class,
                        () -> new OraculoSocketClient("localhost", srv.getLocalPort(), "G", "S", 2000).obter(dia(1), dia(1)), "erro");
                verdadeiro(e.getMessage().contains("credencial recusada"), "mensagem do servidor");
            }
        });
        caso("Cliente da API: conexão recusada vira OraculoException", () -> {
            int porta;
            try (ServerSocket srv = new ServerSocket(0)) {
                porta = srv.getLocalPort();
            }
            int p = porta;
            lanca(OraculoException.class, () -> new OraculoSocketClient("localhost", p, "G", "S", 500).obter(dia(1), dia(1)), "recusada");
        });
        caso("Cliente da API: servidor mudo estoura o timeout em vez de travar", () -> {
            try (ServerSocket srv = new ServerSocket(0)) {
                servir(srv, null, true);
                long inicio = System.nanoTime();
                lanca(OraculoException.class,
                        () -> new OraculoSocketClient("localhost", srv.getLocalPort(), "G", "S", 300).obter(dia(1), dia(1)), "timeout");
                verdadeiro((System.nanoTime() - inicio) / 1_000_000 < 1400, "o timeout deve ocorrer antes de 1,4 s");
            }
        });
    }

    private static void testarDaosMemoria() {
        caso("CarteiraMemoryDAO: CRUD e ordem por identificador", () -> {
            CarteiraDAO dao = new CarteiraMemoryDAO();
            dao.inserir(new Carteira(2, "Bia", "B"));
            dao.inserir(new Carteira(1, "Ana", "A"));
            igual(1, dao.listar().get(0).getId(), "ordem");
            dao.atualizar(new Carteira(1, "Ana Maria", "A"));
            igual("Ana Maria", dao.buscar(1).get().getTitular(), "atualizada");
            dao.excluir(1);
            verdadeiro(dao.buscar(1).isEmpty(), "excluída");
            lanca(ftcoin.dao.PersistenciaException.class, () -> dao.inserir(new Carteira(2, "X", "Y")), "duplicada");
        });
        caso("MovimentacaoMemoryDAO: filtra por carteira e gera identificadores crescentes", () -> {
            MovimentacaoDAO dao = new MovimentacaoMemoryDAO();
            igual(1, dao.proximoId(), "primeiro id");
            dao.inserir(new Movimentacao(1, 1, dia(2), TipoOperacao.COMPRA, BigDecimal.ONE));
            dao.inserir(new Movimentacao(2, 2, dia(2), TipoOperacao.COMPRA, BigDecimal.ONE));
            igual(1, dao.listarPorCarteira(1).size(), "só da carteira 1");
            igual(3, dao.proximoId(), "próximo id");
        });
    }

    private static void testarServicoDeCarteira() {
        caso("CarteiraService: dados inválidos e duplicidade", () -> {
            Cenario c = new Cenario();
            igual("erro.carteira.existe", lanca(NegocioException.class, () -> c.carteiras.incluir(1, "X", "Y"), "dup").getChave(), "dup");
            igual("erro.titular.vazio", lanca(NegocioException.class, () -> c.carteiras.incluir(2, " ", "Y"), "titular").getChave(), "titular");
            igual("erro.id.invalido", lanca(NegocioException.class, () -> c.carteiras.incluir(0, "X", "Y"), "id").getChave(), "id");
        });
        caso("CarteiraService: listagem por titular ignora maiúsculas", () -> {
            Cenario c = new Cenario();
            c.carteiras.incluir(2, "bruno", "B");
            c.carteiras.incluir(3, "Aline", "C");
            igual("Aline", c.carteiras.listarPorTitular().get(0).getTitular(), "primeiro");
            igual("bruno", c.carteiras.listarPorTitular().get(2).getTitular(), "último");
            igual(1, c.carteiras.listarPorId().get(0).getId(), "por id");
        });
        caso("CarteiraService: carteira com movimentações não pode ser excluída", () -> {
            Cenario c = new Cenario();
            c.movimentos.registrar(1, TipoOperacao.COMPRA, dia(1), BigDecimal.ONE);
            igual("erro.carteira.com.movimentos", lanca(NegocioException.class, () -> c.carteiras.excluir(1), "exclusão").getChave(), "chave");
            c.carteiras.incluir(5, "Livre", "Z");
            c.carteiras.excluir(5);
        });
    }

    private static void testarIdiomaEConfig() {
        caso("Idioma: os três arquivos têm exatamente as mesmas chaves", () -> {
            ConfiguracaoIdioma i = new ConfiguracaoIdioma(Idioma.PT);
            Set<String> pt = new TreeSet<>(i.chaves());
            i.definir(Idioma.EN);
            igual(pt, new TreeSet<>(i.chaves()), "EN");
            i.definir(Idioma.ES);
            igual(pt, new TreeSet<>(i.chaves()), "ES");
        });
        caso("Idioma: troca em execução, parâmetros e chave ausente", () -> {
            ConfiguracaoIdioma i = new ConfiguracaoIdioma(Idioma.PT);
            igual("Carteira 7 incluída.", i.texto("msg.carteira.incluida", 7), "PT");
            i.definir(Idioma.EN);
            igual("Wallet 7 added.", i.texto("msg.carteira.incluida", 7), "EN");
            igual("!nao.existe!", i.texto("nao.existe"), "ausente");
        });
        caso("Idioma: detecção automática pelo Locale, com português como padrão", () -> {
            igual(Idioma.EN, Idioma.detectar(java.util.Locale.US), "en");
            igual(Idioma.ES, Idioma.detectar(java.util.Locale.forLanguageTag("es-AR")), "es");
            igual(Idioma.PT, Idioma.detectar(java.util.Locale.JAPAN), "ja cai em pt");
        });
        caso("Configuração: padrões e leitura de valores", () -> {
            ConfigAplicacao padrao = new ConfigAplicacao(new Properties());
            igual(1234, padrao.oraculoPorta(), "porta padrão");
            verdadeiro(!padrao.oraculoSimulado(), "rede por padrão");
            Properties p = new Properties();
            p.setProperty("oraculo.modo", "simulado");
            p.setProperty("oraculo.porta", "9999");
            igual(9999, new ConfigAplicacao(p).oraculoPorta(), "porta lida");
            verdadeiro(new ConfigAplicacao(p).oraculoSimulado(), "simulado");
            p.setProperty("oraculo.porta", "abc");
            lanca(IllegalStateException.class, () -> new ConfigAplicacao(p).oraculoPorta(), "porta inválida");
        });
    }

    private static void testarPresenters() {
        caso("Presenters: fluxo completo com View falsa (incluir, comprar, vender, relatórios, sair)", () -> {
            Cenario c = new Cenario();
            c.cliente.valores.put(dia(1), new BigDecimal("10"));
            c.cliente.valores.put(dia(2), new BigDecimal("12"));
            c.cliente.valores.put(LocalDate.of(2026, 11, 8), new BigDecimal("13"));
            ViewFalsa v = new ViewFalsa();
            ConfiguracaoIdioma i = new ConfiguracaoIdioma(Idioma.PT);
            boolean[] saiu = {false};
            CarteiraPresenter cp = new CarteiraPresenter(v, c.carteiras, i);
            MenuPrincipalPresenter p = new MenuPrincipalPresenter(v, i, cp, c.carteiras, c.movimentos, c.oraculo, RELOGIO, () -> saiu[0] = true);

            v.dadosCarteira = new CarteiraDados("2", "Bruno", "Corretora B");
            p.onIncluirCarteira();
            igual("Carteira 2 incluída.", v.mensagem, "inclusão");
            p.onIncluirCarteira();
            igual("Já existe a carteira 2.", v.erro, "duplicada");
            v.dadosCarteira = new CarteiraDados("abc", "X", "Y");
            p.onIncluirCarteira();
            igual("O identificador deve ser um número inteiro.", v.erro, "id texto");

            v.movimentacao = new DadosMovimentacao("1", "2026-01-01", "4,5");
            p.onComprar();
            verdadeiro(v.mensagem.startsWith("Movimentação registrada: Compra de 4,5"), "compra: " + v.mensagem);
            v.movimentacao = new DadosMovimentacao("1", "02/01/2026", "1");
            p.onVender();
            verdadeiro(v.mensagem.contains("Venda"), "venda");
            v.movimentacao = new DadosMovimentacao("1", "02/01/2026", "100");
            p.onVender();
            verdadeiro(v.erro.startsWith("Saldo insuficiente"), "venda grande: " + v.erro);

            v.idCarteira = "1";
            p.onHistorico();
            verdadeiro(v.texto.contains("2026-01-01  Compra  4,5") && v.texto.contains("2026-01-02  Venda  1"), "histórico: " + v.texto);
            p.onSaldo();
            verdadeiro(v.texto.contains("3,5"), "saldo 3,5: " + v.texto);
            p.onGanhoPerda();
            verdadeiro(v.texto.contains("Resultado a custo médio: R$ 2,00"), "resultado realizado (1 x (12 - 10)): " + v.texto);
            verdadeiro(v.texto.contains("Resultado a mercado"), "mercado");
            p.onListarPorTitular();
            verdadeiro(v.texto.indexOf("Bruno") > v.texto.indexOf("Ana"), "ordem por titular");

            v.data = "2026-01-02";
            p.onCotar();
            igual("Cotação de 2026-01-02: R$ 12,00", v.mensagem, "cotação");
            v.data = "31/02/2026";
            p.onCotar();
            verdadeiro(v.erro.startsWith("Data inválida"), "data inválida");

            p.onIdioma(Idioma.ES);
            igual(1, v.atualizacoes, "atualização de textos");
            p.onAjuda();
            verdadeiro(v.texto.startsWith("FTCoin controla"), "ajuda em espanhol");
            p.onSair();
            verdadeiro(saiu[0] && v.encerrada, "saída fecha recursos e janela");
        });
        caso("Presenter: rede fora mostra mensagem traduzida, sem derrubar o programa", () -> {
            Cenario c = new Cenario();
            c.cliente.redeFora = true;
            ViewFalsa v = new ViewFalsa();
            ConfiguracaoIdioma i = new ConfiguracaoIdioma(Idioma.EN);
            MenuPrincipalPresenter p = new MenuPrincipalPresenter(v, i, new CarteiraPresenter(v, c.carteiras, i), c.carteiras,
                    c.movimentos, c.oraculo, RELOGIO, () -> { });
            v.data = "2026-01-02";
            p.onCotar();
            verdadeiro(v.erro.startsWith("Quote unavailable for 2026-01-02"), "mensagem: " + v.erro);
        });
        caso("CarteiraPresenter: editar e excluir (com e sem confirmação)", () -> {
            Cenario c = new Cenario();
            ViewFalsa v = new ViewFalsa();
            ConfiguracaoIdioma i = new ConfiguracaoIdioma(Idioma.PT);
            CarteiraPresenter cp = new CarteiraPresenter(v, c.carteiras, i);
            v.idCarteira = "1";
            v.dadosCarteira = new CarteiraDados("1", "Ana Maria", "Nova");
            cp.editar();
            igual("Ana Maria", c.carteiras.consultar(1).getTitular(), "editada");
            v.confirma = false;
            cp.excluir();
            igual(1, c.carteiras.listarPorId().size(), "continua existindo");
            v.confirma = true;
            cp.excluir();
            igual(0, c.carteiras.listarPorId().size(), "excluída");
            cp.consultar();
            igual("A carteira 1 não existe.", v.erro, "inexistente");
        });
    }
}

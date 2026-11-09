package ftcoin.presenter;

import ftcoin.i18n.ConfiguracaoIdioma;
import ftcoin.model.Carteira;
import ftcoin.model.Idioma;
import ftcoin.model.Movimentacao;
import ftcoin.model.TipoOperacao;
import ftcoin.service.CarteiraService;
import ftcoin.service.MovimentacaoService;
import ftcoin.service.OraculoService;
import ftcoin.service.ResultadoFinanceiroDTO;
import ftcoin.view.DadosMovimentacao;
import ftcoin.view.MenuPrincipalView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Presenter da janela principal: movimentações, relatórios, ajuda, idioma e saída. */
public final class MenuPrincipalPresenter {
    private final MenuPrincipalView view;
    private final ConfiguracaoIdioma idioma;
    private final CarteiraPresenter carteiraPresenter;
    private final CarteiraService carteiraService;
    private final MovimentacaoService movimentacaoService;
    private final OraculoService oraculo;
    private final Clock relogio;
    private final Runnable aoSair;

    /**
     * @param aoSair ação executada ao sair, antes de fechar a janela (fecha conexões e o log)
     */
    public MenuPrincipalPresenter(MenuPrincipalView view, ConfiguracaoIdioma idioma,
            CarteiraPresenter carteiraPresenter, CarteiraService carteiraService,
            MovimentacaoService movimentacaoService, OraculoService oraculo, Clock relogio, Runnable aoSair) {
        this.view = view;
        this.idioma = idioma;
        this.carteiraPresenter = carteiraPresenter;
        this.carteiraService = carteiraService;
        this.movimentacaoService = movimentacaoService;
        this.oraculo = oraculo;
        this.relogio = relogio;
        this.aoSair = aoSair;
    }

    public void onIncluirCarteira() {
        carteiraPresenter.incluir();
    }

    public void onConsultarCarteira() {
        carteiraPresenter.consultar();
    }

    public void onEditarCarteira() {
        carteiraPresenter.editar();
    }

    public void onExcluirCarteira() {
        carteiraPresenter.excluir();
    }

    public void onComprar() {
        registrar(TipoOperacao.COMPRA, "dlg.comprar");
    }

    public void onVender() {
        registrar(TipoOperacao.VENDA, "dlg.vender");
    }

    /** Mostra a cotação de uma data (padrão: hoje). */
    public void onCotar() {
        executar(() -> {
            String texto = view.solicitarData(idioma.texto("dlg.cotar"), LocalDate.now(relogio).toString());
            if (texto != null) {
                LocalDate data = Entradas.data(texto);
                view.exibirMensagem(idioma.texto("msg.cotacao", data, dinheiro(oraculo.cotar(data).getValor())));
            }
        });
    }

    /** Relatório 1: carteiras por identificador. */
    public void onListarPorId() {
        executar(() -> listar("rel.porid", carteiraService.listarPorId()));
    }

    /** Relatório 2: carteiras por nome do titular. */
    public void onListarPorTitular() {
        executar(() -> listar("rel.portitular", carteiraService.listarPorTitular()));
    }

    /** Relatório 3: saldo atual de uma carteira. */
    public void onSaldo() {
        executar(() -> {
            String id = view.solicitarIdCarteira(idioma.texto("dlg.relatorio.carteira"));
            if (id != null) {
                int idCarteira = Entradas.id(id);
                carteiraService.consultar(idCarteira);
                ResultadoFinanceiroDTO r = movimentacaoService.apurar(idCarteira);
                StringBuilder texto = new StringBuilder();
                linha(texto, "rel.saldo.qtd", quantidade(r.getQuantidadeSaldo()));
                linha(texto, "rel.custo.medio", dinheiro(r.getCustoMedio()));
                linha(texto, "rel.saldo.custo", dinheiro(r.getSaldoCustoMedio()));
                if (r.getCotacaoMercado() != null) {
                    linha(texto, "rel.cotacao.mercado", dinheiro(r.getCotacaoMercado()));
                }
                linha(texto, "rel.saldo.mercado", dinheiro(r.getSaldoMercado()));
                view.exibirTexto(idioma.texto("rel.saldo", idCarteira), texto.toString());
            }
        });
    }

    /** Relatório 4: histórico de movimentação de uma carteira. */
    public void onHistorico() {
        executar(() -> {
            String id = view.solicitarIdCarteira(idioma.texto("dlg.relatorio.carteira"));
            if (id != null) {
                int idCarteira = Entradas.id(id);
                List<Movimentacao> historico = movimentacaoService.historico(idCarteira);
                StringBuilder texto = new StringBuilder();
                for (Movimentacao m : historico) {
                    String tipo = idioma.texto(m.getTipo() == TipoOperacao.COMPRA ? "tipo.compra" : "tipo.venda");
                    texto.append(m.getData()).append("  ").append(tipo).append("  ")
                            .append(quantidade(m.getQuantidade())).append('\n');
                }
                if (historico.isEmpty()) {
                    texto.append(idioma.texto("rel.sem.movimentos"));
                }
                view.exibirTexto(idioma.texto("rel.historico", idCarteira), texto.toString());
            }
        });
    }

    /** Relatório 5: ganho ou perda de cada carteira, a custo médio e a mercado. */
    public void onGanhoPerda() {
        executar(() -> {
            StringBuilder texto = new StringBuilder();
            List<Carteira> carteiras = carteiraService.listarPorId();
            for (Carteira c : carteiras) {
                texto.append(c).append('\n');
                try {
                    ResultadoFinanceiroDTO r = movimentacaoService.apurar(c.getId());
                    linha(texto, "rel.comprado", dinheiro(r.getTotalComprado()));
                    linha(texto, "rel.vendido", dinheiro(r.getTotalVendido()));
                    linha(texto, "rel.res.custo", dinheiro(r.getResultadoCustoMedio()) + "  (" + percentual(r.getPercentualCustoMedio()) + ")");
                    linha(texto, "rel.res.mercado", dinheiro(r.getResultadoMercado()) + "  (" + percentual(r.getPercentualMercado()) + ")");
                } catch (RuntimeException e) {
                    texto.append("    ").append(TratadorErros.mensagem(e, idioma)).append('\n');
                }
                texto.append('\n');
            }
            if (carteiras.isEmpty()) {
                texto.append(idioma.texto("rel.sem.carteiras"));
            }
            view.exibirTexto(idioma.texto("rel.ganhoperda"), texto.toString());
        });
    }

    public void onAjuda() {
        view.exibirTexto(idioma.texto("ajuda.titulo"), idioma.texto("ajuda.texto"));
    }

    public void onCreditos() {
        view.exibirTexto(idioma.texto("creditos.titulo"), idioma.texto("creditos.texto"));
    }

    /** Troca o idioma e refaz os textos da tela. */
    public void onIdioma(Idioma novo) {
        idioma.definir(novo);
        view.atualizarTextos();
    }

    /** Saída: registra o resumo do oráculo, fecha conexões e a janela. */
    public void onSair() {
        oraculo.registrarResumo();
        aoSair.run();
        view.encerrar();
    }

    private void registrar(TipoOperacao tipo, String chaveTitulo) {
        executar(() -> {
            DadosMovimentacao dados = view.solicitarMovimentacao(idioma.texto(chaveTitulo), LocalDate.now(relogio).toString());
            if (dados != null) {
                Movimentacao m = movimentacaoService.registrar(Entradas.id(dados.getIdCarteira()), tipo,
                        Entradas.data(dados.getData()), Entradas.quantidade(dados.getQuantidade()));
                String nome = idioma.texto(tipo == TipoOperacao.COMPRA ? "tipo.compra" : "tipo.venda");
                view.exibirMensagem(idioma.texto("msg.movimento.registrado", nome, quantidade(m.getQuantidade()), m.getData()));
            }
        });
    }

    private void listar(String chaveTitulo, List<Carteira> carteiras) {
        StringBuilder texto = new StringBuilder();
        for (Carteira c : carteiras) {
            texto.append(c).append('\n');
        }
        if (carteiras.isEmpty()) {
            texto.append(idioma.texto("rel.sem.carteiras"));
        }
        view.exibirTexto(idioma.texto(chaveTitulo), texto.toString());
    }

    private void linha(StringBuilder texto, String chave, String valor) {
        texto.append("    ").append(idioma.texto(chave)).append(": ").append(valor).append('\n');
    }

    private String dinheiro(BigDecimal valor) {
        return String.format(idioma.getLocale(), "R$ %,.2f", valor.setScale(2, RoundingMode.HALF_UP));
    }

    private String percentual(BigDecimal valor) {
        return String.format(idioma.getLocale(), "%.2f%%", valor.setScale(2, RoundingMode.HALF_UP));
    }

    private String quantidade(BigDecimal valor) {
        String plano = valor.signum() == 0 ? "0" : valor.stripTrailingZeros().toPlainString();
        char separador = java.text.DecimalFormatSymbols.getInstance(idioma.getLocale()).getDecimalSeparator();
        return plano.replace('.', separador);
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException e) {
            view.exibirErro(TratadorErros.mensagem(e, idioma));
        }
    }
}

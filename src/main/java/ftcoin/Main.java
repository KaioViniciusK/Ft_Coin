package ftcoin;

import java.time.Clock;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import ftcoin.config.ConfigAplicacao;
import ftcoin.dao.CarteiraDAO;
import ftcoin.dao.DAOFactory;
import ftcoin.dao.MovimentacaoDAO;
import ftcoin.i18n.ConfiguracaoIdioma;
import ftcoin.log.LogConfig;
import ftcoin.model.FonteDados;
import ftcoin.model.Idioma;
import ftcoin.oraculo.OraculoClient;
import ftcoin.oraculo.OraculoSimuladoClient;
import ftcoin.oraculo.OraculoSocketClient;
import ftcoin.presenter.CarteiraPresenter;
import ftcoin.presenter.MenuPrincipalPresenter;
import ftcoin.service.CarteiraService;
import ftcoin.service.MovimentacaoService;
import ftcoin.service.OraculoService;
import ftcoin.view.CarteiraSwingView;
import ftcoin.view.MenuPrincipalSwingView;

public final class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private Main() {
    }

    public static void main(String[] args) {
        ConfigAplicacao config;
        FonteDados fonte;
        try {
            config = ConfigAplicacao.carregar();
            fonte = FonteDados.deArgumento(args.length > 0 ? args[0] : null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Erro de configuração: " + e.getMessage());
            System.err.println("Uso: java ftcoin.Main [memoria|mariadb] [pt|en|es]");
            return;
        }
        LogConfig.iniciar(config);
        LOG.log(java.util.logging.Level.INFO, "Fonte de dados: {0}", fonte);
        ConfiguracaoIdioma idioma = args.length > 1
                ? new ConfiguracaoIdioma(Idioma.deCodigo(args[1])) : ConfiguracaoIdioma.automatica();

        DAOFactory fabrica = DAOFactory.obter(fonte, config);
        CarteiraDAO carteiraDAO = fabrica.criarCarteiraDAO();
        MovimentacaoDAO movimentacaoDAO = fabrica.criarMovimentacaoDAO();
        OraculoClient cliente;
        if (config.oraculoSimulado()) {
            LOG.warning("Cotações SIMULADAS: não representam valores reais");
            cliente = new OraculoSimuladoClient();
        } else {
            cliente = new OraculoSocketClient(config.oraculoHost(), config.oraculoPorta(), config.oraculoGrupo(),
                    config.oraculoSenha(), config.oraculoTimeoutMs());
        }
        OraculoService oraculo = new OraculoService(cliente);
        Clock relogio = Clock.systemDefaultZone();
        CarteiraService carteiraService = new CarteiraService(carteiraDAO, movimentacaoDAO);
        MovimentacaoService movimentacaoService = new MovimentacaoService(movimentacaoDAO, carteiraDAO, oraculo, relogio);

        SwingUtilities.invokeLater(() -> {
            MenuPrincipalSwingView view = new MenuPrincipalSwingView(idioma);
            CarteiraPresenter carteiraPresenter =
                    new CarteiraPresenter(new CarteiraSwingView(view.getJanela(), idioma), carteiraService, idioma);
            MenuPrincipalPresenter presenter = new MenuPrincipalPresenter(view, idioma, carteiraPresenter,
                    carteiraService, movimentacaoService, oraculo, relogio, () -> {
                        fabrica.fechar();
                        LogConfig.encerrar();
                    });
            view.iniciar(presenter);
        });
    }
}

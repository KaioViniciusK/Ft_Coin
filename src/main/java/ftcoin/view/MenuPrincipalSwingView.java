package ftcoin.view;

import ftcoin.i18n.ConfiguracaoIdioma;
import ftcoin.model.Idioma;
import ftcoin.presenter.MenuPrincipalPresenter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Janela principal em Swing: barra de menus com submenus, área de texto para relatórios e barra de
 * status colorida (verde para sucesso, vermelho para erro). Apenas encaminha ações ao Presenter.
 */
public final class MenuPrincipalSwingView implements MenuPrincipalView {
    private static final Color VERDE = new Color(0x1B7F3B);
    private static final Color VERMELHO = new Color(0xB3261E);

    private final ConfiguracaoIdioma idioma;
    private final JFrame janela = new JFrame();
    private final JLabel titulo = new JLabel(" ");
    private final JTextArea area = new JTextArea();
    private final JLabel status = new JLabel(" ");
    private MenuPrincipalPresenter presenter;

    public MenuPrincipalSwingView(ConfiguracaoIdioma idioma) {
        this.idioma = idioma;
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        status.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        janela.add(titulo, BorderLayout.NORTH);
        janela.add(new JScrollPane(area), BorderLayout.CENTER);
        janela.add(status, BorderLayout.SOUTH);
        janela.setSize(new Dimension(760, 520));
        janela.setLocationRelativeTo(null);
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                presenter.onSair();
            }
        });
    }

    /** @return a janela, usada como pai dos diálogos de outras Views */
    public JFrame getJanela() {
        return janela;
    }

    /** Liga o Presenter, monta os menus e mostra a janela. */
    public void iniciar(MenuPrincipalPresenter novoPresenter) {
        this.presenter = novoPresenter;
        atualizarTextos();
        janela.setVisible(true);
    }

    @Override
    public void atualizarTextos() {
        janela.setTitle(idioma.texto("app.titulo"));
        janela.setJMenuBar(construirMenus());
        status.setForeground(Color.DARK_GRAY);
        status.setText(idioma.texto("status.pronto"));
        janela.revalidate();
    }

    private JMenuBar construirMenus() {
        JMenuBar barra = new JMenuBar();
        JMenu carteira = new JMenu(idioma.texto("menu.carteira"));
        carteira.add(item("menu.carteira.incluir", presenter::onIncluirCarteira));
        carteira.add(item("menu.carteira.consultar", presenter::onConsultarCarteira));
        carteira.add(item("menu.carteira.editar", presenter::onEditarCarteira));
        carteira.add(item("menu.carteira.excluir", presenter::onExcluirCarteira));
        JMenu movimentacao = new JMenu(idioma.texto("menu.movimentacao"));
        movimentacao.add(item("menu.movimentacao.comprar", presenter::onComprar));
        movimentacao.add(item("menu.movimentacao.vender", presenter::onVender));
        movimentacao.add(item("menu.movimentacao.cotar", presenter::onCotar));
        JMenu relatorios = new JMenu(idioma.texto("menu.relatorios"));
        relatorios.add(item("menu.relatorios.porid", presenter::onListarPorId));
        relatorios.add(item("menu.relatorios.portitular", presenter::onListarPorTitular));
        relatorios.add(item("menu.relatorios.saldo", presenter::onSaldo));
        relatorios.add(item("menu.relatorios.historico", presenter::onHistorico));
        relatorios.add(item("menu.relatorios.ganhoperda", presenter::onGanhoPerda));
        JMenu ajuda = new JMenu(idioma.texto("menu.ajuda"));
        ajuda.add(item("menu.ajuda.texto", presenter::onAjuda));
        ajuda.add(item("menu.ajuda.creditos", presenter::onCreditos));
        JMenu menuIdioma = new JMenu(idioma.texto("menu.idioma"));
        ButtonGroup grupo = new ButtonGroup();
        for (Idioma opcao : Idioma.values()) {
            JRadioButtonMenuItem botao = new JRadioButtonMenuItem(opcao.name(), opcao == idioma.getIdioma());
            botao.addActionListener(e -> presenter.onIdioma(opcao));
            grupo.add(botao);
            menuIdioma.add(botao);
        }
        JMenu sair = new JMenu(idioma.texto("menu.sair"));
        sair.add(item("menu.sair", presenter::onSair));
        barra.add(carteira);
        barra.add(movimentacao);
        barra.add(relatorios);
        barra.add(ajuda);
        barra.add(menuIdioma);
        barra.add(sair);
        return barra;
    }

    private JMenuItem item(String chave, Runnable acao) {
        JMenuItem item = new JMenuItem(idioma.texto(chave));
        item.addActionListener(e -> acao.run());
        return item;
    }

    @Override
    public String solicitarIdCarteira(String tituloDialogo) {
        return JOptionPane.showInputDialog(janela, idioma.texto("campo.carteira"), tituloDialogo, JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public DadosMovimentacao solicitarMovimentacao(String tituloDialogo, String dataPadrao) {
        JTextField carteira = new JTextField();
        JTextField data = new JTextField(dataPadrao);
        JTextField quantidade = new JTextField();
        JPanel painel = new JPanel(new GridLayout(0, 2, 6, 6));
        painel.add(new JLabel(idioma.texto("campo.carteira")));
        painel.add(carteira);
        painel.add(new JLabel(idioma.texto("campo.data")));
        painel.add(data);
        painel.add(new JLabel(idioma.texto("campo.quantidade")));
        painel.add(quantidade);
        int opcao = JOptionPane.showConfirmDialog(janela, painel, tituloDialogo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return opcao == JOptionPane.OK_OPTION
                ? new DadosMovimentacao(carteira.getText(), data.getText(), quantidade.getText()) : null;
    }

    @Override
    public String solicitarData(String tituloDialogo, String dataPadrao) {
        return (String) JOptionPane.showInputDialog(janela, idioma.texto("campo.data"), tituloDialogo,
                JOptionPane.PLAIN_MESSAGE, null, null, dataPadrao);
    }

    @Override
    public void exibirTexto(String tituloTexto, String corpo) {
        titulo.setText(tituloTexto);
        area.setText(corpo);
        area.setCaretPosition(0);
        status.setForeground(Color.DARK_GRAY);
        status.setText(idioma.texto("status.pronto"));
    }

    @Override
    public void exibirMensagem(String texto) {
        status.setForeground(VERDE);
        status.setText(texto);
    }

    @Override
    public void exibirErro(String texto) {
        status.setForeground(VERMELHO);
        status.setText(texto);
        JOptionPane.showMessageDialog(janela, texto, idioma.texto("dlg.erro"), JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void encerrar() {
        janela.dispose();
    }
}

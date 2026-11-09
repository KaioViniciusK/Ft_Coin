package ftcoin.view;

import ftcoin.i18n.ConfiguracaoIdioma;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Implementação Swing de {@link CarteiraView}, baseada em diálogos. Não contém regra de negócio. */
public final class CarteiraSwingView implements CarteiraView {
    private final Component pai;
    private final ConfiguracaoIdioma idioma;

    public CarteiraSwingView(Component pai, ConfiguracaoIdioma idioma) {
        this.pai = pai;
        this.idioma = idioma;
    }

    @Override
    public CarteiraDados solicitarCarteira(String titulo, CarteiraDados inicial, boolean idEditavel) {
        JTextField id = new JTextField(inicial == null ? "" : inicial.getId());
        JTextField titular = new JTextField(inicial == null ? "" : inicial.getTitular());
        JTextField corretora = new JTextField(inicial == null ? "" : inicial.getCorretora());
        id.setEditable(idEditavel);
        JPanel painel = new JPanel(new GridLayout(0, 2, 6, 6));
        painel.add(new JLabel(idioma.texto("campo.id")));
        painel.add(id);
        painel.add(new JLabel(idioma.texto("campo.titular")));
        painel.add(titular);
        painel.add(new JLabel(idioma.texto("campo.corretora")));
        painel.add(corretora);
        int opcao = JOptionPane.showConfirmDialog(pai, painel, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return opcao == JOptionPane.OK_OPTION
                ? new CarteiraDados(id.getText(), titular.getText(), corretora.getText()) : null;
    }

    @Override
    public String solicitarId(String titulo) {
        return JOptionPane.showInputDialog(pai, idioma.texto("campo.carteira"), titulo, JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public boolean confirmar(String titulo, String pergunta) {
        return JOptionPane.showConfirmDialog(pai, pergunta, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public void exibirMensagem(String texto) {
        JOptionPane.showMessageDialog(pai, texto, idioma.texto("dlg.informacao"), JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void exibirErro(String texto) {
        JOptionPane.showMessageDialog(pai, texto, idioma.texto("dlg.erro"), JOptionPane.ERROR_MESSAGE);
    }
}

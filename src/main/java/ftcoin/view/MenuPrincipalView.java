package ftcoin.view;

/** Contrato da janela principal; o Presenter só conhece esta interface, nunca o Swing. */
public interface MenuPrincipalView {
    /** @return o identificador de carteira digitado, ou null se o usuário cancelou */
    String solicitarIdCarteira(String titulo);

    /** @return os dados da compra ou venda, ou null se o usuário cancelou */
    DadosMovimentacao solicitarMovimentacao(String titulo, String dataPadrao);

    /** @return a data digitada, ou null se o usuário cancelou */
    String solicitarData(String titulo, String dataPadrao);

    /** Mostra um texto longo (relatório, ajuda ou créditos) na área principal. */
    void exibirTexto(String titulo, String corpo);

    /** Mostra uma informação ao usuário. */
    void exibirMensagem(String texto);

    /** Mostra uma mensagem de erro ao usuário. */
    void exibirErro(String texto);

    /** Refaz menus e textos após troca de idioma. */
    void atualizarTextos();

    /** Fecha a janela. */
    void encerrar();
}

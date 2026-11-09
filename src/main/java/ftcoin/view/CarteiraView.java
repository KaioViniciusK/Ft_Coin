package ftcoin.view;

/** Contrato da tela de carteiras; o Presenter só conhece esta interface, nunca o Swing. */
public interface CarteiraView {
    /**
     * @param titulo      título do formulário
     * @param inicial     valores iniciais, ou null para formulário vazio
     * @param idEditavel  false para bloquear o identificador (edição)
     * @return os dados digitados, ou null se o usuário cancelou
     */
    CarteiraDados solicitarCarteira(String titulo, CarteiraDados inicial, boolean idEditavel);

    /** @return o identificador digitado, ou null se o usuário cancelou */
    String solicitarId(String titulo);

    /** @return true se o usuário confirmou a pergunta */
    boolean confirmar(String titulo, String pergunta);

    /** Mostra uma informação ao usuário. */
    void exibirMensagem(String texto);

    /** Mostra uma mensagem de erro ao usuário. */
    void exibirErro(String texto);
}

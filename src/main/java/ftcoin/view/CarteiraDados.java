package ftcoin.view;

/** Texto digitado pelo usuário no formulário de carteira; a validação é feita pelo Presenter e pelo Service. */
public final class CarteiraDados {
    private final String id;
    private final String titular;
    private final String corretora;

    public CarteiraDados(String id, String titular, String corretora) {
        this.id = id;
        this.titular = titular;
        this.corretora = corretora;
    }

    public String getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public String getCorretora() {
        return corretora;
    }
}

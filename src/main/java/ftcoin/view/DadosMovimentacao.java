package ftcoin.view;

/** Texto digitado pelo usuário no formulário de compra ou venda. */
public final class DadosMovimentacao {
    private final String idCarteira;
    private final String data;
    private final String quantidade;

    public DadosMovimentacao(String idCarteira, String data, String quantidade) {
        this.idCarteira = idCarteira;
        this.data = data;
        this.quantidade = quantidade;
    }

    public String getIdCarteira() {
        return idCarteira;
    }

    public String getData() {
        return data;
    }

    public String getQuantidade() {
        return quantidade;
    }
}

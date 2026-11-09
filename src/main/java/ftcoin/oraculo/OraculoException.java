package ftcoin.oraculo;

/** Falha ao consultar o servidor de cotações (rede, protocolo ou erro informado pelo servidor). */
public class OraculoException extends Exception {
    private static final long serialVersionUID = 1L;

    public OraculoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public OraculoException(String mensagem) {
        super(mensagem);
    }
}

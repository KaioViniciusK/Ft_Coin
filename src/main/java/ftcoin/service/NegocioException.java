package ftcoin.service;

/**
 * Violação de regra de negócio ou entrada inválida. Carrega a chave da mensagem
 * (internacionalizada na camada de apresentação) e seus argumentos.
 */
public class NegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String chave;
    private final transient Object[] argumentos;

    /**
     * @param chave      chave da mensagem nos arquivos de recursos
     * @param argumentos valores para os parâmetros da mensagem
     */
    public NegocioException(String chave, Object... argumentos) {
        super(chave);
        this.chave = chave;
        this.argumentos = argumentos;
    }

    /**
     * @param causa      exceção original, preservada para o log
     * @param chave      chave da mensagem nos arquivos de recursos
     * @param argumentos valores para os parâmetros da mensagem
     */
    protected NegocioException(Throwable causa, String chave, Object... argumentos) {
        super(chave, causa);
        this.chave = chave;
        this.argumentos = argumentos;
    }

    public String getChave() {
        return chave;
    }

    public Object[] getArgumentos() {
        return argumentos == null ? new Object[0] : argumentos.clone();
    }
}

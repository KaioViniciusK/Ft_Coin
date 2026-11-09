package ftcoin.dao;

/** Falha de acesso aos dados; encapsula a exceção técnica (por exemplo SQLException) como causa. */
public class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

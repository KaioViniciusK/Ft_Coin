package ftcoin.dao;

/** Fábrica dos DAOs em memória. */
public final class MemoryDAOFactory extends DAOFactory {
    @Override
    public CarteiraDAO criarCarteiraDAO() {
        return new CarteiraMemoryDAO();
    }

    @Override
    public MovimentacaoDAO criarMovimentacaoDAO() {
        return new MovimentacaoMemoryDAO();
    }
}

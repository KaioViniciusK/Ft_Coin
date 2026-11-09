package ftcoin.dao;

import ftcoin.config.ConfigAplicacao;

/** Fábrica dos DAOs do MariaDB; todos usam a conexão do Singleton {@link ConexaoBD}. */
public final class MariaDBDAOFactory extends DAOFactory {
    /** @param config fornece URL, usuário e senha ao Singleton */
    public MariaDBDAOFactory(ConfigAplicacao config) {
        ConexaoBD.configurar(config);
    }

    @Override
    public CarteiraDAO criarCarteiraDAO() {
        return new CarteiraMariaDBDAO();
    }

    @Override
    public MovimentacaoDAO criarMovimentacaoDAO() {
        return new MovimentacaoMariaDBDAO();
    }

    @Override
    public void fechar() {
        ConexaoBD.getInstance().fechar();
    }
}

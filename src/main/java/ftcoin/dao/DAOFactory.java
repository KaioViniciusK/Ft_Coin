package ftcoin.dao;

import ftcoin.config.ConfigAplicacao;
import ftcoin.model.FonteDados;

/** Abstract Factory que cria o par de DAOs de uma fonte de dados e isola o resto do sistema dessa escolha. */
public abstract class DAOFactory {
    /**
     * @param fonte  fonte de dados escolhida na partida
     * @param config configuração (usada pela fábrica do MariaDB)
     * @return a fábrica da fonte
     */
    public static DAOFactory obter(FonteDados fonte, ConfigAplicacao config) {
        switch (fonte) {
            case MARIADB:
                return new MariaDBDAOFactory(config);
            case MEMORIA:
            default:
                return new MemoryDAOFactory();
        }
    }

    /** @return um DAO de carteiras desta fonte */
    public abstract CarteiraDAO criarCarteiraDAO();

    /** @return um DAO de movimentações desta fonte */
    public abstract MovimentacaoDAO criarMovimentacaoDAO();

    /** Libera os recursos da fonte (conexões). Chamado ao sair; por padrão não faz nada. */
    public void fechar() {
    }
}

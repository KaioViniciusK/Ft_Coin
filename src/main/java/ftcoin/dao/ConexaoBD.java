package ftcoin.dao;

import ftcoin.config.ConfigAplicacao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton que mantém a única conexão com o MariaDB. A conexão é aberta sob demanda,
 * reaberta se estiver fechada e encerrada por {@link #fechar()} na saída do programa.
 */
public final class ConexaoBD {
    private static final Logger LOG = Logger.getLogger(ConexaoBD.class.getName());
    private static ConexaoBD instancia;
    private static ConfigAplicacao configuracao;

    private Connection conexao;

    private ConexaoBD() {
    }

    /** Define de onde vêm URL, usuário e senha; deve ser chamado antes de {@link #getInstance()}. */
    public static synchronized void configurar(ConfigAplicacao config) {
        configuracao = config;
    }

    /** @return a instância única */
    public static synchronized ConexaoBD getInstance() {
        if (instancia == null) {
            instancia = new ConexaoBD();
        }
        return instancia;
    }

    /**
     * @return a conexão aberta, criando-a ou reabrindo-a se necessário
     * @throws PersistenciaException se a conexão falhar ou a configuração não tiver sido definida
     */
    public synchronized Connection getConexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                if (configuracao == null) {
                    throw new IllegalStateException("ConexaoBD não configurada");
                }
                long inicio = System.nanoTime();
                conexao = DriverManager.getConnection(configuracao.bdUrl(), configuracao.bdUsuario(), configuracao.bdSenha());
                LOG.log(Level.INFO, "Conexão MariaDB aberta em {0} ms", (System.nanoTime() - inicio) / 1_000_000);
            }
            return conexao;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao conectar ao MariaDB", e);
            throw new PersistenciaException("Falha ao conectar ao banco de dados", e);
        }
    }

    /** Fecha a conexão, se aberta. Erros ao fechar são registrados, mas não interrompem a saída. */
    public synchronized void fechar() {
        if (conexao != null) {
            try {
                conexao.close();
                LOG.info("Conexão MariaDB fechada");
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Erro ao fechar a conexão MariaDB", e);
            } finally {
                conexao = null;
            }
        }
    }
}

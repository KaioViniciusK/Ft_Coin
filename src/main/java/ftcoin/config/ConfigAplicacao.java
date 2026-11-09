package ftcoin.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuração externa da aplicação (arquivo ftcoin.properties). Credenciais do grupo e do banco
 * ficam aqui, fora do código-fonte e nunca são escritas no log.
 */
public final class ConfigAplicacao {
    /** Propriedade de sistema que indica um arquivo de configuração alternativo. */
    public static final String PROPRIEDADE_ARQUIVO = "ftcoin.config";
    private static final String ARQUIVO_PADRAO = "ftcoin.properties";

    private final Properties propriedades;

    /** @param propriedades valores já carregados; chaves ausentes assumem o valor padrão */
    public ConfigAplicacao(Properties propriedades) {
        this.propriedades = propriedades;
    }

    /**
     * Carrega o arquivo de configuração, se existir; sem arquivo, usa só os valores padrão.
     *
     * @return a configuração
     * @throws IllegalStateException se o arquivo existir mas não puder ser lido
     */
    public static ConfigAplicacao carregar() {
        Path arquivo = Paths.get(System.getProperty(PROPRIEDADE_ARQUIVO, ARQUIVO_PADRAO));
        Properties propriedades = new Properties();
        if (Files.isRegularFile(arquivo)) {
            try (InputStream entrada = Files.newInputStream(arquivo)) {
                propriedades.load(entrada);
            } catch (IOException e) {
                throw new IllegalStateException("Não foi possível ler " + arquivo, e);
            }
        }
        return new ConfigAplicacao(propriedades);
    }

    private String texto(String chave, String padrao) {
        return propriedades.getProperty(chave, padrao).trim();
    }

    private int inteiro(String chave, int padrao) {
        String valor = texto(chave, String.valueOf(padrao));
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Valor inválido para " + chave + ": " + valor, e);
        }
    }

    /** @return true se as cotações devem vir do gerador local, e não da API (oraculo.modo=simulado) */
    public boolean oraculoSimulado() {
        return "simulado".equalsIgnoreCase(texto("oraculo.modo", "rede"));
    }

    public String oraculoHost() {
        return texto("oraculo.host", "localhost");
    }

    public int oraculoPorta() {
        return inteiro("oraculo.porta", 1234);
    }

    public int oraculoTimeoutMs() {
        return inteiro("oraculo.timeout.ms", 5000);
    }

    public String oraculoGrupo() {
        return texto("oraculo.grupo", "GROUP");
    }

    public String oraculoSenha() {
        return texto("oraculo.senha", "PASSWORD");
    }

    public String bdUrl() {
        return texto("bd.url", "jdbc:mariadb://localhost:3306/ftcoin");
    }

    public String bdUsuario() {
        return texto("bd.usuario", "");
    }

    public String bdSenha() {
        return texto("bd.senha", "");
    }

    /** @return nível do log (nomes de java.util.logging.Level); padrão INFO */
    public String logNivel() {
        return texto("log.nivel", "INFO");
    }

    /** @return padrão do arquivo de log, no formato de FileHandler; padrão ftcoin-%g.log */
    public String logArquivo() {
        return texto("log.arquivo", "ftcoin-%g.log");
    }
}

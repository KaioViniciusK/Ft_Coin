package ftcoin.log;

import ftcoin.config.ConfigAplicacao;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Configura o log com java.util.logging: console para acompanhamento em tempo real e arquivo
 * rotativo para análise posterior. Todas as classes usam loggers filhos de "ftcoin".
 */
public final class LogConfig {
    private static final int TAMANHO_ARQUIVO = 1_000_000;
    private static final int NUMERO_ARQUIVOS = 3;

    private LogConfig() {
    }

    /** Formato de uma linha: data e hora | nível | classe | mensagem (e a exceção, se houver). */
    static final class FormatoLinha extends Formatter {
        private static final DateTimeFormatter DATA_HORA =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        @Override
        public String format(LogRecord registro) {
            StringBuilder linha = new StringBuilder();
            linha.append(DATA_HORA.format(Instant.ofEpochMilli(registro.getMillis())))
                    .append(" | ").append(registro.getLevel())
                    .append(" | ").append(registro.getLoggerName())
                    .append(" | ").append(formatMessage(registro))
                    .append(System.lineSeparator());
            if (registro.getThrown() != null) {
                linha.append("    causa: ").append(registro.getThrown()).append(System.lineSeparator());
            }
            return linha.toString();
        }
    }

    /**
     * Instala os handlers no logger "ftcoin". Se o arquivo não puder ser criado, o programa continua
     * só com o console, e o problema é registrado.
     *
     * @param config fornece o nível e o padrão do nome do arquivo
     */
    public static void iniciar(ConfigAplicacao config) {
        Logger raiz = Logger.getLogger("ftcoin");
        for (Handler antigo : raiz.getHandlers()) {
            raiz.removeHandler(antigo);
            antigo.close();
        }
        Level nivel;
        try {
            nivel = Level.parse(config.logNivel().toUpperCase());
        } catch (IllegalArgumentException e) {
            nivel = Level.INFO;
        }
        raiz.setUseParentHandlers(false);
        raiz.setLevel(nivel);

        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(nivel);
        console.setFormatter(new FormatoLinha());
        raiz.addHandler(console);

        try {
            FileHandler arquivo = new FileHandler(config.logArquivo(), TAMANHO_ARQUIVO, NUMERO_ARQUIVOS, true);
            arquivo.setEncoding(StandardCharsets.UTF_8.name());
            arquivo.setLevel(nivel);
            arquivo.setFormatter(new FormatoLinha());
            raiz.addHandler(arquivo);
        } catch (IOException | SecurityException e) {
            raiz.log(Level.WARNING, "Log em arquivo indisponível; usando só o console", e);
        }
        raiz.log(Level.INFO, "Log iniciado no nível {0}", nivel);
    }

    /** Fecha os handlers, garantindo a gravação do arquivo. Chamado na saída. */
    public static void encerrar() {
        for (Handler handler : Logger.getLogger("ftcoin").getHandlers()) {
            handler.flush();
            handler.close();
        }
    }
}

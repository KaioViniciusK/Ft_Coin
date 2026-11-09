package ftcoin.oraculo;

import ftcoin.model.Cotacao;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

/**
 * Cliente da API proprietária do servidor: abre um socket TCP por consulta e troca objetos Java
 * serializados ({@link NetworkRequest} e {@link NetworkResponse}). Define tempo limite de conexão
 * e de leitura e converte {@link ExchangeRate} em {@link Cotacao}. A senha nunca é registrada no log.
 */
public final class OraculoSocketClient implements OraculoClient {
    private static final Logger LOG = Logger.getLogger(OraculoSocketClient.class.getName());

    private final String host;
    private final int porta;
    private final String grupo;
    private final String senha;
    private final int timeoutMs;

    /**
     * @param host      endereço do servidor
     * @param porta     porta do servidor
     * @param grupo     nome do grupo na API
     * @param senha     senha do grupo na API
     * @param timeoutMs tempo limite, em ms, para conectar e para esperar a resposta
     */
    public OraculoSocketClient(String host, int porta, String grupo, String senha, int timeoutMs) {
        this.host = host;
        this.porta = porta;
        this.grupo = grupo;
        this.senha = senha;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public List<Cotacao> obter(LocalDate primeira, LocalDate ultima) throws OraculoException {
        long inicio = System.nanoTime();
        try {
            NetworkResponse resposta = enviar(new NetworkRequest(grupo, senha, primeira, ultima));
            if (resposta.getErrorMessage() != null) {
                throw new OraculoException("Servidor respondeu com erro: " + resposta.getErrorMessage());
            }
            List<Cotacao> cotacoes = new ArrayList<>();
            if (resposta.getExchangeRates() != null) {
                for (ExchangeRate taxa : resposta.getExchangeRates()) {
                    cotacoes.add(new Cotacao(taxa.getDate(), taxa.getValue()));
                }
            }
            LOG.log(Level.FINE, "API {0}:{1} respondeu {2} cotações para {3} a {4} em {5} ms",
                    new Object[] {host, porta, cotacoes.size(), primeira, ultima, milissegundos(inicio)});
            return cotacoes;
        } catch (InvalidClassException | ClassNotFoundException e) {
            throw new OraculoException("Versão incompatível das classes de protocolo", e);
        } catch (IOException e) {
            throw new OraculoException("Falha de rede ao consultar " + host + ":" + porta, e);
        }
    }

    private NetworkResponse enviar(NetworkRequest requisicao) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(host, porta)) {
            try (ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream())) {
                saida.writeObject(requisicao);
                saida.flush();
                try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {
                    return (NetworkResponse) entrada.readObject();
                }
            }
        }
    }

    private static long milissegundos(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }
}

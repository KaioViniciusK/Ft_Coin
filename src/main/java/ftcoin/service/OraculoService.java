package ftcoin.service;

import ftcoin.model.Cotacao;
import ftcoin.oraculo.OraculoClient;
import ftcoin.oraculo.OraculoException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fornece cotações com cache em memória (Map de data para cotação) para reduzir consultas de rede.
 * As faltas de uma mesma chamada viram uma única requisição, cobrindo a menor faixa de datas necessária.
 * Datas que o servidor não devolveu ficam marcadas e não são consultadas de novo na sessão.
 */
public final class OraculoService {
    private static final Logger LOG = Logger.getLogger(OraculoService.class.getName());
    private static final int JANELA_ULTIMA_COTACAO_DIAS = 7;

    private final OraculoClient cliente;
    private final Map<LocalDate, Cotacao> cache = new HashMap<>();
    private final Set<LocalDate> semCotacao = new HashSet<>();
    private int requisicoes;
    private int acertos;
    private int faltas;
    private int erros;

    public OraculoService(OraculoClient cliente) {
        this.cliente = cliente;
    }

    /**
     * @param data data desejada
     * @return a cotação da data
     * @throws CotacaoIndisponivelException se a data não puder ser obtida
     */
    public synchronized Cotacao cotar(LocalDate data) {
        return cotacoes(Set.of(data)).get(data);
    }

    /**
     * @param datas datas necessárias
     * @return a cotação de cada data
     * @throws CotacaoIndisponivelException se alguma data não puder ser obtida
     */
    public synchronized Map<LocalDate, Cotacao> cotacoes(Collection<LocalDate> datas) {
        garantir(datas);
        Map<LocalDate, Cotacao> resultado = new HashMap<>();
        for (LocalDate data : datas) {
            Cotacao cotacao = cache.get(data);
            if (cotacao == null) {
                throw new CotacaoIndisponivelException(data, null);
            }
            resultado.put(data, cotacao);
        }
        return resultado;
    }

    /**
     * Cotação de mercado: a mais recente até a data, dentro de uma janela de 7 dias. Se a rede falhar,
     * usa a mais recente já guardada no cache dessa janela.
     *
     * @param data data de referência (normalmente hoje)
     * @return a cotação mais recente encontrada
     * @throws CotacaoIndisponivelException se não houver nenhuma cotação na janela
     */
    public synchronized Cotacao ultimaCotacaoAte(LocalDate data) {
        Set<LocalDate> janela = new HashSet<>();
        for (int i = 0; i < JANELA_ULTIMA_COTACAO_DIAS; i++) {
            janela.add(data.minusDays(i));
        }
        CotacaoIndisponivelException falha = null;
        try {
            garantir(janela);
        } catch (CotacaoIndisponivelException e) {
            falha = e;
            LOG.log(Level.WARNING, "Usando o cache para a cotação de mercado até {0}", data);
        }
        for (int i = 0; i < JANELA_ULTIMA_COTACAO_DIAS; i++) {
            Cotacao cotacao = cache.get(data.minusDays(i));
            if (cotacao != null) {
                return cotacao;
            }
        }
        throw falha != null ? falha : new CotacaoIndisponivelException(data, null);
    }

    private void garantir(Collection<LocalDate> datas) {
        LocalDate primeira = null;
        LocalDate ultima = null;
        for (LocalDate data : datas) {
            if (cache.containsKey(data) || semCotacao.contains(data)) {
                acertos++;
                continue;
            }
            faltas++;
            primeira = primeira == null || data.isBefore(primeira) ? data : primeira;
            ultima = ultima == null || data.isAfter(ultima) ? data : ultima;
        }
        if (primeira == null) {
            return;
        }
        requisicoes++;
        long inicio = System.nanoTime();
        try {
            for (Cotacao cotacao : cliente.obter(primeira, ultima)) {
                cache.put(cotacao.getData(), cotacao);
            }
            for (LocalDate dia = primeira; !dia.isAfter(ultima); dia = dia.plusDays(1)) {
                if (!cache.containsKey(dia)) {
                    semCotacao.add(dia);
                }
            }
            LOG.log(Level.INFO, "Cotações de {0} a {1} obtidas em {2} ms",
                    new Object[] {primeira, ultima, (System.nanoTime() - inicio) / 1_000_000});
        } catch (OraculoException e) {
            erros++;
            LOG.log(Level.SEVERE, "Falha ao consultar cotações de " + primeira + " a " + ultima, e);
            throw new CotacaoIndisponivelException(primeira, e);
        }
    }

    /** @return número de consultas feitas ao servidor nesta sessão */
    public synchronized int getRequisicoes() {
        return requisicoes;
    }

    /** @return datas atendidas pelo cache */
    public synchronized int getAcertos() {
        return acertos;
    }

    /** @return datas que precisaram ser buscadas */
    public synchronized int getFaltas() {
        return faltas;
    }

    /** @return consultas que falharam */
    public synchronized int getErros() {
        return erros;
    }

    /** Registra no log o resumo da sessão (requisições, cache e erros). */
    public synchronized void registrarResumo() {
        LOG.log(Level.INFO, "Resumo do oráculo: {0} requisições, {1} acertos de cache, {2} faltas, {3} erros",
                new Object[] {requisicoes, acertos, faltas, erros});
    }
}

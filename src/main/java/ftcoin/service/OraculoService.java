package ftcoin.service;

import ftcoin.model.Cotacao;
import ftcoin.oraculo.OraculoClient;
import ftcoin.oraculo.OraculoException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fornece cotações com cache em memória (Map de data para cotação) para reduzir consultas de rede.
 * As faltas de uma mesma chamada viram uma única requisição, cobrindo a menor faixa de datas necessária.
 *
 * <p>Regras que protegem os resultados financeiros:
 * <ul>
 *   <li>uma cotação só entra no cache depois de a resposta inteira ser validada; falha de rede ou
 *       resposta inconsistente não grava nada;</li>
 *   <li>só se memoriza a ausência de datas anteriores a hoje; a cotação de hoje pode ainda não ter
 *       sido publicada, então ela é consultada de novo na próxima chamada;</li>
 *   <li>uma cotação mais antiga nunca substitui a pedida por causa de uma falha de consulta
 *       (ver {@link #cotacaoDeMercado(LocalDate)}).</li>
 * </ul>
 */
public final class OraculoService {
    private static final Logger LOG = Logger.getLogger(OraculoService.class.getName());
    /** Quantos dias para trás a cotação de mercado pode recuar quando o servidor não tem a data de hoje. */
    private static final int JANELA_COTACAO_MERCADO_DIAS = 7;

    private final OraculoClient cliente;
    private final Clock relogio;
    private final Map<LocalDate, Cotacao> cache = new HashMap<>();
    private final Set<LocalDate> semCotacao = new HashSet<>();
    private int requisicoes;
    private int acertos;
    private int faltas;
    private int erros;

    public OraculoService(OraculoClient cliente) {
        this(cliente, Clock.systemDefaultZone());
    }

    /**
     * @param cliente fonte das cotações
     * @param relogio define "hoje", usado para decidir quais ausências podem ser memorizadas
     */
    public OraculoService(OraculoClient cliente, Clock relogio) {
        this.cliente = cliente;
        this.relogio = relogio;
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
     * Cotação de mercado na data de referência (normalmente hoje). Só recua para um dia anterior
     * (no máximo 7) quando o servidor respondeu com sucesso e confirmou que não tem cotação para os
     * dias mais recentes, como em um fim de semana ou feriado. Se a consulta falhar (rede, timeout,
     * erro do servidor ou resposta inválida), a cotação anterior guardada no cache NÃO é usada como
     * substituta: o resultado seria calculado com um preço que não é o do dia.
     *
     * @param data data de referência
     * @return a cotação mais recente na janela, confirmada pelo servidor
     * @throws CotacaoIndisponivelException se a consulta falhar ou não houver cotação na janela
     */
    public synchronized Cotacao cotacaoDeMercado(LocalDate data) {
        garantir(Set.of(data));
        Cotacao doDia = cache.get(data);
        if (doDia != null) {
            return doDia;
        }
        Set<LocalDate> anteriores = new HashSet<>();
        for (int i = 1; i < JANELA_COTACAO_MERCADO_DIAS; i++) {
            anteriores.add(data.minusDays(i));
        }
        garantir(anteriores);
        for (int i = 1; i < JANELA_COTACAO_MERCADO_DIAS; i++) {
            Cotacao cotacao = cache.get(data.minusDays(i));
            if (cotacao != null) {
                LOG.log(Level.WARNING, "Sem cotação de {0} no servidor; cotação de mercado vem de {1}",
                        new Object[] {data, cotacao.getData()});
                return cotacao;
            }
        }
        throw new CotacaoIndisponivelException(data, null);
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
            Map<LocalDate, Cotacao> recebidas = validar(cliente.obter(primeira, ultima), primeira, ultima);
            cache.putAll(recebidas);
            LocalDate hoje = LocalDate.now(relogio);
            for (LocalDate dia = primeira; dia.isBefore(hoje) && !dia.isAfter(ultima); dia = dia.plusDays(1)) {
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

    /**
     * Confere a resposta antes de ela tocar o cache: lista presente, sem elementos nulos e sem datas
     * repetidas com valores diferentes. Datas fora da faixa pedida são descartadas.
     */
    private static Map<LocalDate, Cotacao> validar(List<Cotacao> lista, LocalDate primeira, LocalDate ultima)
            throws OraculoException {
        if (lista == null) {
            throw new OraculoException("Resposta do oráculo sem lista de cotações");
        }
        Map<LocalDate, Cotacao> validas = new HashMap<>();
        for (Cotacao cotacao : lista) {
            if (cotacao == null) {
                throw new OraculoException("Resposta do oráculo com cotação nula");
            }
            LocalDate data = cotacao.getData();
            if (data.isBefore(primeira) || data.isAfter(ultima)) {
                LOG.log(Level.WARNING, "Cotação de {0} fora da faixa pedida ({1} a {2}) foi descartada",
                        new Object[] {data, primeira, ultima});
                continue;
            }
            Cotacao anterior = validas.putIfAbsent(data, cotacao);
            if (anterior != null && anterior.getValor().compareTo(cotacao.getValor()) != 0) {
                throw new OraculoException("Resposta do oráculo com valores diferentes para " + data);
            }
        }
        return validas;
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

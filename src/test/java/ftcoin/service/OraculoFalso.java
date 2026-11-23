package ftcoin.service;

import ftcoin.model.Cotacao;
import ftcoin.oraculo.OraculoClient;
import ftcoin.oraculo.OraculoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dublê do servidor de cotações: tabela programável, rede que pode cair e registro das consultas. */
final class OraculoFalso implements OraculoClient {
    final Map<LocalDate, BigDecimal> tabela = new HashMap<>();
    final List<LocalDate[]> consultas = new ArrayList<>();
    boolean redeFora;
    boolean respostaNula;
    /** Quando não nula, é devolvida como está, sem olhar a tabela (para simular respostas inconsistentes). */
    List<Cotacao> respostaFixa;

    OraculoFalso com(LocalDate data, String valor) {
        tabela.put(data, new BigDecimal(valor));
        return this;
    }

    @Override
    public List<Cotacao> obter(LocalDate primeira, LocalDate ultima) throws OraculoException {
        consultas.add(new LocalDate[] {primeira, ultima});
        if (redeFora) {
            throw new OraculoException("rede fora");
        }
        if (respostaNula) {
            return null;
        }
        if (respostaFixa != null) {
            return respostaFixa;
        }
        List<Cotacao> resposta = new ArrayList<>();
        for (LocalDate dia = primeira; !dia.isAfter(ultima); dia = dia.plusDays(1)) {
            BigDecimal valor = tabela.get(dia);
            if (valor != null) {
                resposta.add(new Cotacao(dia, valor));
            }
        }
        return resposta;
    }

    /** @return true se alguma consulta feita alcançou a data ou uma posterior */
    boolean consultouAte(LocalDate data) {
        for (LocalDate[] faixa : consultas) {
            if (!faixa[1].isBefore(data)) {
                return true;
            }
        }
        return false;
    }
}

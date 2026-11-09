package ftcoin.oraculo;

import ftcoin.model.Cotacao;
import java.time.LocalDate;
import java.util.List;

/** Fonte de cotações. Separa o OraculoService do protocolo de rede e permite substituí-la nos testes. */
public interface OraculoClient {
    /**
     * @param primeira primeira data da faixa (inclusive)
     * @param ultima   última data da faixa (inclusive)
     * @return as cotações encontradas, possivelmente vazia
     * @throws OraculoException se a consulta falhar
     */
    List<Cotacao> obter(LocalDate primeira, LocalDate ultima) throws OraculoException;
}

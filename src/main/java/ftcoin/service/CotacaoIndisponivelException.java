package ftcoin.service;

import java.time.LocalDate;

/** Não foi possível obter uma cotação necessária (API fora do ar, erro do servidor ou data sem cotação). */
public class CotacaoIndisponivelException extends NegocioException {
    private static final long serialVersionUID = 1L;

    /**
     * @param data  data da cotação que faltou
     * @param causa falha original; pode ser null quando o servidor simplesmente não tem a data
     */
    public CotacaoIndisponivelException(LocalDate data, Throwable causa) {
        super(causa, "erro.oraculo.indisponivel", data);
    }
}

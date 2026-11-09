package ftcoin;

import ftcoin.model.Cotacao;
import ftcoin.oraculo.OraculoClient;
import ftcoin.oraculo.OraculoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Cliente de cotações programável, que conta as consultas e pode simular rede fora. */
final class ClienteFalso implements OraculoClient {
    final Map<LocalDate, BigDecimal> valores = new HashMap<>();
    final List<LocalDate[]> consultas = new ArrayList<>();
    boolean redeFora;

    @Override
    public List<Cotacao> obter(LocalDate primeira, LocalDate ultima) throws OraculoException {
        consultas.add(new LocalDate[] {primeira, ultima});
        if (redeFora) {
            throw new OraculoException("rede fora");
        }
        List<Cotacao> lista = new ArrayList<>();
        for (LocalDate d = primeira; !d.isAfter(ultima); d = d.plusDays(1)) {
            if (valores.containsKey(d)) {
                lista.add(new Cotacao(d, valores.get(d)));
            }
        }
        return lista;
    }
}

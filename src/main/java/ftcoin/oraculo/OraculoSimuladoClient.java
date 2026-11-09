package ftcoin.oraculo;

import ftcoin.model.Cotacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerador local de cotações determinísticas (entre 10,00 e 14,99), para demonstração sem servidor.
 * Não representa valores reais; é ativado apenas por oraculo.modo=simulado.
 */
public final class OraculoSimuladoClient implements OraculoClient {
    @Override
    public List<Cotacao> obter(LocalDate primeira, LocalDate ultima) {
        List<Cotacao> cotacoes = new ArrayList<>();
        for (LocalDate dia = primeira; !dia.isAfter(ultima); dia = dia.plusDays(1)) {
            long centavos = 1000 + Math.floorMod(dia.toEpochDay() * 37, 500);
            cotacoes.add(new Cotacao(dia, BigDecimal.valueOf(centavos, 2)));
        }
        return cotacoes;
    }
}

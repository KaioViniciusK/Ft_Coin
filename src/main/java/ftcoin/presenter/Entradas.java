package ftcoin.presenter;

import ftcoin.service.NegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Conversão do texto digitado em números e datas, com erros no vocabulário do domínio. */
final class Entradas {
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Entradas() {
    }

    static int id(String texto) {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            throw new NegocioException("erro.id.formato");
        }
    }

    static BigDecimal quantidade(String texto) {
        try {
            BigDecimal valor = new BigDecimal((texto == null ? "" : texto.trim()).replace(',', '.'));
            if (valor.signum() <= 0) {
                throw new NegocioException("erro.quantidade.invalida");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new NegocioException("erro.quantidade.invalida");
        }
    }

    static LocalDate data(String texto) {
        String limpo = texto == null ? "" : texto.trim();
        try {
            return limpo.contains("/") ? LocalDate.parse(limpo, DATA_BR) : LocalDate.parse(limpo);
        } catch (DateTimeParseException e) {
            throw new NegocioException("erro.data.invalida");
        }
    }
}

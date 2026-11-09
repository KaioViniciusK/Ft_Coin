package ftcoin.model;

import java.util.Objects;

/** Carteira de FTCoins de um titular em uma corretora. Imutável e sem conhecimento de persistência. */
public final class Carteira {
    private final int id;
    private final String titular;
    private final String corretora;

    /**
     * @param id        identificador positivo
     * @param titular   nome do titular, não vazio
     * @param corretora nome da corretora, não vazio
     * @throws IllegalArgumentException se algum valor for inválido
     */
    public Carteira(int id, String titular, String corretora) {
        if (id <= 0) {
            throw new IllegalArgumentException("O identificador deve ser positivo");
        }
        this.id = id;
        this.titular = exigirTexto(titular, "titular");
        this.corretora = exigirTexto(corretora, "corretora");
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo " + campo + " não pode ser vazio");
        }
        return valor.trim();
    }

    public int getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public String getCorretora() {
        return corretora;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Carteira && ((Carteira) outro).id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " - " + titular + " (" + corretora + ")";
    }
}

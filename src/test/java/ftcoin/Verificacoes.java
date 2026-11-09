package ftcoin;

import java.util.ArrayList;
import java.util.List;

/** Executor mínimo de testes, sem dependências externas (o projeto compila só com a biblioteca padrão). */
public final class Verificacoes {
    /** Um caso de teste. */
    public interface Caso {
        void executar() throws Exception;
    }

    private static final List<String> FALHAS = new ArrayList<>();
    private static int total;

    private Verificacoes() {
    }

    public static void caso(String nome, Caso caso) {
        total++;
        try {
            caso.executar();
            System.out.println("OK    " + nome);
        } catch (Throwable e) {
            FALHAS.add(nome);
            System.out.println("FALHA " + nome + " -> " + e);
        }
    }

    public static void verdadeiro(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    public static void igual(Object esperado, Object atual, String mensagem) {
        if (esperado == null ? atual != null : !esperado.equals(atual)) {
            throw new AssertionError(mensagem + ": esperado " + esperado + " mas foi " + atual);
        }
    }

    public static void proximo(double esperado, java.math.BigDecimal atual, double tolerancia, String mensagem) {
        if (Math.abs(atual.doubleValue() - esperado) > tolerancia) {
            throw new AssertionError(mensagem + ": esperado " + esperado + " ±" + tolerancia + " mas foi " + atual);
        }
    }

    /** Executa o código e exige uma exceção do tipo dado; devolve a exceção para inspeção. */
    public static <T extends Throwable> T lanca(Class<T> tipo, Caso codigo, String mensagem) {
        try {
            codigo.executar();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) {
                return tipo.cast(e);
            }
            throw new AssertionError(mensagem + ": esperava " + tipo.getSimpleName() + " mas veio " + e);
        }
        throw new AssertionError(mensagem + ": esperava " + tipo.getSimpleName() + " mas nada foi lançado");
    }

    /** Imprime o resumo e encerra com código 1 se houve falhas. */
    public static void resumo() {
        System.out.println();
        System.out.println("Testes: " + total + ", aprovados: " + (total - FALHAS.size()) + ", falhas: " + FALHAS.size());
        if (!FALHAS.isEmpty()) {
            System.exit(1);
        }
    }
}

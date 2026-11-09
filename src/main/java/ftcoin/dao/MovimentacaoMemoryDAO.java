package ftcoin.dao;

import ftcoin.model.Movimentacao;
import java.util.ArrayList;
import java.util.List;

/** Simulação de banco de dados de movimentações em memória local. */
public final class MovimentacaoMemoryDAO implements MovimentacaoDAO {
    private final List<Movimentacao> movimentacoes = new ArrayList<>();
    private int ultimoId;

    @Override
    public void inserir(Movimentacao movimentacao) {
        movimentacoes.add(movimentacao);
        ultimoId = Math.max(ultimoId, movimentacao.getId());
    }

    @Override
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        List<Movimentacao> resultado = new ArrayList<>();
        for (Movimentacao m : movimentacoes) {
            if (m.getIdCarteira() == idCarteira) {
                resultado.add(m);
            }
        }
        resultado.sort(Movimentacao.ORDEM_CRONOLOGICA);
        return resultado;
    }

    @Override
    public int proximoId() {
        return ultimoId + 1;
    }
}

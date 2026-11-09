package ftcoin.dao;

import ftcoin.model.Movimentacao;
import java.util.List;

/** Acesso a dados de movimentações. */
public interface MovimentacaoDAO {
    /** Grava uma nova movimentação. */
    void inserir(Movimentacao movimentacao);

    /** @return as movimentações da carteira em ordem cronológica (data e, depois, identificador) */
    List<Movimentacao> listarPorCarteira(int idCarteira);

    /** @return o próximo identificador de movimento livre */
    int proximoId();
}

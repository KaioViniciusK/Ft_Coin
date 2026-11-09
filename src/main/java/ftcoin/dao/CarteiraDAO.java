package ftcoin.dao;

import ftcoin.model.Carteira;
import java.util.List;
import java.util.Optional;

/** Acesso a dados de carteiras. */
public interface CarteiraDAO {
    /** Grava uma nova carteira. */
    void inserir(Carteira carteira);

    /** @return a carteira com o identificador, se existir */
    Optional<Carteira> buscar(int id);

    /** Atualiza titular e corretora da carteira de mesmo identificador. */
    void atualizar(Carteira carteira);

    /** Remove a carteira; não faz nada se ela não existir. */
    void excluir(int id);

    /** @return todas as carteiras, em ordem de identificador */
    List<Carteira> listar();
}

package ftcoin.service;

import ftcoin.dao.CarteiraDAO;
import ftcoin.dao.MovimentacaoDAO;
import ftcoin.model.Carteira;
import java.util.Comparator;
import java.util.List;

/** Regras de negócio de carteiras: validação, unicidade e restrições de exclusão. */
public final class CarteiraService {
    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;

    public CarteiraService(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
    }

    /**
     * @return a carteira criada
     * @throws NegocioException se os dados forem inválidos ou o identificador já existir
     */
    public Carteira incluir(int id, String titular, String corretora) {
        Carteira carteira = criar(id, titular, corretora);
        if (carteiraDAO.buscar(id).isPresent()) {
            throw new NegocioException("erro.carteira.existe", id);
        }
        carteiraDAO.inserir(carteira);
        return carteira;
    }

    /**
     * @return a carteira
     * @throws NegocioException se ela não existir
     */
    public Carteira consultar(int id) {
        return carteiraDAO.buscar(id).orElseThrow(() -> new NegocioException("erro.carteira.inexistente", id));
    }

    /**
     * @return a carteira atualizada
     * @throws NegocioException se os dados forem inválidos ou a carteira não existir
     */
    public Carteira editar(int id, String titular, String corretora) {
        consultar(id);
        Carteira carteira = criar(id, titular, corretora);
        carteiraDAO.atualizar(carteira);
        return carteira;
    }

    /**
     * Exclui a carteira. Carteiras com movimentações não podem ser excluídas, para não perder o histórico.
     *
     * @throws NegocioException se a carteira não existir ou tiver movimentações
     */
    public void excluir(int id) {
        consultar(id);
        if (!movimentacaoDAO.listarPorCarteira(id).isEmpty()) {
            throw new NegocioException("erro.carteira.com.movimentos", id);
        }
        carteiraDAO.excluir(id);
    }

    /** @return todas as carteiras ordenadas por identificador */
    public List<Carteira> listarPorId() {
        List<Carteira> lista = carteiraDAO.listar();
        lista.sort(Comparator.comparingInt(Carteira::getId));
        return lista;
    }

    /** @return todas as carteiras ordenadas por nome do titular (e, em empate, por identificador) */
    public List<Carteira> listarPorTitular() {
        List<Carteira> lista = carteiraDAO.listar();
        lista.sort(Comparator.comparing(Carteira::getTitular, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Carteira::getId));
        return lista;
    }

    private Carteira criar(int id, String titular, String corretora) {
        if (id <= 0) {
            throw new NegocioException("erro.id.invalido");
        }
        if (titular == null || titular.isBlank()) {
            throw new NegocioException("erro.titular.vazio");
        }
        if (corretora == null || corretora.isBlank()) {
            throw new NegocioException("erro.corretora.vazia");
        }
        return new Carteira(id, titular, corretora);
    }
}

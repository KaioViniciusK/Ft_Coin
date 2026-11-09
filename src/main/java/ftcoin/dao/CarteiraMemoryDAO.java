package ftcoin.dao;

import ftcoin.model.Carteira;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/** Simulação de banco de dados de carteiras em memória local. */
public final class CarteiraMemoryDAO implements CarteiraDAO {
    private final TreeMap<Integer, Carteira> carteiras = new TreeMap<>();

    @Override
    public void inserir(Carteira carteira) {
        if (carteiras.putIfAbsent(carteira.getId(), carteira) != null) {
            throw new PersistenciaException("Carteira já existe: " + carteira.getId(), null);
        }
    }

    @Override
    public Optional<Carteira> buscar(int id) {
        return Optional.ofNullable(carteiras.get(id));
    }

    @Override
    public void atualizar(Carteira carteira) {
        if (!carteiras.containsKey(carteira.getId())) {
            throw new PersistenciaException("Carteira inexistente: " + carteira.getId(), null);
        }
        carteiras.put(carteira.getId(), carteira);
    }

    @Override
    public void excluir(int id) {
        carteiras.remove(id);
    }

    @Override
    public List<Carteira> listar() {
        return new ArrayList<>(carteiras.values());
    }
}

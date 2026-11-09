package ftcoin.dao;

import static ftcoin.dao.EsquemaBD.*;

import ftcoin.model.Carteira;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Carteiras no MariaDB, por JDBC manual. Só pede a conexão ao Singleton; não conhece URL nem senha. */
public final class CarteiraMariaDBDAO implements CarteiraDAO {
    private static final String SQL_INSERIR = "INSERT INTO " + TAB_CARTEIRA + " (" + COL_CART_ID + ", "
            + COL_CART_TITULAR + ", " + COL_CART_CORRETORA + ") VALUES (?, ?, ?)";
    private static final String SQL_SELECT = "SELECT " + COL_CART_ID + ", " + COL_CART_TITULAR + ", "
            + COL_CART_CORRETORA + " FROM " + TAB_CARTEIRA;
    private static final String SQL_BUSCAR = SQL_SELECT + " WHERE " + COL_CART_ID + " = ?";
    private static final String SQL_LISTAR = SQL_SELECT + " ORDER BY " + COL_CART_ID;
    private static final String SQL_ATUALIZAR = "UPDATE " + TAB_CARTEIRA + " SET " + COL_CART_TITULAR + " = ?, "
            + COL_CART_CORRETORA + " = ? WHERE " + COL_CART_ID + " = ?";
    private static final String SQL_EXCLUIR = "DELETE FROM " + TAB_CARTEIRA + " WHERE " + COL_CART_ID + " = ?";

    private Connection conexao() {
        return ConexaoBD.getInstance().getConexao();
    }

    @Override
    public void inserir(Carteira carteira) {
        try (PreparedStatement ps = conexao().prepareStatement(SQL_INSERIR)) {
            ps.setInt(1, carteira.getId());
            ps.setString(2, carteira.getTitular());
            ps.setString(3, carteira.getCorretora());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir carteira " + carteira.getId(), e);
        }
    }

    @Override
    public Optional<Carteira> buscar(int id) {
        try (PreparedStatement ps = conexao().prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(converter(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar carteira " + id, e);
        }
    }

    @Override
    public void atualizar(Carteira carteira) {
        try (PreparedStatement ps = conexao().prepareStatement(SQL_ATUALIZAR)) {
            ps.setString(1, carteira.getTitular());
            ps.setString(2, carteira.getCorretora());
            ps.setInt(3, carteira.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar carteira " + carteira.getId(), e);
        }
    }

    @Override
    public void excluir(int id) {
        try (PreparedStatement ps = conexao().prepareStatement(SQL_EXCLUIR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao excluir carteira " + id, e);
        }
    }

    @Override
    public List<Carteira> listar() {
        List<Carteira> carteiras = new ArrayList<>();
        try (PreparedStatement ps = conexao().prepareStatement(SQL_LISTAR); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                carteiras.add(converter(rs));
            }
            return carteiras;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar carteiras", e);
        }
    }

    private Carteira converter(ResultSet rs) throws SQLException {
        return new Carteira(rs.getInt(COL_CART_ID), rs.getString(COL_CART_TITULAR), rs.getString(COL_CART_CORRETORA));
    }
}

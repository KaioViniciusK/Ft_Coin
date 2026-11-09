package ftcoin.dao;

import static ftcoin.dao.EsquemaBD.*;

import ftcoin.model.Movimentacao;
import ftcoin.model.TipoOperacao;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Movimentações no MariaDB, por JDBC manual. O tipo é gravado como CHAR(1) e a quantidade como
 * DECIMAL(10,3): o banco guarda no máximo 3 casas decimais, e a camada de serviço recusa mais que isso.
 */
public final class MovimentacaoMariaDBDAO implements MovimentacaoDAO {
    private static final String SQL_INSERIR = "INSERT INTO " + TAB_MOVIMENTACAO + " (" + COL_MOV_ID_CARTEIRA + ", "
            + COL_MOV_ID + ", " + COL_MOV_DATA + ", " + COL_MOV_TIPO + ", " + COL_MOV_QUANTIDADE
            + ") VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = "SELECT " + COL_MOV_ID_CARTEIRA + ", " + COL_MOV_ID + ", "
            + COL_MOV_DATA + ", " + COL_MOV_TIPO + ", " + COL_MOV_QUANTIDADE + " FROM " + TAB_MOVIMENTACAO
            + " WHERE " + COL_MOV_ID_CARTEIRA + " = ? ORDER BY " + COL_MOV_DATA + ", " + COL_MOV_ID;
    private static final String SQL_PROXIMO_ID = "SELECT COALESCE(MAX(" + COL_MOV_ID + "), 0) + 1 FROM "
            + TAB_MOVIMENTACAO;

    private Connection conexao() {
        return ConexaoBD.getInstance().getConexao();
    }

    @Override
    public void inserir(Movimentacao m) {
        try (PreparedStatement ps = conexao().prepareStatement(SQL_INSERIR)) {
            ps.setInt(1, m.getIdCarteira());
            ps.setInt(2, m.getId());
            ps.setDate(3, Date.valueOf(m.getData()));
            ps.setString(4, String.valueOf(m.getTipo().getCodigo()));
            ps.setBigDecimal(5, m.getQuantidade());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao inserir movimentação " + m.getId(), e);
        }
    }

    @Override
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        List<Movimentacao> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao().prepareStatement(SQL_LISTAR)) {
            ps.setInt(1, idCarteira);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date data = rs.getDate(COL_MOV_DATA);
                    String tipo = rs.getString(COL_MOV_TIPO);
                    BigDecimal quantidade = rs.getBigDecimal(COL_MOV_QUANTIDADE);
                    if (data == null || tipo == null || tipo.isEmpty() || quantidade == null) {
                        throw new PersistenciaException("Movimentação " + rs.getInt(COL_MOV_ID) + " com campos vazios no banco", null);
                    }
                    lista.add(new Movimentacao(rs.getInt(COL_MOV_ID), rs.getInt(COL_MOV_ID_CARTEIRA),
                            data.toLocalDate(), TipoOperacao.fromCodigo(tipo.charAt(0)), quantidade));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar movimentações da carteira " + idCarteira, e);
        }
    }

    @Override
    public int proximoId() {
        try (PreparedStatement ps = conexao().prepareStatement(SQL_PROXIMO_ID); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao obter próximo identificador de movimento", e);
        }
    }
}

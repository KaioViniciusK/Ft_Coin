package com.mvc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mvc.model.Carteira;

public class CarteiraDAOMariaDB implements CarteiraDAO {

    @Override
    public void incluir(Carteira carteira) {
        String sql = "INSERT INTO Carteira (Identificador, NomeTitular, Corretora) VALUES (?, ?, ?)";
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, carteira.getIdentificador());
            stmt.setString(2, carteira.getNomeTitular());
            stmt.setString(3, carteira.getCorretora());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao incluir carteira no banco: " + e.getMessage());
        }
    }

    @Override
    public Carteira consultar(int identificador) {
        String sql = "SELECT * FROM Carteira WHERE Identificador = ?";
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, identificador);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Carteira(
                    rs.getInt("Identificador"),
                    rs.getString("NomeTitular"),
                    rs.getString("Corretora")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar carteira: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void editar(Carteira carteira) {
        String sql = "UPDATE Carteira SET NomeTitular = ?, Corretora = ? WHERE Identificador = ?";
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, carteira.getNomeTitular());
            stmt.setString(2, carteira.getCorretora());
            stmt.setInt(3, carteira.getIdentificador());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao editar carteira: " + e.getMessage());
        }
    }

    @Override
    public void excluir(int identificador) {
        String sql = "DELETE FROM Carteira WHERE Identificador = ?";
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, identificador);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir carteira: " + e.getMessage());
        }
    }

    @Override
    public List<Carteira> listarTodas() {
        List<Carteira> carteiras = new ArrayList<>();
        String sql = "SELECT * FROM Carteira";
        
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                carteiras.add(new Carteira(
                    rs.getInt("Identificador"),
                    rs.getString("NomeTitular"),
                    rs.getString("Corretora")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar carteiras: " + e.getMessage());
        }
        return carteiras;
    }
}
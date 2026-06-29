package com.mvc.model;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.mvc.dao.FabricaConexao;

public class OraculoClient {
    
    public BigDecimal obterCotacao(LocalDate data) {
        String sql = "SELECT Cotacao FROM Oraculo WHERE Data = ?";
        
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Converte a data do Java para a data do Banco SQL
            stmt.setDate(1, Date.valueOf(data));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("Cotacao"); // Retorna o valor real do banco
            } else {
               
                return new BigDecimal("10.00");
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar o banco de dados do Oraculo: " + e.getMessage());
        }
    }
}
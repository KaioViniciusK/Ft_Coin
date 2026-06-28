package com.mvc.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mvc.model.Movimentacao;
import com.mvc.model.TipoOperacao;

public class MovimentacaoDAOMariaDB implements MovimentacaoDAO {

    @Override
    public void registrarMovimentacao(Movimentacao movimentacao) {
        String sql = "INSERT INTO Movimentacao (IdentificadorMovimento, IdentificadorCarteira, DataOperacao, TipoOperacao, QuantidadeMovimentada) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, movimentacao.getIdMovimento());
            stmt.setInt(2, movimentacao.getIdCarteira());
            // Converte LocalDate do Java para Date do Banco de Dados
            stmt.setDate(3, Date.valueOf(movimentacao.getDataOperacao())); 
            // Pega o caractere 'C' ou 'V' do Enum e converte para String
            stmt.setString(4, String.valueOf(movimentacao.getTipoOperacao().getCodigo()));
            stmt.setBigDecimal(5, movimentacao.getQuantidadeMovimentada());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar movimentacao no banco: " + e.getMessage());
        }
    }

    @Override
    public List<Movimentacao> listarPorCarteira(int idCarteira) {
        List<Movimentacao> historico = new ArrayList<>();
        String sql = "SELECT * FROM Movimentacao WHERE IdentificadorCarteira = ?";
        
        try (Connection conn = FabricaConexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCarteira);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Converte a letra 'C' ou 'V' que veio do banco de volta para o Enum
                String tipoStr = rs.getString("TipoOperacao");
                TipoOperacao tipo = tipoStr.equals("C") ? TipoOperacao.COMPRA : TipoOperacao.VENDA;

                historico.add(new Movimentacao(
                    rs.getInt("IdentificadorCarteira"),
                    rs.getInt("IdentificadorMovimento"),
                    // Converte de volta de Date SQL para LocalDate
                    rs.getDate("DataOperacao").toLocalDate(),
                    tipo,
                    rs.getBigDecimal("QuantidadeMovimentada")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar movimentacao da carteira: " + e.getMessage());
        }
        return historico;
    }
}
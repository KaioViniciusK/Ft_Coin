package com.mvc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FabricaConexao {
    // Configurações padrão para quem usa o XAMPP localmente
    private static final String URL = "jdbc:mariadb://localhost:3306/ft_coin";
    private static final String USUARIO = "root"; 
    private static final String SENHA = ""; 

    public static Connection obterConexao() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o banco de dados MariaDB: " + e.getMessage());
        }
    }
}
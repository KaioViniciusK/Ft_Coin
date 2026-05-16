package com.mvc.dao;

import java.util.List;

import com.mvc.model.Carteira;

public interface CarteiraDAO {
    void incluir(Carteira carteira);
    Carteira consultar(int identificador);
    void editar(Carteira carteira);
    void excluir(int identificador);
    List<Carteira> listarTodas();
}
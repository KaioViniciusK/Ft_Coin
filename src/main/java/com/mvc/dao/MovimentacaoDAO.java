package com.mvc.dao;

import java.util.List;

import com.mvc.model.Movimentacao;

public interface MovimentacaoDAO {
    void registrarMovimentacao(Movimentacao movimentacao);
    List<Movimentacao> listarPorCarteira(int idCarteira);
}
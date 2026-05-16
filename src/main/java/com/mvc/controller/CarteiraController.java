package com.mvc.controller;

import com.mvc.dao.CarteiraDAO;
import com.mvc.dao.MovimentacaoDAO;
import com.mvc.model.Carteira;
import com.mvc.model.Movimentacao;
import com.mvc.model.TipoOperacao;

import java.math.BigDecimal;
import java.util.List;

public class CarteiraController {
    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movimentacaoDAO;

    // A injeção de dependência permite usar tanto o banco em memória quanto o MariaDB
    public CarteiraController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
    }

    public void criarCarteira(int id, String titular, String corretora) throws IllegalArgumentException {
        if (carteiraDAO.consultar(id) != null) {
            throw new IllegalArgumentException("Erro: Já existe uma carteira com este identificador.");
        }
        Carteira novaCarteira = new Carteira(id, titular, corretora);
        carteiraDAO.incluir(novaCarteira);
    }

    public BigDecimal calcularSaldoCarteira(int idCarteira) {
        List<Movimentacao> movimentos = movimentacaoDAO.listarPorCarteira(idCarteira);
        BigDecimal saldo = BigDecimal.ZERO;

        for (Movimentacao mov : movimentos) {
            if (mov.getTipoOperacao() == TipoOperacao.COMPRA) {
                saldo = saldo.add(mov.getQuantidadeMovimentada());
            } else if (mov.getTipoOperacao() == TipoOperacao.VENDA) {
                saldo = saldo.subtract(mov.getQuantidadeMovimentada());
            }
        }
        return saldo;
    }

    public void registrarOperacao(Movimentacao movimentacao) throws IllegalArgumentException {
        // Regra de negócio: Não pode vender mais do que tem
        if (movimentacao.getTipoOperacao() == TipoOperacao.VENDA) {
            BigDecimal saldoAtual = calcularSaldoCarteira(movimentacao.getIdCarteira());
            if (saldoAtual.compareTo(movimentacao.getQuantidadeMovimentada()) < 0) {
                throw new IllegalArgumentException("Erro: Saldo insuficiente para realizar a venda.");
            }
        }
        movimentacaoDAO.registrarMovimentacao(movimentacao);
    }
}

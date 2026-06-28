package com.mvc.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;

import com.mvc.dao.CarteiraDAO;
import com.mvc.dao.MovimentacaoDAO;
import com.mvc.model.Carteira;
import com.mvc.model.Movimentacao;
import com.mvc.model.OraculoClient;

public class CarteiraController {
    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final OraculoClient oraculoClient; // Adicionado para os cálculos de ganho/perda
    private int contadorMovimentos = 1; // Simula um auto_increment para os testes em memória

    // Construtor atualizado para receber o OraculoClient
    public CarteiraController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoClient oraculoClient) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
        this.oraculoClient = oraculoClient;
    }

    public void criarCarteira(int id, String titular, String corretora) {
        if (carteiraDAO.consultar(id) != null) {
            throw new IllegalArgumentException("Erro: Já existe uma carteira com este identificador.");
        }
        Carteira novaCarteira = new Carteira(id, titular, corretora);
        carteiraDAO.incluir(novaCarteira);
    }

    public Carteira consultarCarteira(int id) {
        Carteira carteira = carteiraDAO.consultar(id);
        if (carteira == null) {
            throw new IllegalArgumentException("Erro: Carteira não encontrada.");
        }
        return carteira;
    }

    public void editarCarteira(int id, String novoTitular, String novaCorretora) {
        // O método consultarCarteira já lança o erro se não existir
        Carteira carteira = consultarCarteira(id); 
        carteira.setNomeTitular(novoTitular);
        carteira.setCorretora(novaCorretora);
        carteiraDAO.editar(carteira);
    }

    public void excluirCarteira(int id) {
        // Valida se a carteira existe
        consultarCarteira(id);
        
        // não excluir se tiver saldo na conta
        BigDecimal saldo = calcularSaldoCarteira(id);
        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Erro: Não é possível excluir uma carteira com saldo positivo.");
        }
        
        carteiraDAO.excluir(id);
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

    public void comprarMoeda(int idCarteira, BigDecimal quantidade) {
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Erro: A quantidade de compra deve ser maior que zero.");
        }
        
        // Verifica se a carteira existe antes de deixar comprar
        consultarCarteira(idCarteira); 

        Movimentacao compra = new Movimentacao(idCarteira, gerarIdMovimento(), LocalDate.now(), TipoOperacao.COMPRA, quantidade);
        movimentacaoDAO.registrarMovimentacao(compra);
    }

    public void venderMoeda(int idCarteira, BigDecimal quantidade) {
        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Erro: A quantidade de venda deve ser maior que zero.");
        }
        
        // Verifica se a carteira existe antes de deixar vender
        consultarCarteira(idCarteira);

        // Regra de negócio: impedir que venda mais moedas do que possui
        BigDecimal saldoAtual = calcularSaldoCarteira(idCarteira);
        if (saldoAtual.compareTo(quantidade) < 0) {
            throw new IllegalArgumentException("Erro: Saldo insuficiente para realizar a venda. Saldo atual: " + saldoAtual);
        }

        Movimentacao venda = new Movimentacao(idCarteira, gerarIdMovimento(), LocalDate.now(), TipoOperacao.VENDA, quantidade);
        movimentacaoDAO.registrarMovimentacao(venda);
    }

    // Método auxiliar para gerar o ID do movimento na hora da simulação local
    private int gerarIdMovimento() {
        return contadorMovimentos++;
    }

    // Listar carteiras ordenadas por identificador
    public List<Carteira> listarCarteirasPorId() {
        List<Carteira> carteiras = carteiraDAO.listarTodas();
        carteiras.sort(Comparator.comparingInt(Carteira::getIdentificador));
        return carteiras;
    }

    // Listar carteiras ordenadas por nome do titular
    public List<Carteira> listarCarteirasPorNome() {
        List<Carteira> carteiras = carteiraDAO.listarTodas();
        // CASE_INSENSITIVE_ORDER garante que "ana" e "Ana" fiquem juntas na ordem
        carteiras.sort(Comparator.comparing(Carteira::getNomeTitular, String.CASE_INSENSITIVE_ORDER));
        return carteiras;
    }

    // Exibir histórico de movimentação de uma carteira
    public List<Movimentacao> obterHistoricoMovimentacao(int idCarteira) {
        // Valida se a carteira existe antes de buscar o histórico
        consultarCarteira(idCarteira);
        return movimentacaoDAO.listarPorCarteira(idCarteira);
    }

    // Apresentar ganho ou perda total de cada carteira
    public BigDecimal calcularGanhoPerda(int idCarteira) {
        consultarCarteira(idCarteira);

        List<Movimentacao> historico = movimentacaoDAO.listarPorCarteira(idCarteira);
        BigDecimal valorInvestido = BigDecimal.ZERO;

        for (Movimentacao mov : historico) {
            BigDecimal cotacaoNoDia = oraculoClient.obterCotacao(mov.getDataOperacao());
            BigDecimal valorDaOperacao = mov.getQuantidadeMovimentada().multiply(cotacaoNoDia);

            if (mov.getTipoOperacao() == TipoOperacao.COMPRA) {
                valorInvestido = valorInvestido.add(valorDaOperacao);
            } else if (mov.getTipoOperacao() == TipoOperacao.VENDA) {
                valorInvestido = valorInvestido.subtract(valorDaOperacao);
            }
        }

        BigDecimal saldoAtualDeMoedas = calcularSaldoCarteira(idCarteira);
        BigDecimal cotacaoHoje = oraculoClient.obterCotacao(LocalDate.now());
        BigDecimal valorAtualEmReais = saldoAtualDeMoedas.multiply(cotacaoHoje);

        return valorAtualEmReais.subtract(valorInvestido);
    }
}
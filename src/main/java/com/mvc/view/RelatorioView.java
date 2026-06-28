package com.mvc.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import com.mvc.controller.CarteiraController;
import com.mvc.model.Carteira;
import com.mvc.model.Movimentacao;

public class RelatorioView {
    private final Scanner scanner;
    private final CarteiraController controller;

    public RelatorioView(Scanner scanner, CarteiraController controller) {
        this.scanner = scanner;
        this.controller = controller;
    }

    public void exibirMenuRelatorios() {
        int opcao = -1;
        while (opcao != 6) {
            System.out.println("\n" + MenuPrincipalView.ANSI_BLUE + "--- MENU RELATÓRIOS ---" + MenuPrincipalView.ANSI_RESET);
            System.out.println("1. Listar carteiras ordenadas por identificador"); // [cite: 78]
            System.out.println("2. Listar carteiras ordenadas por nome do titular"); // [cite: 79]
            System.out.println("3. Exibir saldo atual de uma carteira"); // [cite: 80]
            System.out.println("4. Exibir histórico de movimentação de uma carteira"); // [cite: 81]
            System.out.println("5. Apresentar ganho ou perda total de cada carteira"); // [cite: 82]
            System.out.println("6. Voltar ao menu principal");
            System.out.print("Escolha uma opção: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer

                switch (opcao) {
                    case 1:
                        listarPorIdUI();
                        break;
                    case 2:
                        listarPorNomeUI();
                        break;
                    case 3:
                        exibirSaldoUI();
                        break;
                    case 4:
                        exibirHistoricoUI();
                        break;
                    case 5:
                        exibirGanhoPerdaUI();
                        break;
                    case 6:
                        break;
                    default:
                        System.out.println(MenuPrincipalView.ANSI_RED + "Erro: Opção inválida!" + MenuPrincipalView.ANSI_RESET);
                }
            } else {
                System.out.println(MenuPrincipalView.ANSI_RED + "Erro: Por favor, digite um número válido." + MenuPrincipalView.ANSI_RESET);
                scanner.nextLine();
            }
        }
    }

    // ==========================================
    // MÉTODOS DE TELA (USER INTERFACE)
    // ==========================================

    private void listarPorIdUI() {
        System.out.println("\n--- Carteiras por Identificador ---");
        List<Carteira> lista = controller.listarCarteirasPorId();
        
        if (lista.isEmpty()) {
            System.out.println(MenuPrincipalView.ANSI_YELLOW + "Nenhuma carteira cadastrada no sistema." + MenuPrincipalView.ANSI_RESET);
            return;
        }
        
        for (Carteira c : lista) {
            System.out.println(c.toString());
        }
    }

    private void listarPorNomeUI() {
        System.out.println("\n--- Carteiras por Nome do Titular ---");
        List<Carteira> lista = controller.listarCarteirasPorNome();
        
        if (lista.isEmpty()) {
            System.out.println(MenuPrincipalView.ANSI_YELLOW + "Nenhuma carteira cadastrada no sistema." + MenuPrincipalView.ANSI_RESET);
            return;
        }
        
        for (Carteira c : lista) {
            System.out.println(c.toString());
        }
    }

    private void exibirSaldoUI() {
        System.out.println("\n--- Saldo Atual ---");
        System.out.print("Digite o ID da carteira: ");
        
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Carteira c = controller.consultarCarteira(id); // Valida se existe e pega o nome
            BigDecimal saldo = controller.calcularSaldoCarteira(id);
            
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Saldo da carteira [" + id + "] de " + c.getNomeTitular() + ": " + saldo + " moedas" + MenuPrincipalView.ANSI_RESET);
            
        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID deve ser um número." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }

    private void exibirHistoricoUI() {
        System.out.println("\n--- Histórico de Movimentação ---");
        System.out.print("Digite o ID da carteira: ");
        
        try {
            int id = Integer.parseInt(scanner.nextLine());
            List<Movimentacao> historico = controller.obterHistoricoMovimentacao(id);
            
            if (historico.isEmpty()) {
                System.out.println(MenuPrincipalView.ANSI_YELLOW + "Nenhuma movimentação registrada para esta carteira." + MenuPrincipalView.ANSI_RESET);
            } else {
                for (Movimentacao mov : historico) {
                    String tipo = (mov.getTipoOperacao().getCodigo() == 'C') ? "COMPRA" : "VENDA";
                    String cor = (tipo.equals("COMPRA")) ? MenuPrincipalView.ANSI_GREEN : MenuPrincipalView.ANSI_RED;
                    
                    System.out.println("Data: " + mov.getDataOperacao() + " | Tipo: " + cor + tipo + MenuPrincipalView.ANSI_RESET + " | Qtd: " + mov.getQuantidadeMovimentada());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID deve ser um número." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }

    private void exibirGanhoPerdaUI() {
        System.out.println("\n--- Ganho ou Perda Total por Carteira ---");
        List<Carteira> lista = controller.listarCarteirasPorId();
        
        if (lista.isEmpty()) {
            System.out.println(MenuPrincipalView.ANSI_YELLOW + "Nenhuma carteira cadastrada no sistema." + MenuPrincipalView.ANSI_RESET);
            return;
        }
        
        // Percorre todas as carteiras do sistema e calcula individualmente [cite: 82]
        for (Carteira c : lista) {
            BigDecimal resultado = controller.calcularGanhoPerda(c.getIdentificador());
            
            // Define a cor: Verde para lucro (>= 0) e Vermelho para prejuízo (< 0)
            String corResultado = (resultado.compareTo(BigDecimal.ZERO) >= 0) ? MenuPrincipalView.ANSI_GREEN : MenuPrincipalView.ANSI_RED;
            
            System.out.println("Carteira [" + c.getIdentificador() + "] - Titular: " + c.getNomeTitular() + 
                               " | Resultado: " + corResultado + "R$ " + resultado + MenuPrincipalView.ANSI_RESET);
        }
    }
}
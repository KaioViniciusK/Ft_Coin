package com.mvc.view;

import java.math.BigDecimal;
import java.util.Scanner;

import com.mvc.controller.CarteiraController;

public class MovimentacaoView {
    private final Scanner scanner;
    private final CarteiraController controller;

    public MovimentacaoView(Scanner scanner, CarteiraController controller) {
        this.scanner = scanner;
        this.controller = controller;
    }

    public void exibirMenuMovimentacao() {
        int opcao = -1;
        while (opcao != 3) {
            System.out.println("\n" + MenuPrincipalView.ANSI_BLUE + "--- MENU MOVIMENTAÇÃO ---" + MenuPrincipalView.ANSI_RESET);
            System.out.println("1. Compra de moeda virtual");
            System.out.println("2. Venda de moeda virtual");
            System.out.println("3. Voltar ao menu principal");
            System.out.print("Escolha uma opção: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer

                switch (opcao) {
                    case 1:
                        comprarMoedaUI();
                        break;
                    case 2:
                        venderMoedaUI();
                        break;
                    case 3:
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


    private void comprarMoedaUI() {
        System.out.println("\n--- Compra de Moeda Virtual ---");
        try {
            System.out.print("Digite o ID da carteira: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Digite a quantidade de moedas para comprar: ");
            BigDecimal quantidade = new BigDecimal(scanner.nextLine());

            controller.comprarMoeda(id, quantidade);
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Sucesso: Compra de " + quantidade + " moedas realizada!" + MenuPrincipalView.ANSI_RESET);

        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID deve ser um número inteiro e a quantidade deve ser um valor numérico válido (ex: 10 ou 15.5)." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }

    private void venderMoedaUI() {
        System.out.println("\n--- Venda de Moeda Virtual ---");
        try {
            System.out.print("Digite o ID da carteira: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Digite a quantidade de moedas para vender: ");
            BigDecimal quantidade = new BigDecimal(scanner.nextLine());

            controller.venderMoeda(id, quantidade);
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Sucesso: Venda de " + quantidade + " moedas realizada!" + MenuPrincipalView.ANSI_RESET);

        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID deve ser um número inteiro e a quantidade deve ser um valor numérico válido (ex: 10 ou 15.5)." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            // Se tentar vender mais do que tem ou se a carteira não existir, o Controller barra e a View mostra o erro aqui
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }
}
package com.mvc.view;

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
                        System.out.println("Compra de moeda - A implementar chamadas ao Controller");
                        break;
                    case 2:
                        System.out.println("Venda de moeda - A implementar chamadas ao Controller");
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
}
package com.mvc.view;

import java.util.Scanner;

import com.mvc.controller.CarteiraController;

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
            System.out.println("1. Listar carteiras ordenadas por identificador");
            System.out.println("2. Listar carteiras ordenadas por nome do titular");
            System.out.println("3. Exibir saldo atual de uma carteira");
            System.out.println("4. Exibir histórico de movimentação de uma carteira");
            System.out.println("5. Apresentar ganho ou perda total de cada carteira");
            System.out.println("6. Voltar ao menu principal");
            System.out.print("Escolha uma opção: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer

                switch (opcao) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        System.out.println("Relatório " + opcao + " - A implementar chamadas ao Controller e DAO");
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
}
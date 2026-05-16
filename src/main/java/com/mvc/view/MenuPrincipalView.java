package com.mvc.view;

import com.mvc.controller.CarteiraController;
import java.util.Scanner;

public class MenuPrincipalView {
    private final Scanner scanner;
    private final CarteiraView carteiraView;
    
    // Códigos ANSI para colorir o terminal
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public MenuPrincipalView(CarteiraController carteiraController) {
        this.scanner = new Scanner(System.in);
        // Passamos o scanner e o controller para as sub-views
        this.carteiraView = new CarteiraView(this.scanner, carteiraController);
    }

    public void exibirMenu() {
        int opcao = -1;
        while (opcao != 5) {
            System.out.println("\n" + ANSI_BLUE + "=== SISTEMA FT COIN ===" + ANSI_RESET);
            System.out.println("1. Carteira");
            System.out.println("2. Movimentação");
            System.out.println("3. Relatórios");
            System.out.println("4. Ajuda");
            System.out.println("5. Sair");
            System.out.print("Escolha uma opção: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer do teclado

                switch (opcao) {
                    case 1:
                        carteiraView.exibirMenuCarteira();
                        break;
                    case 2:
                        System.out.println(ANSI_YELLOW + "Menu de Movimentação em construção..." + ANSI_RESET);
                        break;
                    case 3:
                        System.out.println(ANSI_YELLOW + "Menu de Relatórios em construção..." + ANSI_RESET);
                        break;
                    case 4:
                        System.out.println(ANSI_YELLOW + "Menu de Ajuda em construção..." + ANSI_RESET);
                        break;
                    case 5:
                        System.out.println(ANSI_GREEN + "Saindo do sistema. Até logo!" + ANSI_RESET);
                        break;
                    default:
                        System.out.println(ANSI_RED + "Erro: Opção inválida!" + ANSI_RESET);
                }
            } else {
                System.out.println(ANSI_RED + "Erro: Por favor, digite um número válido." + ANSI_RESET);
                scanner.nextLine(); // Limpa a entrada errada
            }
        }
    }
}
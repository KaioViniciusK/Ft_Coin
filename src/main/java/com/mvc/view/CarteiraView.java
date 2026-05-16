package com.mvc.view;

import java.util.Scanner;

import com.mvc.controller.CarteiraController;

public class CarteiraView {
    private final Scanner scanner;
    private final CarteiraController controller;

    public CarteiraView(Scanner scanner, CarteiraController controller) {
        this.scanner = scanner;
        this.controller = controller;
    }

    public void exibirMenuCarteira() {
        int opcao = -1;
        while (opcao != 5) {
            System.out.println("\n" + MenuPrincipalView.ANSI_BLUE + "--- MENU CARTEIRA ---" + MenuPrincipalView.ANSI_RESET);
            System.out.println("1. Incluir carteira");
            System.out.println("2. Consultar carteira");
            System.out.println("3. Editar carteira");
            System.out.println("4. Excluir carteira");
            System.out.println("5. Voltar ao menu principal");
            System.out.print("Escolha uma opção: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer

                switch (opcao) {
                    case 1:
                        incluirCarteiraUI();
                        break;
                    case 2:
                        System.out.println("Consultar carteira - A implementar pelo Caio/Henrique");
                        break;
                    case 3:
                        System.out.println("Editar carteira - A implementar pelo Caio/Henrique");
                        break;
                    case 4:
                        System.out.println("Excluir carteira - A implementar pelo Caio/Henrique");
                        break;
                    case 5:
                        // Apenas volta para o menu anterior
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

    // O sufixo UI (User Interface) é só pra identificar que é o método de tela
    private void incluirCarteiraUI() {
        System.out.println("\n--- Nova Carteira ---");
        try {
            System.out.print("Digite o ID (número inteiro): ");
            int id = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Digite o Nome do Titular: ");
            String titular = scanner.nextLine();
            
            System.out.print("Digite o Nome da Corretora: ");
            String corretora = scanner.nextLine();

            // A View coleta o dado e manda pro Controller se virar
            controller.criarCarteira(id, titular, corretora);
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Sucesso: Carteira criada!" + MenuPrincipalView.ANSI_RESET);

        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID precisa ser um número inteiro." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            // Se o controller barrar por ID duplicado, o erro estoura aqui vermelho na tela
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }
}
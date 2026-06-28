package com.mvc.view;

import java.util.Scanner;

import com.mvc.controller.CarteiraController;
import com.mvc.model.Carteira;

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
            System.out.print("Escolha uma opÃÂ§ÃÂ£o: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer

                switch (opcao) {
                    case 1:
                        incluirCarteiraUI();
                        break;
                    case 2:
                        consultarCarteiraUI();
                        break;
                    case 3:
                        editarCarteiraUI();
                        break;
                    case 4:
                        excluirCarteiraUI();
                        break;
                    case 5:
                        // Apenas volta para o menu anterior
                        break;
                    default:
                        System.out.println(MenuPrincipalView.ANSI_RED + "Erro: OpÃÂ§ÃÂ£o invÃÂ¡lida!" + MenuPrincipalView.ANSI_RESET);
                }
            } else {
                System.out.println(MenuPrincipalView.ANSI_RED + "Erro: Por favor, digite um nÃÂºmero vÃÂ¡lido." + MenuPrincipalView.ANSI_RESET);
                scanner.nextLine();
            }
        }
    }

    private void incluirCarteiraUI() {
        System.out.println("\n--- Nova Carteira ---");
        try {
            System.out.print("Digite o ID (nÃÂºmero inteiro): ");
            int id = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Digite o Nome do Titular: ");
            String titular = scanner.nextLine();
            
            System.out.print("Digite o Nome da Corretora: ");
            String corretora = scanner.nextLine();

            // A View coleta o dado e manda pro Controller se virar
            controller.criarCarteira(id, titular, corretora);
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Sucesso: Carteira criada!" + MenuPrincipalView.ANSI_RESET);

        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID precisa ser um nÃÂºmero inteiro." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            // Se o controller barrar por ID duplicado, o erro estoura aqui vermelho na tela
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }

    private void consultarCarteiraUI() {
        System.out.println("\n--- Consultar Carteira ---");
        try {
            System.out.print("Digite o ID da carteira: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            Carteira carteira = controller.consultarCarteira(id);
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Carteira encontrada: " + carteira.toString() + MenuPrincipalView.ANSI_RESET);
            
        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID precisa ser um nÃÂºmero inteiro." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }

    private void editarCarteiraUI() {
        System.out.println("\n--- Editar Carteira ---");
        try {
            System.out.print("Digite o ID da carteira que deseja editar: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            // Consulta primeiro para garantir que a carteira existe antes de pedir os novos dados
            Carteira atual = controller.consultarCarteira(id);
            System.out.println("Titular atual: " + atual.getNomeTitular() + " | Corretora atual: " + atual.getCorretora());
            
            System.out.print("Digite o novo Nome do Titular: ");
            String novoTitular = scanner.nextLine();
            
            System.out.print("Digite o novo Nome da Corretora: ");
            String novaCorretora = scanner.nextLine();

            controller.editarCarteira(id, novoTitular, novaCorretora);
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Sucesso: Carteira atualizada!" + MenuPrincipalView.ANSI_RESET);

        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID precisa ser um nÃÂºmero inteiro." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }

    private void excluirCarteiraUI() {
        System.out.println("\n--- Excluir Carteira ---");
        try {
            System.out.print("Digite o ID da carteira que deseja excluir: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            controller.excluirCarteira(id);
            System.out.println(MenuPrincipalView.ANSI_GREEN + "Sucesso: Carteira excluÃÂ­da!" + MenuPrincipalView.ANSI_RESET);
            
        } catch (NumberFormatException e) {
            System.out.println(MenuPrincipalView.ANSI_RED + "Erro: O ID precisa ser um nÃÂºmero inteiro." + MenuPrincipalView.ANSI_RESET);
        } catch (IllegalArgumentException e) {
            // Caso tente excluir uma carteira com saldo ou que nÃÂ£o exista, a mensagem de erro do Controller aparece aqui
            System.out.println(MenuPrincipalView.ANSI_RED + e.getMessage() + MenuPrincipalView.ANSI_RESET);
        }
    }
}
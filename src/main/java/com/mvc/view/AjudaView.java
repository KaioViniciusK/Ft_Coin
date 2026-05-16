package com.mvc.view;

import java.util.Scanner;

public class AjudaView {
    private final Scanner scanner;

    public AjudaView(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibirMenuAjuda() {
        int opcao = -1;
        while (opcao != 3) {
            System.out.println("\n" + MenuPrincipalView.ANSI_BLUE + "--- MENU AJUDA ---" + MenuPrincipalView.ANSI_RESET);
            System.out.println("1. Ajuda do programa");
            System.out.println("2. Créditos");
            System.out.println("3. Voltar ao menu principal");
            System.out.print("Escolha uma opção: ");

            if (scanner.hasNextInt()) {
                opcao = scanner.nextInt();
                scanner.nextLine();

                switch (opcao) {
                    case 1:
                        exibirTextoAjuda();
                        break;
                    case 2:
                        exibirCreditos();
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

    private void exibirTextoAjuda() {
        System.out.println("\n" + MenuPrincipalView.ANSI_GREEN + "=== Ajuda do FT Coin ===" + MenuPrincipalView.ANSI_RESET);
        System.out.println("Este sistema permite gerenciar carteiras de moedas virtuais.");
        System.out.println("- No menu 'Carteira', você pode cadastrar, alterar e excluir usuários.");
        System.out.println("- No menu 'Movimentação', você registra compras e vendas de moedas.");
        System.out.println("- No menu 'Relatórios', você visualiza saldos, históricos e lucros.");
        System.out.println("Use os números indicados nos menus e pressione ENTER para navegar.");
    }

    private void exibirCreditos() {
        System.out.println("\n" + MenuPrincipalView.ANSI_GREEN + "=== Créditos ===" + MenuPrincipalView.ANSI_RESET);
        System.out.println("Sistema FT Coin");
        System.out.println("Data: Maio de 2026");
        System.out.println("Autores (Grupo B-06):");
        System.out.println("- Kaio Vinicius da Silva");
        System.out.println("- Gabriel Hebert Reis de Oliveira");
        System.out.println("- Davi de Paula Garcia");
        System.out.println("- Caio de Jesus Lima");
        System.out.println("- Henrique Carvalho de Mello");
        System.out.println("- Heitor Roberto Mesquita De Souza");
        System.out.println("- Maria Luísa Campos Falcão");
        System.out.println("- João Guilherme Fernandes Frota");
        System.out.println("Copyright (c) 2026 - Faculdade de Tecnologia (FT) Unicamp");
    }
}
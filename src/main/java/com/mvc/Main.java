package com.mvc;

import com.mvc.controller.CarteiraController;
import com.mvc.dao.CarteiraDAOMariaDB;
import com.mvc.dao.MovimentacaoDAOMariaDB;
import com.mvc.model.OraculoClient;
import com.mvc.view.MenuPrincipalView;

public class Main {
    public static void main(String[] args) {
        
        System.out.println("Iniciando o sistema FT Coin conectado ao MariaDB...");

        CarteiraDAOMariaDB carteiraDAO = new CarteiraDAOMariaDB();
        MovimentacaoDAOMariaDB movimentacaoDAO = new MovimentacaoDAOMariaDB();
        
        OraculoClient oraculoClient = new OraculoClient();
  
        CarteiraController controller = new CarteiraController(carteiraDAO, movimentacaoDAO, oraculoClient);
        
        MenuPrincipalView menu = new MenuPrincipalView(controller);
        menu.exibirMenu();
        
        System.out.println("Sistema encerrado com sucesso.");
    }
}
package ftcoin.presenter;

import ftcoin.i18n.ConfiguracaoIdioma;
import ftcoin.model.Carteira;
import ftcoin.service.CarteiraService;
import ftcoin.view.CarteiraDados;
import ftcoin.view.CarteiraView;

/** Presenter das operações de carteira: pede dados à View, valida pelo Service e devolve o resultado. */
public final class CarteiraPresenter {
    private final CarteiraView view;
    private final CarteiraService service;
    private final ConfiguracaoIdioma idioma;

    public CarteiraPresenter(CarteiraView view, CarteiraService service, ConfiguracaoIdioma idioma) {
        this.view = view;
        this.service = service;
        this.idioma = idioma;
    }

    /** Inclui uma carteira. */
    public void incluir() {
        executar(() -> {
            CarteiraDados dados = view.solicitarCarteira(idioma.texto("dlg.carteira.incluir"), null, true);
            if (dados != null) {
                Carteira c = service.incluir(Entradas.id(dados.getId()), dados.getTitular(), dados.getCorretora());
                view.exibirMensagem(idioma.texto("msg.carteira.incluida", c.getId()));
            }
        });
    }

    /** Consulta e mostra uma carteira. */
    public void consultar() {
        executar(() -> {
            String id = view.solicitarId(idioma.texto("dlg.carteira.consultar"));
            if (id != null) {
                Carteira c = service.consultar(Entradas.id(id));
                view.exibirMensagem(idioma.texto("campo.id") + ": " + c.getId() + "\n"
                        + idioma.texto("campo.titular") + ": " + c.getTitular() + "\n"
                        + idioma.texto("campo.corretora") + ": " + c.getCorretora());
            }
        });
    }

    /** Edita titular e corretora de uma carteira existente. */
    public void editar() {
        executar(() -> {
            String id = view.solicitarId(idioma.texto("dlg.carteira.editar"));
            if (id == null) {
                return;
            }
            Carteira atual = service.consultar(Entradas.id(id));
            CarteiraDados dados = view.solicitarCarteira(idioma.texto("dlg.carteira.editar"),
                    new CarteiraDados(String.valueOf(atual.getId()), atual.getTitular(), atual.getCorretora()), false);
            if (dados != null) {
                service.editar(atual.getId(), dados.getTitular(), dados.getCorretora());
                view.exibirMensagem(idioma.texto("msg.carteira.editada", atual.getId()));
            }
        });
    }

    /** Exclui uma carteira, após confirmação. */
    public void excluir() {
        executar(() -> {
            String id = view.solicitarId(idioma.texto("dlg.carteira.excluir"));
            if (id == null) {
                return;
            }
            Carteira atual = service.consultar(Entradas.id(id));
            if (view.confirmar(idioma.texto("dlg.confirmar"), idioma.texto("confirm.excluir", atual))) {
                service.excluir(atual.getId());
                view.exibirMensagem(idioma.texto("msg.carteira.excluida", atual.getId()));
            }
        });
    }

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException e) {
            view.exibirErro(TratadorErros.mensagem(e, idioma));
        }
    }
}

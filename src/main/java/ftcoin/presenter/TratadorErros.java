package ftcoin.presenter;

import ftcoin.dao.PersistenciaException;
import ftcoin.i18n.ConfiguracaoIdioma;
import ftcoin.service.NegocioException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Traduz exceções em mensagens do idioma ativo; falhas técnicas vão para o log, não para o usuário. */
final class TratadorErros {
    private static final Logger LOG = Logger.getLogger(TratadorErros.class.getName());

    private TratadorErros() {
    }

    static String mensagem(RuntimeException e, ConfiguracaoIdioma idioma) {
        if (e instanceof NegocioException) {
            NegocioException negocio = (NegocioException) e;
            return idioma.texto(negocio.getChave(), negocio.getArgumentos());
        }
        if (e instanceof PersistenciaException) {
            LOG.log(Level.SEVERE, "Falha de persistência", e);
            return idioma.texto("erro.persistencia");
        }
        LOG.log(Level.SEVERE, "Erro inesperado", e);
        return idioma.texto("erro.inesperado");
    }
}

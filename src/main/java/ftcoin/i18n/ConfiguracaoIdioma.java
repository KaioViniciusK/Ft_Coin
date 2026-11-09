package ftcoin.i18n;

import ftcoin.model.Idioma;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Internacionalização por ResourceBundle (português, inglês e espanhol). O idioma inicial vem do
 * Locale do sistema (automático) e pode ser trocado em execução (manual).
 */
public final class ConfiguracaoIdioma {
    private static final Logger LOG = Logger.getLogger(ConfiguracaoIdioma.class.getName());
    private static final String BASE = "ftcoin.i18n.mensagens";
    private static final ResourceBundle.Control SEM_FALLBACK =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private Idioma idioma;
    private ResourceBundle pacote;

    /** @param idioma idioma inicial */
    public ConfiguracaoIdioma(Idioma idioma) {
        definir(idioma);
    }

    /** @return configuração no idioma do sistema (português se não houver suporte) */
    public static ConfiguracaoIdioma automatica() {
        return new ConfiguracaoIdioma(Idioma.detectar(Locale.getDefault()));
    }

    /** Troca o idioma em uso. */
    public void definir(Idioma novo) {
        this.idioma = novo;
        this.pacote = ResourceBundle.getBundle(BASE, Locale.forLanguageTag(novo.getCodigo()), SEM_FALLBACK);
    }

    public Idioma getIdioma() {
        return idioma;
    }

    public Locale getLocale() {
        return idioma.getLocale();
    }

    /**
     * Devolve o texto da chave no idioma atual. Argumentos viram texto antes de entrar na mensagem
     * ({0}, {1}...). Chave ausente não interrompe o programa: é registrada e devolvida entre "!".
     *
     * @param chave      chave do texto
     * @param argumentos valores dos parâmetros
     * @return o texto formatado
     */
    public String texto(String chave, Object... argumentos) {
        try {
            String modelo = pacote.getString(chave);
            if (argumentos == null || argumentos.length == 0) {
                return modelo;
            }
            Object[] textos = new Object[argumentos.length];
            for (int i = 0; i < argumentos.length; i++) {
                textos[i] = String.valueOf(argumentos[i]);
            }
            return MessageFormat.format(modelo, textos);
        } catch (MissingResourceException e) {
            LOG.log(Level.WARNING, "Texto ausente: {0} ({1})", new Object[] {chave, idioma});
            return "!" + chave + "!";
        }
    }

    /** @return as chaves do idioma atual, usadas para comparar os três arquivos */
    public java.util.Set<String> chaves() {
        return pacote.keySet();
    }
}

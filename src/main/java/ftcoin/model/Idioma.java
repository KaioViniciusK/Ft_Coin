package ftcoin.model;

import java.util.Locale;

/** Idiomas suportados pela interface. */
public enum Idioma {
    PT("pt"),
    EN("en"),
    ES("es");

    private final String codigo;

    Idioma(String codigo) {
        this.codigo = codigo;
    }

    /** @return o código de duas letras do idioma, usado nos arquivos de recursos */
    public String getCodigo() {
        return codigo;
    }

    /** @return o Locale correspondente, usado para formatar números */
    public Locale getLocale() {
        return this == PT ? Locale.forLanguageTag("pt-BR") : Locale.forLanguageTag(codigo);
    }

    /**
     * Detecção automática: escolhe o idioma pelo Locale; idiomas não suportados caem em português.
     *
     * @param locale o Locale do sistema
     * @return o idioma suportado mais adequado
     */
    public static Idioma detectar(Locale locale) {
        return deCodigo(locale == null ? null : locale.getLanguage());
    }

    /**
     * Seleção manual: converte "pt", "en" ou "es" (sem distinguir maiúsculas) no idioma.
     *
     * @param codigo código de idioma; null ou desconhecido resulta em português
     * @return o idioma
     */
    public static Idioma deCodigo(String codigo) {
        if (codigo != null) {
            for (Idioma idioma : values()) {
                if (idioma.codigo.equalsIgnoreCase(codigo.trim())) {
                    return idioma;
                }
            }
        }
        return PT;
    }
}

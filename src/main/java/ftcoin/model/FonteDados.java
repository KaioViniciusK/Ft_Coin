package ftcoin.model;

/** Fontes de persistência disponíveis. */
public enum FonteDados {
    MEMORIA,
    MARIADB;

    /**
     * Converte o argumento de linha de comando; sem argumento, usa memória.
     *
     * @param argumento "memoria" ou "mariadb" (sem distinguir maiúsculas), ou null
     * @return a fonte escolhida
     * @throws IllegalArgumentException se o argumento não for reconhecido
     */
    public static FonteDados deArgumento(String argumento) {
        if (argumento == null || argumento.isBlank()) {
            return MEMORIA;
        }
        for (FonteDados fonte : values()) {
            if (fonte.name().equalsIgnoreCase(argumento.trim())) {
                return fonte;
            }
        }
        throw new IllegalArgumentException("Fonte de dados desconhecida: " + argumento);
    }
}

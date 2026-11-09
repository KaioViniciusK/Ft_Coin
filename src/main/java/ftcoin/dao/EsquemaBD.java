package ftcoin.dao;

/**
 * Nomes das tabelas e colunas do banco fornecido pelo professor, concentrados em um só lugar.
 * Os nomes das tabelas são em maiúsculas porque o MariaDB em Linux diferencia maiúsculas de minúsculas.
 */
final class EsquemaBD {
    static final String TAB_CARTEIRA = "CARTEIRA";
    static final String COL_CART_ID = "IdCarteira";
    static final String COL_CART_TITULAR = "Titular";
    static final String COL_CART_CORRETORA = "Corretora";

    static final String TAB_MOVIMENTACAO = "MOVIMENTACAO";
    static final String COL_MOV_ID_CARTEIRA = "IdCarteira";
    static final String COL_MOV_ID = "IdMovimento";
    static final String COL_MOV_DATA = "Data";
    static final String COL_MOV_TIPO = "TipoOperacao";
    static final String COL_MOV_QUANTIDADE = "Quantidade";

    private EsquemaBD() {
    }
}

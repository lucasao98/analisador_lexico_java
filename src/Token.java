import java.util.ArrayList;

public class Token {
    private Integer coluna;
    private Integer linha;
    private String token;
    private String lexema;

    public Token(Integer linha, Integer coluna, String token, String lex) {
        this.coluna = coluna;
        this.linha = linha;
        this.token = token;
        this.lexema = lex;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public Integer getColuna() {
        return coluna;
    }

    public void setColuna(Integer coluna) {
        this.coluna = coluna;
    }

    public Integer getLinha() {
        return linha;
    }

    public void setLinha(Integer linha) {
        this.linha = linha;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

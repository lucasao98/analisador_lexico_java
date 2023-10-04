import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static final Character[] alfabeto_upper = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    public static final Character[] moedas = {'G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    public static final Character[] alfabeto_lower = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    public static final Character[] numeros = {'0','1','2','3','4','5','6','7','8','9'};
    public static final Character[] operacoes_arit_log = {'+','-','/','*','~','&','|'} ;
    public static final Character[] operacoes_relacionais = {'!','=','>','<'};
    public static final Character[] atribuicao = {':','='};
    public static final Character[] delimitadores = {',','(',')'};
    public static final Character[] comentarios = {'#'};
    public static final Character[] cadeias = {'\"'};
    public static final Character[] espaco = {' '};
    public static final String[] palavras_reservadas = new String[]{"programa","fim_programa","se","senao","entao","imprima","leia","enquanto"};

    public static void main(String[] args) {
        ArrayList<Token> tabela_tokens = new ArrayList<Token>();
        String path = "/home/luca/IdeaProjects/Compiladores/src/";
        String fileName = "ex3.cic";
        ArrayList<String> saida_erros = new ArrayList<String>();
        tabela_tokens = analisadorLexico(path+fileName, saida_erros);
        geraArquivoSaidaI(tabela_tokens);
        geraArquivoSaidaII(saida_erros);
        geraArquivoSaidaIII(tabela_tokens);
    }

    public static ArrayList<Token> analisadorLexico(String path, ArrayList<String> saida_erros){
        ArrayList <Character> pilha_estatica = new ArrayList<Character>();
        ArrayList<Token> tabela_tokens = new ArrayList<Token>();
        String lexema = "";
        Integer estado = 0;
        Integer linha = 0;
        String variavel_aux_erro = "";

        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            String line = br.readLine();
            Integer i = 0;
            Character caractere_atual = null;
            Integer coluna_inicial_caractere = null;
            Boolean comentario_fechado = false;
            String linha_abertura_comentario = "";

            while (line != null){
                if(line.isEmpty()){
                    saida_erros.add("["+linha+"]"+"\n");
                    line = br.readLine();
                    linha++;
                }
                line = preparaLinha(line);

                for(i=0;i<line.length();i++){
                    if(line.charAt(i) == ' ' && estado != 14){
                        continue;
                    }
                    caractere_atual = line.charAt(i);

                    switch (estado){
                        case 0:
                            if(buscaElementoNoArray(caractere_atual, comentarios, null,null)){
                                estado = 17;
                            }else if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                coluna_inicial_caractere = i;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 39;
                            }else if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'G','Z')){
                                coluna_inicial_caractere = i;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 6;
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                coluna_inicial_caractere = i;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 1;
                            }else if(buscaElementoNoArray(caractere_atual, alfabeto_lower, null, null)){
                                coluna_inicial_caractere = i;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 31;
                            }else if(buscaElementoNoArray(caractere_atual, delimitadores, null, null)){
                                coluna_inicial_caractere = i;
                                defineDelimitador(caractere_atual, estado,linha,coluna_inicial_caractere,tabela_tokens,pilha_estatica);
                            }else if(buscaElementoNoArray(caractere_atual, cadeias, null, null)){
                                coluna_inicial_caractere = i;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 13;
                            }else if(caractere_atual == '\'' ){
                                estado = 20;
                                coluna_inicial_caractere = i;
                                linha_abertura_comentario = line;
                            }else if(buscaElementoNoArray(caractere_atual, operacoes_relacionais, null,null)){
                                coluna_inicial_caractere = i;
                                if(caractere_atual == '<'){
                                    setPilhaEstatica(caractere_atual, pilha_estatica);
                                    estado = 34;
                                }else if(caractere_atual == '='){
                                    estado = 33;
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_IGUAL", "",pilha_estatica, estado,tabela_tokens);
                                    i = voltaCursor(i);
                                }else if(caractere_atual == '>'){
                                    estado = 36;
                                    setPilhaEstatica(caractere_atual, pilha_estatica);
                                }else if(caractere_atual == '!'){
                                    estado = 30;
                                }
                            }else if(buscaElementoNoArray(caractere_atual, atribuicao, null,null)){
                                coluna_inicial_caractere = i;
                                if(caractere_atual == ':'){
                                    setPilhaEstatica(caractere_atual, pilha_estatica);
                                    estado = 37;
                                }
                            }else if(buscaElementoNoArray(caractere_atual, operacoes_arit_log, null,null)){
                                coluna_inicial_caractere = i;
                                estado = 29;
                                if(caractere_atual == '+'){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_SOMA", "", pilha_estatica, estado,tabela_tokens);
                                }else if(caractere_atual == '-'){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_SUB", "", pilha_estatica, estado,tabela_tokens);
                                }else if(caractere_atual == '*'){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_MULT", "", pilha_estatica, estado,tabela_tokens);
                                }else if(caractere_atual == '/'){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_DIV", "", pilha_estatica, estado,tabela_tokens);
                                }else if(caractere_atual == '&'){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_AND", "", pilha_estatica, estado,tabela_tokens);
                                }else if(caractere_atual == '|'){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_OR", "", pilha_estatica, estado,tabela_tokens);
                                }else if(caractere_atual == '~'){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_NOT", "", pilha_estatica, estado,tabela_tokens);
                                }
                            }
                            else if(buscaElementoNoArray(caractere_atual, moedas, null,null)){
                                coluna_inicial_caractere = i;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 6;
                            }else if(caractere_atual == '\n'){
                                continue;
                            }
                            else {
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Token não reconhecido", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                            }
                            break;
                        case 1:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == '.'){
                                estado = 2;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == 'e'){
                                estado = 22;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                estado = 16;
                                for(Character ch: pilha_estatica){
                                    lexema += ch;
                                }
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_NUMERO", lexema,pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                                i = voltaCursor(i);

                            }
                            break;
                        case 2:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')
                            || buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                estado = 4;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Numero Mal Formatado", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 3:
                            for(Character ch: pilha_estatica){
                                lexema += ch;
                            }
                            estado = estadoFinal(linha, coluna_inicial_caractere,"TK_NUMERO", lexema,pilha_estatica, estado,tabela_tokens);
                            lexema = limpaLexema(lexema);
                            i =voltaCursor(i);
                            break;
                        case 4:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == 'e'){
                                estado = 12;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                estado = 3;
                                for(Character ch: pilha_estatica){
                                    lexema += ch;
                                }
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_NUMERO", lexema, pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                            }
                            break;
                        case 5:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, null,null) ||
                                    buscaElementoNoArray(caractere_atual, alfabeto_lower, null,null) ||
                                    buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == '>'){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 23;

                                for(Character ch: pilha_estatica){
                                    lexema += ch;
                                }
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_ID", lexema,pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                            }
                            break;
                        case 6:
                            if(caractere_atual == '$'){
                                estado = 7;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Moeda Mal Formatada", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 7:
                            if(buscaElementoNoArray(caractere_atual, numeros, null, null)){
                                estado = 8;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Moeda Mal Formatada", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 8:
                            if(buscaElementoNoArray(caractere_atual, numeros, null, null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == '.'){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 9;
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Moeda Mal Formatada", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 9:
                            if(buscaElementoNoArray(caractere_atual, numeros, null, null)){
                                estado = 10;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Moeda Mal Formatada", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 10:
                            if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                estado = 11;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Moeda Mal Formatada", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 11:
                            estado = 41;
                            break;
                        case 12:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F') ||
                                    buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                estado = 24;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }
                            break;
                        case 13:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, null,null) ||
                                    buscaElementoNoArray(caractere_atual, alfabeto_lower, null,null) ||
                                    buscaElementoNoArray(caractere_atual, numeros, null,null) ||
                                    buscaElementoNoArray(caractere_atual, operacoes_relacionais, null,null) ||
                                    buscaElementoNoArray(caractere_atual, operacoes_arit_log, null,null) ||
                                    buscaElementoNoArray(caractere_atual, atribuicao, null,null) ||
                                    buscaElementoNoArray(caractere_atual, delimitadores, null,null)){
                                estado = 14;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == '\"'){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 15;

                                for(Character ch: pilha_estatica){
                                    lexema += ch;
                                }
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_CADEIA", lexema,pilha_estatica, estado, tabela_tokens);
                                lexema = limpaLexema(lexema);
                            }
                            break;
                        case 14:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, null,null) ||
                                    buscaElementoNoArray(caractere_atual, alfabeto_lower, null,null) ||
                                    buscaElementoNoArray(caractere_atual, numeros, null,null) ||
                                    buscaElementoNoArray(caractere_atual, operacoes_relacionais, null,null) ||
                                    buscaElementoNoArray(caractere_atual, operacoes_arit_log, null,null) ||
                                    buscaElementoNoArray(caractere_atual, atribuicao, null, null) ||
                                    buscaElementoNoArray(caractere_atual, delimitadores, null, null) ||
                                    buscaElementoNoArray(caractere_atual, espaco, null, null)
                            ){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == '\"'){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 15;

                                for(Character ch: pilha_estatica){
                                    lexema += ch;
                                }
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_CADEIA", lexema,pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro,linha,line,coluna_inicial_caractere, "Cadeia não fechada", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                            }
                            break;
                        case 17:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, null,null)){
                                estado = 18;
                            }else if(buscaElementoNoArray(caractere_atual, alfabeto_lower, null,null)){
                                estado = 18;
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                estado = 18;
                            }else if(buscaElementoNoArray(caractere_atual, operacoes_relacionais, null,null)){
                                estado = 18;
                            }else if(buscaElementoNoArray(caractere_atual, operacoes_arit_log, null,null)){
                                estado = 18;
                            }else if(buscaElementoNoArray(caractere_atual, atribuicao, null,null)){
                                estado = 18;
                            }else if(buscaElementoNoArray(caractere_atual, delimitadores, null,null)){
                                estado = 18;
                            }
                            break;
                        case 18:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, null,null)){
                            }else if(buscaElementoNoArray(caractere_atual, alfabeto_lower, null,null)){
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                            }else if(buscaElementoNoArray(caractere_atual, operacoes_relacionais, null,null)){
                            }else if(buscaElementoNoArray(caractere_atual, operacoes_arit_log, null,null)){
                            }else if(buscaElementoNoArray(caractere_atual, atribuicao, null,null)){
                            }else if(buscaElementoNoArray(caractere_atual, delimitadores, null,null)){
                            }else if(caractere_atual == '\n'){
                                estado = 19;
                                estado = retornaEstadoInicial();
                                coluna_inicial_caractere = 0;
                            }
                            break;
                        case 20:
                            if(caractere_atual == '\''){
                                estado = 42;

                            }
                            break;
                        case 21:
                            if(caractere_atual == '\''){
                                estado = 27;
                            }
                            break;
                        case 22:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                estado = 12;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                estado = 12;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == '-'){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 25;
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Numero Mal Formatado", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 24:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                estado = 3;
                            }
                            break;
                        case 25:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                estado = 12;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                estado = 12;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere,"Numero Mal Formatado", saida_erros);
                                estado = retornaEstadoInicial();
                                pilha_estatica.clear();
                                i = voltaCursor(i);
                            }
                            break;
                        case 26:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == 'e'){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 22;
                            }else{
                                estado = 3;
                            }
                            break;
                        case 27:
                            if(caractere_atual == '\''){
                                estado = 44;
                            }
                            break;
                        case 30:
                            if(caractere_atual == '='){
                                estado = 32;
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_DIFERENTE", "",pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                            }
                            break;
                        case 31:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_lower, null,null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else if(caractere_atual == '_'){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                            }else{
                                estado = 40;
                                lexema = salvaLexema(pilha_estatica);

                                if(verificaPalavraReservada(lexema)){
                                    estado = estadoFinal(linha, coluna_inicial_caractere,"TK_"+lexema, "",pilha_estatica, estado,tabela_tokens);
                                    i = voltaCursor(i);
                                }
                                else{
                                    variavel_aux_erro = setError(variavel_aux_erro, linha, line, coluna_inicial_caractere, "Palavra Reservada não encontrada", saida_erros);
                                    estado = retornaEstadoInicial();
                                    pilha_estatica.clear();
                                    i = voltaCursor(i);
                                }
                                lexema = limpaLexema(lexema);
                            }
                            break;
                        case 34:
                            if(buscaElementoNoArray(caractere_atual, alfabeto_lower, null,null)){
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 5;
                            }else if(caractere_atual == '='){
                                estado = 32;
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_MENOR_IGUAL", "",pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                            }else{
                                estado = 35;
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_MENOR", "",pilha_estatica, estado,tabela_tokens);
                                i = voltaCursor(i);
                            }
                            break;
                        case 36:
                            if(caractere_atual == '='){
                                estado = 32;
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_MAIOR_IGUAL", "",pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                            }else{
                                estado = 35;
                                for(Character ch: pilha_estatica){
                                    lexema += ch;
                                }
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_MAIOR", lexema,pilha_estatica, estado,tabela_tokens);
                                lexema = limpaLexema(lexema);
                                i = voltaCursor(i);
                            }
                            break;
                        case 37:
                            if(caractere_atual == '='){
                                estado = 38;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = estadoFinal(linha, coluna_inicial_caractere,"TK_ATRIB", "",pilha_estatica, estado,tabela_tokens);
                            }
                        break;
                        case 39:
                           if(caractere_atual == '$'){
                               setPilhaEstatica(caractere_atual, pilha_estatica);
                               estado = 7;
                           }else if(buscaElementoNoArray(caractere_atual, alfabeto_upper, 'A','F')){
                                coluna_inicial_caractere = i;
                                setPilhaEstatica(caractere_atual, pilha_estatica);
                                estado = 26;
                           }else if(buscaElementoNoArray(caractere_atual, numeros, null,null)){
                               coluna_inicial_caractere = i;
                               setPilhaEstatica(caractere_atual, pilha_estatica);
                               estado = 26;
                           }else if(caractere_atual == '.'){
                               setPilhaEstatica(caractere_atual, pilha_estatica);
                               estado = 26;
                           }else{
                               estado = 16;
                           }
                            break;
                        case 41:
                            for(Character ch: pilha_estatica){
                                lexema += ch;
                            }
                            estado = estadoFinal(linha, coluna_inicial_caractere,"TK_MOEDA", lexema,pilha_estatica, estado,tabela_tokens);
                            lexema = limpaLexema(lexema);
                            i = voltaCursor(i);
                            break;
                        case 42:
                            if(caractere_atual == '\''){
                                estado = 43;
                            }
                            break;
                        case 43:
                            if(caractere_atual == '\''){
                                estado = 27;
                            }
                            estado = 21;
                            break;
                        case 44:
                            if(caractere_atual == '\''){
                                estado = 45;
                            }
                            break;
                        case 45:
                            i = voltaCursor(i);
                            comentario_fechado = true;
                            estado = 0;
                            break;
                    }
                }
                if(comentario_fechado == false && estado == 21){
                    line = lerArquivoNovamente(path, linha_abertura_comentario);
                    estado = 0;
                    variavel_aux_erro = linha_abertura_comentario;
                    setError(variavel_aux_erro, linha, linha_abertura_comentario, coluna_inicial_caractere, "Linha de comentário não finalizada", saida_erros);
                    linha++;
                }

                if(!variavel_aux_erro.isEmpty()){
                    variavel_aux_erro = "";
                    line = br.readLine();
                }else{
                    saida_erros.add("["+linha+"]"+line);
                    line = br.readLine();
                    linha++;
                }
            }


        }catch (IOException e){
            System.out.println("Error "+e.getMessage());
        }

        return tabela_tokens;
    }

    public static Boolean buscaElementoNoArray(Character elementoProcurado, Character[] lista, Character primeiro_elemento, Character ultimo_elemento){
        Integer i = null;
        Integer it = null;
        Integer tam = null;
        Boolean findLast = false;
        Boolean findFirst = false;

        if(primeiro_elemento == null && ultimo_elemento == null){
            i = 0;
            tam = lista.length;
        }else{
            for(Integer k = 0;k<lista.length;k++){
                if(findFirst == false){
                    if(lista[k] == primeiro_elemento){
                        i = k;
                        findFirst = true;
                    }
                }
                if(findLast == false){
                    if(lista[k] == ultimo_elemento){
                        tam = k+1;
                        findLast = true;
                    }
                }
            }
        }

        for(it = i;it<tam;it++){
            if(lista[it].equals(elementoProcurado)){
                return true;
            }
        }

        return false;
    }

    public static Boolean verificaPalavraReservada(String lexema){
        for(String palavra_reservada: palavras_reservadas){
            if(palavra_reservada.equals(lexema)){
                return true;
            }
        }
        return false;
    }
    public static String limpaLexema(String lexema){
        lexema = "";

        return lexema;
    }

    public static Integer retornaEstadoInicial(){
        return 0;
    }

    public static String preparaLinha(String linha){
        linha += '\n';

        return linha;
    }

    public static Integer voltaCursor(Integer posicao_atual){
        posicao_atual--;
        return posicao_atual;
    }

    public static Integer estadoFinal(Integer linha, Integer coluna, String tipo_id, String lexema,ArrayList<Character> pilha_estatica, Integer estado,ArrayList<Token> tabela_tokens){
        Token tk = new Token(linha, coluna, tipo_id,lexema);
        tabela_tokens.add(tk);
        pilha_estatica.clear();
        estado = retornaEstadoInicial();

        return estado;
    }

    public static void  setPilhaEstatica(Character ch, ArrayList<Character> pilha){
        pilha.add(ch);
    }

    public static Integer defineDelimitador(Character ch, Integer estado, Integer linha, Integer col, ArrayList<Token> tabela_tokens, ArrayList<Character> pilha_estatica){
        estado = 28;
        if(ch == '('){
            estado = estadoFinal(linha, col,"TK_AB_PAREN", "", pilha_estatica, estado,tabela_tokens);
        }else if(ch == ')'){
            estado = estadoFinal(linha, col,"TK_FE_PAREN", "", pilha_estatica, estado,tabela_tokens);
        }else if(ch == ','){
            estado = estadoFinal(linha, col,"TK_VIRGULA", "", pilha_estatica, estado,tabela_tokens);
        }

        return estado;
    }

    public static String salvaLexema(ArrayList<Character> pilha){
        String variavel_auxiliar = "";
        StringBuilder lexemaBuilder = new StringBuilder(variavel_auxiliar);
        for(Character ch: pilha){
            lexemaBuilder.append(ch);
        }
        variavel_auxiliar = lexemaBuilder.toString();

        return variavel_auxiliar;
    }

    public static String setError(String variavel_aux_erro, Integer linha, String line, Integer coluna_inicial_caractere, String nome_erro, ArrayList<String> saida_erros){
        variavel_aux_erro ="["+linha+"] "+line;
        Integer it = null;
        String linha_erro = "   ";
        for(it = 0;it <= coluna_inicial_caractere;it++){
            linha_erro += '_';
        }
        linha_erro += "^\n";
        variavel_aux_erro += linha_erro;
        variavel_aux_erro += "   Erro léxico na linha "+linha+" coluna "+coluna_inicial_caractere+":";
        variavel_aux_erro +=  nome_erro+"\n";
        saida_erros.add(variavel_aux_erro);

        return variavel_aux_erro;
    }

    public static String lerArquivoNovamente(String path, String last_line){
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            line = preparaLinha(line);
            if(line.equals(last_line)){
                line = br.readLine();
            }

            return line;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void geraArquivoSaidaI(ArrayList<Token> tabela_tokens){
        String path = "/home/luca/IdeaProjects/Compiladores/src/Saída I";
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path))){
            bw.write("+-----+------+--------------+-----------------------+");
            bw.newLine();
            bw.write("| LIN |\t"+" COL |"+" TOKEN\t\t|"+" LEXEMA\t\t\t\t|");
            bw.newLine();
            bw.write("+-----+------+--------------+-----------------------+");
            bw.newLine();
            for(Integer k =0;k<tabela_tokens.size();k++){
                bw.write("|\t"+tabela_tokens.get(k).getLinha()+" |\t  "+tabela_tokens.get(k).getColuna()+" |\t"+tabela_tokens.get(k).getToken().toUpperCase()+" | "+tabela_tokens.get(k).getLexema()+"\t\t\t\t\t\t|");
                bw.newLine();
                bw.write("+-----+------+--------------+-----------------------+");
                bw.newLine();
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Saída I gerada com sucesso!");
    }

    public static void geraArquivoSaidaII(ArrayList<String> saida_erros){
        String path = "/home/luca/IdeaProjects/Compiladores/src/Saída II";

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path))){
            Integer tamanho_array = saida_erros.size();
            Integer it = null;
            for(it = 0;it < tamanho_array;it++){
                bw.write(saida_erros.get(it));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Saída II gerada com sucesso!");
    }

    public static void geraArquivoSaidaIII(ArrayList<Token> tabela_tokens){
        String path = "/home/luca/IdeaProjects/Compiladores/src/Saída III";

        HashMap<String, Integer> tabela_auxiliar = new HashMap<String, Integer>();
        Integer contador = 0;
        Integer total = 0;
        for(Integer k = 0;k<tabela_tokens.size();k++){
            if(tabela_auxiliar.get(tabela_tokens.get(k).getToken()) == null){
                contador = 1;
                tabela_auxiliar.put(tabela_tokens.get(k).getToken(),contador);
                total += 1;
            }else{
                contador++;
                tabela_auxiliar.put(tabela_tokens.get(k).getToken(),contador);
                total += 1;
            }
        }
        HashMap<String, Integer> tabela_ordenada = new HashMap<String, Integer>();
        tabela_ordenada = tabela_auxiliar.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path))){
            bw.write("+-----+-------------+");
            bw.newLine();
            bw.write("| TOKEN\t|\tUsos\t|");
            bw.newLine();
            bw.write("+-----+-------------+");
            bw.newLine();
            Set<String> keys = tabela_ordenada.keySet();
            for(String id:keys){
                bw.write("| "+id.toUpperCase()+" | "+tabela_ordenada.get(id)+"\t|\n");
                bw.write("+-----+-------------+");
                bw.newLine();
            }
            bw.write("| TOTAL | "+total+" \t\t|");
            bw.newLine();
            bw.write("+-----+-------------+");
        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("Saída III gerada com sucesso!");
    }
}
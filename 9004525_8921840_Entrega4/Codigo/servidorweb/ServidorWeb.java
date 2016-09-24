/***********************************************************************/
/* UNIVERSIDADE DE SAO PAULO - ESCOLA DE ARTES, CIENCIAS E HUMANIDADES */
/*---------------------------------------------------------------------*/
/* Bruno Salerno Rocha - 9004525 - Turma 94                            */
/* Caio Tavares Cruz - 8921840 - Turma 94                              */
/*---------------------------------------------------------------------*/
/* Classe principal do Projeto de Redes - 2o Sem 2016                  */
/* Servidor Web Multithreaded                                          */
/***********************************************************************/

package servidorweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.* ;
import java.util.ArrayList;
import java.util.List;

public class ServidorWeb {
    
    /* Parte C - Variáveis do arquivo de configuração */
    static String senhaAutenticacao;
    static List<String> diretoriosAutenticados;
    static int opcaoDiretorio;
    static String headerListaConteudo;
    static String footerListaConteudo;
    static int porta;
    static File logFile;
    /* Fim Parte C - Variáveis do arquivo de configuração */

    //Thread principal - Thread MAIN
    public static void main(String[] args) throws Exception{
        
        /* Parte C - Lendo Arquivo de Configuração */
        
        //Obter arquivo de configuracao - Se ele nao existir da erro fatal e aborta
        File config = ServidorWeb.getConfigFile();
        
        //Ler o arquivo de configuracao e preencher as variaveis globais
        leConfig(config);
        
        //Se a opcao de controle de diretorios for 1 - listar conteudo - preencher o header e o footer
        if(opcaoDiretorio == 1)
            preencheDadosPagina();
        
        //Obter arquivo de log
        Logger.getLogFile();
        
        /* Fim Parte C */
        
        //Criacao do socket para a porta escolhida
        ServerSocket socket = new ServerSocket(ServidorWeb.porta);
        
        //while(true) para estar sempre aceitando requisicoes
        while(true){
            //Aceita conexao
            Socket connectionSocket = socket.accept();
            
            //Cria o HttpRequest associado ao socket
            HttpRequest req = new HttpRequest(connectionSocket);
            
            //Cria uma nova thread para processar a requisicao
            Thread thread = new Thread(req);
            
            //Processa a solicitação HTTP na nova thread
            thread.start();
            
        }
   }
    
    //Método que pega o arquivo de configuracao
    private static File getConfigFile()
    {
        File config = new File("config/config.txt");
        
        if(!config.exists())
        {
            System.out.println("Falha ao obter o arquivo de configuração\nErro fatal\nEncerrando servidor");
            System.exit(1);
        }
        
        return config;
    }
    
    //Método que lê o arquivo de configuração e preenche os atributos globais
    private static void leConfig(File config) throws Exception
    {
        BufferedReader r = new BufferedReader(new FileReader(config));
        String[] linhaPorta, linhaSenha, linhaOpcao;
        String linhaDiretorio;
        List<String> listaDiretorios;
        
        listaDiretorios = new ArrayList<String>();
        
        //As quatro primeiras linhas são cabeçalho, basta ignorar
        for(int i = 1; i <= 4; i++)
            r.readLine();
        
        //A quinta linha contem a porta
        linhaPorta = r.readLine().split(" ");
        ServidorWeb.porta = Integer.parseInt(linhaPorta[1]);
        
        //Se a porta for menor que 1025, a porta 6789 é usada por padrão
        if(ServidorWeb.porta < 1025)
            ServidorWeb.porta = 6789;
        
        //A sexta linha esta em branco
        r.readLine();
        
        //A sétima linha inicia a lista de diretorios autenticados
        r.readLine();
        while((linhaDiretorio = r.readLine()).length() != 0)
        {
            //Enquanto não encontrar a quebra de linha, ir guardando os diretorios
           listaDiretorios.add(linhaDiretorio);
        }
        ServidorWeb.diretoriosAutenticados = listaDiretorios;
        
        //A proxima linha é a da senha
        linhaSenha = r.readLine().split(" ");
        ServidorWeb.senhaAutenticacao = linhaSenha[3];
        
        //A próxima linha está vazia
        r.readLine();
        
        //A próxima linha é a opção de diretorio
        linhaOpcao = r.readLine().split(" ");
        ServidorWeb.opcaoDiretorio = Integer.parseInt(linhaOpcao[3]);
        
        //Encerrar leitura
        r.close();
        
    }
    
    public static void preencheDadosPagina()
    {
        BufferedReader scan1, scan2;
        
        ServidorWeb.headerListaConteudo = "";
        ServidorWeb.footerListaConteudo = "";
        
        try
        {
            String nextLine;
            
            //tentar obter os arquivos de cabecalho e rodape da listagem de arquivos
            scan1 = new BufferedReader(new FileReader("config/header.html"));
            while((nextLine = scan1.readLine()) != null)
                ServidorWeb.headerListaConteudo = ServidorWeb.headerListaConteudo + nextLine;
            scan1.close();
            
            scan2 = new BufferedReader(new FileReader("config/footer.html"));         
            while((nextLine = scan2.readLine()) != null)
                ServidorWeb.footerListaConteudo = ServidorWeb.footerListaConteudo + nextLine;
            scan2.close();
        }
        catch(FileNotFoundException e)
        {
            //Se ele nao achar um dos arquivos, montar em texto um HTML basico
            ServidorWeb.headerListaConteudo = "<HTML>"
                    + "<HEAD><TITLE>Lista de Arquivos</TITLE></HEAD>"
                    + "<BODY><P>";
            
            ServidorWeb.footerListaConteudo = "</P></BODY>"
                    + "</HTML>";
        }
        catch(Exception e)
        {
            System.out.println("Erro: " + e);
        }
        
    }
}

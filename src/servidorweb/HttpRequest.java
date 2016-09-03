/***********************************************************************/
/* UNIVERSIDADE DE SAO PAULO - ESCOLA DE ARTES, CIENCIAS E HUMANIDADES */
/*---------------------------------------------------------------------*/
/* Bruno Salerno Rocha - 9004525 - Turma 94                            */
/* Caio Tavares Cruz - 8921840 - Turma 94                              */
/*---------------------------------------------------------------------*/
/* Classe responsavel por processar as solicitações HTTP               */
/* Servidor Web Multithreaded                                          */
/***********************************************************************/

package servidorweb;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.activation.MimetypesFileTypeMap;

final class HttpRequest implements Runnable
{
    //Referencia de fim de cabecalho
    private final static String CLRF = "\r\n";
    
    //Referencia do socket utilizado para conexao
    private Socket connectionSocket;
    
    //Metodo construtor - Associa um socket ao objeto de thread
    public HttpRequest(Socket _processador)
    {
        this.connectionSocket = _processador;
    }
   
    //Metodo run - organiza os processamentos de requisicao - Necessario por conta da interface Runnable
    public void run()
    {
        try
        {
            //Tentar processar a requisicao
            processRequest();
        }
        catch(Exception e)
        {
            //Imprimir a excecao, caso haja
            System.out.println("Erro: " + e);
        }
        
    }
    
    //Metodo responsavel por processar as requisicoes HTTP
    private void processRequest() throws Exception
    {
        String requestLine, headerLine, nomeArq, statusLine, contentTypeLine;
        FileInputStream enviaArquivo, entityBody;
        
        //Abre uma stream para ler o que foi recebido
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        
        //Abre uma stream para devolver mensagens
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        
        //Fazer a leitura dos dados - Linha de requisicao
        requestLine = inFromClient.readLine();
        
        //Iniciar impressao do novo bloco
        System.out.println();
        System.out.println(requestLine);
        
        //Fazer a leitura dos dados - enquanto houver linhas de cabecalho
        while((headerLine = inFromClient.readLine()).length() != 0)
        {
            System.out.println(headerLine);
        }
        
        /* Parte B: Enviando uma Resposta - Begin */
        
        //Pegar o nome do arquivo
        StringTokenizer token = new StringTokenizer(requestLine);
        token.nextToken();
        
        nomeArq = token.nextToken();
        
        //O browser envia o nome do arquivo com uma barra "\"
        //Colocar um ponto "." antes do nome do arquivo referencia o diretório atual
        nomeArq = "." + nomeArq;
        
        //Tentar obter o arquivo
        try
        {
            enviaArquivo = new FileInputStream(nomeArq);
            
            statusLine = "HTTP/1.1 200 OK" + CLRF;
            contentTypeLine = "Content-type: " + this.contentType(nomeArq) + CLRF;
            entityBody = enviaArquivo;
        }
        catch(FileNotFoundException e)
        {
            statusLine = "HTTP/1.1 404 Not Found";
            contentTypeLine = "Content-type: text/html" + CLRF;
            entityBody = getErro404();
        }
        
        //Enviar a resposta
        
        //Linha de status
        outToClient.writeBytes(statusLine);
        
        //Cabeçalho
        outToClient.writeBytes(contentTypeLine);
        
        //Marcador de fim de cabeçalho
        outToClient.writeBytes(CLRF);
        
        //Enviar os dados
        
        /* Parte B: Enviando uma Resposta - End */
        
        //Fechar as streams
        inFromClient.close();
        outToClient.close();
        connectionSocket.close();
    }
    
    //Método que recebe uma nome de arquivo (string) e retorna um content-type
    //Alteramos esse método pois o da especificação não considerava a classe nativa do Java que já retorna o tipo
    //MIME de um arquivo
    public String contentType(String nomeArq)
    {
        File arq = new File(nomeArq);
        MimetypesFileTypeMap mapa = new MimetypesFileTypeMap();
        
        return mapa.getContentType(arq);
    }
    
    //Método que retorna os dados do arquivo que contém o ERRO 404
    public FileInputStream getErro404()
    {
        try
        {
            return new FileInputStream("Erro404.html");
        }
        catch(Exception e)
        {
            System.out.println("Não foi possível retornar o arquivo de erro 404.");
            return null;
        }
    }

}
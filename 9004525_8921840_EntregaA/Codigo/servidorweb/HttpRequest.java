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
        String requestLine, headerLine;
        
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
        
        //Fechar as streams
        inFromClient.close();
        outToClient.close();
        connectionSocket.close();
    }

}
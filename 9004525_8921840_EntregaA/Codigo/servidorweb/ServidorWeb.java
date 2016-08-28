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

import java.net.* ;

public class ServidorWeb {

    //Thread principal - Thread MAIN
    public static void main(String[] args) throws Exception{
        //Definicao de porta do listener
        int port = 6789; 
        
        //Criacao do socket para a porta escolhida
        ServerSocket socket = new ServerSocket(port);
        
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
}

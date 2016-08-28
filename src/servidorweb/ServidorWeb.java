
package servidorweb;

import java.io.* ;
import java.net.* ;
import java.util.* ;

public class ServidorWeb {

    
    public static void main(String[] args) throws Exception{
        //Definicao de porta do listener
        int port = 6789; 
        //Criacao do socket para a porta escolhida
        ServerSocket socket = new ServerSocket(port);
        //while(true) para estar sempre aceitando requisicoes
        while(true){
            //Aceita conexao
            Socket connectionSocket = socket.accept();
            //Le conexao e imprime o que foi recebido
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            System.out.println(inFromClient.readLine());
        }
   }
    
    
}

final class HttpRequest implements Runnable
{

   
    public void run() {
      }

}

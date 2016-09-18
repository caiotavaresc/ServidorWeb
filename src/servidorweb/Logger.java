/***********************************************************************/
/* UNIVERSIDADE DE SAO PAULO - ESCOLA DE ARTES, CIENCIAS E HUMANIDADES */
/*---------------------------------------------------------------------*/
/* Bruno Salerno Rocha - 9004525 - Turma 94                            */
/* Caio Tavares Cruz - 8921840 - Turma 94                              */
/*---------------------------------------------------------------------*/
/* Classe responsavel por escrever informacoes no registo de log       */
/* Servidor Web Multithreaded                                          */
/***********************************************************************/

package servidorweb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public final class Logger implements Runnable{
    String LinhaHost;
    Date DataRequisicao;
    String Conteudo;
    long QuantBytesResposta;
    
    //Metodo que pega o arquivo de log. Se ele nao existir, entao cria
    public static void getLogFile()
    {
        boolean exists;
        
        ServidorWeb.logFile = new File("config/log.txt");
        exists = ServidorWeb.logFile.exists();
            
        //Se o arquivo nao existir, cria-lo
        try
        {
            FileWriter arq = new FileWriter(ServidorWeb.logFile, true);
            PrintWriter print = new PrintWriter(arq);
            
            //Mover o ponteiro para o fim do arquivo

            //Se o arquivo nao existia antes, criar o cabecalho
            if(!exists)
            {
                print.println(rept("#",46));
                print.println("#  Arquivo de Log - Registro de Requisições  #");
                print.println(rept("#",46));
                print.println();
            }
                
            //Escrever data/hora de inicio do servidor
            print.println("Servidor iniciado em: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            print.println(rept("-",46));
            print.println();

            //fechar o fluxo
            print.close();
            arq.close();
        }
        catch(IOException e)
        {
            System.out.println("Nao foi possivel obter o arquivo de log\nErro fatal\nEncerrando programa");
            System.exit(1);
        }
    }
    
    //Metodo auxiliar para repetir caracteres
    public static String rept(String texto, int number)
    {
        String saida = "";
        
        for(int i = 1; i <= number; i++)
            saida = saida + texto;
        
        return saida;
    }
    
    //Metodo run, responsavel por escrever os dados no log
    public void run()
    {
        try 
        {
            String[] hostLine, host;
            SimpleDateFormat fmt;
            
            //Quebrar a linha de host para pegar o endereco e a porta de origem
            hostLine = this.LinhaHost.split(" ");
            host = hostLine[1].split(":");
            
            //Montar formato de data
            fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            
            //Se a requisição for só a barra '/' significa que a raiz foi requisitada
            if(this.Conteudo.equals("/"))
                this.Conteudo = "raiz";
            
            FileWriter arq = new FileWriter(ServidorWeb.logFile, true);
            PrintWriter print = new PrintWriter(arq);
            
            // Informacoes do log
            print.println("Endereço de Origem: " + host[0]);
            print.println("Porta de Origem: " + host[1]);
            print.println("Horario da Requisicao: " + fmt.format(this.DataRequisicao));
            print.println("Conteudo Requisitado: " + this.Conteudo);
            print.println("Quantidade de bytes da resposta: " + this.QuantBytesResposta);
            print.println();
  
            //Fechar o fluxo
            print.close();
            arq.close();
        } 
        catch (IOException ex) 
        {    
            System.out.println("Nao foi possivel registrar a informacao no log");
        }
    }
}

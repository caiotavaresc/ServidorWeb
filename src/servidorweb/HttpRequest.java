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
    
    private Logger log;
    
    //Metodo construtor - Associa um socket ao objeto de thread
    public HttpRequest(Socket _processador)
    {
        this.connectionSocket = _processador;
        this.log = new Logger();
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
            e.printStackTrace();
        }
        
    }
    
    //Metodo responsavel por processar as requisicoes HTTP
    private void processRequest() throws Exception
    {
        String requestLine="", hostLine, headerLine, nomeArq, statusLine="", contentTypeLine="", saidaHTML, Authorization="";
        FileInputStream enviaArquivo=null, entityBody=null;
        File diretorio;
        
        //Data e hora da requisicao
        log.DataRequisicao = new Date();
        
        //Inicializacao forcada
        boolean listaDiretorio = false;
        saidaHTML = "";
        
        //Abre uma stream para ler o que foi recebido
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        
        //Abre uma stream para devolver mensagens
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        
        //Fazer a leitura dos dados - Linha de requisicao
        requestLine = inFromClient.readLine();
        
        //Iniciar impressao do novo bloco
        System.out.println();
        System.out.println(requestLine);
        
        //Linha do host
        hostLine = inFromClient.readLine();
        System.out.println(hostLine);
        
        //Mandar a linha de Host para o log
        log.LinhaHost = hostLine;
        
        //Fazer a leitura dos dados - enquanto houver linhas de cabecalho
        headerLine = inFromClient.readLine();
        while(headerLine.length() != 0)
        {
            System.out.println(headerLine);
            headerLine = inFromClient.readLine();
            //Verifica se a requisição tem autorizacao
            if(headerLine.contains("Authorization")){
                Authorization=headerLine;
            }
        }
        
        /* Parte B: Enviando uma Resposta - Begin */
        
        //Pegar o nome do arquivo
        StringTokenizer token = new StringTokenizer(requestLine);
        token.nextToken();
        
        nomeArq = token.nextToken();
        
        //Mandar o conteudo para o objeto de log
        log.Conteudo = nomeArq;
        
        //O browser envia o nome do arquivo com uma barra "\"
        //Adicionar um ponto (.) ao início para pesquisar no diretório atual
        nomeArq = "." + nomeArq;
        
        //Tentar obter o arquivo
        try
        {    
            boolean acesso=true;
            /* Parte C - Tratativa de diretorios */
            diretorio = new File(nomeArq);
            
            /*  Autenticação */
            //Verifica se precisa de autorização
            for (int i=0; i<ServidorWeb.diretoriosAutenticados.size();i++){
                    if(diretorio.getPath().contains(ServidorWeb.diretoriosAutenticados.get(i))){
                        //Verifica se veio autorizacao no header
                        if(Authorization.length()==0){
                            statusLine = "HTTP/1.1 401 Authorization Required" + CLRF;
                            contentTypeLine = "WWW-Authenticate: Basic realm=\"protected\"" + CLRF;
                            entityBody = enviaArquivo = getErro(401);
                            acesso=false;
                        }else{
                            String[] split;
                            split=Authorization.split("Authorization: Basic ");
                            String auth=split[1];
                            //Se a autenticação for aceita
                            if(auth.equals(ServidorWeb.senhaAutenticacao)){
                                acesso=true;
                            //Se não for aceita    
                            }else{
                                statusLine = "HTTP/1.1 401 Authorization Required" + CLRF;
                                contentTypeLine = "WWW-Authenticate: Basic realm=\"protected\"" + CLRF;
                                entityBody = enviaArquivo = getErro(401);
                                acesso=false;
                            }
                        }
                       
                    }
            }
            //Se o acesso é livre
            if(acesso){
                if(diretorio.isDirectory())
                {
                    //Se for um diretorio, verificar a opcao
                    switch(ServidorWeb.opcaoDiretorio)
                    {
                        case 1:
                            saidaHTML = montaArquivoRetorno(diretorio, nomeArq);
                            listaDiretorio = true;
                            break;
                        case 2:
                            //Opcao 2 - Enviar a mensagem de conteudo nao pode ser listado
                            nomeArq = "config/ConteudoIndisponivel.html";
                            break;
                        case 3:
                            //Opcao 3 - Enviar o arquivo index.html
                            nomeArq = diretorio.getPath()+"/index.html";
                            break;
                    }
                }
                /* Fim PARTE C */

                statusLine = "HTTP/1.1 200 OK" + CLRF;

                /* Parte C - Para a opcao de listar os arquivos do diretorio, nao sera necessario abrir o arquivo */
                if(listaDiretorio)
                {
                    contentTypeLine = "Content-type: text/html" + CLRF;                
                    entityBody = null;
                }
                else
                {
                    //Aqui so vai entrar se o que foi pedido for um arquivo mesmo
                    enviaArquivo = new FileInputStream(nomeArq);
                    contentTypeLine = "Content-type: " + this.contentType(nomeArq) + CLRF;
                    entityBody = enviaArquivo;
                }
            }
            
        }
        catch(FileNotFoundException e)
        {
            statusLine = "HTTP/1.1 404 Not Found" + CLRF;
            contentTypeLine = "Content-type: text/html" + CLRF;
            entityBody = enviaArquivo = getErro(404);
        }
        
        //Enviar a resposta
        
        //Linha de status
        outToClient.writeBytes(statusLine);
        log.QuantBytesResposta += statusLine.getBytes().length;
        
        //Cabeçalho
        outToClient.writeBytes(contentTypeLine);
        log.QuantBytesResposta += contentTypeLine.getBytes().length;
        
        //Marcador de fim de cabeçalho
        outToClient.writeBytes(CLRF);
        log.QuantBytesResposta += CLRF.getBytes().length;
        
        //Envia os dados (sem if para ver se o arquivo existe pois nesse caso envia o arquivo de erro)
        if(listaDiretorio)
        {
            outToClient.writeBytes(saidaHTML);
            log.QuantBytesResposta += saidaHTML.getBytes().length;
        }
        else
        {
            sendBytes(entityBody, outToClient);
            enviaArquivo.close();
        }
        
        /* Parte B: Enviando uma Resposta - End */
        
        //Fechar as streams
        inFromClient.close();
        outToClient.close();
        connectionSocket.close();
        
        //Criar uma nova thread para escrever no log
        Thread threadLog = new Thread(log);
        threadLog.start();
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
    
    //Método que retorna os dados do arquivo que contém o ERRO requirido
    public FileInputStream getErro(int erro)
    {
        try
        {
            return new FileInputStream("config/Erro"+erro+".html");
        }
        catch(Exception e)
        {
            System.out.println("Não foi possível retornar o arquivo de erro"+erro+".");
            e.printStackTrace();
            return null;
        }
    }
    
    //Método que envia os dados do corpo
    private void sendBytes(FileInputStream enviaArquivo, DataOutputStream outToClient) throws Exception {
        // Constrói um buffer de 1K para comportar os bytes no caminho para o socket.
        byte[] buffer = new byte[1024];
	int bytes = 0;
	// Copia o arquivo requisitado dentro da cadeia de saída do socket.
	while((bytes = enviaArquivo.read(buffer)) != -1 ) 
        {
		outToClient.write(buffer, 0, bytes);
                log.QuantBytesResposta += bytes;
        }
    }
    
    //Método que monta um arquivo de retorno quando a opção é listar os arquivos do servidor
    public static String montaArquivoRetorno(File diretorio, String nomeArq)
    {
        String[] arquivos, split;
        String saidaHTML, preLink;
        
        preLink = "";
        //Se o diretorio nao terminar com / a pagina entende como um arquivo individual
        //Para controlar os links, contornamos esse problema
        if(nomeArq.charAt(nomeArq.length()-1) != '/')
        {
            split = nomeArq.split("/");
            preLink = split[split.length-1] + "/";
        }
        
        //listar todos os arquivos do diretorio;
        arquivos = diretorio.list();
        
        //Montar a saida de dados
        saidaHTML = ServidorWeb.headerListaConteudo;
        
        //Exibir os diretorios pai e atual (default)
        saidaHTML = saidaHTML + "<A href='"+preLink+".'>.</A><BR>";
        saidaHTML = saidaHTML + "<A href='"+preLink+"..'>..</A><BR>";
        
        for(int i = 0; i < arquivos.length; i++)
        {
            String separador = "";
            File arq = new File(diretorio.getPath()+"\\"+ arquivos[i]);
            if(arq.isDirectory())
                separador = "/";
            
            saidaHTML = saidaHTML + "<A href='"+preLink +arquivos[i]+ separador +"'>" + arquivos[i] + "</A><BR>";
        }
        
        saidaHTML = saidaHTML + ServidorWeb.footerListaConteudo;
        
        return saidaHTML;
    }
    
}

package interfaceServidor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class server{
    static ServerSocket serverSocket;
    static telaPrincipalServer rodando;
    static Vector<miniServer> todasConex = new Vector<>();
    public static String nomeJogador = "";
    public static int horaInicio = 0;
    public static int minutoInicio = 0;
    public static Vector<Integer> usoCpu = new Vector<>();
    public static Vector<Integer> usoRam = new Vector<>();
    public static Vector<Integer> cargaGpu = new Vector<>();
    public static Vector<Integer> usoVram = new Vector<>();
    
    public static capturaDados loopDeCaptura;
    
    
        public static class capturaDados extends Thread {
                boolean running = true;
        public void run() {
            while (running) {
                String loc = "C:\\windows\\system32";
                int cpu = 0,ram = 0;
                String comando = "cmd.exe /c wmic cpu get loadpercentage /value";
                Runtime r = Runtime.getRuntime();
                try {
                    Process p = r.exec(comando,null,new File(loc));
                    InputStream processStdOutput = p.getInputStream();
                    Reader rr = new InputStreamReader(processStdOutput);
                    BufferedReader br = new BufferedReader(rr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.equals("")) {
                            String[] saida = line.split("=");
                            if (saida.length == 2){
                                cpu = Integer.parseInt(saida[1]);//System.out.println("CPU: " + saida[1]);
                            }
                            break;
                        }
                    }
                    p.waitFor();

                    line = "";
                    //Uso de ram agora
                    long total = 0, livre = 0;
                    //TOTAL
                    comando = "cmd.exe /c wmic os get TotalVisibleMemorySize /value";
                    r = Runtime.getRuntime();
                    p = r.exec(comando,null,new File(loc));
                    processStdOutput = p.getInputStream();
                    rr = new InputStreamReader(processStdOutput);
                    br = new BufferedReader(rr);
                    while ((line = br.readLine()) != null) {
                        if (!line.equals("")) {
                            String[] saida = line.split("=");
                            //System.out.println("LINHA:"+saida[1]+"!");
                            total = Long.parseLong(saida[1]);
                            break;
                        }
                    }
                    p.waitFor();
                    //LIVRE
                    comando = "cmd.exe /c wmic os get FreePhysicalMemory /value";
                    r = Runtime.getRuntime();
                    p = r.exec(comando,null,new File(loc));
                    processStdOutput = p.getInputStream();
                    rr = new InputStreamReader(processStdOutput);
                    br = new BufferedReader(rr);
                    while ((line = br.readLine()) != null) {
                        if (!line.equals("")) {
                            String[] saida = line.split("=");
                            livre = Long.parseLong(saida[1]);
                            break;
                        }
                    }
                    p.waitFor();
                    double av = ((double) (total - livre) / (double) total) * 100;
                    ram = (int)((long)av);
                    

                    //Uso de GPU agora
//                    line = "";
                    int usoGpu = 0, memGpu = 0;
//                    comando = "cmd.exe /c nvidia-smi -q -d UTILIZATION";
                    ProcessBuilder builder = new ProcessBuilder("cmd.exe","/c","cd C:\\Users\\MoonlightServer\\Documents && nvidia-smi.exe -q -d UTILIZATION");
                    builder.redirectErrorStream(true);
                    //String arq = "";
                    r = Runtime.getRuntime();
                    p = builder.start();//r.exec(comando,null,new File(loc));
                    processStdOutput = p.getInputStream();
                    rr = new InputStreamReader(processStdOutput);
                    br = new BufferedReader(rr);
                    while ((line = br.readLine()) != null) {
                        //arq+=line;
                        if (line.contains("Gpu") && line.contains("%")) {
                            String[] saida = line.split(":");
                            if (saida[1].charAt(2)!=' ') {
                                usoGpu = Integer.parseInt(String.valueOf(saida[1].charAt(1)) + String.valueOf(saida[1].charAt(2)));
                            } else {
                                usoGpu = Integer.parseInt(String.valueOf(saida[1].charAt(1)));
                            }
                        } else if (line.contains("Memory") && line.contains("%")) {
                            String[] saida = line.split(":");
                            if (saida[1].charAt(2)!=' ') {
                                memGpu = Integer.parseInt(String.valueOf(saida[1].charAt(1)) + String.valueOf(saida[1].charAt(2)));
                            } else {
                                memGpu = Integer.parseInt(String.valueOf(saida[1].charAt(1)));
                            }
                        }
                    }
                    p.waitFor();
                    
                    
                    usoCpu.add(cpu);
                    usoRam.add(ram);
                    cargaGpu.add(usoGpu);
                    usoVram.add(memGpu);
                    rodando.mudatextoGrande(cpu+"\n"+ram+"\n"+usoGpu+"\n"+memGpu+"\n");
                    
                    if (p != null) {
                        p.destroy();
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,"Erro de IO ao coletar dados pc!"+ex);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // Importante para n dar erros quando interromper a thread
                    break;
                }catch (Exception ex) {
                    System.out.println("ERRO: "+ex);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException ex) {
                    //JOptionPane.showMessageDialog(null,"Erro de dormir 500ms ao coletar dados pc!"+ex);
                }
            }
        }
    }
    

    
    public static void comecaCaptura(){
                    usoCpu = new Vector<>();//Limpa os
                    usoRam = new Vector<>();//dados anteriores
                    cargaGpu = new Vector<>();//e starta a thread para colher dados
                    usoVram = new Vector<>();//e starta a thread para colher dados
                    loopDeCaptura = new capturaDados();
                    loopDeCaptura.start();
    }
    
    public static int[] calculaMedias(){
        int medias[] = new int[4];
        int med1=0,med2=0,med3=0,med4=0;
        for (int i = 0; i < usoCpu.size(); i++) {
            med1+=usoCpu.elementAt(i);
            med2+=usoRam.elementAt(i);
            med3+=cargaGpu.elementAt(i);
            med4+=usoVram.elementAt(i);
        }
        medias[0] = (int)((double)med1/(double)usoCpu.size());
        medias[1] = (int)((double)med2/(double)usoCpu.size());
        medias[2] = (int)((double)med3/(double)usoCpu.size());
        medias[3] = (int)((double)med4/(double)usoCpu.size());
        return medias;
    }
    
    public static int calculaTempoJogo(int h,int m){
        int mindaHora = 0;
        int minutos = 0;
        if(h>horaInicio){
            mindaHora = (h-horaInicio)*60;
            minutos = m - minutoInicio;
        }else if(h == horaInicio){
            minutos = m - minutoInicio;
        }else {
            mindaHora = (24-(horaInicio - h))*60;
            minutos = m - minutoInicio;
        }
        return minutos + mindaHora;
    }
    
    public static void stopWMI(){
        String loc = "C:\\windows\\system32";
                int cpu = 0,ram = 0;
                String comando = "net stop winmgmt /y";
                Runtime r = Runtime.getRuntime();
                try {
                    Process p = r.exec(comando,null,new File(loc));
                    p.waitFor();
        }catch(Exception e){
                //JOptionPane.showMessageDialog(null,"Erro ao abrir um socket!"+e);
            }
    }
    
    public static void startWMI(){
        String loc = "C:\\windows\\system32";
                int cpu = 0,ram = 0;
                String comando = "net start winmgmt";
                Runtime r = Runtime.getRuntime();
                try {
                    Process p = r.exec(comando,null,new File(loc));
                    p.waitFor();
        }catch(Exception e){
                //JOptionPane.showMessageDialog(null,"Erro ao abrir um socket!"+e);
            }
    }
    
    public static void desligaPC(){
        try {
            
            TimeUnit.MILLISECONDS.sleep(500);
            String loc = "C:\\windows\\system32";
                int cpu = 0,ram = 0;
                String comando = "cmd.exe /c shutdown -s -t 0";
                Runtime r = Runtime.getRuntime();
                Process p = r.exec(comando,null,new File(loc));
        } catch (Exception e) {
        }
    }
    
    
    public static class miniServer extends Thread {
        public Socket clientSocket;
        public PrintWriter out;
        public BufferedReader in;

        public miniServer(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try{
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            rodando.mudatextoIp(clientSocket.getInetAddress().getCanonicalHostName());
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                 //TRATAR COMANDOS DO CLIENTE, POR ENQUANTO:
                 //"Half-Life 3" é finalizar aquele cliente, os outros são separados por ":" e tem 3 campos:
                 //o primeiro é o que fazer, DELAY, JITTER, PERDA. O segundo é o valor e o terceiro é se é no vídeo ou no input
                 //alvo pode ser INPUT ou VIDEO
                 //COMECOU:NomeJogador:Hora24h em XXXX ao receber esse comando começa a gravação do jogo (pegar vários dados de tempos em tempos, 
                 //como uso de cpu, memoria etc)
                 //TERMINOU:Qoe:HORA24h em XXXX ao receber esse comando entender que terminou secção e gravar o QoE avaliado!
                   // Comentar essas linhas quando for para 
                rodando.mudatextoGrande(inputLine);                                     // produção
                if ("Half-Life 3".equals(inputLine)){
                    out.println("bye");//resposta ao cliente
                    break;
                }else if ("DESLIGAAMBOS".equals(inputLine)){
                    out.println("LETSDIE");//resposta ao cliente
                    in.close();
                    out.close();
                    clientSocket.close();
                    desligaPC();
                }
                
                String comando = inputLine.split(":")[0];
                
                 if ("TERMINOU".equals(comando)){//Finalizou e coletou os dados
                    //TERIMOU:QoE: DATAFIM:DelayVideo:DelayComandos:JitterVideo:JitterComandos:PerdaVideo:PerdaComandos
                    //       :1a10:  HHMM : xx ms    : xx ms       : xx ms     : xx ms        : xx %     : xx %
                    //AINDA A SER FEITO: Parte do cliente que vai gerar o delay, jitter e perda, essas informações devem ser enviadas para serem 
                    //gravadas no BD.
                    
                    String QoE = inputLine.split(":")[1];//valor é o inputLine.split(":")[1]
                    String hora = inputLine.split(":")[2];
                    //Finalizar a Thread criada em COMECOU e calcular as médias e salvar no BD
                    loopDeCaptura.interrupt();
                    //terminou de capturar os dados, salvar no arquivo:
                    int horaFim = Integer.parseInt(hora)/100;
                    int minutoFim = Integer.parseInt(hora) - horaFim*100;
                    int tempoJogo = calculaTempoJogo(horaFim,minutoFim);
                    //Ate agora temos: NomeJogador, QoE, tempoJogo em minutos, usoCpu,usoRam,usoGpu,usoVram
                    FileWriter fw = new FileWriter("dados.csv", true);
                    BufferedWriter conexao = new BufferedWriter(fw);
                    int[] medias = calculaMedias();
                    loopDeCaptura.running = false;
                    stopWMI();
                    startWMI();
                    conexao.newLine();
                    //TERIMOU:QoE: DATAFIM:DelayVideo:DelayComandos:JitterVideo:JitterComandos:PerdaVideo:PerdaComandos
                    //       :1a10:  HHMM : xx ms    : xx ms       : xx ms     : xx ms        : xx %     : xx %
                    
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH-mm");  
                    Date date = new Date();
                    
                    conexao.write(nomeJogador+";"+QoE+";"+tempoJogo+";"+medias[0]+";"+medias[1]+";"+medias[2]+";"+medias[3]+";"+
                            inputLine.split(":")[3]+";"+inputLine.split(":")[4]+";"+inputLine.split(":")[5]+";"+inputLine.split(":")[6]+";"+inputLine.split(":")[7]+";"+
                            inputLine.split(":")[8]+";"+inputLine.split(":")[9]+";"+formatter.format(date));//escrever no arquivo
                    conexao.close();
                    out.println("OK TERMINOU");//resposta ao cliente
                    
                }else if ("COMECOU".equals(comando)){//começou a coleta os dados
                    
                    nomeJogador = inputLine.split(":")[1];
                    horaInicio = Integer.parseInt(inputLine.split(":")[2])/100; //Hora em XXXX 24h
                    minutoInicio = Integer.parseInt(inputLine.split(":")[2]) - horaInicio*100;
                    comecaCaptura();
                    out.println("OK COMECOU");//resposta ao cliente
                    
                }else {
                out.println(inputLine);
                }
            }

            in.close();
            out.close();
            clientSocket.close();
            }catch(IOException e){
                //JOptionPane.showMessageDialog(null,"Erro ao abrir um socket!"+e);
            }
        }
    }
    
    public static void start(int port){
        try{
        serverSocket = new ServerSocket(port);
        while (true){
            miniServer a = new miniServer(serverSocket.accept());
            a.start();
            todasConex.add(a);
        }
        }catch(IOException e){
            //JOptionPane.showMessageDialog(null,"Erro ao abrir o servidor!");
        }
    }

    public static void stop() throws IOException {
        serverSocket.close();
    }

    public static void main(String args[]) {
        rodando = new telaPrincipalServer();
        rodando.setVisible(true);
        //Ao fechar a janela
        rodando.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    stop();
                    for (int i = 0; i < todasConex.size(); i++) {
                        todasConex.elementAt(i).clientSocket.close();
                        todasConex.elementAt(i).in.close();
                        todasConex.elementAt(i).out.close();
                    }
                    rodando.dispose();
                    System.exit(0);
                } catch (IOException ex) {
                    //JOptionPane.showMessageDialog(null,"Erro ao fechar conexoes!");
                }
            }
        });
        //Ao fechar a janela
        start(3333);
    }
    
    
}

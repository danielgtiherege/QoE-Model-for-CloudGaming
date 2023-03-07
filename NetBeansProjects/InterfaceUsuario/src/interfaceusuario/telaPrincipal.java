/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaceusuario;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 *
 * @author DanielGT
 */
public class telaPrincipal extends javax.swing.JFrame {
    static Socket clientSocket;
    static PrintWriter out;
    static BufferedReader in;
    static File arquivoTxt;
    static int portaComandos = 47999;//DPORT
    static int portaVideo = 47998;//DPORT
    static String ipServidor = "192.168.20.199";
    static int[] delay = {0,25,50,75,100,125,150};
    static int[] jitter = {0,5,10,15,20,25};
    static String[] perda = {"0","0.5","1","1.5","2","2.5","3"};
    static long alteraMinutos = 186000;
    static int erros = 1;
    /**
     * Creates new form telaPrincipal
     */

    public static void pegaIp(){
        arquivoTxt = new File("ipservidor.txt");
        //Leitura do arquivo com o ip do servidor para JavaSocket
        if(arquivoTxt.exists() && arquivoTxt.isFile()){
            try {
                FileReader arq = new FileReader("ipservidor.txt");
                BufferedReader lerArq = new BufferedReader(arq);
                ipServidor = lerArq.readLine();
                String s[] = ipServidor.split("\\.");
                if(s.length != 4){
                    ipServidor = "127.0.0.1";
                    return;
                }
                arq.close();
            } catch (IOException e) {
                salvaErroLog("ERRO linha 83: "+e);
            } catch (NullPointerException e){
                salvaErroLog("ERRO linha 85: "+e);
            }
        }
    }    
    
    public static String pegaIpCliente(){
        String IP = "";
            IP = "hostname -I";
            IP = rodaComando(IP).split("\n")[0].replace(" ","");
            String s[] = IP.split("\\.");
                if(s.length != 4){
                    return null;
                }
            return IP;
    }
    
    public static void startConnection(String ip, int port) {
        try{
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //a funcao sendmessage agora e void e a gente trata as mensagens do servidor aqui como uma thread.
        recebeMensagens recebeA = new recebeMensagens();
        recebeA.start();
        }catch(IOException e){
            salvaErroLog("ERRO linha 109: "+e);
        }
    }

    
    public static class recebeMensagens extends Thread {
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if ("REINICIAR".equals(inputLine)){//reiniciar
                        reiniciaPC();
                    }else if("DESLIGAR".equals(inputLine)){//reiniciar
                        desligaPC();
                    }
                }
            } catch (Exception e) {
                salvaErroLog("ERRO linha 124: "+e);
            }
        }
    }
    
    //Aqui antes retornava STRING com a resposta do servidor. Agora estou ignorando a resposta dele,
    //O que o servidor manda, olho na thread.
    public static void sendMessage(String msg) throws IOException {
        out.println(msg);
        //String resp = in.readLine();
        //return resp;
    }

    public static void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
    
    public class AltTabStopper implements Runnable {
        //classe para consumir todos os TAB pressionados
     private boolean working = true;
     private JFrame frame;

     public AltTabStopper(JFrame frame){
          this.frame = frame;
     }

     public void stop(){
          working = false;
     }
     public void run(){
         try{
             Robot robot = new Robot();
             while (working){
                  robot.keyRelease(KeyEvent.VK_TAB);
                  frame.requestFocus();
                  Thread.sleep(10);
             }
         } catch (Exception e){ 
             salvaErroLog("ERRO linha 163: "+e);
         }
     }
}
    
    public telaPrincipal() {
        this.setUndecorated(true);
        initComponents();
        this.setTitle("Pesquisa QoE Daniel Henriques");
        this.setLocationRelativeTo(null);
        BufferedImage img = null,img2 = null;
        try {
            img = ImageIO.read(getClass().getResource("icon.png"));
            img2 = ImageIO.read(getClass().getResource("half-life-png-10.png"));
            ImagemHalfLife.setIcon(new ImageIcon(img));
            iconeLateral.setIcon(new ImageIcon(img2));
            //this.setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            
            //Essa sequencia de código impede fechamento por alt+f4, qualquer filho do JFrame pode ser utilizado
            iconeLateral.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK), "stopclose");
            iconeLateral.getActionMap().put("stopclose", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Não ligo para o seu alt+f4...");
                }
            });
            
            
            
            //ALT+END Fecha sem desligar o PC
            iconeLateral.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.ALT_DOWN_MASK), "fechaSemDesligar");
            iconeLateral.getActionMap().put("fechaSemDesligar", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        sendMessage("Half-Life 3");
                        stopConnection();
                        System.exit(0);
                    } catch (IOException ex) {
                        Logger.getLogger(telaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            //ALT+F12 Muda entre 1 e 3 minutos
            iconeLateral.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.ALT_DOWN_MASK), "mudaOsMinutos");
            iconeLateral.getActionMap().put("mudaOsMinutos", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(alteraMinutos == 186000){
                        alteraMinutos = 98000;
                        jTextArea1.setText("Você pode jogar quantas vezes quiser, em \n" +
                                            "partidas de 1,5 minuto. Sempre classifique\n" +
                                            "a Qualidade de Experiência ao terminar\n" +
                                            "a partida. \n" +
                                            "NAO FECHE O JOGO, ELE VAI SE FECHAR!");
                    }else if(alteraMinutos == 98000){
                        alteraMinutos = 66000;
                        jTextArea1.setText("Você pode jogar quantas vezes quiser, em \n" +
                                            "partidas de 1 minuto. Sempre classifique\n" +
                                            "a Qualidade de Experiência ao terminar\n" +
                                            "a partida. \n" +
                                            "NAO FECHE O JOGO, ELE VAI SE FECHAR!");
                    }else{
                        alteraMinutos = 186000;
                        jTextArea1.setText("Você pode jogar quantas vezes quiser, em \n" +
                                            "partidas de 3 minutos. Sempre classifique\n" +
                                            "a Qualidade de Experiência ao terminar\n" +
                                            "a partida. \n" +
                                            "NAO FECHE O JOGO, ELE VAI SE FECHAR!");
                    }
                }
            });
            
            
            
        } catch (IOException ex) {
            Logger.getLogger(telaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            salvaErroLog("ERRO linha 241: "+ex);
        }
        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        this.setIconImage(newImage);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        labelNome = new javax.swing.JLabel();
        campoNome = new javax.swing.JTextField();
        botaoJogar = new javax.swing.JButton();
        ImagemHalfLife = new javax.swing.JLabel();
        iconeLateral = new javax.swing.JLabel();
        botaoSair = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1920, 1080));
        setMinimumSize(new java.awt.Dimension(1920, 1080));
        setPreferredSize(new java.awt.Dimension(1920, 1080));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(204, 204, 204));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 1, 36)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setText("Você pode jogar quantas vezes quiser, em \npartidas de 3 minutos. Sempre classifique\na Qualidade de Experiência ao terminar\na partida. \nNAO FECHE O JOGO, ELE VAI SE FECHAR!");
        jScrollPane1.setViewportView(jTextArea1);

        labelNome.setFont(new java.awt.Font("Tahoma", 3, 18)); // NOI18N
        labelNome.setText("Nome do Jogador:");

        campoNome.setFont(new java.awt.Font("Tahoma", 3, 14)); // NOI18N
        campoNome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoNomeMouseClicked(evt);
            }
        });

        botaoJogar.setBackground(new java.awt.Color(204, 204, 204));
        botaoJogar.setForeground(new java.awt.Color(0, 51, 51));
        botaoJogar.setText("INICIAR NOVA PARTIDA");
        botaoJogar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoJogarMouseClicked(evt);
            }
        });

        ImagemHalfLife.setText("jLabel1");

        iconeLateral.setText("jLabel1");

        botaoSair.setText("SAIR");
        botaoSair.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoSairMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(botaoJogar, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelNome)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoNome, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(342, 342, 342))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(478, 478, 478)
                        .addComponent(ImagemHalfLife, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(iconeLateral, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1066, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 609, Short.MAX_VALUE)
                .addComponent(botaoSair, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ImagemHalfLife, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoNome, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelNome))
                .addGap(18, 18, 18)
                .addComponent(botaoJogar, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(211, 211, 211))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(botaoSair, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 413, Short.MAX_VALUE)
                .addComponent(iconeLateral, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static String pegaPlacaRede(){
        String comando = "/sbin/ip -4 -o a | cut -d ' ' -f 2,7 | cut -d '/' -f 1";
        String saida = rodaComando(comando);
        if(saida == null) return null;
        String ipDeProcura = pegaIpCliente();//true ele tenta pegar automático e false ele olha o arquivo ipCliente.txt na pasta do jar
        if(ipDeProcura == null) return null;
        String linha [] = saida.split("\n");
        saida = "";
        for (int i = 0; i < linha.length; i++) {
            if(linha[i].contains(ipDeProcura)){
                saida = linha[i].split(" ")[0];
                break;
            }
        }
        if(saida.equals("")){
            return null;
        }
        return saida;
    }
    
    public static String rodaComando(String command){
        String[] commands = new String[] { "/bin/sh", "-c", command};
        Process proc = null;
        //ExecutorService executor = Executors.newCachedThreadPool();
        try {
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream();
            proc = builder.start();
            StringBuffer buffer = new StringBuffer();
            String line;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append('\n');
                }
            } catch (Exception e) {
                salvaErroLog("ERRO linha 397: "+e);
                return null;
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    salvaErroLog("ERRO linha 403: "+e);
                    return null;
                }
            }
            line = buffer.toString();
            // */
            // child proc exit code
            int waitFor = proc.waitFor();
            return line;
        } catch (IOException e) {
            salvaErroLog("ERRO linha 413: "+e);
            return null;
        } catch (InterruptedException e) {
            salvaErroLog("ERRO linha 416: "+e);
            return null;
        }finally{
            proc.destroy();
        }
    }
    
    public static String[] geraDelays(){
        Random r = new Random();
        String placaRede = pegaPlacaRede();
        if(placaRede == null) placaRede = "enp4s0";
        // Ele pega a placa de rede que tem o IP salvo no arquivo ipcliente.txt
        // Caso este arquivo não exista ou o IP esteja errado, ele utiliza como
        // Padrão a placa enp4s0.
        try{
            String[] retorna = new String[4];
            //VIDEO,COMANDOS,AMBOS depois DELAY JITTER e PERDA
            //no caso de AMBOS cada campo do DELAY sera assim -> 50:100
            String concatena = "echo senha | sudo -S tc qdisc add dev "+placaRede+" root handle 1: prio ; ";
            int sorteio = r.nextInt(3)+1;
            int sorteioD1 = r.nextInt(delay.length), sorteioJ1 = r.nextInt(jitter.length), sorteioP1 = r.nextInt(perda.length);
            int sorteioD2 = r.nextInt(delay.length), sorteioJ2 = r.nextInt(jitter.length), sorteioP2 = r.nextInt(perda.length);
            if((sorteioD1 == 0)&&(sorteioJ1 == 0)&&(sorteioP1 == 0)&&(sorteioD2 == 0)&&(sorteioJ2 == 0)&&(sorteioP2 == 0)) return null;
            
            int porta;
            if(sorteio != 3){
                int valor = 2;
                //VIDEO ou COMANDOS
                if(sorteio == 1){
                    porta = portaVideo;
                    retorna[0] = "VIDEO";
                }else {
                    porta = portaComandos;
                    retorna[0] = "COMANDOS";
                }
                
                if(sorteioJ1 != 0){
                    int provDelay = 50;
                    if(sorteioD1 != 0) provDelay = delay[sorteioD1];
                    retorna[1] = ""+provDelay;
                    retorna[2] = ""+jitter[sorteioJ1];
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 delay "+provDelay
                            +"ms "+(provDelay/2)+"ms "
                            +jitter[sorteioJ1]+"% ; "
                            + "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: pfifo limit 100000 ; ";
                            valor++;
                }else if(sorteioD1 != 0){
                    retorna[1] = ""+delay[sorteioD1];
                    retorna[2] = "0";
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 delay "+delay[sorteioD1]
                            +"ms ; ";
                    valor++;
                }else{
                    retorna[2] = "0";
                    retorna[1] = "0";
                }
                if(sorteioP1 != 0){
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 loss "+perda[sorteioP1]+"% ; ";
                    valor++;
                    retorna[3] = ""+perda[sorteioP1];
                }
                
                for (int i = 2; i < valor; i++) {
                    concatena+= "sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip dport "+porta+" 0xffff flowid 1:"+i+" ; "
                            + "echo senha | sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip protocol 17 0xff flowid "+i+" ; ";
                }
                concatena+= "sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip dport "+porta+" 0xffff flowid 1:"+valor+" ; "
                            + "echo senha | sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip protocol 17 0xff flowid "+valor;
            }else{
                //AMBOS
                retorna[0] = "AMBOS";
                int valor = 2;
                if(sorteioJ1 != 0){
                    int provDelay = 50;
                    if(sorteioD1 != 0) provDelay = delay[sorteioD1];
                    retorna[1] = ""+provDelay;
                    retorna[2] = ""+jitter[sorteioJ1];
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 delay "+provDelay
                            +"ms "+(provDelay/2)+"ms "
                            +jitter[sorteioJ1]+"% ; "
                            + "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: pfifo limit 100000 ; ";
                            valor++;
                }else if(sorteioD1 != 0){
                    retorna[1] = ""+delay[sorteioD1];
                    retorna[2] = "0";
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 delay "+delay[sorteioD1]
                            +"ms ; ";
                    valor++;
                }else{
                    retorna[1] = "0";
                    retorna[2] = "0";
                }
                if(sorteioP1 != 0){
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 loss "+perda[sorteioP1]+"% ; ";
                    valor++;
                    retorna[3] = perda[sorteioP2];
                }else{
                    retorna[3] = "0";
                }
                
                if(sorteioJ2 != 0){
                    int provDelay = 50;
                    if(sorteioD2 != 0) provDelay = delay[sorteioD2];
                    retorna[1] += ":"+provDelay;
                    retorna[2] += ":"+jitter[sorteioJ2];
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 delay "+provDelay
                            +"ms "+(provDelay/2)+"ms "
                            +jitter[sorteioJ2]+"% ; "
                            + "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: pfifo limit 100000 ; ";
                            valor++;
                }else if(sorteioD2 != 0){
                    retorna[1] += ":"+delay[sorteioD2];
                    retorna[2] += ":0";
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 delay "+delay[sorteioD2]
                            +"ms ; ";
                    valor++;
                }else{
                    retorna[1] += ":0";
                    retorna[2] += ":0";
                }
                if(sorteioP2 != 0){
                    concatena+= "echo senha | sudo -S tc qdisc add dev "+placaRede+" parent 1:"+valor+" handle "+valor+"0: netem limit 100000 loss "+perda[sorteioP2]+"% ; ";
                    valor++;
                    retorna[3] += ":"+perda[sorteioP2];
                }else{
                    retorna[3] += ":0";
                }
                //valor pode estar entre 2 e 8
                int quantosVideo = 0;
                if(sorteioJ1 != 0){
                    quantosVideo++;
                }else if(sorteioD1 != 0){
                    quantosVideo++;
                }
                if(sorteioP1 != 0){
                    quantosVideo++;
                }
                
                int quantosComando = 0;
                if(sorteioJ2 != 0){
                    quantosComando++;
                }else if(sorteioD2 != 0){
                    quantosComando++;
                }
                if(sorteioP2 != 0){
                    quantosComando++;
                }
                
                int i = 2;
                if(quantosVideo != 0){
                    for (; i < quantosVideo; i++) {
                        concatena+= "sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip dport "+portaVideo+" 0xffff flowid 1:"+i+" ; "
                            + "echo senha | sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip protocol 17 0xff flowid "+i+" ; ";
                    }
                    if(quantosComando == 0) concatena+= "sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip dport "+portaVideo+" 0xffff flowid 1:"+quantosVideo+" ; "
                            + "echo senha | sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip protocol 17 0xff flowid "+quantosVideo;
                    else concatena+= "sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip dport "+portaVideo+" 0xffff flowid 1:"+quantosVideo+" ; "
                            + "echo senha | sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip protocol 17 0xff flowid "+quantosVideo+" ; ";
                }
                if(quantosComando != 0){
                    for (; i < quantosComando; i++) {
                        concatena+= "sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip dport "+portaComandos+" 0xffff flowid 1:"+i+" ; "
                            + "echo senha | sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip protocol 17 0xff flowid "+i+" ; ";
                    }
                    concatena+= "sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip dport "+portaComandos+" 0xffff flowid 1:"+quantosComando+" ; "
                            + "echo senha | sudo -S tc filter add dev "+placaRede+" protocol ip parent 1:0 u32 match ip protocol 17 0xff flowid "+quantosComando;
                }
                
               
            }

            
            String geraDELAYS = rodaComando(concatena);
            //salvaErroLog("-------------DELAYS GERADOS ------------------\n"+geraDELAYS+"-------------------------------------------\n");
//            String[] comandoDelays = new String[] {"/bin/bash", "-c",concatena};
//            ProcessBuilder builderDe = new ProcessBuilder(comandoDelays);
//            builderDe.redirectErrorStream(true);
//            Process pD = builderDe.start();
//            Thread.sleep(500);
//            int waitFor = pD.waitFor();
//            
            r = null;
            //System.out.println(concatena);
            return retorna;
        }catch(Exception e){
            salvaErroLog("ERRO linha 601: "+e);
        }
        return null;
    }
    
    public static void salvaErroLog(String s){
        try {
            FileWriter fw = new FileWriter("log.txt", true);
            BufferedWriter conexao = new BufferedWriter(fw);
            conexao.newLine();
            conexao.write("Erro N "+erros+":\n");
            conexao.write(s);
            erros++;
            conexao.close();
            fw.close();
        } catch (Exception e) {
            //
        }
    }
    
    public static void escreveEmArquivo(String s,String arquivo){
        try {
            FileWriter fw = new FileWriter(arquivo, true);
            BufferedWriter conexao = new BufferedWriter(fw);
            conexao.newLine();
            conexao.write(s);
            erros++;
            conexao.close();
            fw.close();
        } catch (Exception e) {
            //
        }
    }
    
    //
    public static void retiraTudo(){
        try{
            String placaRede = pegaPlacaRede();
            if(placaRede == null) placaRede = "enp4s0";
            
            String comando = "echo senha | sudo -S tc qdisc del dev "+placaRede+" root";
            
            rodaComando(comando); 
                 
        }catch(Exception e){
            salvaErroLog("ERRO linha 639: "+e);
        }
    }
    
    public static void desligaPC(){
        try{
            String[] comandoDelays = new String[] {"/bin/bash", "-c", "echo senha | sudo -S shutdown -P -t 2 now"};
            ProcessBuilder builderDe = new ProcessBuilder(comandoDelays);
            builderDe.redirectErrorStream(true);
            Process pD = builderDe.start();
        }catch(Exception e){
            salvaErroLog("ERRO linha 650: "+e);
        }
    }
    
    public static void reiniciaPC(){
        try{
            String[] comandoDelays = new String[] {"/bin/bash", "-c", "echo senha | sudo -S shutdown -r now"};
            ProcessBuilder builderDe = new ProcessBuilder(comandoDelays);
            builderDe.redirectErrorStream(true);
            Process pD = builderDe.start();
        }catch(Exception e){
            salvaErroLog("ERRO linha 661: "+e);
        }
    }
    
                    
    
    public static class rodarComando extends Thread {
        
        String comandoExecutar = " ";
        Process p = null;
        
        public rodarComando(String comando){
            comandoExecutar = ""+comando;
        }
        
        public void terminarComando(){
            if (p == null){
                return;
            }
            try {
                p.destroy();
                p.destroyForcibly();
            } catch (Exception ex) {
                    escreveEmArquivo("Erro ao terminarExecução: "+ex.getMessage(),"log.txt");
            }
        }
        
        public void run() {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("bash", "-c", comandoExecutar);
                
                try {
                    p = processBuilder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    String lines = "";
                    while ((line = reader.readLine()) != null) {
                            lines+=line+"\n";
                    }
                    escreveEmArquivo("Saida da execução: \n"+lines,"logMoonlight.txt");
                    
                } catch (IOException ex) {
                    System.err.println("Erro ao executar comando, erro: "+ex.getMessage());
                }catch (Exception ex) {
                    System.err.println("Erro ao coletar comando, erro: "+ex.getMessage());
                    //System.out.println("ERRO: "+ex);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException ex) {
                    System.err.println("Erro ao dormir 500ms, erro: "+ex.getMessage());
                }
        }
    }
                    

    
    private void botaoJogarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoJogarMouseClicked
        int resposta;
        Object[] options = { "Sim", "Cancelar" };
                 resposta = JOptionPane.showOptionDialog(null, "Tente jogar por todos os 3 minutos antes de dar a nota, obrigado!", "Informação", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                 //confirmar == 0 e cancelar == 1
                 if(resposta == JOptionPane.CANCEL_OPTION || resposta == JOptionPane.CLOSED_OPTION || resposta == JOptionPane.NO_OPTION){
                     return;
                 }
                 if(campoNome.getText().equals("")){
                     JOptionPane.showMessageDialog(null,"Nome não pode ser deixado em branco!");
                     return;
                 }
            try {
                String[] HLouSpelunky = {"Half-Life 2","Spelunky","FEAR"};
                BufferedImage img = ImageIO.read(getClass().getResource("icon.png"));  
                String qualJogo = (String)JOptionPane.showInputDialog(null,"Quer jogar Half-Life 2, Spelunky ou F.E.A.R?", "Avaliar", JOptionPane.QUESTION_MESSAGE,new ImageIcon(img),HLouSpelunky,HLouSpelunky[0]);
                 
                //----------------------------------------------
                //Socket todo pronto, só mandar um sendMessage com as configurações e depois um pra gravar no banco de dados.
                 SimpleDateFormat formatter = new SimpleDateFormat("HHmm");  
                 Date date = new Date();
                 
                 sendMessage("COMECOU:"+campoNome.getText()+":"+formatter.format(date));
                 
                 retiraTudo();
                 
                 //salvaErroLog("retirou delays.\n");
                 
                 String[] valorDelays = geraDelays();
                 
                 //salvaErroLog("gerou delays.\n");
                 
                 String comandoJogo = "moonlight --quit-after --1080 --vsync stream "+ipServidor+" HALFLIFE2";
                 if(qualJogo.equals("Spelunky")){
                     comandoJogo = "moonlight --quit-after --1080 --vsync stream "+ipServidor+" SPELUNKY";
                 }else if(qualJogo.equals("FEAR")){
                     comandoJogo = "moonlight --quit-after --1080 --vsync stream "+ipServidor+" FEAR";
                 }
                 String[] comandoInicia = new String[] {"/bin/bash", "-c", comandoJogo};//"/bin/sh"
                 
                 //salvaErroLog("Jogo selecionado: "+qualJogo+"\n+ComandoJogo: "+comandoJogo+"\n");
                 
                 rodarComando rodar = new rodarComando(comandoJogo);
                 rodar.comandoExecutar = comandoJogo;
                 rodar.start();
                 //ProcessBuilder builder = new ProcessBuilder(comandoInicia);
                 //builder.redirectErrorStream();
                 //Process p = builder.start();
                 
                 //salvaErroLog("comando de rodar jogo executado.\n");
                 
                 TimeUnit.MILLISECONDS.sleep(alteraMinutos);
                 //Thread.sleep(alteraMinutos);//3 minutos
                 
                 sendMessage("FECHAJOGOS");
                 
                 rodar.terminarComando();
                 //p.destroy();
                 rodar.interrupt();
                 
                 //salvaErroLog("Destruiu thread depois de 3 minutos.\n");
                 
                 retiraTudo();
                
                 //salvaErroLog("retirou delays novamente.\n");
                 
                 String QoE = "";
                 String[] valores = {"Extremamente péssimo", "Péssimo", "Ruim", "Razoável", "Bom", "Excelente", "Ideal"};

                 int n = JOptionPane.showOptionDialog(null,"Como você classifica a Qualidade de Experiência da partida?","Avaliação de QoE",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,valores,valores[0]);
                    
                 if(n==-1){//A pessoa fechou a janela, salvar como null
                    QoE = "null";
                 }else{
                     // de 0 a 6 o QoE que deve virar de -3 a 3, logo subtrair -3 dele
                     n = n - 3;
                     QoE = ""+n;
                 }
                 
                 String JogariaNovamente = "";
                 String[] valores2 = {"NAO", "SIM"};

                 int n2 = JOptionPane.showOptionDialog(null,"Você jogaria desta forma mais partidas?","Avaliação de QoE",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,valores2,valores2[0]);
                    
                 if(n2==-1){//A pessoa fechou a janela, salvar como null
                    JogariaNovamente = "null";
                 }else if(n2==0){
                    JogariaNovamente = "NAO";
                 }else{
                    JogariaNovamente = "SIM";
                 }
                 
                 String comoSeSente = "";
                 String[] valores3 = {"Triste", "Indiferente", "Feliz"};

                 int n3 = JOptionPane.showOptionDialog(null,"Como você está se sentindo hoje?","Avaliação de QoE",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,valores3,valores3[0]);
                    
                 if(n3==-1){//A pessoa fechou a janela, salvar como null
                    comoSeSente = "null";
                 }else if(n3==0){
                    comoSeSente = "Triste";
                 }else if(n3==1){
                    comoSeSente = "Indiferente";
                 }else{
                      comoSeSente = "Feliz";
                 }
                 
                 
                 date = new Date();//recapturar o date
                 
                 if(valorDelays == null) {
                     //System.err.println("DEU RUIM NOS DELAYS OU TUDO 0");
                     sendMessage("TERMINOU:"+QoE+":"+formatter.format(date)+":0:0:0:0:0:0");
                     return;
                 }
                 String DelayVideo = "",DelayComandos = "",JitterVideo = "",JitterComandos = "",PerdaVideo = "",PerdaComandos = "";
                 
                 if(valorDelays[0].equals("AMBOS")){
                     DelayVideo = valorDelays[1].split(":")[0];
                     DelayComandos = valorDelays[1].split(":")[1];
                     JitterVideo = valorDelays[2].split(":")[0];
                     JitterComandos = valorDelays[2].split(":")[1];
                     PerdaVideo = valorDelays[3].split(":")[0];
                     PerdaComandos = valorDelays[3].split(":")[1];
                 }else if(valorDelays[0].equals("VIDEO")){
                     DelayVideo = valorDelays[1];
                     DelayComandos = "0";
                     JitterVideo = valorDelays[2];
                     JitterComandos = "0";
                     PerdaVideo = valorDelays[3];
                     PerdaComandos = "0";
                 }else{
                     DelayVideo = "0";
                     DelayComandos = valorDelays[1];
                     JitterVideo = "0";
                     JitterComandos = valorDelays[2];
                     PerdaVideo = "0";
                     PerdaComandos = valorDelays[3];
                 }
                 
                 if(DelayVideo.equals("null")) DelayVideo = "0";
                 if(DelayComandos.equals("null")) DelayComandos = "0";
                 if(JitterVideo.equals("null")) JitterVideo = "0";
                 if(JitterComandos.equals("null")) JitterComandos = "0";
                 if(PerdaVideo.equals("null")) PerdaVideo = "0";
                 if(PerdaComandos.equals("null")) PerdaComandos = "0";
                 
                 //TERIMOU:QoE: DATAFIM:DelayVideo:DelayComandos:JitterVideo:JitterComandos:PerdaVideo:PerdaComandos
                 //       :1a10:  HHMM : xx ms    : xx ms       : xx ms     : xx ms        : xx %     : xx %
                 String terminou = "TERMINOU:"+QoE+":"+formatter.format(date)+":"+DelayVideo+":"+DelayComandos
                         +":"+JitterVideo+":"+JitterComandos+":"+PerdaVideo+":"+PerdaComandos+":"+qualJogo+":"+JogariaNovamente+":"+comoSeSente;
                 //System.out.println(terminou);
                 sendMessage(terminou);
            
        } catch (IOException ex) {
                salvaErroLog("ERRO linha 806: "+ex);
        } catch (InterruptedException ex) {
            salvaErroLog("ERRO linha 808: "+ex);
        }
    }//GEN-LAST:event_botaoJogarMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            //sendMessage("Half-Life 3");
            sendMessage("DESLIGAAMBOS");
            stopConnection();
            desligaPC();
            System.exit(0);
        } catch (IOException ex) {
            salvaErroLog("ERRO linha 820: "+ex);
        }
    }//GEN-LAST:event_formWindowClosing

    private void botaoSairMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoSairMouseClicked
         int resposta;
        Object[] options = { "Sim", "Cancelar" };
                 resposta = JOptionPane.showOptionDialog(null, "Deseja sair? Os dois Computadores serão desligados!", "Informação", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                 //confirmar == 0 e cancelar == 1
                 if(resposta == JOptionPane.CANCEL_OPTION || resposta == JOptionPane.CLOSED_OPTION || resposta == JOptionPane.NO_OPTION){
                     return;
                 }
                 try {
                    sendMessage("DESLIGAAMBOS");
                    stopConnection();
                    desligaPC();
                    System.exit(0);
                } catch (IOException ex) {
                       salvaErroLog("ERRO linha 838: "+ex);
                }
    }//GEN-LAST:event_botaoSairMouseClicked

    private void campoNomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoNomeMouseClicked
        campoNome.setSelectionStart(0);
        campoNome.setSelectionEnd(campoNome.getText().length());
    }//GEN-LAST:event_campoNomeMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(telaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(telaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(telaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(telaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        
        pegaIp();
        startConnection(ipServidor,3333);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new telaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ImagemHalfLife;
    private javax.swing.JButton botaoJogar;
    private javax.swing.JButton botaoSair;
    private javax.swing.JTextField campoNome;
    private javax.swing.JLabel iconeLateral;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel labelNome;
    // End of variables declaration//GEN-END:variables
}

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.sound.sampled.*;

public class ScatterClient extends JFrame
{
   private JPanel jpGame, jpUI, jpChat, jpNorth, jpWest, jpSouth;
   private JMenuBar menu;
   private JMenu jmFile, jmHelp;
   private JMenuItem jmiExit, jmiAbout, jmiInst;
   private JButton jbJoinGame, jbReady, jbExtGame, jbSend, jbClick, jbStart;
   private JLabel jlPlyrs, jlCntDwn;
   private JTextArea jtaPlyrList, jtaChatBox, jtaClient;
   private String IP = "localhost";
   private Socket sock = null;
   private ObjectOutputStream oos = null;
   private int clientNum = 0;
   private Vector<String> clients = new Vector<String>();
   
   /* CONSTRUCTOR */
   public ScatterClient()
   {
      menu = new JMenuBar();
      add(menu);
      
      jmFile = new JMenu("File");
      menu.add(jmFile);
      
      jmHelp = new JMenu("Help");
      menu.add(jmHelp);
      
      jmiExit = new JMenuItem("Exit");
      jmFile.add(jmiExit);
      jmiExit.setMnemonic('E');
      jmiExit.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae){
               System.exit(0);
            }
         });
   
      jmiAbout = new JMenuItem("About");
      jmHelp.add(jmiAbout);
      jmiAbout.setMnemonic('A');
      jmiAbout.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae){
               JOptionPane.showMessageDialog(jmiAbout, "Scatter V1.7\n\nCreated by: Jason Kirshner\n\nScatter " + 
                                             "is a reaction based game, first person to ten points wins the game.\n" +
                                             "Click the green before the other person to gain a point,\n" +
                                             "but if you click too early and hit the red, you lose a point.\n" +
                                             "Good luck and let the fastest person win!");
            }
         });

      
      jmiInst = new JMenuItem("Instructions");
      jmHelp.add(jmiInst);
      jmiInst.setMnemonic('I');
      jmiInst.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae){
               JOptionPane.showMessageDialog(jmiInst, "1. Join a game and enter your player name.\n" + 
                                                      "2. Hit ready to get ready to start a game.\n" +
                                                      "3. The black button should turn red once all players are ready.\n" +
                                                      "4. Have one of the players click Start once all players are ready.\n" +
                                                      "5. As soon as the button turns green CLICK!\n" +
                                                      "6. Winner and loser is announced at the end of the game.");
            }
         });
      
      jpUI = new JPanel(new BorderLayout());
      
      jpChat = new JPanel(new BorderLayout());
      
      jpSouth = new JPanel(new FlowLayout());
      
      jpWest = new JPanel();
      
      jpGame = new JPanel(new BorderLayout());
         
      jlPlyrs = new JLabel("Players:");
      jpWest.add(jlPlyrs);
         
      jtaPlyrList = new JTextArea(25, 10);
      jtaPlyrList.setDisabledTextColor(Color.BLACK);
      jtaPlyrList.setEnabled(false);
      jtaPlyrList.setLineWrap(true); 
      JScrollPane scroll = new JScrollPane (jtaPlyrList);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      jpWest.add(scroll, BorderLayout.WEST);
      
      jtaChatBox = new JTextArea(22, 20);
      jtaChatBox.setEditable(false);
      jtaChatBox.setLineWrap(true);
      JScrollPane scroll2 = new JScrollPane (jtaChatBox);
      scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      jpChat.add(scroll2, BorderLayout.NORTH);
      
      jtaClient = new JTextArea(5, 5);
      jtaClient.setLineWrap(true);
      JScrollPane scroll3 = new JScrollPane (jtaClient);
      scroll3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      jpChat.add(scroll3, BorderLayout.CENTER);
      
      jpUI.add(jpWest, BorderLayout.WEST);
      
      jlCntDwn = new JLabel("Game Begins When Color Turns Red, But Don't Click If It's Red!");
      jpGame.add(jlCntDwn, BorderLayout.NORTH);
      
      jbJoinGame = new JButton("Join A Game");
      jbReady = new JButton("Ready");
      jbSend = new JButton("Send");
      jbStart = new JButton("Start");
      jbClick = new JButton();
      jbClick.setBackground(Color.BLACK);
      jbClick.setEnabled(false);
     
      jbJoinGame.addActionListener(
            new ActionListener(){
               public void actionPerformed(ActionEvent ae) {
                  try
                  {
                     String name;
                     name = JOptionPane.showInputDialog(jbJoinGame, "Create New playername");
                     clients.add(name);
                     sock = new Socket(IP, 16789);
                     oos = new ObjectOutputStream(sock.getOutputStream());
                     ClientThread ct = new ClientThread(sock);
                     jbJoinGame.setEnabled(false);
                     ct.start();
                     name.toUpperCase();
                     oos.writeUTF(name);
                     oos.flush();
                     
                     jbReady.addActionListener(
                        new ActionListener(){
                           public void actionPerformed(ActionEvent ae) {
                              try
                              {
                                 oos.writeUTF("ready");
                                 oos.flush();
                              }
                              catch(IOException ioe)
                              {
                                 System.out.println("Ready failed...");
                              }
                           }	
                        });
                        
                     jbSend.addActionListener(
                        new ActionListener(){
                           public void actionPerformed(ActionEvent ae){
                              if(jtaClient.getText().length() > 0)
                              {
                                 try
                                 {
                                    String clientMsg = jtaClient.getText();
                                    oos.writeUTF(name + ": " + clientMsg);		// Writes some String to server
                                    oos.flush();
                                    jtaClient.setText("");
                                 }
                                 catch(IOException ioe)
                                 {
                                    System.out.println("Send failed...");
                                 }
                              }   
                           }	
                        });
                        
                     jbClick.addActionListener(
                        new ActionListener(){
                           public void actionPerformed(ActionEvent ae) {
                              try
                              {
                                 if(jbClick.getBackground() == Color.RED)
                                 {
                                    oos.writeUTF(name + "-");
                                    oos.flush();
                                 }
                                 if(jbClick.getBackground() == Color.GREEN);
                                 {
                                    oos.writeUTF(name + "+");
                                    oos.flush();
                                 }
                              }
                              catch(IOException ioe)
                              {
                                 System.out.println("Click failed...");
                              }
                           }	
                        });
                        
                     jbStart.addActionListener(
                        new ActionListener(){
                           public void actionPerformed(ActionEvent ae) {
                              try
                              {
                                 if(!(jbReady.isEnabled()))
                                 {
                                    oos.writeUTF("start");
                                    oos.flush();
                                    jbStart.setEnabled(false);
                                 }
                              }
                              catch(IOException ioe)
                              {
                                 System.out.println("Start failed...");
                              }
                           }	
                        });
                  }
                  catch(IOException ioe)
                  {
                     System.out.println("Can't connect to server");
                  }
               }	
            });
      jpSouth.add(jbJoinGame);
      jpSouth.add(jbReady);
      jpChat.add(jbSend, BorderLayout.SOUTH);
      jpGame.add(jbClick, BorderLayout.CENTER);
      jpGame.add(jbStart, BorderLayout.SOUTH);
      jpUI.add(jpSouth, BorderLayout.SOUTH);
      
      add(jpUI, BorderLayout.WEST);
      add(jpChat, BorderLayout.EAST);
      add(jpGame, BorderLayout.CENTER);
      
      setJMenuBar(menu);
      setTitle("Scatter");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      setResizable(false);
      setSize(1000, 500);
      setVisible(true);
   }
   
   public void winSound()
   {
      try
      {
         File win = new File("win.wav");
         AudioInputStream stream = AudioSystem.getAudioInputStream(win);
         AudioFormat format = stream.getFormat();
         DataLine.Info info = new DataLine.Info(Clip.class, format);
         Clip clip = (Clip) AudioSystem.getLine(info);
         clip.open(stream);
         clip.start();
      }
      catch(Exception e){}
   }
   
   public void loseSound()
   {
      try
      {
         File lose = new File("lose.wav");
         AudioInputStream stream = AudioSystem.getAudioInputStream(lose);
         AudioFormat format = stream.getFormat();
         DataLine.Info info = new DataLine.Info(Clip.class, format);
         Clip clip = (Clip) AudioSystem.getLine(info);
         clip.open(stream);
         clip.start();
      }
      catch(Exception e){}
   }
   
   //Thread to send messages to Server
   class ClientThread extends Thread
   {
      Socket sock = null;
      String serverMsg = "";
      int serverNum = 0;
      String name = "";
      int num;
      
      public ClientThread(Socket sock)
      {
         this.sock = sock;
      }
      
      public void run()
      {
         try
         {  
            num = clientNum++;
            name = clients.get(num);
            System.out.println(name);
            
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
          
            while((serverMsg = ois.readUTF()) != null)
            {
               if(serverMsg.indexOf(':') >= 0)
               {
                  jtaChatBox.append("\n" + serverMsg);
               }
               else if((serverMsg.indexOf('$') >= 0) && (!(serverMsg.indexOf(':') >= 0)))
               {
                  jbStart.setEnabled(false);
                  jbClick.setBackground(Color.GREEN);
               }
               else if((serverMsg.indexOf('?') >= 0) && (!(serverMsg.indexOf(':') >= 0)))
               {
                  jbClick.setBackground(Color.RED);
               }
               else if((serverMsg.indexOf('!') >= 0) && (!(serverMsg.indexOf(':') >= 0)))
               {
                  if((serverMsg.indexOf(name) >= 0))
                  {
                     winSound();
                  }
                  else
                  {
                     loseSound();
                  }
               }
               else
               {
                  jtaPlyrList.append("\n" + serverMsg);
                  jbClick.setEnabled(true);
                  jbReady.setEnabled(false);
                  jbClick.setBackground(Color.RED);
               }
            }
         }
         catch(ConnectException ce)
         { 
            System.out.println("Server is Not Online"); 
         }
         catch(UnknownHostException uhe)
         {
            System.out.println("no host");
         }
         catch(IOException ioe)
         {
            System.out.println("Server was terminated...");
            ioe.printStackTrace();
         }
      }
   }
   
   public static void main(String [] args)
   {
      new ScatterClient();
   }
}
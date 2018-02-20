import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

/**  
Scatter - Final Project
Server
**/
public class ScatterServer
{
   
   // Attributes
   Random r = new Random();
   private final static int LOW = 1000;
   private final static int HIGH = 6000;
   private int result = 0;
   private int numRdy = 0;
   private boolean done = false;
   private int j = 0;
   
   //Array of PrintWriters
   Vector<PrintWriter> toPlayers = new Vector<PrintWriter>();
   Vector<String> playerNames = new Vector<String>();
   Vector<ObjectOutputStream> toPlayersObj = new Vector<ObjectOutputStream>();
   /**
    * main method that calls the ScatterServer constructor
    */
   public static void main(String [] args)
   {
      new ScatterServer();
   }
   /**
    * ScatterServer constructor that opens the ServerSocket and the Socket and runs the ThreadServer
    */
   public ScatterServer()
   {
      ServerSocket ss = null;
      try
      {
         ss = new ServerSocket(16789);
         Socket sock = null;
         while(true)// run server forever once up
         {     
            sock = ss.accept(); // wait for connection
            System.out.println("Connection Made");
            ThreadServer ths = new ThreadServer(sock);
            ths.start();
         } // end while
      }     
      catch(BindException be)
      {
         System.out.println("Server already running on this computer, stopping.");
      } 
      catch(IOException ioe)
      {
         System.out.println("Connection Failure");
      }
   } // end main
   /**
    * ThreadServer that extends Thread.
    * Thread that reads/writes to PrintWriter and BufferedReader
    */
   class ThreadServer extends Thread
   {
      Socket sock;
      
      public ThreadServer(Socket sock)
      {
         this.sock = sock;
      }
      public void run()
      {
         String clientMsg = "";
         try
         {
            int point = 0;
            //Input
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            //Output
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            //PrintWriter opw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            				
            //toPlayers.add(opw); //adds to output list
            toPlayersObj.add(oos);
            
            clientMsg = ois.readUTF();
           
            playerNames.add(clientMsg);
            System.out.println(clientMsg);
            
            loop:
            while(done == false)
            {
               if(playerNames.size() >= 2)
               {
                  clientMsg = ois.readUTF();
                  System.out.println(clientMsg);
                  if(clientMsg.indexOf(':') >= 0)
                  {  
                     System.out.println("inside Ready");
                     for(ObjectOutputStream os: toPlayersObj)//writes to all clients
                     {
                        System.out.println(clientMsg);
                        os.writeUTF(clientMsg);	//to client
                        os.flush(); //clears the remaining data in the PrintWriter
                     }
                  }
                  if(clientMsg.equals("ready"))
                  {
                     numRdy++;
                     while(numRdy <= playerNames.size())
                     {
                        System.out.println("Within loop");
                        if(numRdy == playerNames.size())
                        {
                           for(ObjectOutputStream os: toPlayersObj)
                           {
                              for(String str: playerNames)
                              {
                                 System.out.println(str);
                                 os.writeUTF(str);
                                 os.flush();
                              }
                           }
                           break loop;
                        }
                        done = true;
                        break loop;
                     }
                  }
               }   
            }
            
            if(done)
            {  
               loop2:
               while(done)
               {
                  if((clientMsg = ois.readUTF()) != null )//continuously reads lines of input
                  {  
                     if(clientMsg.indexOf(':') >= 0)
                     {  
                        for(ObjectOutputStream os: toPlayersObj)//writes to all clients
                        {
                           System.out.println(clientMsg);
                           os.writeUTF(clientMsg);	//to client
                           os.flush(); //clears the remaining data in the PrintWriter
                        }
                     }
                     for(String s: playerNames)
                     {
                        if((clientMsg.toLowerCase().indexOf(s.toLowerCase()) != -1) && (!(clientMsg.indexOf(':') >= 0)))
                        {
                           if(clientMsg.indexOf('-') >= 0)
                           {
                              point--;
                              String sPoint = Integer.toString(point);
                              for(ObjectOutputStream os: toPlayersObj)//writes to all clients
                              {
                                 System.out.println("sent >");
                                 os.writeUTF("?");	//to client
                                 os.flush(); //clears the remaining data in the PrintWriter
                              }
                           }
                           else if(clientMsg.indexOf('+') >= 0)
                           {
                              point++;
                              if(point == 10)
                              {
                                 for(ObjectOutputStream os: toPlayersObj)//writes to all clients
                                 {
                                    System.out.println("Winner is " + s + "!");
                                    os.writeUTF("Winner is " + s + "!");
                                    os.flush();
                                    done = false;
                                    break loop2;
                                 }
                              }
                              for(ObjectOutputStream os: toPlayersObj)//writes to all clients
                              {
                                 System.out.println("sent >");
                                 os.writeUTF("?");	//to client
                                 os.flush(); //clears the remaining data in the PrintWriter
                              }
                           }
                        }
                     }
                  }
                  if((clientMsg.equals("start")) || (clientMsg.indexOf("-") >= 0) || (clientMsg.indexOf("+") >= 0))
                  {
                     result = r.nextInt(HIGH-LOW) + LOW;
                     System.out.println(result);
                     sleep(result);
                        
                     for(ObjectOutputStream os: toPlayersObj)//writes to all clients
                     {
                        os.writeUTF("$");	//to client
                        os.flush(); //clears the remaining data in the PrintWriter
                     }
                  }
               }
            }
         }
         catch( SocketException se )
         { 
            System.out.println("Client Disconnected..."); 
         }
         catch( IOException ioe ) 
         { 
            System.out.println("Connection Failed"); 
            ioe.printStackTrace();
         }
         catch(InterruptedException ie)
         {
            ie.printStackTrace();
         }
       /*catch(ClassNotFoundException cnfe)
         {
            System.out.println("Something wrong with ois");
         }*/
      } // end while
   } // end class ThreadServer 
} // end ScatterServer
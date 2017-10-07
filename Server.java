import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
/*
 * The StartRemotePeers class b PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class Server {
	int peerId;
	ServerSocket serverSocket;
    List<Socket> socket = new ArrayList<Socket>();

	public Server(int peerId, ServerSocket serverSocket){
	  this.peerId = peerId;
      this.serverSocket = serverSocket; 
	}

    public void listen(){
      try{
      	while(true){
      		Socket temp = serverSocket.accept();
      		socket.add(temp);

      		new Handler(temp).start();
      		//System.out.println("One peer connection to peerID: " + peerId);
      	}
      }
      catch(Exception e){
         System.out.println(e);
      }      
    }

    public static class Handler extends Thread{
    	Socket socket;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        

        public Handler(Socket socket) {
          this.socket = socket;
        }

         public void run(){


        }

    }



}
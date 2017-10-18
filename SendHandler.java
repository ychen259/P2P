import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;

/*
 * The StartRemotePeers class b PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class SendHandler implements Runnable {
   Socket socket;
   ObjectOutputStream out;         //stream write to the socket
   ObjectInputStream in;

   public SendHandler(Socket socket){
     this.socket = socket;
   }

   public void run(){
    
   }

}
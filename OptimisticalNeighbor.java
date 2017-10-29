import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.Map.Entry;

public class OptimisticalNeighbor implements Runnable {
   peerProcess peer;

   Map<Integer, DataOutputStream> allOutStream = new HashMap<Integer, DataOutputStream>(); /*store all output stream*/
                                                                                              /*I need this to send have message to all neighbors*/
                                                                                              /*Integer: neighbor peer ID*/
                                                                                              /*ObjectOutputStream: their output stream*/

  int NumberOfPreferredNeighbors;


   public OptimisticalNeighbor(peerProcess peer, Map<Integer,DataOutputStream> allOutStream){
     this.peer = peer;
     this.allOutStream = allOutStream;

     fileInfo info = new fileInfo();
     NumberOfPreferredNeighbors = info.NumberOfPreferredNeighbors;
   }

  public void sendMessage(DataOutputStream out, byte[] msg){
    try{
      //stream write the message
      out.write(msg);
      out.flush();
    }
    catch(IOException ioException){
      ioException.printStackTrace();
    }
  }

  public void run(){

  }
}
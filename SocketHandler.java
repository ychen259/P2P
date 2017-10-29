import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;

import java.util.concurrent.*;

/*
 * The StartRemotePeers class b PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class SocketHandler implements Runnable {
	peerProcess peer;
    DataOutputStream out;         //stream write to the socket
 	DataInputStream in;
 
  Map<Integer,DataOutputStream> allOutStream = new HashMap<Integer, DataOutputStream>(); /*store all output stream*/
                                                                                              /*I need this to send have message to all neighbors*/
                                                                                              /*Integer: neighbor peer ID*/
                                                                                              /*ObjectOutputStream: their output stream*/

  Map<Integer,DataInputStream> allInputStream = new HashMap<Integer, DataInputStream>(); /*store all input stream*                                                                        
                                                                                                  /*Integer: neighbor peer ID*/
                                                                                                  /*ObjectOutputStream: their input stream*/
  ScheduledExecutorService  executor = Executors.newScheduledThreadPool(1);

  int UnchokingInterval;
  int OptimisticUnchokingInterval;

	public SocketHandler(int peerId){
	  this.peer = new peerProcess(peerId);

    fileInfo info = new fileInfo();
    UnchokingInterval = info.UnchokingInterval;
    OptimisticUnchokingInterval = info.OptimisticUnchokingInterval;
	}

    public void run(){
      /*build socket with all other peer*/
      Utilities.threadSleep(500);
      peer.buildSocket();

      int numberOfNeighbor = peer.NeighborPeerInfo.size();

      /*Build output stream and input stream*/
      for(int i = 0; i < numberOfNeighbor; i++){
        int neighborId = Integer.parseInt(peer.NeighborPeerInfo.get(i).peerId);
        try{
          /*using key peerID to get Socket from neighborSocket map*/
          Socket socket = peer.neighborSocket.get(neighborId); 

          DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
          outputStream.flush();
          DataInputStream inputStream = new DataInputStream(socket.getInputStream());
          allInputStream.put(neighborId, inputStream);   
          allOutStream.put(neighborId, outputStream);
        }
        catch(Exception e){
          System.out.println(e);
        }
      }

      /*send handshake message to all neighbor*/
      handshake handshakeMsg = new handshake(peer.peerId);
      
      for(int i = 0; i < numberOfNeighbor; i++){
        int neighborId = Integer.parseInt(peer.NeighborPeerInfo.get(i).peerId);

        try{
          /*Get peer and this neighbor's input and output stream*/
          out = allOutStream.get(neighborId);
          in = allInputStream.get(neighborId);
         /******************************************Send and receive Handshake********************************************/

		      /*convert handshake object to 32 byte array*/
		      byte[] handshakeMsgByteArray = Utilities.combineByteArray(handshakeMsg.header, handshakeMsg.zeroBits);
		      handshakeMsgByteArray = Utilities.combineByteArray(handshakeMsgByteArray, handshakeMsg.peerId);

          /*send handshake message to a neighbor*/
          out.write(handshakeMsgByteArray);
          out.flush();

          System.out.println("Peer " + peer.peerId + ": handshake message send to " + neighborId);
          /*****************************************Receive all kinds of message********************************************/
          Thread receiveHandler = new Thread(new ReceiveHandler(peer, neighborId, in, out, allOutStream));
          receiveHandler.start();
        }
        catch(Exception e){
          System.out.println(e);
        }
      }
      
      executor.scheduleAtFixedRate(new preferredNeighbor(peer, allOutStream), 0, UnchokingInterval, TimeUnit.SECONDS);

    }

}
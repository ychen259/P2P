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
  ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;
 
  Map<Integer,ObjectOutputStream> allOutStream = new HashMap<Integer, ObjectOutputStream>(); /*store all output stream*/
                                                                                              /*I need this to send have message to all neighbors*/
                                                                                              /*Integer: neighbor peer ID*/
                                                                                              /*ObjectOutputStream: their output stream*/

  Map<Integer,ObjectInputStream> allInputStream = new HashMap<Integer, ObjectInputStream>(); /*store all input stream*                                                                        
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

	public void sendMessage(byte[] msg){
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
      /*build socket with all other peer*/
      peer.buildSocket();

      int numberOfNeighbor = peer.NeighborPeerInfo.size();

      /*Build output stream and input stream*/
      for(int i = 0; i < numberOfNeighbor; i++){
        int neighborId = Integer.parseInt(peer.NeighborPeerInfo.get(i).peerId);
        try{
          /*using key peerID to get Socket from neighborSocket map*/
          Socket socket = peer.neighborSocket.get(neighborId); 

          ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
          outputStream.flush();
          ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
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
		      sendMessage(handshakeMsgByteArray);
          System.out.println("Peer " + peer.peerId + ": handshake message send to " + neighborId);
          /*****************************************Receive all kinds of message********************************************/
          Thread receiveHandler = new Thread(new ReceiveHandler(peer, neighborId, in, out, allOutStream));
          receiveHandler.start();
        }
        catch(Exception e){
          System.out.println(e);
        }
      }
      
      executor.scheduleAtFixedRate(new preferredNeighbor(peer, allOutStream), 0, OptimisticUnchokingInterval, TimeUnit.SECONDS);

    }

}
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;

/*
 * The StartRemotePeers class b PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class Handler implements Runnable {
	peerProcess peer;
    ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;

	public Handler(int peerId){
	  this.peer = new peerProcess(peerId);
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
      byte[] lengthOfMessage = new byte [4]; /*Store the message length*/
      int length;                     /*Convert lenghtOfMessage to int and store it in lenght*/
      byte[] msgType = new byte[1];
      byte[] playload;

      peer.buildSocket();

      /*send handshake message to all neighbor*/
      handshake handshakeMsg = new handshake(peer.peerId);
      int numberOfNeighbor = peer.NeighborPeerInfo.size();
      for(int i = 0; i < numberOfNeighbor; i++){
        int neighborId = Integer.parseInt(peer.NeighborPeerInfo.get(i).peerId);

        try{
          /*using key peerID to get Socket from neighborSocket map*/
          Socket socket = peer.neighborSocket.get(neighborId); 

          out = new ObjectOutputStream(socket.getOutputStream());
		  out.flush();
		  in = new ObjectInputStream(socket.getInputStream());
 
         /******************************************Send and receive Handshake********************************************/

		  /*convert handshake object to 32 byte array*/
		  byte[] handshakeMsgByteArray = Utilities.combineByteArray(handshakeMsg.header, handshakeMsg.zeroBits);
		  handshakeMsgByteArray = Utilities.combineByteArray(handshakeMsgByteArray, handshakeMsg.peerId);

          /*send handshake message to a neighbor*/
		  sendMessage(handshakeMsgByteArray);
        
          /*Handshake message is 32 bytes*/
          byte[] message = new byte[32];
          in.readFully(message, 0, message.length);
          
          byte[] msgHeader = Arrays.copyOfRange(message, 0, 18); /* copy index from 0 to 18 (not include 18)*/
          String msgHeaderInString = new String(msgHeader); /*convert byte array to String*/

          byte[] msgId = Arrays.copyOfRange(message, 28, 32); /* copy index from 28 to 32 (not include 32)*/
          int msgIdInInt = Utilities.ByteArrayToint(msgId); /*convert byte array to int*/

          /*check whether the handshake header is right and the peer ID is the expected one. */
          if("P2PFILESHARINGPROJ".equals(msgHeaderInString) && msgIdInInt == neighborId){
          	System.out.println("peer " + peer.peerId + " correctly receive handshake message from " + neighborId);
          }else{
          	System.out.println("peer " + peer.peerId + " does not correctly receive handshake message from " + neighborId);
          }

         /*******************************************Send Bitfield ******************************************************/
          byte[] bitfield = peer.bitfieldMap.get(peer.peerId); 

          message bitfieldMsg = (new message()).bitfield(bitfield);

          /*conver object message to byte array*/
		  byte[] bitfieldMsgByteArray = Utilities.combineByteArray(bitfieldMsg.msgLen, bitfieldMsg.msgType);
		  bitfieldMsgByteArray = Utilities.combineByteArray(bitfieldMsgByteArray, bitfieldMsg.payload);

          sendMessage(bitfieldMsgByteArray);
          /*****************************************Send bitfield complete********************************************/
        }
        catch(Exception e){
        	System.out.println(e);
        }

        try{
        /*Handle all kinds of message*/
        in.readFully(lengthOfMessage, 0, lengthOfMessage.length);
        length = Utilities.ByteArrayToint(lengthOfMessage);
System.out.println("length: " + length);
          in.readFully(msgType, 0, msgType.length);
Utilities.printByteArray(msgType);
          playload = new byte[length-1];
          in.readFully(playload, 0, playload.length);

          Utilities.printByteArray(playload);
        }catch(Exception e){
        	System.out.println(e);
        }


      }

    }

}
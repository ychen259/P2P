import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;

/*
 * The StartRemotePeers class b PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class ReceiveHandler implements Runnable {
  peerProcess peer;
  int neighborId;
  ObjectOutputStream out;         //stream write to the socket
  ObjectInputStream in;

  public ReceiveHandler(peerProcess peer, int neighborId, ObjectInputStream in, ObjectOutputStream out){
    this.peer = peer;
    this.neighborId = neighborId;
    this.out = out;
    this.in = in;
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
    byte[] playload = null;

    while(true){
        try{
            /*Handle all kinds of message*/
            /*length == length of message (first 4 bytes)*/
            /*msgType[0] == type of message (byte 5)*/
            /*playload == rest of message (all byte after byte 5)*/
            in.readFully(lengthOfMessage, 0, lengthOfMessage.length);

            /*max length is 2^32-1 = 4 294 967 295*/
            /*if the length is greater than 2^32 -1 ==> this is handshake message*/
            length = Utilities.ByteArrayToint(lengthOfMessage);

            /*Read one byte from socket*/
            in.readFully(msgType, 0, msgType.length);
            
            /*If it is handshake message, then the fifth char is I*/
            byte flagForHandshake = 'I';
            /*Handle handshake message*/
            if(msgType[0] == flagForHandshake){
          	    System.out.println("Peer " + peer.peerId + ": receive handshake message from " + neighborId); 
          	    byte [] restByte = new byte[27];

                /*Read the another 28 byte from socket*/
                in.readFully(restByte, 0, restByte.length);

                /*Handshake message is 32 bytes*/
                /*Combine first 4 bytes + 1 + 27 bytes into 32 bytes*/
                byte[] message = Utilities.combineByteArray(lengthOfMessage, msgType);
                message = Utilities.combineByteArray(message, restByte);      

                byte[] msgHeader = Arrays.copyOfRange(message, 0, 18); /* copy index from 0 to 18 (not include 18)*/
                String msgHeaderInString = new String(msgHeader); /*convert byte array to String*/
  
                byte[] msgId = Arrays.copyOfRange(message, 28, 32); /* copy index from 28 to 32 (not include 32)*/
                int msgIdInInt = Utilities.ByteArrayToint(msgId); /*convert byte array to int*/
                
                //System.out.println("Msg ID: " +	 msgIdInInt + "  Except value is : " + neighborId);
                
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
          
                System.out.println("Peer " + peer.peerId + ": Bitfield message is sent to " + neighborId);

            }
            /*Handle Actual message*/
            else{
                /*length - 1 = message length - length of msgType = length of playload*/
                if((length - 1) > 0){
                  playload = new byte[length-1];
                  in.readFully(playload, 0, playload.length);
                }


                /*choke = 0*/
                /*unchoke = 1*/
                /*interested = 2*
                /*notInterested = 3*/
                /*have = 4*/
                /*bitfield = 5*/
                /*request = 6*/
                /*piece = 7*/
                /*else for handshake message*/
                if(msgType[0] == 0){
 
                }
                else if(msgType[0] == 1){
            
                }
                else if(msgType[0] == 2){
                    System.out.println("Interested message");
                }
                else if(msgType[0] == 3){
                    System.out.println("not Interested message");          
                }
                else if(msgType[0] == 4){
            
                }
                else if(msgType[0] == 5){
    		        System.out.println("Peer " + peer.peerId + ": receive bitfield message from " + neighborId);  

                    /*receive bitfield and update the bitfield of my neighbor*/
                    /*playload inside of message is bitmap of its neighbor*/
                    peer.bitfieldMap.put(neighborId, playload);

                    /*When it receives bitfield, it also can upldate its isInterested hashmap and decide to send interested message to neighbor or not*/
                    int numberOfPiece = peer.numberOfPiece;
                    int myId = peer.peerId;
                    byte [] myBitfieldMap = peer.bitfieldMap.get(myId);
                    byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId);
                    boolean hasInterestingPiece = false; /*flag to check*/

                    /*Try to find interesting piece from neighbor == compare bitmap*/
                    /*send a interested message if neighbor has some pieces (al least one) I want*/
                    /*send a not interestedm message if neighbor does not have any interesting piece*/
                    for(int i = 0 ; i < numberOfPiece; i++){
                        if(Utilities.isSetBitInBitfield(myBitfieldMap, i) == false && Utilities.isSetBitInBitfield(neighborBitfieldMap, i) == true){

             	            hasInterestingPiece = true;
                            /*send an interesting message and break*/
                            message interestedMsg = (new message()).interested();

                            /*conver object message to byte array*/
		                    byte[] interestedMsgByteArray = Utilities.combineByteArray(interestedMsg.msgLen, interestedMsg.msgType);

		                    sendMessage(interestedMsgByteArray);
		                    System.out.println("Peer " + myId + ": Interested message is send to " + neighborId);
		                    break;
                        }
                    }

                    if(hasInterestingPiece == false){
           	            /*send an not interesting message*/
                        message notInterestedMsg = (new message()).notInterested();
                        /*conver object message to byte array*/
		                byte[] notInterestedMsgByteArray = Utilities.combineByteArray(notInterestedMsg.msgLen, notInterestedMsg.msgType);

		                sendMessage(notInterestedMsgByteArray);
		                System.out.println("Peer " + myId + ": not Interested message is send to " + neighborId);
                    }
                }
                else if(msgType[0] == 6){
            
                }
                else if(msgType[0] == 7){
            
                }
            }

        /*print out the bitmap for testing*/
       /* int numberOfPiece = peer.numberOfPiece;
        int sizeOfBitfield = ((peer.numberOfPiece % 8) == 0? numberOfPiece/8: numberOfPiece/8 + 1 );

        for(Map.Entry<Integer,byte[]> entry : peer.bitfieldMap.entrySet()){
          System.out.print("Bitfield for " + entry.getKey() + ": ");
          for(int i = 0; i < sizeOfBitfield; i++){
            System.out.print(entry.getValue()[i] + " ");
          }
          System.out.println("\n");
        }*/
        /*End testing here*/

    }
    catch(Exception e){
     System.out.println("error on receving message");
    }

    }
  }

}
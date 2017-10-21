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
  Map<Integer,ObjectOutputStream> allOutStream = new HashMap<Integer, ObjectOutputStream>(); /*store all output stream*/
                                                                                              /*I need this to send have message to all neighbors*/
                                                                                              /*Integer: neighbor peer ID*/
                                                                                              /*ObjectOutputStream: their output stream*/

  long startDownloadTime;
  long stopDownloadTime;

  public ReceiveHandler(peerProcess peer, int neighborId, ObjectInputStream in, ObjectOutputStream out, Map<Integer,ObjectOutputStream> allOutStream){
    this.peer = peer;
    this.neighborId = neighborId;
    this.out = out;
    this.in = in;
    this.allOutStream = allOutStream;
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

  public void sendMessageToAll(byte[] msg){
	try{
        for(Map.Entry<Integer,ObjectOutputStream> entry : allOutStream.entrySet()){
            ObjectOutputStream outstream = entry.getValue();
            outstream.write(msg);
            outstream.flush();
        }

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

/*   untest
*choke
*unchoke
*have
*request
*piece
*/
                /*choke = 0*/
                /*unchoke = 1*/
                /*interested = 2*
                /*notInterested = 3*/
                /*have = 4*/
                /*bitfield = 5*/
                /*request = 6*/
                /*piece = 7*/
                if(msgType[0] == 0){
                    System.out.println("Peer " + peer.peerId + ": receive choke message from " + neighborId);
                    peer.isChoke.put(neighborId, true);
                    peer.downloadRate.put(neighborId, 0); // set the download rate from this neighbor to 0
                                                          // Because it sends me a choke message
                }
                else if(msgType[0] == 1){
                    System.out.println("Peer " + peer.peerId + ": receive unchoke message from " + neighborId);
                    peer.isChoke.put(neighborId, false);

                    byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId);
                    byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId);
                    int numberOfPiece = peer.numberOfPiece;
                    int numberOfPieceIhave = 0;
                    for(int i = 0; i < numberOfPiece; i++){
                       if(Utilities.isSetBitInBitfield(myBitfieldMap, i) == true){
                         numberOfPieceIhave++;
                       }
                    }

                    if(numberOfPieceIhave == numberOfPiece){
                      System.out.println("Peer" + peer.peerId + " : I have complete file");
                    }
                    else{
                        /****Get random interesting piece from neighbor ****/
                        int desiredIndex = 0;
                        Random rand = new Random();

                        while(true){
                          desiredIndex = rand.nextInt(peer.numberOfPiece); /*generate random number from 0 to (numberOfPiece-1)*/

                          /*Break out the loop until find a valid index*/
                          if(Utilities.isSetBitInBitfield(myBitfieldMap, desiredIndex) == false && 
                            Utilities.isSetBitInBitfield(neighborBitfieldMap, desiredIndex) == true)
                              break;

                        }

                      /***send the request message to neighbor***/
                        message requestMsg = (new message()).request(desiredIndex); /*create a message object*/
                        byte[] requestMsgByteArray = Utilities.combineByteArray(requestMsg.msgLen, requestMsg.msgType);//conver object message to byte array
                        requestMsgByteArray = Utilities.combineByteArray(requestMsgByteArray, requestMsg.payload); //conver object message to byte array
                        sendMessage(requestMsgByteArray);

                        System.out.println("Peer:" + peer.peerId + ": send request message to " + neighborId);
                    }

                }
                else if(msgType[0] == 2){
                    System.out.println("Peer " + peer.peerId + ": receive interested message from " + neighborId);

                    /*If receive interested message, then change the isInterested talbe*/
                    peer.isInterested.put(neighborId, true);

                }
                else if(msgType[0] == 3){
                    System.out.println("Peer " + peer.peerId + ": receive not Interested message from " + neighborId);

                    /*If receive interested message, then change the isInterested talbe*/
                    peer.isInterested.put(neighborId, false);    
                }
                else if(msgType[0] == 4){
                    System.out.println("Peer " + peer.peerId + ": receive have message from " + neighborId);
                    int indexOfPiece = Utilities.ByteArrayToint(playload);
                     
                     /*update the bitfield for neighbor*/
                     byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId); /*get bitfield from hash table*/
                     Utilities.setBitInBitfield(neighborBitfieldMap, indexOfPiece); /*update bitfield*/
                     peer.bitfieldMap.put(neighborId, neighborBitfieldMap); /*Store the bitfield back to hashmap*/

                     /*Check if I have that piece or not. If I do not have that piece, set to neighbor to interested neighbor*/
                     byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId);
                     if(Utilities.isSetBitInBitfield(myBitfieldMap, indexOfPiece) == false){
                     	peer.isInterested.put(neighborId, true);
                     }
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
                    System.out.println("Peer " + peer.peerId + ": receive request message from " + neighborId);

                    /*******Get piece of data from file********/
                    String filename = "./peer_" + peer.peerId + "/" + (new fileInfo().FileName);
                    int pieceSize = new fileInfo().PieceSize;
                    int indexOfPiece = Utilities.ByteArrayToint(playload);
                    byte [] piece = Utilities.readPieceFromFile(filename, pieceSize, indexOfPiece);

                    /*set a start time before send data*/
                    startDownloadTime = System.currentTimeMillis();

                    /***send the piece of data to neighbor***/
                    message pieceMsg = (new message()).piece(indexOfPiece, piece); /*create a message object*/
		                byte[] pieceMsgByteArray = Utilities.combineByteArray(pieceMsg.msgLen, pieceMsg.msgType);//conver object message to byte array
		                pieceMsgByteArray = Utilities.combineByteArray(pieceMsgByteArray, pieceMsg.payload); //conver object message to byte array
                    sendMessage(pieceMsgByteArray);
                }
                else if(msgType[0] == 7){
                    System.out.println("Peer " + peer.peerId + ": receive piece message from " + neighborId);


                    /*first 4 byte in playload is piece index, rest is actual piece*/ 
                    byte [] indexOfPieceByteArray = Arrays.copyOfRange(playload, 0, 5); // read first 4 byte from pllayload
                    byte [] piece = Arrays.copyOfRange(playload, 5, (length - 1 + 1)); // length - length of message type ==  length of play load 
                                                                                       // because I want to read from 5 to end of playload
                                                                                       // I plue 1, syntax of copyOfRange (will not read length -1 + 1)
                                                                                       // It will read till playload[lengthOfPlayload -1]
                    
                    int indexOfPiece = Utilities.ByteArrayToint(indexOfPieceByteArray);

                    /*store piece into myfile*/
                    String filename = "./peer_" + peer.peerId + "/" + (new fileInfo().FileName);
                    int pieceSize = new fileInfo().PieceSize;
                    Utilities.writePieceToFile(filename, pieceSize, indexOfPiece, piece);

                    /*record the download stop time, calculate the current download rate*/
                    stopDownloadTime = System.currentTimeMillis();
                    int downloadRate = piece.length / (int)(stopDownloadTime - startDownloadTime);
                    peer.downloadRate.put(neighborId, downloadRate);

                    /*update my bitfield*/
                     byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId); /*get bitfield from hash table*/
                     Utilities.setBitInBitfield(myBitfieldMap, indexOfPiece); /*update bitfield*/
                     peer.bitfieldMap.put(peer.peerId, myBitfieldMap); /*Store the bitfield back to hashmap*/

                    /*If all piece have been download, then report I receive whole file*/
                    int numberOfPiece = peer.numberOfPiece;
                    int numberOfPieceIhave = 0;
                    for(int i = 0; i < numberOfPiece; i++){
                       if(Utilities.isSetBitInBitfield(myBitfieldMap, i) == true){
                       	 numberOfPieceIhave++;
                       }
                    }

                    if(numberOfPieceIhave == numberOfPiece){
                    	System.out.println("Peer" + peer.peerId + ": I have complete file");
                    }
                    
                    /*send a have message to all my neighbor*/
                    message haveMsg = (new message()).have(indexOfPiece); /*create a message object*/
		                byte[] haveMsgByteArray = Utilities.combineByteArray(haveMsg.msgLen, haveMsg.msgType);//conver object message to byte array
		                haveMsgByteArray = Utilities.combineByteArray(haveMsgByteArray, haveMsg.payload); //conver object message to byte array
                    sendMessageToAll(haveMsgByteArray);

                    /*check do I need to send an not interested message or not*/ 
                    /*If neighbor not longer has any interesting piece, send an not interested message*/
                    boolean hasInterestingPiece = false;
                    byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId);
                    for(int i = 0 ; i < numberOfPiece; i++){
                        if(Utilities.isSetBitInBitfield(myBitfieldMap, i) == false && Utilities.isSetBitInBitfield(neighborBitfieldMap, i) == true){
             	            hasInterestingPiece = true;
		                    break;
                        }
                    }

                    if(hasInterestingPiece == false){
           	            /*send an not interesting message*/
                        message notInterestedMsg = (new message()).notInterested();
                        /*conver object message to byte array*/
		                byte[] notInterestedMsgByteArray = Utilities.combineByteArray(notInterestedMsg.msgLen, notInterestedMsg.msgType);

		                sendMessage(notInterestedMsgByteArray);
		                System.out.println("Peer " + peer.peerId + ": not Interested message is send to " + neighborId);
		                break;
                    }

                    /*If I am not choked by neighbor, request more pieces, send a request message*/
                    boolean isChokeByNeighbor = peer.isChoke.get(neighborId);

                    if(isChokeByNeighbor == false){
                        /****Get random interesting piece from neighbor ****/
                        int desiredIndex;
                        Random rand = new Random();

                        while(true){
                          desiredIndex = rand.nextInt(numberOfPiece); /*generate random number from 0 to (numberOfPiece-1)*/

                          /*Break out the loop until find a valid index*/
                          if(Utilities.isSetBitInBitfield(myBitfieldMap, desiredIndex) == false && Utilities.isSetBitInBitfield(neighborBitfieldMap, desiredIndex) == true)
                          	break;
                        }

                    	/***send the request message to neighbor***/
                        message requestMsg = (new message()).request(desiredIndex); /*create a message object*/
		                    byte[] requestMsgByteArray = Utilities.combineByteArray(requestMsg.msgLen, requestMsg.msgType);//conver object message to byte array
		                    requestMsgByteArray = Utilities.combineByteArray(requestMsgByteArray, requestMsg.payload); //conver object message to byte array
                        sendMessage(requestMsgByteArray);
                        System.out.println("Peer:" + peer.peerId + ": send request message to " + neighborId);
                    }
                }
            }
    }
    catch(Exception e){
     System.out.println("error on receving message");
    }

    }
  }

}
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

  public void sendMessage(message msg){
	  try{
		  //stream write the message
		  out.writeObject(msg);
		  out.flush();
	  }
	  catch(IOException ioException){
		  ioException.printStackTrace();
	  }
  }

  public void sendMessageToAll(message msg){
	  try{
        for(Map.Entry<Integer,ObjectOutputStream> entry : allOutStream.entrySet()){
            ObjectOutputStream outstream = entry.getValue();
            outstream.writeObject(msg);
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

    while(true){
        try{
            /*Handle all kinds of message*/
            /*length == length of message (first 4 bytes)*/
            /*msgType[0] == type of message (byte 5)*/
            /*playload == rest of message (all byte after byte 5)*/
            message input = (message)in.readObject();

            length = Utilities.ByteArrayToint(input.msgLen);

            /*Read one byte from socket*/
            //in.readObject(msgType);
            
            msgType = input.msgType;

            /*If it is handshake message, then the fifth char is I*/
            byte flagForHandshake = 'I';

            /*Handle handshake message*/
            if(msgType[0] == flagForHandshake){
              byte [] header = input.header;
              byte [] peerId = input.peerId;
          	  handleHandshakeMessage(header, peerId);    
            }
            /*Handle Actual message*/
            else{
                /*choke = 0*/
                /*unchoke = 1*/
                /*interested = 2*
                /*notInterested = 3*/
                /*have = 4*/
                /*bitfield = 5*/
                /*request = 6*/
                /*piece = 7*/
                if(msgType[0] == 0){
                  handleChokeMessage();
                }
                else if(msgType[0] == 1){
                  handleUnchokeMessage();
                }
                else if(msgType[0] == 2){
                  handleInterestedMessage();
                }
                else if(msgType[0] == 3){
                  handleNotInterestedMessage();    
                }
                else if(msgType[0] == 4){
                  byte [] payload = input.payload;
                  handleHaveMessage(payload);
                }
                else if(msgType[0] == 5){
                                    byte [] payload = input.payload;
                  handleBitfieldMessage(payload);
                }
                else if(msgType[0] == 6){
                                    byte [] payload = input.payload;
                  handleRequestMessage(payload);
                }
                else if(msgType[0] == 7){
                                    byte [] payload = input.payload;
                  handlePieceMessage(payload);
                }
            }
          }
          catch(Exception e){
             System.out.println(e);
          }

          int numberOfPiece = peer.numberOfPiece;
          int numOfPeerHaveCompleteFile = 0;
          for(Map.Entry<Integer,byte[]> entry : peer.bitfieldMap.entrySet()){
            if(Utilities.checkForCompelteFile(entry.getValue(), numberOfPiece))
            numOfPeerHaveCompleteFile++;
          }

          int numberOfPeer = peer.bitfieldMap.size();

          /*When everyone has complete file, stop the program*/
          if(numOfPeerHaveCompleteFile == numberOfPeer) break;

    }
  }

  public void handleHandshakeMessage(byte[] msgHeader, byte[] msgId){
    try{
        System.out.println("Peer " + peer.peerId + ": receive handshake message from " + neighborId); 
        byte [] restByte = new byte[27];

        /*Read the another 27 byte from socket*/
        //in.readObject(restByte);

        /*Handshake message is 32 bytes*/
        /*Combine first 4 bytes + 1 + 27 bytes into 32 bytes*/
        //byte[] message = Utilities.combineByteArray(lengthOfMessage, msgType);
        //message = Utilities.combineByteArray(message, restByte);      

        //byte[] msgHeader = Arrays.copyOfRange(message, 0, 18); /* copy index from 0 to 18 (not include 18)*/
        //byte[] msgHeader = 
        String msgHeaderInString = new String(msgHeader); /*convert byte array to String*/
  
        //byte[] msgId = Arrays.copyOfRange(message, 28, 32); /* copy index from 28 to 32 (not include 32)*/
        int msgIdInInt = Utilities.ByteArrayToint(msgId); /*convert byte array to int*/
                
        //System.out.println("Msg ID: " +  msgIdInInt + "  Except value is : " + neighborId);
                
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
   //     byte[] bitfieldMsgByteArray = Utilities.combineByteArray(bitfieldMsg.msgLen, bitfieldMsg.msgType);
     //   bitfieldMsgByteArray = Utilities.combineByteArray(bitfieldMsgByteArray, bitfieldMsg.payload);

        sendMessage(bitfieldMsg);
          
        System.out.println("Peer " + peer.peerId + ": Bitfield message is sent to " + neighborId);
    }catch(Exception e){
      System.out.println("Error on reciving handshake message");
    }
  }

   public void handleChokeMessage(){
    System.out.println("Peer " + peer.peerId + ": receive choke message from " + neighborId);
    peer.isChoke.put(neighborId, true);
    peer.downloadRate.put(neighborId, 0); // set the download rate from this neighbor to 0
                                                          // Because it sends me a choke message
  }

  public void handleUnchokeMessage(){
    System.out.println("Peer " + peer.peerId + ": receive unchoke message from " + neighborId);
    peer.isChoke.put(neighborId, false);

    byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId);
    byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId);
    int numberOfPiece = peer.numberOfPiece;

    boolean completeFile = Utilities.checkForCompelteFile(myBitfieldMap, numberOfPiece);

    if(completeFile){
        System.out.println("Peer" + peer.peerId + " : I have complete file");
    }
    else{
        int desiredIndex = getDesiredIndex(myBitfieldMap, neighborBitfieldMap);

        /***send the request message to neighbor***/
        message requestMsg = (new message()).request(desiredIndex); /*create a message object*/
        //byte[] requestMsgByteArray = Utilities.combineByteArray(requestMsg.msgLen, requestMsg.msgType);//conver object message to byte array
       // requestMsgByteArray = Utilities.combineByteArray(requestMsgByteArray, requestMsg.payload); //conver object message to byte array
        sendMessage(requestMsg);

        /*set requestedBitfield after send request message to advoid request same piece from different neighbor*/
        Utilities.setBitInBitfield(peer.requestedBitfield, desiredIndex);

        System.out.println("Peer:" + peer.peerId + ": send request message to " + neighborId);
    }
  }

  public int getDesiredIndex(byte [] myBitfieldMap, byte [] neighborBitfieldMap){

    /****Get random interesting piece from neighbor ****/
    int desiredIndex;
    Random rand = new Random();

    while(true){
      desiredIndex = rand.nextInt(peer.numberOfPiece); /*generate random number from 0 to (numberOfPiece-1)*/

      /*Break out the loop until find a valid index*/
      if(Utilities.isSetBitInBitfield(myBitfieldMap, desiredIndex) == false && 
        Utilities.isSetBitInBitfield(neighborBitfieldMap, desiredIndex) == true && 
        Utilities.isSetBitInBitfield(peer.requestedBitfield, desiredIndex) == false);
        break;
    }

    return desiredIndex;
  }

  public void handleInterestedMessage(){
    System.out.println("Peer " + peer.peerId + ": receive interested message from " + neighborId);

    /*If receive interested message, then change the isInterested talbe*/
    peer.isInterested.put(neighborId, true);
  }

  public void handleNotInterestedMessage(){
    System.out.println("Peer " + peer.peerId + ": receive not Interested message from " + neighborId);

    /*If receive interested message, then change the isInterested talbe*/
    peer.isInterested.put(neighborId, false);  
  }  

  public void handleHaveMessage(byte [] payload){
    try{
       /*byte [] playload = new byte[length-1];
       in.readObject(playload);*/


       System.out.println("Peer " + peer.peerId + ": receive have message from " + neighborId);
       int indexOfPiece = Utilities.ByteArrayToint(payload);
                     
       /*update the bitfield for neighbor*/
       byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId); /*get bitfield from hash table*/
       Utilities.setBitInBitfield(neighborBitfieldMap, indexOfPiece); /*update bitfield*/
       peer.bitfieldMap.put(neighborId, neighborBitfieldMap); /*Store the bitfield back to hashmap*/

       /*Check if I have that piece or not. If I do not have that piece, send interested message to neighbor*/
       byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId);
       if(Utilities.isSetBitInBitfield(myBitfieldMap, indexOfPiece) == false){
         message interestedMsg = (new message()).interested();

         /*conver object message to byte array*/
         //byte[] interestedMsgByteArray = Utilities.combineByteArray(interestedMsg.msgLen, interestedMsg.msgType);

         sendMessage(interestedMsg);
         System.out.println("Peer " + peer.peerId + ": send interested message to " + neighborId);
       }
    }catch(Exception e){
      System.out.println("Error on receiving Have message");
    }
  }

  public void handleBitfieldMessage(byte [] payload){
    try{
        System.out.println("Peer " + peer.peerId + ": receive bitfield message from " + neighborId); 
       /*byte [] playload = new byte[length-1];
       in.readObject(playload);*/


        /*receive bitfield and update the bitfield of my neighbor*/
        /*playload inside of message is bitmap of its neighbor*/
        peer.bitfieldMap.put(neighborId, payload);

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
           // byte[] interestedMsgByteArray = Utilities.combineByteArray(interestedMsg.msgLen, interestedMsg.msgType);

            sendMessage(interestedMsg);
            System.out.println("Peer " + myId + ": Interested message is send to " + neighborId);
            break;
          }
        }

        if(hasInterestingPiece == false){
          /*send an not interesting message*/
          message notInterestedMsg = (new message()).notInterested();

          /*conver object message to byte array*/
          //byte[] notInterestedMsgByteArray = Utilities.combineByteArray(notInterestedMsg.msgLen, notInterestedMsg.msgType);

          sendMessage(notInterestedMsg);
          System.out.println("Peer " + myId + ": not Interested message is send to " + neighborId);
        }
    }
    catch(Exception e){
      System.out.println("Error on receiving bitfield message");
    }
  }

  public void handleRequestMessage(byte [] payload){
    try{
        System.out.println("Peer " + peer.peerId + ": receive request message from " + neighborId);
       /*byte [] playload = new byte[length-1];
       in.readObject(playload);*/



        /*******Get piece of data from file********/
        String filename = "./peer_" + peer.peerId + "/" + (new fileInfo().FileName);
        int pieceSize = new fileInfo().PieceSize;
        int indexOfPiece = Utilities.ByteArrayToint(payload);

        int numberOfPiece = peer.numberOfPiece;
        byte [] piece = Utilities.readPieceFromFile(filename, pieceSize, indexOfPiece, numberOfPiece);

        /*set a start time before send data*/
        startDownloadTime = System.currentTimeMillis();

        /***send the piece of data to neighbor***/
        message pieceMsg = (new message()).piece(indexOfPiece, piece); /*create a message object*/
        //byte[] pieceMsgByteArray = Utilities.combineByteArray(pieceMsg.msgLen, pieceMsg.msgType);//conver object message to byte array
        //pieceMsgByteArray = Utilities.combineByteArray(pieceMsgByteArray, pieceMsg.payload); //conver object message to byte array
        sendMessage(pieceMsg);
        System.out.println("Peer " + peer.peerId + ": Piece message is send to " + neighborId);  
    }
    catch(Exception e){
      System.out.println("Error on receiving request message");
    }
  }

  public void handlePieceMessage(byte [] payload){
    try{
        System.out.println("Peer " + peer.peerId + ": receive piece message from " + neighborId);



        /*first 4 byte in playload is piece index, rest is actual piece*/ 
        byte [] indexOfPieceByteArray = Arrays.copyOfRange(payload, 0, 5);// read first 4 byte from pllayload
        byte [] piece = Arrays.copyOfRange(payload, 5, payload.length); // length - length of message type ==  length of play load 
                                                                           // because I want to read from 5 to end of playload
                                                                          // I plue 1, syntax of copyOfRange (will not read length -1 + 1)
                                                                          // It will read till playload[lengthOfPlayload -1]
                    
        int indexOfPiece = Utilities.ByteArrayToint(indexOfPieceByteArray);

        /*store piece into myfile*/
        String filename = "./peer_" + peer.peerId + "/" + (new fileInfo().FileName);
        int pieceSize = new fileInfo().PieceSize;
        int numberOfPiece = peer.numberOfPiece;
        Utilities.writePieceToFile(filename, pieceSize, indexOfPiece, piece, numberOfPiece);

        /*record the download stop time, calculate the current download rate*/
        stopDownloadTime = System.currentTimeMillis();
        int downloadRate = piece.length / (int)(stopDownloadTime - startDownloadTime);
        peer.downloadRate.put(neighborId, downloadRate);

        /*update my bitfield*/
        byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId); /*get bitfield from hash table*/
        Utilities.setBitInBitfield(myBitfieldMap, indexOfPiece); /*update bitfield*/
        peer.bitfieldMap.put(peer.peerId, myBitfieldMap); /*Store the bitfield back to hashmap*/

        /*If all piece have been download, then report I receive whole file*/

        boolean completeFile = Utilities.checkForCompelteFile(myBitfieldMap, numberOfPiece);

        if(completeFile){
          System.out.println("Peer" + peer.peerId + " : I have complete file");
        }       

        /*send a have message to all my neighbor*/
        message haveMsg = (new message()).have(indexOfPiece); /*create a message object*/
        //byte[] haveMsgByteArray = Utilities.combineByteArray(haveMsg.msgLen, haveMsg.msgType);//conver object message to byte array
       // haveMsgByteArray = Utilities.combineByteArray(haveMsgByteArray, haveMsg.payload); //conver object message to byte array
        sendMessageToAll(haveMsg);
     
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
            //byte[] notInterestedMsgByteArray = Utilities.combineByteArray(notInterestedMsg.msgLen, notInterestedMsg.msgType);

            sendMessage(notInterestedMsg);
            System.out.println("Peer " + peer.peerId + ": not Interested message is send to " + neighborId);
        }

        /*If I am not choked by neighbor, request more pieces, send a request message*/
        boolean isChokeByNeighbor = peer.isChoke.get(neighborId);

        if(isChokeByNeighbor == false){
            /****Get random interesting piece from neighbor ****/
            int desiredIndex = getDesiredIndex(myBitfieldMap, neighborBitfieldMap);

            /***send the request message to neighbor***/
            message requestMsg = (new message()).request(desiredIndex); /*create a message object*/
            //byte[] requestMsgByteArray = Utilities.combineByteArray(requestMsg.msgLen, requestMsg.msgType);//conver object message to byte array
            //requestMsgByteArray = Utilities.combineByteArray(requestMsgByteArray, requestMsg.payload); //conver object message to byte array
            sendMessage(requestMsg);

            /*set requestedBitfield after send request message to advoid request same piece from different neighbor*/
            Utilities.setBitInBitfield(peer.requestedBitfield, desiredIndex);
            System.out.println("Peer:" + peer.peerId + ": send request message to " + neighborId);
         }

    }catch(Exception e){
        System.out.println("Error on receiving Piece message");    
    }
  }
}
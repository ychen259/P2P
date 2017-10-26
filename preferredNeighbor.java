import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.Map.Entry;

public class preferredNeighbor implements Runnable {
   peerProcess peer;

   Map<Integer,DataOutputStream> allOutStream = new HashMap<Integer, DataOutputStream>(); /*store all output stream*/
                                                                                              /*I need this to send have message to all neighbors*/
                                                                                              /*Integer: neighbor peer ID*/
                                                                                              /*ObjectOutputStream: their output stream*/

  int NumberOfPreferredNeighbors;


   public preferredNeighbor(peerProcess peer, Map<Integer,DataOutputStream> allOutStream){
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

  public int getDesiredIndex(byte [] myBitfieldMap, byte [] neighborBitfieldMap){

    /****Get random interesting piece from neighbor ****/
    int desiredIndex;
    Random rand = new Random();
    boolean hasDesiredIndex = Arrays.equals(myBitfieldMap, neighborBitfieldMap);

    if(hasDesiredIndex) return -1;

    while(true){
      desiredIndex = rand.nextInt(peer.numberOfPiece); /*generate random number from 0 to (numberOfPiece-1)*/

      /*Break out the loop until find a valid index*/
      if(Utilities.isSetBitInBitfield(myBitfieldMap, desiredIndex) == false && 
        Utilities.isSetBitInBitfield(neighborBitfieldMap, desiredIndex) == true && 
        Utilities.isSetBitInBitfield(peer.requestedBitfield, desiredIndex) == false){
            Utilities.setBitInBitfield(peer.requestedBitfield, desiredIndex);
                  break;
      }
    }

    return desiredIndex;
  }

    public void run(){
        /*start after half second*/
        Utilities.threadSleep(500);

        /*sort the download map*/
        /*sort the download rate from high to low*/
        List<Entry<Integer, Integer>> list = new LinkedList<>(peer.downloadRate.entrySet());
        Collections.sort(list, new Comparator<Object>() {
          @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return -((Comparable<Integer>) ((Map.Entry<Integer, Integer>) (o1)).getValue()).compareTo(((Map.Entry<Integer, Integer>) (o2)).getValue());
            }
        });

        Map<Integer, Integer> sorted_downloadRate = new LinkedHashMap<>();
        for (Iterator<Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) it.next();
            sorted_downloadRate.put(entry.getKey(), entry.getValue());
        }
        /*sort the download map end here*/

        int size;
        if(sorted_downloadRate.size() > NumberOfPreferredNeighbors){
          size = NumberOfPreferredNeighbors;
          /*send unchoke message to neighbor with fast download rate*/
          for(int i = 0; i < size; i++){
              int neighborId = (int)(sorted_downloadRate.keySet().toArray()[i]);
                boolean neighborIsChoke = peer.isChoke.get(neighborId);
                DataOutputStream out = allOutStream.get(neighborId);

                /*If neighbor is unchoke already, we do not have to send unchoke message*/
                /*We just need to send request message*/
                if(neighborIsChoke == false){
                    byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId);
                    byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId);
                    int numberOfPiece = peer.numberOfPiece;

                    boolean completeFile = Utilities.checkForCompelteFile(myBitfieldMap, numberOfPiece);

                    if(completeFile){
                      System.out.println("Peer" + peer.peerId + " : I have complete file");
                    }
                    else{
                        /****Get random interesting piece from neighbor ****/
                        int desiredIndex = getDesiredIndex(myBitfieldMap, neighborBitfieldMap);

                      /***send the request message to neighbor***/
                        message requestMsg = (new message()).request(desiredIndex); /*create a message object*/
                       //byte[] requestMsgByteArray = Utilities.combineByteArray(requestMsg.msgLen, requestMsg.msgType);//conver object message to byte array
                       // requestMsgByteArray = Utilities.combineByteArray(requestMsgByteArray, requestMsg.payload); //conver object message to byte array
                        sendMessage(out, requestMsg.message);
//Utilities.threadSleep(10);
                        System.out.println("Peer:" + peer.peerId + ": send request message to " + neighborId);
                        continue;
                    }
                } 


                message unchokeMsg = (new message()).unchoke();

                /*conver object message to byte array*/
                //byte[] unchokeMsgByteArray = Utilities.combineByteArray(unchokeMsg.msgLen, unchokeMsg.msgType);

                /*send a unchoke message*/
                sendMessage(out, unchokeMsg.message);
                //System.out.println("I am here : "  + i );
                System.out.println("Peer " + peer.peerId + ": unchoke message send to " + neighborId);
            }

            /*send choke message to neighbor with slow download rate*/
            for(int i = size; i < sorted_downloadRate.size(); i++){
              int neighborId = (int)(sorted_downloadRate.keySet().toArray()[i]);

                DataOutputStream out = allOutStream.get(neighborId);

                message chokeMsg = (new message()).choke();

                /*conver object message to byte array*/
                //byte[] chokeMsgByteArray = Utilities.combineByteArray(chokeMsg.msgLen, chokeMsg.msgType);

                /*send a unchoke message*/
                sendMessage(out, chokeMsg.message);
                // System.out.println("I am here : "  + i );
                System.out.println("Peer " + peer.peerId + ": choke message send to " + neighborId);
            }


        }
        /*If number of preferred neighbor is greater than number of neighbor*/
        /*all neighbor are optimistic neighbor*/
        else{

          size = sorted_downloadRate.size();

          /*send unchoke message to neighbor with fast download rate*/
          for(int i = 0; i < size; i++){
              int neighborId = (int)(sorted_downloadRate.keySet().toArray()[i]);

                boolean neighborIsChoke = peer.isChoke.get(neighborId);
                DataOutputStream out = allOutStream.get(neighborId);

                /*If neighbor is unchoke already, we do not have to send unchoke message*/
                /*send a request message*/
                if(neighborIsChoke == false){
                    byte [] neighborBitfieldMap = peer.bitfieldMap.get(neighborId);
                    byte [] myBitfieldMap = peer.bitfieldMap.get(peer.peerId);
                    int numberOfPiece = peer.numberOfPiece;

                    boolean completeFile = Utilities.checkForCompelteFile(myBitfieldMap, numberOfPiece);

                    if(completeFile){
                      System.out.println("Peer" + peer.peerId + " : I have complete file");
                    }
                    else{
                        /****Get random interesting piece from neighbor ****/
                        int desiredIndex = getDesiredIndex(myBitfieldMap, neighborBitfieldMap);
                        if (desiredIndex == -1) continue;

                        /***send the request message to neighbor***/
                        message requestMsg = (new message()).request(desiredIndex); /*create a message object*/
                       //byte[] requestMsgByteArray = Utilities.combineByteArray(requestMsg.msgLen, requestMsg.msgType);//conver object message to byte array
                       // requestMsgByteArray = Utilities.combineByteArray(requestMsgByteArray, requestMsg.payload); //conver object message to byte array
                        sendMessage(out, requestMsg.message);
//Utilities.threadSleep(10);
                        System.out.println("Peer:" + peer.peerId + ": send request message to " + neighborId);
                        continue;
                    }
                  }

                message unchokeMsg = (new message()).unchoke();

                /*conver object message to byte array*/
                //byte[] unchokeMsgByteArray = Utilities.combineByteArray(unchokeMsg.msgLen, unchokeMsg.msgType);

                /*send a unchoke message*/
                sendMessage(out, unchokeMsg.message);
                System.out.println("Peer " + peer.peerId + ": unchoke message send to " + neighborId);
          }  

        }

    }
}
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.Map.Entry;

public class preferredNeighbor implements Runnable {
   peerProcess peer;

   Map<Integer, DataOutputStream> allOutStream = new HashMap<Integer, DataOutputStream>(); /*store all output stream*/
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
        Utilities.threadSleep(1000);

        /*sort the download map*/
        /*sort the download rate from high to low*/
        List<Entry<Integer, Double>> list = new LinkedList<>(peer.downloadRate.entrySet());
        Collections.sort(list, new Comparator<Object>() {
          @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return -((Comparable<Double>) ((Map.Entry<Integer, Double>) (o1)).getValue()).compareTo(((Map.Entry<Integer, Double>) (o2)).getValue());
            }
        });

        Map<Integer, Double> sorted_downloadRate = new LinkedHashMap<>();
        for (Iterator<Entry<Integer, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) it.next();
            sorted_downloadRate.put(entry.getKey(), entry.getValue());
        }
        /*sort the download map end here*/

        int size;
        if(sorted_downloadRate.size() > NumberOfPreferredNeighbors){
          int numberOfNeighborIsPick = 0;
          /*send unchoke message to neighbor who is interested in me and has fast download rate*/
          for(int i = 0; i < sorted_downloadRate.size(); i++){
                int neighborId = (int)(sorted_downloadRate.keySet().toArray()[i]);
                boolean neighborIsChoke = peer.neighborIChoke.get(neighborId);
                boolean neighborIsInterested = peer.isInterested.get(neighborId);
                DataOutputStream out = allOutStream.get(neighborId);
                if(neighborIsInterested == false){

                    message chokeMsg = (new message()).choke();

                    /*send a unchoke message*/
                    sendMessage(out, chokeMsg.message);

                    System.out.println("Peer " + peer.peerId + ": choke message send to " + neighborId);

                    continue;
                }

                numberOfNeighborIsPick++;
                if(numberOfNeighborIsPick > NumberOfPreferredNeighbors){
                    message chokeMsg = (new message()).choke();

                    /*send a unchoke message*/
                    sendMessage(out, chokeMsg.message);

                    System.out.println("Peer " + peer.peerId + ": choke message send to " + neighborId);

                }
                else{
                  /*If neighbor is unchoke already, we do not have to send unchoke message*/
                  /*We just need to send request message*/
                  if(neighborIsChoke == false){
                      continue;
                  } 


                  message unchokeMsg = (new message()).unchoke();

                  /*send a unchoke message*/
                  sendMessage(out, unchokeMsg.message);

                  System.out.println("Peer " + peer.peerId + ": unchoke message send to " + neighborId);
                }
          }

        }
        /*If number of preferred neighbor is greater than number of neighbor*/
        /*all neighbor are optimistic neighbor*/
        else{

          size = sorted_downloadRate.size();

          /*send unchoke message to neighbor with fast download rate*/
          for(int i = 0; i < size; i++){
              int neighborId = (int)(sorted_downloadRate.keySet().toArray()[i]);

                boolean neighborIsChoke = peer.neighborIChoke.get(neighborId);
                boolean neighborIsInterested = peer.isInterested.get(neighborId);

                if(neighborIsInterested == false) continue;

                /*If neighbor is unchoke already, we do not have to send unchoke message*/
                /*send a request message*/
                if(neighborIsChoke == false){
                    continue;
                }


                DataOutputStream out = allOutStream.get(neighborId);

                message unchokeMsg = (new message()).unchoke();

                /*conver object message to byte array*/
                //byte[] unchokeMsgByteArray = Utilities.combineByteArray(unchokeMsg.msgLen, unchokeMsg.msgType);

                /*send a unchoke message*/
                sendMessage(out, unchokeMsg.message);

                /*I did not choke neighbor*/
                peer.neighborIChoke.put(neighborId, false);

                System.out.println("Peer " + peer.peerId + ": unchoke message send to " + neighborId);
          }  

        }

    }
}
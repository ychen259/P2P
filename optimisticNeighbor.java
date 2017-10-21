import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.Map.Entry;

public class optimisticNeighbor implements Runnable {
   peerProcess peer;

   Map<Integer,ObjectOutputStream> allOutStream = new HashMap<Integer, ObjectOutputStream>(); /*store all output stream*/
                                                                                              /*I need this to send have message to all neighbors*/
                                                                                              /*Integer: neighbor peer ID*/
                                                                                              /*ObjectOutputStream: their output stream*/

  int NumberOfPreferredNeighbors;


   public optimisticNeighbor(peerProcess peer, Map<Integer,ObjectOutputStream> allOutStream){
     this.peer = peer;
     this.allOutStream = allOutStream;

     fileInfo info = new fileInfo();
     NumberOfPreferredNeighbors = info.NumberOfPreferredNeighbors;
   }

	public void sendMessage(ObjectOutputStream out, byte[] msg){
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

                /*If neighbor is unchoke already, we do not have to send unchoke message*/
                if(neighborIsChoke == false) continue;

          	    ObjectOutputStream out = allOutStream.get(neighborId);

          	    message unchokeMsg = (new message()).unchoke();

          	    /*conver object message to byte array*/
			    byte[] unchokeMsgByteArray = Utilities.combineByteArray(unchokeMsg.msgLen, unchokeMsg.msgType);

          	    /*send a unchoke message*/
		 	    sendMessage(out, unchokeMsgByteArray);
		 	    //System.out.println("I am here : "  + i );
		 	    System.out.println("Peer " + peer.peerId + ": unchoke message send to " + neighborId);
       	    }

       	    /*send choke message to neighbor with slow download rate*/
       	    for(int i = size; i < sorted_downloadRate.size(); i++){
         	    int neighborId = (int)(sorted_downloadRate.keySet().toArray()[i]);

          	    ObjectOutputStream out = allOutStream.get(neighborId);

          	    message chokeMsg = (new message()).choke();

          	    /*conver object message to byte array*/
			    byte[] chokeMsgByteArray = Utilities.combineByteArray(chokeMsg.msgLen, chokeMsg.msgType);

          	    /*send a unchoke message*/
		 	    sendMessage(out, chokeMsgByteArray);
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

                /*If neighbor is unchoke already, we do not have to send unchoke message*/
                if(neighborIsChoke == false) continue;

          	    ObjectOutputStream out = allOutStream.get(neighborId);

          	    message unchokeMsg = (new message()).unchoke();

          	    /*conver object message to byte array*/
			    byte[] unchokeMsgByteArray = Utilities.combineByteArray(unchokeMsg.msgLen, unchokeMsg.msgType);

          	    /*send a unchoke message*/
		 	    sendMessage(out, unchokeMsgByteArray);
		 	    System.out.println("Peer " + peer.peerId + ": unchoke message send to " + neighborId);
         	}  

        }

    }
}

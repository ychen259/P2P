/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to aegins remote peer processes. 
 * It reads configuration filedapt this program to your running environment.
 */

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
/*
 * The StartRemotePeers class b PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class peerProcess {
    /*My own Info*/
    int peerId; //my peerID
    int port;  //my port number
    int numberOfPiece;
    Map<Integer,Boolean> isInterested = new HashMap<Integer, Boolean>(); //the neighbor I am interested in
                                                                        // Integer: peerID || Boolean: true(interested)

    /*neighbor Info*/
    List<RemotePeerInfo> NeighborPeerInfo = new ArrayList<RemotePeerInfo>();  //store all its neighbor info
                                                                              //RemotePeerInfo has variable
                                                                              //peerID, peerAddress, peerPort, haveFile

    Map<Integer,Boolean> isChoke = new HashMap<Integer, Boolean>();  //the neighbor send me choke message
                                                                     // Integer: peerID || Boolean: true(choke)    

    Map<Integer, Integer> downloadRate = new HashMap<Integer, Integer>(); //My downloading rate for neighbors

    Map<Integer, Socket> neighborSocket = new HashMap<Integer, Socket>();//neighbor's socket
    																	 //Integer: peerID || Socket: their socket

    /*Everyone's bitfield (including myself)*/
    Map<Integer,byte[]> bitfieldMap = new HashMap<Integer, byte[]>(); // Everyone's bitfield
                                                                      //Integer：peerID || byte[]: bitfield


    public peerProcess(int peerId){
    	this.peerId = peerId;

    	fileInfo fileInfo = new fileInfo();

        /*Calculate the number of piece*/
    	if((fileInfo.FileSize % fileInfo.PieceSize ) != 0 ) 
    		numberOfPiece = (fileInfo.FileSize / fileInfo.PieceSize) + 1;  
    	else
    		numberOfPiece = fileInfo.FileSize / fileInfo.PieceSize;

        /*fileInfo has all peer Info array*/
    	List<RemotePeerInfo> peerInfoArray = fileInfo.peerInfoArray;

        /*store all my neighbor Info into isChoke and isInterested except myself*/
        /*the default vallue for isChoke is true*/
        /*the default value for isInterested is false*/
        /*default value for downloadRate is 0*/

    	for(int i = 0; i < peerInfoArray.size(); i++){
    		int id = Integer.parseInt(peerInfoArray.get(i).peerId);
    		if(this.peerId == id){ 
    			port = Integer.parseInt(peerInfoArray.get(i).peerPort);
    			continue;
    		}
    		else{
    			NeighborPeerInfo.add(peerInfoArray.get(i));  /*add new value into NeighborPeerInfo*/
    			isChoke.put(id, true); /*add new value into isChoke*/
    			isInterested.put(id, false); /*add new value into isInterested*/
    			downloadRate.put(id, 0); /*set initial download rate to 0*/
    		}
    	}

        /*convert number of bit to number of Byte to store the bitfield*/
        int sizeOfbitfield = ((numberOfPiece%8) == 0? numberOfPiece/8: numberOfPiece/8 + 1);

        /*set the bitfield to all one if the peer has complete file*/
        /*set the bitfield to all zero if the peer has not file*/
    	for(int i = 0; i < peerInfoArray.size(); i++){
          int id = Integer.parseInt(peerInfoArray.get(i).peerId);
          if(peerInfoArray.get(i).haveFile == true){
          	byte [] full = new byte[sizeOfbitfield];
            /*set bitfield to all one*/
          	for(int j = 0; j < numberOfPiece; j++){
                Utilities.setBitInBitfield(full, j); 
          	}

            bitfieldMap.put(id, full);
          }  
          else{
            byte [] empty = new byte[sizeOfbitfield];
            for(int j = 0; j < sizeOfbitfield; j++){
                empty[j] = (byte)0x00; /*full[j] = 0000 0000*/
            }

            bitfieldMap.put(id, empty);
          }
    	}

    }

    /*build up socket between peers
    Ex. peer 1000 need to build socket with all its neighbor socket*/
    public void buildSocket()  {
      try{
        ServerSocket serverSocket = new ServerSocket(port);

        for(int i = 0; i < NeighborPeerInfo.size(); i++){
        	int neighborId = Integer.parseInt(NeighborPeerInfo.get(i).peerId);
      	  String neighborHost = NeighborPeerInfo.get(i).peerAddress;
      	  int neighborPort = Integer.parseInt(NeighborPeerInfo.get(i).peerPort);
   
          /*the peer process with peer ID 1003 in the above example should 
            make TCP connections to the peer processes with peer ID 1001 and peer ID 1002. In 
            the same way, the peer process with peer ID 1004 should make TCP connections to the 
            peer processes with peer ID 1001, 1002, and 1003.*/

          /*If my Id greater than neighbor ID, then I send TCP connection to them;*/
          /*else I will wait to them to send a TCP connection to me*/
          if(peerId > neighborId){
        	  try{
                 Socket socket = new Socket(neighborHost, neighborPort);
                 neighborSocket.put(neighborId, socket);
                 //System.out.println(socket);
                 System.out.println("peer ID: " + peerId + " send TCP request to peerId: " + neighborId);
            }
            catch(Exception e){
            	System.out.println(e);
            }
          }
          else{
        	  try{
                Socket socket = serverSocket.accept();
                neighborSocket.put(neighborId, socket);
                //System.out.println(socket);
                System.out.println("peer ID: " + peerId + " listen to " + port);
              }
            catch(Exception e){
            	System.out.println(e);
            }            
          }  
        }
      }
      catch(Exception e){
        System.out.println(e);
      }
      
    }

    /*test My constructor*/
	public static void main(String[] args) throws IOException{
		/**************************Test Code**********************************************************************/
    /*
		int id = Integer.parseInt(args[0]);
        peerProcess test = new peerProcess(id);
       // test.buildSocket();

        int size = test.NeighborPeerInfo.size();

        System.out.println("\nNeighbor info");
        for(int i=0; i<size; i++){
         System.out.println(test.NeighborPeerInfo.get(i).peerId + " " + 
            test.NeighborPeerInfo.get(i).peerAddress + " " + 
            test.NeighborPeerInfo.get(i).peerPort + " " + 
            test.NeighborPeerInfo.get(i).haveFile);
        }

        System.out.println("\nisChoke info");
        for(Map.Entry<Integer,Boolean> entry : test.isChoke.entrySet()){
          System.out.println("My Id: " + test.peerId + "  is Choked for " + entry.getKey() + ": " + entry.getValue());

        }

        System.out.println("\nisInterested info");
        for(Map.Entry<Integer,Boolean> entry : test.isInterested.entrySet()){
          System.out.println("My Id: " + test.peerId + "  is Interested in " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nDownload info");
        for(Map.Entry<Integer,Integer> entry : test.downloadRate.entrySet()){
          System.out.println("My Id: " + test.peerId + "  download Rate from peer " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nneighborSocket info");
        for(Map.Entry<Integer,Socket> entry : test.neighborSocket.entrySet()){
          System.out.println("My Id: " + test.peerId + "  neighbor Socket from peer " + entry.getKey() + ": " + entry.getValue());
        }
        

        int numberOfPiece = test.numberOfPiece;
        int peerId = test.peerId;
        int port = test.port;
        int sizeOfBitfield = ((test.numberOfPiece % 8) == 0? numberOfPiece/8: numberOfPiece/8 + 1 );
        
        System.out.println("\nnumber of piece = " + numberOfPiece);
        System.out.println("peer ID = " + peerId);
        System.out.println("port = " + port);
        System.out.println("\nBitfield info");
        for(Map.Entry<Integer,byte[]> entry : test.bitfieldMap.entrySet()){
          System.out.print("Bitfield for " + entry.getKey() + ": ");
          for(int i = 0; i < sizeOfBitfield; i++){
            System.out.print(entry.getValue()[i] + " ");
          }
          System.out.println("\n");
        }

        byte[] value = test.bitfieldMap.get(1000);
        for(int i = 0; i < numberOfPiece; i++){
          boolean result = Utilities.isSetBitInBitfield(value, i);
          System.out.println("piece " + i + " is set: " + result);
        }
      */

     Thread peer1000 = new Thread(new Handler(1000));
     Thread peer1001 = new Thread(new Handler(1001));
     peer1000.start();
     peer1001.start();

		/**************************Test Code End Here**********************************************************************/

    }
}

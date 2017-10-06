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
/*
 * The StartRemotePeers class b PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class peerProcess extends Thread {
    /*My own Info*/
    int peerId; //my peerID
    int numberOfPiece;
    Map<Integer,Boolean> isInterested = new HashMap<Integer, Boolean>(); //the neighbor I am interested in
                                                                        // Integer: peerID || Boolean: true(interested)

    /*neighbor Info*/
    List<RemotePeerInfo> NeighborPeerInfo = new ArrayList<RemotePeerInfo>();  //store all its neighbor info
                                                                              //RemotePeerInfo has variable
                                                                              //peerID, peerAddress, peerPort, haveFile

    Map<Integer,Boolean> isChoke = new HashMap<Integer, Boolean>();  //the neighbor send me choke message
                                                                     // Integer: peerID || Boolean: true(choke)    

    /*Everyone's bitfield*/
    Map<Integer,byte[]> bitfieldArray = new HashMap<Integer, byte[]>(); // neighbor's bitfield
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
    	for(int i = 0; i < peerInfoArray.size(); i++){
    		int id = Integer.parseInt(peerInfoArray.get(i).peerId);
    		if(this.peerId == id) continue;
    		else{
    			NeighborPeerInfo.add(peerInfoArray.get(i));  /*add new value into NeighborPeerInfo*/

    			isChoke.put(id, true); /*add new value into isChoke*/
    			isInterested.put(id, false); /*add new value into isInterested*/
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

            bitfieldArray.put(id, full);
          }  
          else{
            byte [] empty = new byte[sizeOfbitfield];
            for(int j = 0; j < sizeOfbitfield; j++){
                empty[j] = (byte)0x00; /*full[j] = 0000 0000*/
            }

            bitfieldArray.put(id, empty);
          }
    	}
    }

    /*test My constructor*/
	public static void main(String[] args){
        peerProcess test = new peerProcess(1002);
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
          System.out.println(test.peerId + "  isChoke for " + entry.getKey() + ": " + entry.getValue());

        }

        System.out.println("\nisInterested info");
        for(Map.Entry<Integer,Boolean> entry : test.isInterested.entrySet()){
          System.out.println(test.peerId + "  isInterested for " + entry.getKey() + ": " + entry.getValue());
        }

        int numberOfPiece = test.numberOfPiece;
        int peerId = test.peerId;
        int sizeOfBitfield = ((test.numberOfPiece % 8) == 0? numberOfPiece/8: numberOfPiece/8 + 1 );

        System.out.println("\nnumber of piece = " + numberOfPiece);
        System.out.println("peer ID = " + peerId);
        System.out.println("\nBitfield info");
        for(Map.Entry<Integer,byte[]> entry : test.bitfieldArray.entrySet()){
          System.out.print("Bitfield for " + entry.getKey() + ": ");
          for(int i = 0; i < sizeOfBitfield; i++){
            System.out.print(entry.getValue()[i] + " ");
          }
          System.out.println();
        }

        byte[] value = test.bitfieldArray.get(1000);
        for(int i = 0; i < numberOfPiece; i++){
          boolean result = Utilities.isSetBitInBitfield(value, i);
          System.out.println("piece " + i + " is set: " + result);
        }
    }
}

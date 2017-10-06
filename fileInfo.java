/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.io.*;
import java.util.*;
import java.util.logging.*;
/*
 * The StartRemotePeers class begins remote peer processes. 
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class fileInfo {
/*Common.cfg*/
  int NumberOfPreferredNeighbors;
  int UnchokingInterval;
  int OptimisticUnchokingInterval;
  String FileName;
  int FileSize;
  int PieceSize;

/*PeerInfo.cfg*/ 
   List<RemotePeerInfo> peerInfoArray = new ArrayList<RemotePeerInfo>();

  public fileInfo(){
  	BufferedReader reader;
  	String line;
  	File Common = new File("Common.cfg");
  	File PeerInfo = new File("PeerInfo.cfg");

  	try{
      reader = new BufferedReader(new FileReader(Common));
  
      while ((line = reader.readLine()) != null){
        String[] result = line.split("\\s+");
        if(result[0].equals("NumberOfPreferredNeighbors"))
        	NumberOfPreferredNeighbors = Integer.parseInt(result[1]);
        if(result[0].equals("UnchokingInterval")) 
        	UnchokingInterval = Integer.parseInt(result[1]);
        if(result[0].equals("OptimisticUnchokingInterval")) 
        	OptimisticUnchokingInterval = Integer.parseInt(result[1]);
        if(result[0].equals("FileName"))
        	FileName = result[1];
        if(result[0].equals("FileSize")) 
        	FileSize = Integer.parseInt(result[1]);
        if(result[0].equals("PieceSize")) 
        	PieceSize = Integer.parseInt(result[1]);
      }
    }catch(Exception e){
    	System.out.println("Cannot load Common.cfg file");
    }

    try{
      reader = new BufferedReader(new FileReader(PeerInfo));
      while ((line = reader.readLine()) != null){ 
        String[] result = line.split("\\s+");
        RemotePeerInfo temp = new RemotePeerInfo(result[0], result[1], result[2]);
        if(result[3].equals("1")) temp.haveFile = true;

        peerInfoArray.add(temp);
      }
    }catch(Exception e){
    	System.out.println("Cannot load PeerInfo.cfg file");
    }
  }


    /*test the constructor*/
    public static void main(String[] args){ 

      fileInfo test = new fileInfo();

      System.out.println("\nCommon.cfg info");
      System.out.println("NumberOfPreferredNeighbors  " + test.NumberOfPreferredNeighbors);
      System.out.println("UnchokingInterval  " + test.UnchokingInterval);
      System.out.println("OptimisticUnchokingInterval  " + test.OptimisticUnchokingInterval);
      System.out.println("FileName  " + test.FileName);
      System.out.println("FileSize  " + test.FileSize);
      System.out.println("PieceSize  " + test.PieceSize);

      System.out.println("\nPeerInfo.cfg info");
      for (int i = 0; i < test.peerInfoArray.size(); i++) {
        System.out.println(test.peerInfoArray.get(i).peerId + " " + test.peerInfoArray.get(i).peerAddress + " " + test.peerInfoArray.get(i).peerPort + " " + test.peerInfoArray.get(i).haveFile);
      }
    }


}	

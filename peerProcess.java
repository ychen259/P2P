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
public class peerProcess {


	public static void main(String[] args){
		try{
		Logger logger = Logger.getLogger("peerProcess");
		File file = new File("test" + args[0] + ".txt");
		FileHandler fileHandler = new FileHandler(file.getName());
		fileHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(fileHandler);

	
              logger.warning("invalid " + args[0]);
           
        }
        catch(Exception e){
        	System.out.println(e);
        }

	 // String id = args[0];
     // System.out.println("I am heresssssssssssssssss" + id);
	}

}

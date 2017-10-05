import java.io.*;
import java.util.*;

public class handshake {  
  byte[] header = new byte[18];
  byte [] zeroBits = new byte[10];
  byte[] peerId = new byte[4];  
 
  public handshake(int peerId){
    this.header = "P2PFILESHARINGPROJ".getBytes();
    this.zeroBits = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    this.peerId = Utilities.intToByteArray(peerId);
  }
}
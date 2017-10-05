import java.io.*;
import java.util.*;

public class message {  
  byte[] msgLen = new byte[4];
  byte msgType;
  byte[] payload; 

  static byte choke = 0;
  static byte unchoke = 1;
  static byte interested = 2;
  static byte notInterested = 3;
  static byte have = 4;
  static byte bitfield = 5;
  static byte request = 6;
  static byte piece = 7;

/*
*    Msg Length	|	Msg Type	|	Payload		 
*  	(4 bytes)	|	(1 byte)	|	(n bytes)			
*/

  /*Msg == 1; type = choke; payload = null*/
  static public message choke(){
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1);  /*only Msg Type and without payload*/
    result.msgType = choke;
    return result;
  }

  /*Msg == 1; type = unchoke; payload = null*/
  static public message unchoke(){
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1);  /*only Msg Type and without payload*/
    result.msgType = unchoke;

    return result;
  }

  /*Msg == 1; type = interested; payload = null*/
  static public message interested(){
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1);  /*only Msg Type and without payload*/
    result.msgType = interested;

    return result;
  }

  /*Msg == 1; type = notInterested; payload = null*/
  static public message notInterested(){
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1);  /*only Msg Type and without payload*/
    result.msgType = notInterested;

    return result;
  }

  /*Msg == 5; type = notInterested; payload = pieceIndex (4 bytes)*/
  static public message have(int pieceIndex){
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1+4);  /*Msg type(1) + pieceIndex(4)*/
    result.msgType = have;
    result.payload = Utilities.intToByteArray(pieceIndex);
    return result;
  }

  /*Msg = 1+n ; type = bitfield; payload = bitfields (# of pieces == (file size / piece size) (n bytes))*/
  static public message bitfield(byte[] bitfields){
  	int lenOfbitfields = bitfields.length;
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1 + lenOfbitfields);  /*Msg type(1) + size of bitfields*/
    result.msgType = bitfield;
    result.payload = bitfields;
    return result;
  }

  /*Msg = 5 ; type = request; payload = pieceIndex (4 bytes)*/
  static public message request(int pieceIndex){
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1+4);  /*Msg type(1) + pieceIndex(4)*/
    result.msgType = request;
    result.payload = Utilities.intToByteArray(pieceIndex);
    return result;
  }

  /*Msg = 1+ 4 + n; type = piece; payload = pieceIndex (4 bytes) + pieces(size of pieces)(n bytes)*/
  static public message piece(int pieceIndex, byte [] pieces){
  	int lengthOfpieces = pieces.length;
  	message result = new message();
    result.msgLen = Utilities.intToByteArray(1+4+lengthOfpieces);  /*Msg type(1) + pieceIndex(4)*/
    result.msgType = piece;

    byte[] index = new byte[4];
    index = Utilities.intToByteArray(pieceIndex);
    
    /*I need to combine pieceIndex and context of pieces and store it into payload*/
    byte[] combined = new byte[index.length + pieces.length];
    System.arraycopy(index, 0, combined,0, index.length);
    System.arraycopy(pieces,0, combined, index.length, pieces.length);

    result.payload = combined;
    return result;
  }

      /* Test all my function*/
      public static void main(String[] args) {  
        int pieceIndex = 15;
        byte[] pieces = new byte[]{1, 2, 3, 4, 5};
        byte[] bitfields = new byte[]{0,1,0,1,1,0,1,1,0,1,1};

        message choke = new message().choke();   
        System.out.println("choke Msg length:   " + Utilities.ByteArrayToint(choke.msgLen));
        System.out.println("choke Msg type:   " + choke.msgType);
        Utilities.printByteArray(choke.payload); 


        message unchoke = new message().unchoke();        
        System.out.println("\nunchoke Msg length:   " + Utilities.ByteArrayToint(unchoke.msgLen));
        System.out.println("unchoke Msg type:   " + unchoke.msgType);
        Utilities.printByteArray(unchoke.payload);   

        message interested = new message().interested();    
        System.out.println("\ninterested Msg length:   " + Utilities.ByteArrayToint(interested.msgLen));
        System.out.println("interested Msg type:   " + interested.msgType);
        Utilities.printByteArray(interested.payload);    

        message notInterested = new message().notInterested();       
        System.out.println("\nnotInterested Msg length:   " + Utilities.ByteArrayToint(notInterested.msgLen));
        System.out.println("notInterested Msg type:   " + notInterested.msgType);
        Utilities.printByteArray(notInterested.payload); 

        message have = new message().have(pieceIndex);
        System.out.println("\nhave Msg length:   " + Utilities.ByteArrayToint(have.msgLen));
        System.out.println("have Msg type:   " + have.msgType);
        Utilities.printByteArray(have.payload); 

        message bitfield = new message().bitfield(bitfields);       
        System.out.println("\nbitfield Msg length:   " + Utilities.ByteArrayToint(bitfield.msgLen));
        System.out.println("bitfield Msg type:   " + bitfield.msgType);
        Utilities.printByteArray(bitfield.payload);  

        message request = new message().request(pieceIndex); 
        System.out.println("\nrequest Msg length:   " + Utilities.ByteArrayToint(request.msgLen));
        System.out.println("request Msg type:   " + request.msgType);
        Utilities.printByteArray(request.payload); 

        message piece = new message().piece(pieceIndex, pieces);
        System.out.println("\npiece Msg length:   " + Utilities.ByteArrayToint(piece.msgLen));
        System.out.println("piece Msg type:   " + piece.msgType);
        Utilities.printByteArray(piece.payload); 
      }
      
}
import java.io.*;
import java.nio.*;
import java.util.*;

public class Utilities {  
  /*result[0] store left most byte of number*/
  /*For example if i = 256 = 2^8*/
  /*result[0] = 0000 0000*/
  /*result[1] = 0000 0000*/
  /*result[2] = 0000 0001*/
  /*result[3] = 0000 0000*/
  static public byte[] intToByteArray(int i){
    byte[] result = new byte[4];
    result[0] = (byte)(i >>> 24);
    result[1] = (byte)(i >>> 16);
    result[2] = (byte)(i >>> 8);
    result[3] = (byte)(i);

    return result;
  }

  /*transfer byte array into integer*/
  /*For example*/
  /*value[0] = 0000 0000*/
  /*value[1] = 0000 0000*/
  /*value[2] = 0000 0001*/
  /*value[3] = 0000 0000*/
  /*result = 256*/
  static public int ByteArrayToint(byte [] value){
  	int result = 0;
    ByteBuffer wrapped = ByteBuffer.wrap(value);
    result = wrapped.getInt();
    return result;
  }

  /*if bitfield is 0010 1001
                   1100 0101
    and pieceIndex = 10

    Output: bitfield is 0010 1001
                        1110 0101*/
  static public void setBitInBitfield(byte[] bitfield, int pieceIndex){
    int row = pieceIndex/8;
    int column = pieceIndex %8 ;
    int position = 7 - column;
    byte flag = (byte)(1 << position);
    bitfield[row] |= flag;
  }

  /*if bitfield is 0010 1001
                   1100 0101
    and pieceIndex = 10

    Output:  false (because bitfield[10] != 1)*/
  static public boolean isSetBitInBitfield(byte[] bitfield, int pieceIndex){
    boolean result;
    int row = pieceIndex/8;
    int column = pieceIndex %8 ;
    int position = 7 - column;
    byte flag = (byte)(1 << position);
    result = ((bitfield[row] & flag) != 0);
    return result;
  }

  /*combine two array together*/
  /*Ex. byte [] a = new byte[]{'a', 'b'};
        byte [] b = new byte[]{'c', 'd'}
    Output: combined = new byte[]{'a', 'b', 'c', 'd'};*/
  static public byte[] combineByteArray(byte[] a, byte[] b){
    byte[] combined = new byte[a.length + b.length];
    System.arraycopy(a, 0, combined, 0         ,a.length);
    System.arraycopy(b, 0, combined, a.length, b.length);
    return combined;
  }

  /*Read byte from (pieceSize*indexOfPiecce) to [(pieceSize*indexOfPiecce) + pieceSize] from file*/
  /*(pieceSize*indexOfPiecce) to [(pieceSize*indexOfPiecce) + pieceSize] == read a piece from file*/
  static public byte[] readPieceFromFile(String filename, int pieceSize, int indexOfPiece){

    byte [] result = new byte[pieceSize];
      
    try{
      RandomAccessFile rf = new RandomAccessFile(filename, "r");
      rf.seek(pieceSize * indexOfPiece);
      rf.read(result);

    }catch(Exception e){
        System.out.println(e);
    }
    
    return result;
  } 


  /*Write data[] into file in particular position (start from pieceSize*indexOfPiece) */
  static public void writePieceToFile(String filename, int pieceSize, int indexOfPiece, byte[] data){
    try{
      RandomAccessFile rf = new RandomAccessFile(filename, "rw");
      rf.seek(pieceSize * indexOfPiece);
      rf.write(data);

    }catch(Exception e){
      System.out.println(e);
    }
  }  


  /*For testing*/
  static public void printByteArray(byte[] value){
  	if(value == null){
  		System.out.print("Msg payload: null\n");
  		return;
  	}
    int length = value.length;
    int i=0;
    System.out.print("Msg payload:  ");
    for(i=0; i < length; i++){
      System.out.print((byte)value[i] + " ");
    }
    System.out.println();
  }
  
 

}  
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
    result[0] = (byte)(i >> 24);
    result[1] = (byte)(i >> 16);
    result[2] = (byte)(i >> 8);
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
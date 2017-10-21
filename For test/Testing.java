import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.Map.Entry;

public class Testing {
    public static void main(String[] args) {
                            int desiredIndex;
                        Random rand = new Random();

                        while(true){
                          desiredIndex = rand.nextInt(100); /*generate random number from 0 to (numberOfPiece-1)*/

                          /*Break out the loop until find a valid index*/
                          if(desiredIndex > 10 && desiredIndex < 20)
                            break;
System.out.println("I am here");
                        }
                          System.out.println("index" + desiredIndex);
                        

    }
}


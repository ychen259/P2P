import java.io.*;

public class Main {

   public static void main(String[] args) {
      try {
         RandomAccessFile raf = new RandomAccessFile("test.txt", "rw");

         raf.writeInt(123);

         // set the file pointer at 0 position
         raf.seek(0);

         // print the string
         System.out.println("data:" +raf.readInt());

         // print current length
         System.out.println(raf.length());

         // set the file length to 30
         raf.setLength(30);

         System.out.println(raf.length());
         raf.close();
      } catch (IOException ex) {
         ex.printStackTrace();
      }

   }
}
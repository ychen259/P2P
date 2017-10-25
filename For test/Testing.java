import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.util.Map.Entry;

public class Testing {
    public static void main(String[] args) {
      int [] a = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

      int [] b = Arrays.copyOfRange(a, 4, 10);

      for(int i = 0; i < 10; i++){
        System.out.print(a[i] + "  ");
      }
      System.out.println();

      for(int i = 0; i < b.length; i++){
        System.out.print(b[i] + "  ");
      }
      System.out.println();
    }
}


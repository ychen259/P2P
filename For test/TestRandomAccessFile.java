import java.io.IOException;  
import java.io.RandomAccessFile;  
  
public class TestRandomAccessFile {
    
    static public byte[] readPieceFromFile(String filename, int pieceSize, int indexOfPiecce){

      byte [] result = new byte[pieceSize];
      
      try{
        RandomAccessFile rf = new RandomAccessFile(filename, "r");
        rf.seek(pieceSize * indexOfPiecce);
        rf.read(result);

      }catch(Exception e){
        System.out.println(e);
      }
      return result;
    } 

    static public void writePieceFromFile(String filename, int pieceSize, int indexOfPiecce, byte[] data){
      try{
        RandomAccessFile rf = new RandomAccessFile(filename, "rw");
        rf.seek(pieceSize * indexOfPiecce);
        rf.write(data);

      }catch(Exception e){
        System.out.println(e);
      }
    } 

    static public void printByteArray(byte[] value){
    if(value == null){
      System.out.print("Msg payload: null\n");
      return;
    }
    int length = value.length;
    int i=0;
    System.out.print("Msg payload:  ");
    for(i=0; i < length; i++){
      System.out.print((char)value[i] + " ");
    }
    System.out.println();
  }


    public static void main(String[] args) throws IOException {  
        /*RandomAccessFile rf = new RandomAccessFile("rtest.dat", "rw");  
        for (int i = 0; i < 10; i++) {  
            
            rf.writeDouble(i * 1.414);  
        }  
        rf.close();  
        rf = new RandomAccessFile("rtest.dat", "rw");  
          
        rf.seek(5 * 8);  
          
        rf.writeDouble(47.0001);  
        rf.close();  
        rf = new RandomAccessFile("rtest.dat", "r");  
        for (int i = 0; i < 10; i++) {  
            System.out.println("Value " + i + ": " + rf.readDouble());  
        }  
        rf.close();  */
       //RandomAccessFile rf = new RandomAccessFile("test.dat", "r");
       String file = "./a/"+ "test.dat";
       byte [] result = readPieceFromFile(file,2, 0);
       printByteArray(result);
        writePieceFromFile("test1.dat", 2, 0, result);
      //writePieceFromFile(wf, 2, 1, result);
     // writePieceFromFile(wf, 2, 2, result);

    }  
}



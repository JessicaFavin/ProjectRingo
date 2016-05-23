import java.io.*;
import java.net.*;
//import org.apache.commons.io.FileUtils;

public class Client{

  public static void main(String[] args) {
    try {
      DatagramSocket dso = new DatagramSocket(5555);
      byte[] data = new byte[463];
      DatagramPacket paquet = new DatagramPacket(data,data.length);

      FileOutputStream fos = new FileOutputStream("./received/454.jpg", true);
      //FileOutputStream fos = new FileOutputStream("./received/test.txt");
      dso.receive(paquet);
      String mess = new String(paquet.getData(),0,paquet.getLength());
      while(!mess.equals("DONE")){
        //----------------writes into file-------------------------------------
        byte[] content = mess.getBytes();
        //FileUtils.writeByteArrayToFile(file, content);
        fos.write(paquet.getData());
        fos.flush();
        dso.receive(paquet);
        mess = new String(paquet.getData(),0,paquet.getLength());
      }
      fos.close();
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
      System.exit(0);
    }
  }

}

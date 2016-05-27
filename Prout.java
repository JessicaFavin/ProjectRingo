import java.net.*;
import java.util.Scanner;

public class Prout{
	public static void main(String[] args) {
		try{
	      String res = "DOWN";
	      //envoie du paquet en udp
	      DatagramSocket dso=new DatagramSocket();
	      byte[]data;
	      data = res.getBytes();
	      InetAddress addr = InetAddress.getByName("225.1.2.4");

	      DatagramPacket paquet = new DatagramPacket(data,data.length,
	      addr, 5557);
	      dso.send(paquet);
	      System.out.println("Down message sent to the ring");
	      dso.close();
	    } catch (Exception e){
	      System.out.println(e);
	      e.printStackTrace();
	    }
	}
}
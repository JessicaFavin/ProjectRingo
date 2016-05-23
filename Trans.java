import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;

public class Trans{

  public static void main(String[] args) {
    int id_trans = 0;
    int num_parts = 0;
    String[] file_parts;
    try {
      System.out.println("entity_address entity_app_port file_needed UDP_port");
      Scanner sc = new Scanner(System.in);
      String response = sc.nextLine();
      //construction du message de l'application
      String[] parts = response.split(" ", 3);
      int idm = (int) (Math.random()*99999999);
      String mess = "APPl "+idm+" TRANS### REQ "+parts[2].length()+" "+parts[2];
      System.out.println(mess);
      /*
      //connexion en UDP
      Inet4Address entity_address = (Inet4Address) InetAddress.getByName(parts[0]);;
      int entity_UDP = Integer.parseInt(parts[1]);
      DatagramSocket dso = new DatagramSocket(Integer.parseInt(parts[3]));
      byte[] data = new byte[463];
      DatagramPacket paquet = new DatagramPacket(data,data.length);

      //sends message to be diffused to the ring
      InetSocketAddress ia = new InetSocketAddress(entity_address, entity_UDP);
      paquet = new DatagramPacket(mess.getBytes(), mess.getBytes().getLength(), ia);
      dso.send(paquet);
      */
      //connexion au serveur TCP de l'entité
      Inet4Address entity_address = (Inet4Address) InetAddress.getByName(parts[0]);;
      int entity_TCP = Integer.parseInt(parts[1]);
      Socket sock = new Socket(entity_address, entity_TCP);
      BufferedReader br = new BufferedReader(
      new InputStreamReader(
      sock.getInputStream()));
      PrintWriter pw = new PrintWriter(
      new OutputStreamWriter(
      sock.getOutputStream()));
      pw.print(mess);
      pw.flush();
      //waits for answer
      response = br.readLine();
      parts = response.split("");
      //if file found
      if(parts.length==7 && parts[3].equals("ROK")){
        //receive file and save it to the received directory
        id_trans = Integer.parseInt(parts[4]);
        String filename = parts[6];
        num_parts = Integer.parseInt(parts[7]);
        for(int i=0; i<num_parts; i++){
          System.out.println("prout");
        }
      }
      //quit shit
    } catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

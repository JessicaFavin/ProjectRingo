import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;

public class Diff{

  public static void main(String[] args) {
    try {
      System.out.println("entity_address entity_TCP_port message_to_be_transferred");
      Scanner sc = new Scanner(System.in);
      String response = sc.nextLine();
      //construction du message de l'application
      String[] parts = response.split(" ", 3);
      int idm = (int) (Math.random()*99999999);
      String mess = "APPl "+idm+" DIFF### "+parts[2].length()+" "+parts[2];
      System.out.println(mess);
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
      //sends message to be diffused to the ring
      pw.println(mess);
      pw.flush();
    } catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

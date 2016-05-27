import java.net.*;
import java.io.*;
import java.util.*;
import java.util.UUID;
import java.util.Scanner;

public class Trans{

  private static String randomId(){
     String uuid = UUID.randomUUID().toString().substring(0,8);
     return uuid;
  }

  private static String formatInt(int i, int max) throws Exception{
    String res = Integer.toString(i);
    while(res.length()<max){
      res = "0"+res;
    }
    if(res.length()>max){
      throw new Exception("Int is bigger than expected");
    }

    return res;
  }


  public static void main(String[] args) {
    try {
      System.out.println("entity_address entity_app_port file_needed");
      Scanner sc = new Scanner(System.in);
      String response = sc.nextLine();
      //construction du message de l'application
      String[] parts = response.split(" ", 3);
      String idm = randomId();
      String mess = "APPL "+idm+" TRANS### REQ "+formatInt(parts[2].length(),2)+" "+parts[2];
      System.out.println(mess);

      //connexion au serveur TCP de l'entité
      Inet4Address entity_address = (Inet4Address) InetAddress.getByName(parts[0]);;
      int entity_TCP = Integer.parseInt(parts[1]);
      Socket sock = new Socket(entity_address, entity_TCP);
      PrintWriter pw = new PrintWriter(
      new OutputStreamWriter(
      sock.getOutputStream()));
      pw.print(mess);
      pw.flush();
      //quit shit
    } catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

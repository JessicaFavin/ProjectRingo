import java.net.*;
import java.io.*;
import java.util.*;
import java.util.UUID;
import java.util.Scanner;

public class Gest{

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
      System.out.println("entity_address entity_TCP_port WHOS");
      System.out.println("entity_address entity_TCP_port TEST ring_concerned");
      System.out.println("entity_address entity_TCP_port GBYE");
      System.out.println("to shut down: QUIT");
      Scanner sc = new Scanner(System.in);
      String response = "";
      while(true){
        response = sc.nextLine();
        if(response.equals("QUIT")){
          System.exit(0);
        }
        //construction du message de l'application
        String[] parts = response.split(" ", 4);
        String idm = randomId();
        String res = "";
        switch(parts[2]){
          case "WHOS":
            res = "GEST WHOS";
            break;
          case "GBYE":
            res = "GEST GBYE";
            break;
          case "TEST":
            res = "GEST TEST "+parts[3].trim();
            break;
          default:
            res = "PROBLEM";
            break;
        }
        if(!res.equals("PROBLEM")){
          //connexion au serveur TCP de l'entité
          Inet4Address entity_address = (Inet4Address) InetAddress.getByName(parts[0]);;
          int entity_TCP = Integer.parseInt(parts[1]);
          Socket sock = new Socket(entity_address, entity_TCP);
          PrintWriter pw = new PrintWriter(
          new OutputStreamWriter(
          sock.getOutputStream()));
          //sends message to be diffused to the ring
          pw.print(res);
          pw.flush();
          System.out.println(res);
          pw.close();
          sock.close();
        }
      }
      
    } catch(Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

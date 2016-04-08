import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class Entity{
  private static int next_id = 0;

  public static void main(String[] args) {
    try{//init les param de l'entity
      int id;
      int input_UDP = 4343;
      int input_TCP;
      InetAddress next_address;
      int next_input_UDP;
      Inet4Address emergency_address;
      //mulitcast sur l'adresse pour restreindre le message uniquement à la meme adresse
      int emergency_UDP = 4344;
      if(args.length!=6 && args.length!=4){
        System.out.println("java Entity port_UDP port_TCP [next_address] [next_port_UDP] emergency_address emergency_UDP");
      }
      if(args.length == 6){
        id = next_id;
        next_id++;
        input_UDP = Integer.parseInt(args[0]);
        input_TCP = Integer.parseInt(args[1]);
        next_address = InetAddress.getByName(args[2]);
        next_input_UDP = Integer.parseInt(args[3]);
        //gestion de la detection de problème plus tard
        emergency_address = (Inet4Address) InetAddress.getByName(args[4]);
        emergency_UDP = Integer.parseInt(args[5]);
      }else if(args.length == 4){
        id = next_id;
        next_id++;
        input_UDP = Integer.parseInt(args[0]);
        input_TCP = Integer.parseInt(args[1]);
        //recuperation de self address
        InetAddress self_address = InetAddress.getByName(Inet4Address.getLocalHost().getHostAddress());//127.000.000.001;
        next_address = self_address;
        next_input_UDP = input_UDP;
        //gestion de la detection de problème plus tard
        emergency_address = (Inet4Address) InetAddress.getByName(args[2]);
        emergency_UDP = Integer.parseInt(args[3]);
      } else {
        System.exit(1);
      }
      //--------------connexion en udp non bloquante------------------------------
      // a faire
      //-------------connexion en tcp non bloquante-------------------------------
      //a faire
      //-----connexion en udp au port emercy non bloquante -----------------------
      //a faire
      Selector sel=Selector.open();
      DatagramChannel udp_in=DatagramChannel.open();
      DatagramChannel udp_emergency=DatagramChannel.open();
      udp_in.configureBlocking(false);
      udp_emergency.configureBlocking(false);
      udp_in.bind(new InetSocketAddress(input_UDP));
      udp_emergency.bind(new InetSocketAddress(emergency_UDP));
      udp_in.register(sel,SelectionKey.OP_READ);
      udp_emergency.register(sel,SelectionKey.OP_READ);
      ByteBuffer buff=ByteBuffer.allocate(100);
      while(true){
        System.out.println("Waiting for messages");
        sel.select();
        Iterator<SelectionKey> it=sel.selectedKeys().iterator();
        while(it.hasNext()){
          SelectionKey sk=it.next();
          it.remove();
          if(sk.isReadable() && sk.channel()==udp_in){
            System.out.println("Message input_UDP recu");
            udp_in.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            buff.clear();
            System.out.println("Message :"+st);
          } else if (sk.isReadable() && sk.channel()==udp_emergency){
            System.out.println("Message emergency_UDP recu");
            udp_emergency.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            buff.clear();
            System.out.println("Message :"+st);
          } /*else if (sk.isReadable() && sk.channel()==tcp_in){
            System.out.println("Message input_TCP recu");
            tcp_in.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            buff.clear();
            System.out.println("Message :"+st);
          } */else{
            System.out.println("Que s'est il passe");
          }
        }
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class Entity{
  private static int next_id = 0;

  public static void main(String[] args) {
    try{//init les param de l'entity
      int id;
      int input_UDP = 4343;
      int input_TCP = 4344;
      InetAddress next_address = (Inet4Address) InetAddress.getByName("127.0.0.1");
      int next_input_UDP = 4343;
      //mulitcast sur l'adresse pour restreindre le message uniquement à la meme adresse
      Inet4Address emergency_address = (Inet4Address) InetAddress.getByName("127.0.0.1");
      int emergency_UDP = 4345;
      System.out.println("Create new ring (C port_UDP port_TCP emergency_address emergency_UDP) ");
      System.out.println("or join one (J port_UDP port_TCP address_entity insertion_entity_TCP) ?");
      Scanner sc = new Scanner(System.in);
      String response = sc.nextLine();
      String[] parts = response.split(" ");
      if(parts[0].equals("C")&&parts.length==5){
        id = next_id;
        next_id++;
        input_UDP = Integer.parseInt(parts[1]);
        input_TCP = Integer.parseInt(parts[2]);
        //recuperation de self address
        InetAddress self_address = InetAddress.getByName(Inet4Address.getLocalHost().getHostAddress());//127.000.000.001;
        next_address = self_address;
        next_input_UDP = input_UDP;
        //gestion de la detection de problème plus tard
        emergency_address = (Inet4Address) InetAddress.getByName(parts[3]);
        emergency_UDP = Integer.parseInt(parts[4]);
        System.out.println(id+" "+input_UDP+" "+input_TCP+" "+next_address+" "+next_input_UDP+" "+emergency_address+" "+emergency_UDP);
        System.out.println("All infos received. Creating the ring now!");
      } else if(parts[0].equals("J")&&parts.length==5){
        id = next_id;
        next_id++;
        input_UDP = Integer.parseInt(parts[1]);
        input_TCP = Integer.parseInt(parts[2]);
        //TCP communcation with the entity to join the ring
        InetAddress entity_address = InetAddress.getByName(Inet4Address.getLocalHost().getHostAddress());
        if(!parts[3].equals("localhost")){
          entity_address = (Inet4Address) InetAddress.getByName(parts[3]);
        }
        Socket tmp_sock = new Socket(entity_address, Integer.parseInt(parts[4]));
				BufferedReader br = new BufferedReader(
				new InputStreamReader(
				tmp_sock.getInputStream()));
				PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(
				tmp_sock.getOutputStream()));
        //reads welcome message
				String conf = br.readLine();
        System.out.println(conf);
        parts = conf.split(" ");
        if(parts[0].equals("WELC")&&parts.length==5){
          //does not receive the correct message???
          System.out.println(parts[1]+" "+parts[2]);
          next_address = (Inet4Address) InetAddress.getByName(parts[2].replaceAll("/",""));;
          next_input_UDP = Integer.parseInt(parts[1]);
          //gestion de la detection de problème plus tard
          emergency_address = (Inet4Address) InetAddress.getByName(parts[4].replaceAll("/",""));
          emergency_UDP = Integer.parseInt(parts[3]);
          InetAddress self_address = InetAddress.getByName(Inet4Address.getLocalHost().getHostAddress());//127.000.000.001;
          conf = "NEWC "+input_UDP+" "+self_address;
  				pw.println(conf);
  				pw.flush();
        } else {
          System.out.println("The other one is acting weird on WELC. I'm out of here!");
					System.exit(1);
        }
				conf = br.readLine();
				if(!conf.equals("ACK")){
					System.out.println("The other one is acting weird on ACK. I'm out of here!");
					System.exit(1);
				}
        System.out.println("All infos received. Entering the ring now!");
				pw.close();
				br.close();
				tmp_sock.close();
      } else {
        System.out.println("This is not a choice start over!");
      }

      //create selector
      Selector sel=Selector.open();
      //--------------connexion en udp non bloquante----------------------------
      DatagramChannel udp_in=DatagramChannel.open();
      udp_in.configureBlocking(false);
      udp_in.bind(new InetSocketAddress(input_UDP));
      udp_in.register(sel,SelectionKey.OP_READ);
      //-----connexion en udp au port emercy non bloquante ---------------------
      /*DatagramChannel udp_emergency=DatagramChannel.open();
      udp_emergency.configureBlocking(false);
      udp_emergency.bind(new InetSocketAddress(emergency_UDP));
      udp_emergency.register(sel,SelectionKey.OP_READ);*/
      //-------------connexion en tcp non bloquante-----------------------------
      ServerSocketChannel tcp_in = ServerSocketChannel.open();
      tcp_in.configureBlocking(false);
      tcp_in.bind(new InetSocketAddress(input_TCP));
      tcp_in.register(sel, SelectionKey.OP_ACCEPT);

      ByteBuffer buff=ByteBuffer.allocate(100);
      System.out.println("Ringoooo!");
      //---------------boucle d'attente d'une action---------------------------
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
          } /*else if (sk.isReadable() && sk.channel()==udp_emergency){
            System.out.println("Message emergency_UDP recu");
            udp_emergency.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            buff.clear();
            System.out.println("Message :"+st);
          } */else if (sk.isAcceptable() && sk.channel()==tcp_in){
            Socket comSock = (tcp_in.accept()).socket();
            BufferedReader comBR = new BufferedReader(
  							new InputStreamReader(
  									comSock.getInputStream()));
  					PrintWriter comPW = new PrintWriter(
  							new OutputStreamWriter(
  									comSock.getOutputStream()));
            String welc = "WELC "+next_input_UDP+" "+next_address+" "+emergency_UDP+" "+emergency_address;
            comPW.println(welc);
            comPW.flush();
            String st = comBR.readLine();
            parts = st.split(" ");
            if((parts[0]).equals("NEWC")){
              next_input_UDP = Integer.parseInt(parts[1]);
              next_address = (Inet4Address) InetAddress.getByName(parts[2].replaceAll("/",""));
              comPW.println("ACK");
              comPW.flush();
            } else {
              comPW.println("Message NEWC non conforme recommencez la procédure d'insertion.");
              comPW.flush();
            }
            //------------------close communication-----------------------------
            comBR.close();
  					comPW.close();
  					comSock.close();
  					System.out.println("Connection closed.\n");
          } else{
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

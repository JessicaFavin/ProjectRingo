import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.net.StandardSocketOptions;
import java.nio.channels.*;

public class Entity{

  private static String whos(int id, Inet4Address ip, int port){
    int idm = (int) (Math.random()*99999999);
    String res = "MEMB "+idm+" "+id+" "+ip+" "+port;
    return res;
  }

  private static boolean got_file(String filename){
    File f = new File("./shared/"+filename);
    if(f.exists() && f.isFile()) {
        return true;
    }
    return false;
  }

  private static String formatAddress(Inet4Address address){
    //à revoir pour coder l'adresse sur 15 octets!!!!
    return address.toString().replaceAll("/","");
  }

  private static Appli appliOf(String appli){
    if(appli.equals("TRANS")) {
      return Appli.TRANS;
    } else if(appli.equals("DIFF")) {
      return Appli.DIFF;
    } else if(appli.equals("PENDU")) {
      return Appli.PENDU;
    }

    return Appli.NONE;
  }

  public static void main(String[] args) {
    try{
      int id = 0;
      int appli_TCP = 0;
      int input_appli = 0;
      int output_appli = 0;
      RingInfo ring_one = new RingInfo();
      RingInfo ring_two = new RingInfo();
      Appli appli_active = Appli.NONE;
      String message_appli = "";

      Debug debug = new Debug(false);
      if(args.length==1 && (args[0].equals("-d")||args[0].equals("--debug"))){
        debug.activate();
      }
      //-------------------begin entity---------------------------------
      System.out.println("Create new ring (C port_UDP port_TCP emergency_address emergency_UDP application_TCP) ");
      System.out.println("or join one (J port_UDP port_TCP address_entity insertion_entity_TCP application_TCP) ?");
      Scanner sc = new Scanner(System.in);
      String response = sc.nextLine();
      String[] init = response.split(" ");
      if(init[0].equals("C")&&init.length==6){
        //id is the udp port for now
        id = Integer.parseInt(init[1]);
        ring_one.init_self_ring(init[1], init[2], init[4], init[3]);
        appli_TCP =  Integer.parseInt(init[5]);
        System.out.println("All infos received. Creating the ring now!");
      } else if(init[0].equals("J")&&init.length==6){
        //id is the udp port for now
        id = Integer.parseInt(init[1]);
        InetAddress entity_address = (Inet4Address) InetAddress.getByName(init[3]);
        int entity_TCP = Integer.parseInt(init[4]);
        appli_TCP =  Integer.parseInt(init[5]);
        debug.display("Connecting int TCP to entity");
        Socket tmp_sock = new Socket(entity_address, entity_TCP);
				BufferedReader br = new BufferedReader(
				new InputStreamReader(
				tmp_sock.getInputStream()));
				PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(
				tmp_sock.getOutputStream()));
        debug.display("Connected to entity");
        //reads welcome message
				String conf = br.readLine();
        System.out.println(conf);
        String[] welc = conf.split(" ");
        if(welc[0].equals("WELC")&&welc.length==5){
          debug.display(welc[1]+" "+welc[2]);
          //init ring
          ring_one.init_ring(init[1], init[2], welc[3], welc[4], welc[1], welc[2]);
          Inet4Address self_address = (Inet4Address) InetAddress.getByName(Inet4Address.getLocalHost().getHostAddress());
          conf = "NEWC "+ring_one.getUdpIn()+" "+formatAddress(self_address);
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
        System.out.println("All good. Entering the ring now!");
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
      udp_in.bind(new InetSocketAddress(ring_one.getUdpIn()));
      udp_in.register(sel,SelectionKey.OP_READ);
      //-----------------multicast non bloquant---------------------------------
      //revoir l'interface de connexion
      //faire une fonction qui parcours les interfaces active et en selectionne une??
      //NetworkInterface.getNetworkInterface().nextElement();
      NetworkInterface interf = NetworkInterface.getByName("wlan0");
      InetAddress group = ring_one.getAddressMult();
      DatagramChannel udp_emergency = DatagramChannel.open()
      .setOption(StandardSocketOptions.SO_REUSEADDR, true)
      .bind(new InetSocketAddress(ring_one.getUdpMult()))
      .setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
      udp_emergency.configureBlocking(false);
      udp_emergency.register(sel, SelectionKey.OP_READ);
      udp_emergency.join(group, interf);
      //-------------connexion en tcp_input non bloquante-----------------------------
      ServerSocketChannel tcp_in = ServerSocketChannel.open();
      tcp_in.configureBlocking(false);
      tcp_in.bind(new InetSocketAddress(ring_one.getTcpIn()));
      tcp_in.register(sel, SelectionKey.OP_ACCEPT);
      //-------------connexion en tcp_appli non bloquante-----------------------------
      ServerSocketChannel tcp_appli = ServerSocketChannel.open();
      tcp_appli.configureBlocking(false);
      tcp_appli.bind(new InetSocketAddress(appli_TCP));
      tcp_appli.register(sel, SelectionKey.OP_ACCEPT);

      ByteBuffer buff=ByteBuffer.allocate(100);
      System.out.println("Ringoooo!");
      //---------------boucle d'attente d'une action---------------------------
      while(true){
        //System.out.println("Waiting for messages");
        sel.select();
        Iterator<SelectionKey> it=sel.selectedKeys().iterator();
        while(it.hasNext()){
          SelectionKey sk=it.next();
          it.remove();
          //--------------------Ring input UDP----------------------------------
          if(sk.isReadable() && sk.channel()==udp_in){
            //System.out.println("Message input_UDP recu");
            udp_in.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            buff = ByteBuffer.allocate(100);
            String[] parts = st.split(" ", 5);
            System.out.println(st);
            Integer idm_received = Integer.parseInt(parts[1]);
            Appli appli_received = appliOf(parts[2].replaceAll("#", ""));

            //if appli active est la meme que l'appli recu activate communication
            //et que l'id de transfert est le meme pour le transfert de fichier
            //doit passer la comsock en args de la fonction qui traitera les infos
            //doit sauvegarder la comSock en dehors du while
            // dans la fonction check if QUIT to close comSock and put
            //active appli to none again

            //tcp readable pour lire ok a chaque fois et quit a la fin de l'appli

            //si le message n'est pas déjà passé par ici - ie this est l'expediteur
            if(!ring_one.getMessageList().contains(idm_received)){
              System.out.println("Message reçu :"+parts[4]);
              //envoie du paquet en udp
              DatagramSocket dso=new DatagramSocket();
              byte[] data;
              data = st.getBytes();
              DatagramPacket paquet = new DatagramPacket(data,data.length,
              ring_one.getAddressNext(), ring_one.getUdpNext());
              dso.send(paquet);
            } else  {
              ring_one.getMessageList().remove(idm_received);
            }
          //---------------------Ring emergency_UDP-----------------------------
          } else if (sk.isReadable() && sk.channel()==udp_emergency){
            System.out.println("Message emergency_UDP recu");
            udp_emergency.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            buff.clear();
            System.out.println("Message: "+st);
          //--------------------Ring input_TCP--------------------
          } else if (sk.isAcceptable() && sk.channel()==tcp_in){
            Socket comSock = (tcp_in.accept()).socket();
            BufferedReader comBR = new BufferedReader(
  							new InputStreamReader(
  									comSock.getInputStream()));
  					PrintWriter comPW = new PrintWriter(
  							new OutputStreamWriter(
  									comSock.getOutputStream()));
            String welc = "WELC "+ring_one.getUdpNext()+" "+formatAddress(ring_one.getAddressNext())
              +" "+ring_one.getUdpMult()+" "+formatAddress(ring_one.getAddressMult());
            comPW.println(welc);
            comPW.flush();
            String st = comBR.readLine();
            String[] parts = st.split(" ");
            if((parts[0]).equals("NEWC")){
              ring_one.insertion(parts[1], parts[2]);
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
          //------------------------Ring application TCP------------------------
          } else if (sk.isAcceptable() && sk.channel()==tcp_appli){
            Socket comSock = (tcp_appli.accept()).socket();
            BufferedReader comBR = new BufferedReader(
  							new InputStreamReader(
  									comSock.getInputStream()));
  					PrintWriter comPW = new PrintWriter(
  							new OutputStreamWriter(
  									comSock.getOutputStream()));
            System.out.println("Application connected!");
            //reads initial message and sends it to the ring
            String s = comBR.readLine();
            String [] parts = s.split(" ", 3);
            int idm_received = Integer.parseInt(parts[1]);
            ring_one.getMessageList().add(idm_received);
            //remettre appli_active a none à la fermeture de la connexion!
            appli_active = appliOf(parts[2].replaceAll("#", ""));
            //envoie du paquet en udp
            DatagramSocket dso=new DatagramSocket();
            byte[]data;
            data = s.getBytes();
            DatagramPacket paquet = new DatagramPacket(data,data.length,
            ring_one.getAddressNext(), ring_one.getUdpNext());
            dso.send(paquet);
            //------------------close communication-----------------------------
            comBR.close();
  					comPW.close();
  					comSock.close();
          } else if(ring_two.isInitiated()){
            //do every channel of the second ring in here
            //udp_in
            //udp_mult
          } else {
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

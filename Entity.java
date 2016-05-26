import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.net.StandardSocketOptions;
import java.nio.channels.*;
import java.io.File;
import java.net.*;
import java.util.UUID;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Entity{

  private static String randomId(){
     String uuid = UUID.randomUUID().toString().subString(0,8);
  }

  public static String sendWhos(){
    String idm = randomId();
    String res = "WHOS "+idm;
    return res;
  }

  private static String handleWhos(String id, Inet4Address ip, int port){
    String idm = randomId();
    String res = "MEMB "+idm+" "+id+" "+ip+" "+port;
    return res;
  }

  private static String sendGbye(Inet4Address ip, int port, Inet4Address ip_succ, int port_succ){
    String res = null;
    try{
      String idm = randomId();
      res = "GBYE "+idm+" "+formatAddress(ip)+" "+formatInt(port, 4)+" "+formatAddress(ip_succ)+" "+formatInt(port_succ, 4);
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
    return res;
  }

  private static boolean handleGbye(String st, RingInfo ring){
    try{
      String[] parts = st.split(" ");
      String current_next_port = Integer.toString(ring.getUdpNext()).replaceAll("/","");
      String current_next_ip =  ring.getAddressNext().toString().replaceAll("/","");
      if(parts[2].equals(current_next_ip) && parts[3].equals(current_next_port)){
        String new_udp = parts[5].trim();
        String new_addr = parts[4].trim();
        //sends EYBG confirmation to the ring
        DatagramSocket dso=new DatagramSocket();
        byte[] data = eybg().getBytes();
        InetSocketAddress ia = new InetSocketAddress(ring.getAddressNext(), ring.getUdpNext());
        DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
        dso.send(paquet);
        dso.close();
        //actualize the successor
        ring.newSucc(new_udp, new_addr);
        return false;
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
    //valeur to be stored in passMessage
    return true;
  }

  private static String eybg(){
    String idm = randomId();
    String res = "EYBG "+idm;
    return res;
  }

  public static String sendTest(RingInfo ring){
    String idm = randomId();
    String ip_diff = ring.getAddressMult().toString();
    String port_diff = Integer.toString(ring.getUdpMult());
    String res = "TEST "+idm+" "+ip_diff+" "+port_diff;
    return res;
  }

  private static boolean got_file(String filename){
    String current = System.getProperty("user.dir");
    File f = new File(current+"/shared/"+(filename.trim()));
    if(f==null){
    }else{
      if(f.exists()) {
        return true;
      }
    }
    return false;
  }

  private static String getNbMessFile(String filename){
    try{
      Path path = Paths.get("./shared/"+(filename.trim()));
      byte[] fileArray = Files.readAllBytes(path);
      return formatInt((fileArray.length/463)+1, 8);
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
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

  private static String formatAddress(Inet4Address address){
    //à revoir pour coder l'adresse sur 15 octets!!!!
    String adr = address.toString().replaceAll("/","");
    String[] parts = adr.split("\\.");
    int count;
    for(int i =0; i<parts.length; i++){
      count = 3-parts[i].length();
      String tmp = parts[i];
      parts[i]="";
      for(int j=0; j<count; j++){
        parts[i] = parts[i]+"0";
      }
      parts[i]+=tmp;
    }
    return String.join(".", parts[0], parts[1], parts[2], parts[3]);
  }

  private static Appli appliOf(String appli){
    if(appli.equals("TRANS")) {
      return Appli.TRANS;
    } else if(appli.equals("DIFF")) {
      return Appli.DIFF;
    } else if(appli.equals("PENDU")) {
      return Appli.PENDU;
    } else if(appli.equals("GEST")) {
      return Appli.GEST;
    }

    return Appli.NONE;
  }

  private static BufferedReader getBR(Socket comSock){
    BufferedReader br = null;
    try{
      br = new BufferedReader(new InputStreamReader(comSock.getInputStream()));
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
    return br;
  }

  private static PrintWriter getPW(Socket comSock){
    PrintWriter pw = null;
    try{
      pw = new PrintWriter(new OutputStreamWriter(comSock.getOutputStream()));
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
    return pw;
  }

    public static String getAddress() {
    try {
      Enumeration<NetworkInterface> listNi = NetworkInterface.getNetworkInterfaces();
      while (listNi.hasMoreElements()) {
        NetworkInterface nic = listNi.nextElement();
        Enumeration<InetAddress> listIa = nic.getInetAddresses();
        InetAddress iac = null;
        while (listIa.hasMoreElements())
          iac = listIa.nextElement();

        return iac.getHostAddress();
      }
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }

    return null;
  }

  private static byte[] concatenateByteArrays(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length]; 
    System.arraycopy(a, 0, result, 0, a.length); 
    System.arraycopy(b, 0, result, a.length, b.length); 
    return result;
  } 

  private static void insert_entity(BufferedReader comBR, PrintWriter comPW, RingInfo ring_one, 
    RingInfo ring_two, DatagramChannel udp_emergency_two, Selector sel, Debug debug){
    try{
      String welc = "WELC "+formatAddress(ring_one.getAddressNext())+" "+ring_one.getUdpNext()
        +" "+formatAddress(ring_one.getAddressMult())+" "+ring_one.getUdpMult();
      debug.display(welc);
      comPW.println(welc);
      comPW.flush();
      String st = comBR.readLine();
      String[] parts = st.split(" ");
      if(parts[0].equals("NEWC")) {
        ring_one.insertion(parts[1], parts[2]);
        comPW.println("ACKC");
        comPW.flush();
        debug.display("Entiy inserted");
      } else if(parts[0].equals("DUPL")) {
        //init_ring(String udp_in, String tcp_in, String udp_mult,String address_mult, String udp_next, String address_next)
        ring_two.init_ring(Integer.toString(ring_one.getUdpIn()), Integer.toString(ring_one.getTcpIn()), parts[4].trim(), parts[3], parts[2], parts[1]);
        //new udp_mult to initialize!
        NetworkInterface interf = NetworkInterface.getNetworkInterfaces().nextElement();
        InetAddress group = ring_two.getAddressMult();
        udp_emergency_two = DatagramChannel.open()
        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
        .bind(new InetSocketAddress(ring_two.getUdpMult()))
        .setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
        udp_emergency_two.configureBlocking(false);
        udp_emergency_two.register(sel, SelectionKey.OP_READ);
        udp_emergency_two.join(group, interf);
        debug.display("Multicast_dupl connected");
        comPW.println("ACKD");
        comPW.flush();
        debug.display("Ring duplicated");
      } else {
        comPW.println("Message NEWC or DUPL non conforme recommencez la procédure d'insertion.");
        comPW.flush();
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  private static String appliToString(Appli a){
    switch (a){
      case TRANS:
        return "TRANS";
      case PENDU:
        return "PENDU";
      case GEST:
        return "GEST";
      case NONE:
        return "NONE";
      default:
        return "NONE";
    }
  }

  public static void main(String[] args) {
    try{
      String id = randomId();
      int appli_TCP = 0;
      int input_appli = 0;
      int output_appli = 0;
      RingInfo ring_one = new RingInfo();
      RingInfo ring_two = new RingInfo();
      Appli appli_active = Appli.NONE;
      TransInfo trans = null;
      Debug debug = new Debug(false);
      DatagramChannel udp_emergency_two = null;

      if(args.length==1 && (args[0].equals("-d")||args[0].equals("--debug"))){
        debug.activate();
        debug.display("Debug is activated");
      }

      //-------------------begin entity---------------------------------
      System.out.println("Create new ring (C port_UDP port_TCP emergency_address emergency_UDP application_TCP) ");
      System.out.println("or join one (J port_UDP port_TCP address_entity insertion_entity_TCP application_TCP) ?");
      Scanner sc = new Scanner(System.in);
      String response = sc.nextLine();
      String[] init = response.split(" ");
      if(init[0].equals("C")&&init.length==6){
        ring_one.init_self_ring(init[1], init[2], init[4], init[3]);
        appli_TCP =  Integer.valueOf(init[5]);
        Inet4Address self_address = ring_one.getAddressNext();
        debug.display("Self address : "+self_address);
        System.out.println("All infos received. Creating the ring now!");
      } else if(init[0].equals("J")&&init.length==6){
        InetAddress entity_address = (Inet4Address) InetAddress.getByName(init[3]);
        int entity_TCP = Integer.valueOf(init[4]);
        appli_TCP =  Integer.valueOf(init[5]);
        debug.display("Connecting int TCP to entity");
        Socket tmp_sock = new Socket(entity_address, entity_TCP);
				BufferedReader br = getBR(tmp_sock);
				PrintWriter pw = getPW(tmp_sock);
        debug.display("Connected to entity");
        //reads welcome message
				String conf = br.readLine();
        System.out.println(conf);
        String[] welc = conf.split(" ");
        if(welc[0].equals("WELC")&&welc.length==5){
          debug.display(welc[1]+" "+welc[2]);
          //init ring
          ring_one.init_ring(init[1], init[2], welc[4], welc[3], welc[2], welc[1]);
          Inet4Address self_address = (Inet4Address) InetAddress.getByName(Entity.getAddress());
          debug.display("Self address : "+self_address);
          conf = "NEWC "+ring_one.getUdpIn()+" "+formatAddress(self_address);
  				pw.println(conf);
  				pw.flush();
        } else {
          System.out.println("The other one is acting weird on WELC. I'm out of here!");
					System.exit(1);
        }
				conf = br.readLine();
				if(!conf.equals("ACKC")){
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
      //--------------connexion en udp_in non bloquante----------------------------
      DatagramChannel udp_in=DatagramChannel.open();
      udp_in.configureBlocking(false);
      udp_in.bind(new InetSocketAddress(ring_one.getUdpIn()));
      udp_in.register(sel,SelectionKey.OP_READ);
      debug.display("UDP connected");
      //-----------------udp_mult non bloquant---------------------------------
      NetworkInterface interf = NetworkInterface.getNetworkInterfaces().nextElement();
      InetAddress group = ring_one.getAddressMult();
      DatagramChannel udp_emergency = DatagramChannel.open()
      .setOption(StandardSocketOptions.SO_REUSEADDR, true)
      .bind(new InetSocketAddress(ring_one.getUdpMult()))
      .setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
      udp_emergency.configureBlocking(false);
      udp_emergency.register(sel, SelectionKey.OP_READ);
      udp_emergency.join(group, interf);
      debug.display("Multicast connected");
      //-------------connexion en tcp_input non bloquante-----------------------------
      ServerSocketChannel tcp_in = ServerSocketChannel.open();
      tcp_in.configureBlocking(false);
      tcp_in.bind(new InetSocketAddress(ring_one.getTcpIn()));
      tcp_in.register(sel, SelectionKey.OP_ACCEPT);
      debug.display("TCP connected");
      //-------------connexion en tcp_appli non bloquante-----------------------------
      ServerSocketChannel tcp_appli = ServerSocketChannel.open();
      tcp_appli.configureBlocking(false);
      tcp_appli.bind(new InetSocketAddress(appli_TCP));
      tcp_appli.register(sel, SelectionKey.OP_ACCEPT);
      debug.display("TCP appli connected");

      //taille du buffer à revoir??
      ByteBuffer buff = ByteBuffer.allocate(512);
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
            boolean passMessage = true;
            udp_in.receive(buff);
            String st = new String(buff.array(),0,buff.array().length);
            String[] parts = st.split(" ", 8);
            String idm_received = parts[1];
            Appli appli_received = appliOf(parts[2].replaceAll("#", ""));
            //les if servent à savoir si les messages sont à l'attention de l'entité
            //s'ils faut les traiter ou juste les faire suivre à l'anneau
            if(appli_received==Appli.TRANS){
              if(appli_received == appli_active){
                if (parts[3].equals("REQ") && ring_one.getMessageList().contains(idm_received)){
                  debug.display("File not found in the ring.");
                  appli_active = Appli.NONE;
                  //pas besoin de le supprimer car cas traité dans la boucle if(passMessage)
                }
                if(parts[3].equals("ROK") && parts[6].trim().equals(trans.getFilename())){
                  debug.display("File found begin saving the parts of the file");
                  trans.init(st, debug);
                  passMessage = false;
                }
                if(parts[3].equals("SEN") && parts[4].trim().equals(trans.getIdTrans())) {
                  trans.insert_message(buff, debug);
                  if(trans.isFull()){
                    //copy file_parts to dst
                    trans.copy_file(debug);
                    trans = null;
                    appli_active = Appli.NONE;
                  }
                  passMessage = false;
                }
              } else {
                String filename = parts[5].trim();
                if (parts[3].equals("REQ") && got_file(filename)) {
                  passMessage = false;
                  String idm = randomId();
                  String idtrans = randomId();
                  String nummess = getNbMessFile(filename);
                  String answer = "APPL "+idm+" TRANS### ROK "+idtrans+" "+formatInt(filename.trim().length(), 2)+" "+filename+" "+nummess;
                  DatagramSocket dso=new DatagramSocket();
                  byte[] data = new byte[512];
                  data = answer.getBytes();
                  InetSocketAddress ia = new InetSocketAddress(ring_one.getAddressNext(), ring_one.getUdpNext());
                  DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
                  dso.send(paquet);
                  //sends parts of the file
                  Path path = Paths.get("./shared/"+(filename.trim()));
                  byte[] fileArray = Files.readAllBytes(path);
                  //----------------------sends bytes to client-------------------------
                  debug.display("Send messages");
                  int count = 0;
                  for(int i=0; i<fileArray.length; i=i+463){
                    idm = randomId();
                    //copy of range doesn't take the last caracter
                    debug.display(Integer.toString(count));
                    byte[] sub = null;
                    if(count == (Integer.valueOf(nummess)-1)){
                      sub = Arrays.copyOfRange(fileArray, i, fileArray.length);
                    } else {
                      sub = Arrays.copyOfRange(fileArray, i, i+463);
                    }
                    String pref = "APPL "+idm+" TRANS### SEN "+idtrans+" "+formatInt(count,8)+" "+formatInt(sub.length,3)+" ";
                    byte[] sen = concatenateByteArrays(pref.getBytes(), sub);
                    paquet = new DatagramPacket(sen, sen.length, ia);
                    dso.send(paquet);
                    count++;
                  }
                  dso.close();
                }
              }
            } else 
            if(appli_received==Appli.PENDU){
              debug.display("Pendu received");
            } else 
            if(appli_received==Appli.GEST){
              debug.display("Gestion protocole received");
            }

            if(passMessage) {
              //si le message n'est pas déjà passé par ici - ie this est l'expediteur
              if(!ring_one.getMessageList().contains(idm_received)){
                //System.out.println("Message reçu : "+st);
                //envoie du paquet en udp
                DatagramSocket dso=new DatagramSocket();
                byte[] data;
                data = st.getBytes();
                DatagramPacket paquet = new DatagramPacket(data,data.length,
                ring_one.getAddressNext(), ring_one.getUdpNext());
                dso.send(paquet);
                dso.close();
              } else  {
                ring_one.getMessageList().remove(idm_received);
              }
            }
          //---------------------Ring emergency_UDP-----------------------------
          } else if (sk.isReadable() && sk.channel()==udp_emergency){
            System.out.println("Message emergency_UDP recu");
            udp_emergency.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            System.out.println("Message: "+st);
          //--------------------Ring input_TCP--------------------
          } else if (sk.isAcceptable() && sk.channel()==tcp_in){
            Socket comSock = (tcp_in.accept()).socket();
            BufferedReader comBR = getBR(comSock);
  					PrintWriter comPW = getPW(comSock);
            insert_entity(comBR, comPW, ring_one, ring_two, udp_emergency_two, sel, debug);
            comBR.close();
  					comPW.close();
  					comSock.close();
          //------------------------Ring application TCP------------------------
          } else if (sk.isAcceptable() && sk.channel()==tcp_appli){
            Socket comSock = (tcp_appli.accept()).socket();
            BufferedReader comBR = getBR(comSock);
  					PrintWriter comPW = getPW(comSock);
            debug.display("Application connected!");
            //reads initial message and sends it to the ring
            String s = comBR.readLine();
            String [] parts = s.split(" ", 6);
            String idm_received = parts[1];
            //saves id of message sent to list
            ring_one.getMessageList().add(idm_received);
            appli_active = appliOf(parts[2].replaceAll("#", ""));
            if(appli_active==Appli.TRANS){
              debug.display("Create Transinfo");
              trans = new TransInfo(parts[5].trim());
            }
            //envoie du paquet en udp
            debug.display("Appli message sent to the ring");
            DatagramSocket dso=new DatagramSocket();
            byte[]data;
            data = s.getBytes();
            DatagramPacket paquet = new DatagramPacket(data,data.length,
            ring_one.getAddressNext(), ring_one.getUdpNext());
            dso.send(paquet);
            dso.close();
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
          buff = ByteBuffer.allocate(512);
        }
      }
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

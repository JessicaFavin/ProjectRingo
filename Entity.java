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
     String uuid = UUID.randomUUID().toString().substring(0,8);
     return uuid;
  }

  public static String sendWhos(RingInfo ring_one, RingInfo ring_two, Debug debug){
    try{
      String idm = randomId();
      String res = "WHOS "+idm;
      //envoie du paquet en udp
      ring_one.getMessageList().add(idm);
      debug.display("Enregistré : "+Boolean.toString(ring_one.getMessageList().contains(idm)));
      DatagramSocket dso=new DatagramSocket();
      byte[]data;
      data = res.getBytes();
      DatagramPacket paquet = new DatagramPacket(data,data.length,
      ring_one.getAddressNext(), ring_one.getUdpNext());
      dso.send(paquet);
      if(ring_two.isInitiated()) {
        ring_two.getMessageList().add(idm);
        paquet = new DatagramPacket(data,data.length,
        ring_two.getAddressNext(), ring_two.getUdpNext());
        dso.send(paquet);
      }
      debug.display("Whos message sent to the ring");
      dso.close();
      return idm;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
  }

  private static void handleWhos(String id, RingInfo ring_one, RingInfo ring_two, Debug debug){

    try{
      String idm = randomId();
      String ip = getAddress();
      int port = ring_one.getUdpIn();
      String res = "MEMB "+idm+" "+id+" "+ip+" "+formatInt(port, 4);
      //envoie du paquet en udp
      ring_one.getMessageList().add(idm);
      DatagramSocket dso=new DatagramSocket();
      byte[]data;
      data = res.getBytes();
      DatagramPacket paquet = new DatagramPacket(data,data.length,
      ring_one.getAddressNext(), ring_one.getUdpNext());
      dso.send(paquet);
      if(ring_two.isInitiated()){
        ring_two.getMessageList().add(idm);
        paquet = new DatagramPacket(data,data.length,
        ring_two.getAddressNext(), ring_two.getUdpNext());
        dso.send(paquet);
      }
      debug.display("Memb message sent to the ring");
      dso.close();
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  private static String sendGbye(RingInfo ring, Debug debug){
    if(!ring.isInitiated()){
      return "";
    }
    try{
      String idm = randomId();
      Inet4Address ip = ring.getSelfAddress();
      Inet4Address ip_succ = ring.getAddressNext();
      int port = ring.getUdpIn();
      int port_succ = ring.getUdpNext();
      String res = "GBYE "+idm+" "+formatAddress(ip)+" "+formatInt(port, 4)+" "+formatAddress(ip_succ)+" "+formatInt(port_succ, 4);
      //envoie du paquet en udp
      ring.getMessageList().add(idm);
      DatagramSocket dso=new DatagramSocket();
      byte[]data;
      data = res.getBytes();
      DatagramPacket paquet = new DatagramPacket(data,data.length,
      ring.getAddressNext(), ring.getUdpNext());
      dso.send(paquet);
      debug.display("Gbye message sent to the ring");
      dso.close();
      return idm;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
    return null;
  }

  private static boolean handleGbye(String st, RingInfo ring_one, RingInfo ring_two, Debug debug){
    try{
      String[] parts = st.split(" ");
      String current_next_port_one = Integer.toString(ring_one.getUdpNext());
      String current_next_ip_one =  formatAddress(ring_one.getAddressNext());
      debug.display("GBYE test if concerned");
      if(parts[2].equals(current_next_ip_one) && parts[3].equals(current_next_port_one)){
        debug.display("ring_one concerned");
        String new_udp = parts[5].trim();
        String new_addr = parts[4].trim();
        //sends EYBG confirmation to the ring
        DatagramSocket dso=new DatagramSocket();
        byte[] data = eybg().getBytes();
        InetSocketAddress ia = new InetSocketAddress(ring_one.getAddressNext(), ring_one.getUdpNext());
        DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
        dso.send(paquet);
        dso.close();
        //actualize the successor
        ring_one.newSucc(new_udp, new_addr);
        debug.display("Successor on ring 1 actualized");
        return false;
      }
      if(ring_two.isInitiated()){
        String current_next_port_two = Integer.toString(ring_two.getUdpNext());
        String current_next_ip_two =  formatAddress(ring_two.getAddressNext());
        if(parts[2].equals(current_next_ip_two) && parts[3].equals(current_next_port_two)){
          debug.display("ring_two concerned");
          String new_udp = parts[5].trim();
          String new_addr = parts[4].trim();
          //sends EYBG confirmation to the ring
          DatagramSocket dso=new DatagramSocket();
          byte[] data = eybg().getBytes();
          InetSocketAddress ia = new InetSocketAddress(ring_two.getAddressNext(), ring_two.getUdpNext());
          DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
          dso.send(paquet);
          dso.close();
          //actualize the successor
          if(new_udp.equals(formatInt(ring_two.getUdpNext(),4)) && formatAddress(new_addr).equals(formatAddress(getAddress()))){
            ring_two = new RingInfo();
            debug.display("Ring 2 deleted");
          } else {
            ring_two.newSucc(new_udp, new_addr);
            debug.display("Successor on ring 2 actualized");
          }
          return false;
        }
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

  public static void sendTest(RingInfo ring, Debug debug){
    if(!ring.isInitiated()){
      return;
    }
    debug.display("Start sending test");
    try{
      String idm = randomId();
      Inet4Address ip_diff = ring.getAddressMult();
      int port_diff = ring.getUdpMult();
      String res = "TEST "+idm+" "+formatAddress(ip_diff)+" "+formatInt(port_diff, 4);
      //envoie du paquet en udp
      ring.getMessageList().add(idm);
      DatagramSocket dso=new DatagramSocket();
      byte[]data;
      data = res.getBytes();
      DatagramPacket paquet = new DatagramPacket(data,data.length,
      ring.getAddressNext(), ring.getUdpNext());
      dso.send(paquet);
      debug.display("Test message sent to the ring");
      dso.close();
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
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

  private static String formatAddress(String adr){
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
    } else if(appli.equals("DICO")) {
      return Appli.DICO;
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
      if(ring_two.isInitiated()){
          comPW.println("NOTC");
          comPW.flush();
          return;
      }
      String welc = "WELC "+formatAddress(ring_one.getAddressNext())+" "+ring_one.getUdpNext()
        +" "+formatAddress(ring_one.getAddressMult())+" "+ring_one.getUdpMult();
      //debug.display(welc);
      comPW.println(welc);
      comPW.flush();
      String st = comBR.readLine();
      String[] parts = st.split(" ");
       if(parts[0].equals("NEWC")) {
        ring_one.insertion(parts[1], parts[2]);
        comPW.println("ACKC");
        comPW.flush();
        debug.display("Entity inserted");
      } else if(parts[0].equals("DUPL")) {
         
        debug.display("Message reçu : "+st);
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
      case DICO:
        return "DICO";
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
      TestInfo test = null;
      Debug debug = new Debug(false);
      DatagramChannel udp_emergency_two = null;

      if(args.length==1 && (args[0].equals("-d")||args[0].equals("--debug"))){
        debug.activate();
        debug.display("Debug is activated");
      }

      //-------------------begin entity---------------------------------
      System.out.println("Create new ring (C port_UDP port_TCP emergency_address emergency_UDP application_TCP) ");
      System.out.println("join one (J port_UDP port_TCP address_entity insertion_entity_TCP application_TCP) ?");
      System.out.println("duplicate one (D port_UDP port_TCP address_entity entity_TCP application_TCP emergency_address emergency_UDP) ?");
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
      } else if(init[0].equals("D")&&init.length==8){
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
        if(welc[0].trim().equals("NOTC")){
          System.out.println("Ring full.");
          System.exit(0);
        } else if(welc[0].equals("WELC")&&welc.length==5){
          debug.display(welc[1]+" "+welc[2]);
          //init ring
          ring_one.init_ring(init[1], init[2], welc[4], welc[3], welc[2], welc[1]);
          Inet4Address self_address = (Inet4Address) InetAddress.getByName(Entity.getAddress());
          Inet4Address second_addr_mult = (Inet4Address) InetAddress.getByName(init[6]);
          int second_port_mult = Integer.valueOf(init[7]);
          debug.display("Self address : "+self_address);
          conf = "DUPL "+formatAddress(self_address)+" "+ring_one.getUdpIn()+" "+formatAddress(second_addr_mult)+" "+formatInt(second_port_mult,4);
          pw.println(conf);
          pw.flush();
        } else {
          System.out.println("The other one is acting weird on WELC. I'm out of here!");
          System.exit(1);
        }
        conf = br.readLine();
        if(!conf.equals("ACKD")){
          System.out.println("The other one is acting weird on ACKD. I'm out of here!");
          System.exit(1);
        }
        System.out.println("All good. Entering the ring now!");
        pw.close();
        br.close();
        tmp_sock.close();
      } else {
        System.out.println("This is not a choice start over!");
        System.exit(0);
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
            //debug.display(st);
            String[] parts = st.split(" ", 8);
            String idm_received = parts[1];
            if(parts[0].equals("APPL")){
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
              } else if(appli_received==Appli.DICO){
                debug.display("Dico received");
              }
            } else {
              switch(parts[0]){
                case "WHOS":
                  if(!ring_one.getMessageList().contains(idm_received)){
                    handleWhos(id, ring_one, ring_two, debug);
                  } 
                  passMessage = true;
                  break;
                case "GBYE":
                  passMessage = handleGbye(st, ring_one, ring_two, debug);
                  break;
                case "TEST":
                  //do nothing just pass it
                  passMessage = true;
                  break;
                case "EYBG":
                  if(appli_active==Appli.GBYE){
                    System.exit(0);
                  }
                  passMessage = false;
                  break;
                default:
                  //do nothing just pass it
                  passMessage = true;
                  break;
              }
            }

            if(passMessage) {
              //si le message n'est pas déjà passé par ici - ie this est l'expediteur
              if(!ring_one.getMessageList().contains(idm_received)){
                debug.display("Message reçu : "+st);
                //envoie du paquet en udp
                ring_one.getMessageList().add(idm_received);
                DatagramSocket dso=new DatagramSocket();
                byte[] data;
                data = st.getBytes();
                DatagramPacket paquet = new DatagramPacket(data,data.length,
                ring_one.getAddressNext(), ring_one.getUdpNext());
                dso.send(paquet);

                dso.close();
              } else  {
                if(parts[0].equals("TEST") && test!=null){
                  test.stopTimer(debug);
                }
                ring_one.getMessageList().remove(idm_received);
              }
              if(ring_two.isInitiated()){
                if(!ring_two.getMessageList().contains(idm_received)){
                  //System.out.println()
                  //envoie du paquet en udp
                  ring_two.getMessageList().add(idm_received);
                  DatagramSocket dso=new DatagramSocket();
                  byte[] data;
                  data = st.getBytes();
                  DatagramPacket paquet = new DatagramPacket(data,data.length,
                  ring_two.getAddressNext(), ring_two.getUdpNext());
                  dso.send(paquet);
                  dso.close();
                } else  {
                  if(parts[0].equals("TEST") && test!=null){
                    test.stopTimer(debug);
                  }
                  ring_two.getMessageList().remove(idm_received);
                }
              }
            }

            
          //---------------------Ring emergency_UDP-----------------------------
          } else if (sk.isReadable() && sk.channel()==udp_emergency){
            System.out.println("Emergency_UDP ring 1 recu");
            udp_emergency.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            debug.display(st);
            if(st.trim().equals("DOWN")){
              debug.display("Ring 1 down.");
              if(ring_two.isInitiated()){
                debug.display("Swapping Ring 2 and Ring 1 and killing it.");
                ring_one = ring_two;
                ring_two = new RingInfo();
              } else {
                debug.display("Killing the ring. Bye.");
                System.exit(1);
              }
              
            }
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
            //debug.display(s);
            String idm = "";
            String [] parts = s.split(" ", 6);
            if(parts[0].equals("GEST")){
              switch(parts[1].trim()){
                case "WHOS":
                  idm = sendWhos(ring_one, ring_two, debug);
                  ring_one.getMessageList().add(idm);
                  ring_two.getMessageList().add(idm);
                  break;
                case "GBYE":
                  if(parts[2].trim().equals("1")){
                    idm = sendGbye(ring_one, debug);
                    ring_one.getMessageList().add(idm);
                  } else {
                    idm = sendGbye(ring_two, debug);
                    ring_two.getMessageList().add(idm);
                  }
                  appli_active = Appli.GBYE;
                  break;
                case "TEST":
                  debug.display("TEST");
                  System.out.println(parts[2].trim().equals("1"));
                  if(parts[2].trim().equals("1")){
                    test = new TestInfo(ring_one, debug);
                     sendTest(ring_one, debug);
                  } else {
                    test = new TestInfo(ring_two, debug);
                    sendTest(ring_two, debug);
                  }
                  break;
                default:
                  break;
              }
            } else {
              String idm_received = parts[1];
              //saves id of message sent to list
              appli_active = appliOf(parts[2].replaceAll("#", ""));
              if(appli_active==Appli.TRANS){
                debug.display("Create TransInfo");
                trans = new TransInfo(parts[5].trim());
              }
              //envoie du paquet en udp
              ring_one.getMessageList().add(idm_received);
              DatagramSocket dso=new DatagramSocket();
              byte[]data;
              data = s.getBytes();
              DatagramPacket paquet = new DatagramPacket(data,data.length,
              ring_one.getAddressNext(), ring_one.getUdpNext());
              dso.send(paquet);
              if(ring_two.isInitiated()){
                ring_two.getMessageList().add(idm_received);
                data = s.getBytes();
                paquet = new DatagramPacket(data,data.length,
                ring_two.getAddressNext(), ring_two.getUdpNext());
                dso.send(paquet);
              }
              debug.display("Appli message sent to the ring");
              dso.close();
            }
            
            //------------------close communication-----------------------------
            comBR.close();
  					comPW.close();
  					comSock.close();
          } else if(ring_two.isInitiated() && sk.isReadable() && sk.channel()==udp_emergency_two){
            System.out.println("Emergency_UDP ring 2 recu");
            udp_emergency_two.receive(buff);
            String st=new String(buff.array(),0,buff.array().length);
            debug.display(st);
            if(st.trim().equals("DOWN")){
              debug.display("Ring 2 down. Killing Ring 2");
              ring_two = new RingInfo();
            }
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

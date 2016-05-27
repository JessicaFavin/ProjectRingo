import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class RingInfo{

  private int udp_in;
  private int tcp_in;
  private int udp_mult;
  private Inet4Address address_mult;
  private int udp_next;
  private Inet4Address address_next;
  private ArrayList<String> message_list;
  private Inet4Address self_address;
  private boolean initiated;

  public RingInfo(){
    try{
      this.udp_in = 0;
      this.tcp_in = 0;
      this.udp_mult = 0;
      this.address_mult = (Inet4Address) InetAddress.getByName("0.0.0.0");
      this.udp_next = 0;
      this.address_next = (Inet4Address) InetAddress.getByName("0.0.0.0");
      this.message_list = new ArrayList<String>();
      this.initiated = false;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void init_ring(String udp_in, String tcp_in, String udp_mult,
  String address_mult, String udp_next, String address_next){
    try{
      this.udp_in = Integer.valueOf(udp_in);
      this.tcp_in = Integer.valueOf(tcp_in);
      this.udp_mult = Integer.valueOf(udp_mult);
      this.address_mult = (Inet4Address) InetAddress.getByName(address_mult);
      this.udp_next = Integer.valueOf(udp_next);
      this.address_next = (Inet4Address) InetAddress.getByName(address_next);
      this.message_list = new ArrayList<String>();
      this.self_address = (Inet4Address) InetAddress.getByName(Entity.getAddress());
      this.initiated = true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void init_self_ring(String udp_in, String tcp_in, String udp_mult,
  String address_mult){
    try{
      this.udp_in = Integer.valueOf(udp_in);
      this.tcp_in = Integer.valueOf(tcp_in);
      this.udp_mult = Integer.valueOf(udp_mult);
      this.address_mult = (Inet4Address) InetAddress.getByName(address_mult);
      //next entity is self
      this.udp_next = this.udp_in;
      this.address_next = (Inet4Address) InetAddress.getByName(Entity.getAddress());
      this.message_list = new ArrayList<String>();
      this.self_address = (Inet4Address) InetAddress.getByName(Entity.getAddress());
      this.initiated = true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void insertion(String udp_next, String address_next){
    try{
      this.udp_next = Integer.valueOf(udp_next);
      this.address_next = (Inet4Address) InetAddress.getByName(address_next);
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public int getUdpIn(){
    return udp_in;
  }

  public int getTcpIn(){
    return tcp_in;
  }

  public int getUdpMult(){
    return udp_mult;
  }

  public Inet4Address getAddressMult(){
    return address_mult;
  }

  public int getUdpNext(){
    return udp_next;
  }

  public Inet4Address getAddressNext(){
    return address_next;
  }

  public void newSucc(String new_udp, String new_addr){
    try{
      this.udp_next = Integer.valueOf(new_udp);
      this.address_next = (Inet4Address) InetAddress.getByName(new_addr);
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public Inet4Address getSelfAddress(){
    return self_address;
  }

  public ArrayList<String> getMessageList(){
    return message_list;
  }

  public boolean isInitiated(){
    return initiated;
  }

}

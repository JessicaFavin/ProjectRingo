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
  private ArrayList<Integer> message_list;
  private boolean initiated;

  public RingInfo(){
    try{
      this.udp_in = 0;
      this.tcp_in = 0;
      this.udp_mult = 0;
      this.address_mult = (Inet4Address) InetAddress.getByName("0.0.0.0");
      this.udp_next = 0;
      this.address_next = (Inet4Address) InetAddress.getByName("0.0.0.0");
      this.message_list = new ArrayList<Integer>();
      this.initiated = false;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void init_ring(int udp_in, int tcp_in, int udp_mult,
  Inet4Address address_mult, int udp_next, Inet4Address address_next){
    this.udp_in = udp_in;
    this.tcp_in = tcp_in;
    this.udp_mult = udp_mult;
    this.address_mult = address_mult;
    this.udp_next = udp_next;
    this.address_next = address_next;
    this.message_list = new ArrayList<Integer>();
    this.initiated = true;
  }

  public void init_ring(String udp_in, String tcp_in, String udp_mult,
  String address_mult, String udp_next, String address_next){
    try{
      this.udp_in = Integer.parseInt(udp_in);
      this.tcp_in = Integer.parseInt(tcp_in);
      this.udp_mult = Integer.parseInt(udp_mult);
      this.address_mult = (Inet4Address) InetAddress.getByName(address_mult);
      this.udp_next = Integer.parseInt(udp_next);
      this.address_next = (Inet4Address) InetAddress.getByName(address_next);
      this.message_list = new ArrayList<Integer>();
      this.initiated = true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void init_self_ring(String udp_in, String tcp_in, String udp_mult,
  String address_mult){
    try{
      this.udp_in = Integer.parseInt(udp_in);
      this.tcp_in = Integer.parseInt(tcp_in);
      this.udp_mult = Integer.parseInt(udp_mult);
      this.address_mult = (Inet4Address) InetAddress.getByName(address_mult);
      //next entity is self
      this.udp_next = this.udp_in;
      this.address_next = (Inet4Address) InetAddress.getByName(Inet4Address.getLocalHost().getHostAddress());;
      this.message_list = new ArrayList<Integer>();
      this.initiated = true;
    } catch (Exception e){
      System.out.println(e);
      e.printStackTrace();
    }
  }

  public void insertion(String udp_next, String address_next){
    try{
      this.udp_next = Integer.parseInt(udp_next);
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

  public ArrayList<Integer> getMessageList(){
    return message_list;
  }

  public boolean isInitiated(){
    return initiated;
  }

}

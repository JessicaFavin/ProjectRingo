public class Entity{
  private static  int           next_id = 0;
  private         int           id;
  private         int           input_UDP;
  private         int           input_TCP;
  private         InetAddress   next_address;
  private         int           next_input_UDP;
  private         Inet4Address  emergency_address;
  //mulitcast sur l'adresse pour restreindre le message uniquement à la meme adresse
  private         int           emergency_UDP;

  public static void main(String[] args) {
    if(args.length!=6 && args.length!=4){
      System.out.println("java Entity port_UDP port_TCP [next_address] [next_port_UDP] emergency_address emergency_UDP");
    }
    if(args.length == 6){
      this.id = next_id;
      next_id++;
      this.input_UDP = args[0];
      this.input_TCP = args[1];
      this.next_address = args[2];
      this.next_input_UDP = args[3];
      //gestion de la detection de problème plus tard
      this.emergency_address = args[4];
      this.emergency_UDP = args[5];
    }
    if(args.length == 4){
      this.id = next_id;
      next_id++;
      this.input_UDP = args[0];
      this.input_TCP = args[1];
      //recuperation de self address
      InetAddress self_address = 127.000.000.001;
      this.next_address = self_address;
      this.next_input_UDP = this.input_UDP;
      //gestion de la detection de problème plus tard
      this.emergency_address = args[2];
      this.emergency_UDP = args[3];
    }
    //--------------connexion en udp non bloquante------------------------------
    // a faire
    //-------------connexion en tcp non bloquante-------------------------------
    //a faire
    //-----connexion en udp au port emercy non bloquante -----------------------
    //a faire
  }
}

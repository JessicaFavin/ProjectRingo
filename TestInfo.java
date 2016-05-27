import java.util.Date;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class TestInfo{

	Timer timer;
	RingInfo ring;

	private static void sendDown(RingInfo ring, Debug debug){
	    try{
	      String res = "DOWN";
	      //envoie du paquet en udp
	      DatagramSocket dso=new DatagramSocket();
	      byte[]data;
	      data = res.getBytes();
	      DatagramPacket paquet = new DatagramPacket(data,data.length,
	      ring.getAddressMult(), ring.getUdpMult());
	      dso.send(paquet);
	      debug.display("Down message sent to the ring");
	      dso.close();
	    } catch (Exception e){
	      System.out.println(e);
	      e.printStackTrace();
	    }
	  }

	public TestInfo(RingInfo ring, Debug debug){
		this.ring = ring;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				sendDown(ring, debug);
			}	
		};
		
		timer = new Timer();
		long delay = 9000;
		timer.schedule(task, delay);
	}

	public void stopTimer(Debug debug){
		debug.display("TEST okay, stopping timer");
		timer.cancel();
		timer.purge();
	}

}
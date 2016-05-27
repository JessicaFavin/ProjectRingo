import java.util.Date;
import java.net.*;
import java.util.Timer;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.TimerTask;
import java.nio.channels.*;

public class TestInfo{

	Timer timer;
	RingInfo ring;
	DatagramChannel dc;

	private static void sendDown(RingInfo ring, DatagramChannel dc, Debug debug){
	    try{
			ByteBuffer buff = ByteBuffer.allocate(5);

			String res = "DOWN";
			//envoie du paquet en udp
			byte[] data;
			data = res.getBytes();
			buff.put(data);
			buff.flip();
			InetSocketAddress ia = new InetSocketAddress(ring.getAddressMult(), ring.getUdpMult());
			//DatagramPacket paquet = new DatagramPacket(data,data.length,);
			dc.send(buff, ia);
			debug.display("Down message sent to the ring");
	    } catch (Exception e){
	      System.out.println(e);
	      e.printStackTrace();
	    }
	  }

	public TestInfo(RingInfo ring, DatagramChannel dc, Debug debug){
		this.ring = ring;
		this.dc = dc;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				sendDown(ring, dc, debug);
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
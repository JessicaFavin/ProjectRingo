import java.util.Scanner;
import java.io.File;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Test{

	private static boolean got_file(String filename){
    File f = new File("./shared/"+filename);
    if(f.exists() && f.isFile()) {
        return true;
    }
    return false;
  }

	public static void main(String args[]){
		Scanner sc = new Scanner(System.in);
		System.out.println("Which file?");
		String answer = sc.nextLine();
		try{
			if(got_file(answer)){
				byte[] fileArray;
				Path path = Paths.get("./shared/"+answer);
				fileArray = Files.readAllBytes(path);
				try {
					DatagramSocket dso = new DatagramSocket();
					byte[] data = new byte[463];
					DatagramPacket paquet = new DatagramPacket(data,data.length);
					InetSocketAddress ia = new InetSocketAddress("127.0.1.1", 5555);
					System.out.println("Connected in UDP");
					//----------------------sends bytes to client-------------------------
					for(int i=0; i<fileArray.length; i=i+463){
						//copy of range doesn't take the last caracter
						byte[] sub = Arrays.copyOfRange(fileArray, i, i+463);
						paquet = new DatagramPacket(sub, sub.length, ia);
						dso.send(paquet);
					}
					paquet = new DatagramPacket(("Done").getBytes(), 4, ia);
					dso.send(paquet);
				} catch (Exception e) {
					System.out.println("Problem with client.");
				}

			} else {
				System.out.println("File does not exists or it is not a file");
			}
		} catch(Exception e){
			System.out.println(e);
      e.printStackTrace();
		}
	}
}

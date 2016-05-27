import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Scanner;

public class User{

	private static String randomId(){
		String uuid = UUID.randomUUID().toString().substring(0,8);
		return uuid;
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

	public static void showActions(){
		System.out.println("WHOS");
		System.out.println("TEST ring_concerned");
		System.out.println("GBYE");
		System.out.println("DICO word_to_be_defined");
		System.out.println("DATE");
		System.out.println("DIFF message_to_be_transferred");
		System.out.println("TRANS file_needed");
		System.out.println("to shut down: QUIT");
		System.out.println("to show actions again: HELP");
	}

	public static String getDate(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      Calendar cal = Calendar.getInstance();
      String res = dateFormat.format(cal.getTime());
      return res;
	}

	public static void main(String[] args) {
		try {
			Inet4Address entity_address;
			int entity_port;
			System.out.println("entity_address entity_appli_port");
			Scanner sc = new Scanner(System.in);
			String response = sc.nextLine();
			String[] parts = response.split(" ");
			entity_address = (Inet4Address) InetAddress.getByName(parts[0]);
			entity_port = Integer.parseInt(parts[1].trim());
			showActions();
			while(true){
				response = sc.nextLine();
				//construction du message de l'application
				parts = response.split(" ", 2);
				String idm = randomId();
				String res = "";
				switch(parts[0]){
					case "QUIT":
						System.exit(0);
						break;
					case "WHOS":
						res = "GEST WHOS";
						break;
					case "GBYE":
						res = "GEST GBYE";
						break;
					case "TEST":
						res = "GEST TEST "+parts[1].trim();
						break;
					case "DICO":
						String id_dico = randomId();
      				res = "APPL "+idm+" DICO#### REQ "+formatInt(parts[1].trim().length(),3)+" "+parts[1].trim()+" "+id_dico;
						break;
					case "DIFF":
						String content = "";
						if(parts[1].trim().length()>485){
							content = parts[1].substring(0,486);
						} else {
							content = parts[1].trim();
						}
        				res = "APPL "+idm+" DIFF#### "+formatInt(content.length(),3)+" "+content;
						break;
					case "DATE":
						String date = getDate();
						res = "APPL "+idm+" DATE#### "+formatInt(date.length(),3)+" "+date;
						break;
					case "TRANS":
						res = "APPL "+idm+" TRANS### REQ "+formatInt(parts[1].trim().length(),2)+" "+parts[1].trim();
						break;
					case "HELP":
						showActions();
						break;
					default:
						res = "PROBLEM";
						break;
				}
				if(!res.equals("PROBLEM")){
					//connexion au serveur TCP de l'entité
					Socket sock = new Socket(entity_address, entity_port);
					PrintWriter pw = new PrintWriter(
					new OutputStreamWriter(
					sock.getOutputStream()));
					//sends message to be diffused to the ring
					pw.print(res);
					pw.flush();
					//System.out.println(res);
					pw.close();
					sock.close();
				}
			}
		} catch(Exception e){
		System.out.println(e);
		e.printStackTrace();
		}
	}
}
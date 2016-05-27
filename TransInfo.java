import java.util.Scanner;
import java.io.File;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.nio.ByteBuffer;

public class TransInfo{
	private String filename;
	private int max_mess;
	private byte[][] file_parts;
	private String id_trans;
	private boolean waiting_sen;
	private int received;

	public TransInfo(String name){
		this.filename = name;
		this.max_mess = 0;
		this.id_trans = "";
		this.file_parts = null;
		this.waiting_sen = false;
		this.received = 0;
	}

	public void init(String st, Debug debug) throws Exception {
		String[] parts = st.split(" ");
		debug.display("ROK recu par client : "+st);
		if(checkFormatSEN(parts)){
			this.max_mess = Integer.valueOf(parts[7].trim());
			this.id_trans = parts[4];
			this.file_parts = new byte[max_mess][463];
			this.waiting_sen = true;
			debug.display("init should be okay");
		} else {
			throw new Exception("Format du message de transfert (ROK) incorrect");
		}
	}

	private boolean checkFormatSEN(String[] parts){
		if(parts.length==8){
			//à completer
			return true;
		}
		return false;
	}

	private boolean checkFormatROK(String[] parts){
		if(parts.length==8){
			//à completer
			return true;
		}
		return false;
	}

	public void insert_message(ByteBuffer buff, Debug debug) throws Exception {
		String mess = new String(buff.array(),0,buff.array().length);
		String[] parts = mess.split(" ", 8);
		if(checkFormatSEN(parts)){
			int i = Integer.valueOf(parts[5]);
			debug.display("inserting message n°"+i);
			byte[] sub = Arrays.copyOfRange(buff.array(), 49, buff.array().length);
			if(i==(max_mess-1)){
				debug.display("reallocating : "+Integer.toString(buff.array().length-49));
				file_parts[i] = new byte[Integer.valueOf(parts[6])];
			}
			file_parts[i] = sub;
			received++;
		} else {
			throw new Exception("Format du message de transfert (SEN) incorrect");
		}
	}

	public void copy_file(Debug debug){
		try{
			FileOutputStream fos = new FileOutputStream(("./received/"+filename), false);
			debug.display("Start copying file to directory");
			for(int i=0; i<max_mess; i++){
				debug.display(i+"/"+(max_mess-1));
				fos.write(file_parts[i]);
				fos.flush();
			}
			debug.display("Copy over");
			fos.close();
	    } catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
	    }
	}

	public boolean isFull(){
		return this.received == this.max_mess;
	}

	public String getIdTrans(){
		return this.id_trans;
	}

	public String getFilename(){
		return filename;
	}

	public boolean isWaitingSen(){
		return waiting_sen;
	}

}
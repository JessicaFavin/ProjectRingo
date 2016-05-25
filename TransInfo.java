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

	public TransInfo(String name){
		this.filename = name;
		this.max_mess = 0;
		this.id_trans = "";
		this.file_parts = null;
		this.waiting_sen = false;
	}

	public void init(String st, Debug debug) throws Exception {
		String[] parts = st.split(" ");
		System.out.println("ROK recu par client : "+st);
		if(checkFormat(parts)){
			//max_mess still needs to be fixed
			this.max_mess = 0; //Integer.parseInt(parts[7]);
			this.id_trans = parts[4];
			this.file_parts = new byte[max_mess][512];
			this.waiting_sen = true;
		} else {
			throw new Exception("Format du message de transfert (SEN) incorrect");
		}
	}

	private boolean checkFormat(String[] parts){
		if(parts.length==7){
			//à completer
			return true;
		}
		return false;
	}

	public void insert_message(ByteBuffer buff, Debug debug) throws Exception {
		String mess = new String(buff.array(),0,buff.array().length);
		String[] parts = mess.split(" ");
		if(checkFormat(parts)){
			int i = Integer.parseInt(parts[5]);
			debug.display("inserting message n°"+i);
			byte[] test = Arrays.copyOfRange(buff.array(), 0, 50);
			mess = new String(test,0,test.length);
			debug.display("Should be the beginning of the message :\n"+mess);
			byte[] sub = Arrays.copyOfRange(buff.array(), 50, buff.array().length);
			file_parts[i] = sub;
		} else {
			throw new Exception("Format du message de transfert (SEN) incorrect");
		}
	}

	public void copy_file(Debug debug){
		try{
			FileOutputStream fos = new FileOutputStream(("./received/"+filename), true);
			debug.display("Start copying file to directory");
			for(int i=0; i<max_mess; i++){
				debug.display(i+"/"+max_mess);
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
		int i=0;
		for (byte b[] : this.file_parts){
			if (b!=null) i++;
		}
		this.waiting_sen = false;
		return i == this.max_mess;
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
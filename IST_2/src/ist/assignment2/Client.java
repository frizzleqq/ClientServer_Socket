/*
 * Florian Fritz
 * 0800640
 * IST - Assignment 2 
 */

package ist.assignment2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class OutputThread extends Thread{
	
	private DataOutputStream output = null;
	
	InputStreamReader isr = null;
    BufferedReader br = null;
	
	public OutputThread(DataOutputStream output){
		this.output = output;
		
		isr = new InputStreamReader(System.in);
		br = new BufferedReader(isr);
	}
	
	public void run(){
		while(true){
			try {
				output.writeUTF(br.readLine());
			} catch (IOException e) {
				System.out.println("Sending aborted.");
				break;
			}
		}	
	}
}

class InputThread extends Thread{
	private DataInputStream input = null;
	String line = "";
	
	public InputThread(DataInputStream input){
		this.input = input;
	}
	
	public void run(){
		while(true){	
			try {
				line = new String(input.readUTF());
				System.out.println(line);
				Thread.sleep(500);
			} catch (IOException | InterruptedException e) {
				System.out.println("Listening aborted.");
				break;
			}	
		}
	}
}

public class Client {

	public static void main(String[] args){
		
		Socket client = null;
		DataOutputStream output = null;
		DataInputStream input = null;		
		
		try {
			client = new Socket("localhost", 1254);
			output = new DataOutputStream(client.getOutputStream());
			input = new DataInputStream(client.getInputStream());
			
		} catch (IOException e) {
			System.out.println(e);
		}
		
		if(client != null && output != null && input != null){
			
			System.out.println("Client startet.");
			
			InputThread input_t = new InputThread(input);
			OutputThread output_t = new OutputThread(output);
			
			output_t.start();
			input_t.start();
			
			try{
				input_t.join();
			} catch (InterruptedException e){
				System.out.println(e);
			}
			
			output_t.interrupt();
			
			try {
				output.close();
				input.close();
			    client.close();
			} catch (IOException e) {
				System.out.println("error close" + e);
			}	  		
		}	
		System.out.println("The End!");
		System.exit(0);

	}

}

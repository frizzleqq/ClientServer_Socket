/*
 * Florian Fritz
 * 0800640
 * IST - Assignment 2 
 */

package ist.assignment2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class HelpServer extends Thread{
	
	public boolean running = false;

    private Socket socket = null;
    
    private DataOutputStream output = null;
    private DataInputStream input = null;
    
    BufferedReader b = null;
    
    private static int counter = 1;
    private int number;
    
    public HelpServer(Socket socket) {

        super("HelpServer");  // was genau macht das?
        this.socket = socket;
        number = counter;
        counter++;
        
        try {
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());  //vielleicht mit bufferedreader machen BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println(e);
		}

    }

    public void run(){
    	
    		System.out.println("Client" + number + " connected.");
    		
    		running = true;
    	
    		String request;
			try {
				while(running){
					String answer = null;
					request = new String(input.readUTF());
					System.out.println("Client" + number + " request: " + request);
					
					String [] split = request.split(" ");
					
					if(split[0].equals("liststudents"))
						answer = liststudents();
					else if(split[0].equals("getstudentid"))
						answer = SearchID(request);
					else
						answer = "Bad Request!";
					
					output.writeUTF(answer);
				}
			} catch (IOException e) {
				System.out.println("Client" + number + " ended.");
				running = false;
			}			
			
			if(running)
				close();
			
    }
    
    public String liststudents(){
    	
    	try {
			b = new BufferedReader(new FileReader("resources/students.txt"));
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
    	
    	String help = null;
    	String s = "";
    	
    	boolean init = true;  //um erste zeile zu ignorieren
    	
    	try {
			while((help = b.readLine()) != null){
				if(init){
					init = false;
				}
				else{
					String [] split = help.split(" ");
					s = s + split[0] + " " + split[1];
					s = s + "\n";
				}				
			}
		} catch (IOException e) {
			System.out.println(e);
		}
    	
    	try {
			b.close();
		} catch (IOException e) {
			System.out.println(e);
		}
    	
    	return s;
    }
    
    public String SearchID(String request){
    	
    	try {
			b = new BufferedReader(new FileReader("resources/students.txt"));
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
    	
    	String help = null;
    	String s = "";
    	
    	boolean init = true;  //um erste zeile zu ignorieren
    	boolean found = false; // für return bei nicht gefunden
    	
    	try{
    	
    	String [] splitrequest = request.split(" ");
    	
    	request = splitrequest[1] + " " + splitrequest[2];
    	}catch(Exception e){
    		System.out.println(e);
    	}
    	
    	try {
			while((help = b.readLine()) != null){
				if(init){
					init = false;
				}
				else{
					String [] split = help.split(" ");
					s = split[0] + " " + split[1];
					
					if(request.equals(s)){
						s = split[2];
						found = true;
						break;
					}
						
				}				
			}
		} catch (IOException e) {
			System.out.println(e);
		}
    	
    	try {
			b.close();
		} catch (IOException e) {
			System.out.println(e);
		}
    	
    	if(found)
    		return request + " ID: " + s;
    	else
    		return "Error: The student "+ request + " does not exist!";
    }
    
    public void close(){
    	
    	try {
			output.writeUTF("Shutting down...");
		} catch (IOException e) {
			System.out.println("Coult not send shutdown warning.");
		}
    	
    	this.interrupt();
    	
    	try {	
			System.out.println("Client" + number + " disconnected");
			output.close();
			input.close();
			socket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
    	
    	running = false;    	
    }
}

class MainServer extends Thread{
	
	private ServerSocket serverSocket = null;
	
	private HelpServer[] client = null;
	
	private int count = 0;
	
	public MainServer(){
		try {
            serverSocket = new ServerSocket(1254);
            client = new HelpServer[99];   //max 99 clients
        } catch (IOException e) {
            System.err.println("Could not listen on port: 1254");
        }
		
		System.out.println("Start listening...");
	}
	
	public void run(){
		try{
			while(true){
				Socket clientSocket = serverSocket.accept();
				
                client[count] = new HelpServer(clientSocket);
                client[count].start();
                count++;
			}
		} catch (IOException e) {
			System.err.println("Serversocket closed.");
		}  
	}
	
	public void terminate(){
		
		for(int i = 0; i < count; i++){
			if(client[i].running)
				client[i].close();
		}
			
		try {
			Thread.sleep(100);
        	serverSocket.close();
        } catch (IOException | InterruptedException e) {
            System.err.println("Socket-close error.");
        }
	}
	
}

public class Server {

	public static void main(String[] args) {
		
		MainServer server = new MainServer();
		
		server.start();
		
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
		
		server.terminate();
         
	}

}

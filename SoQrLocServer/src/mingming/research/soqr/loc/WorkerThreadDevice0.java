package mingming.research.soqr.loc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 */
public class WorkerThreadDevice0 extends Thread{

	protected Socket clientSocket = null;
	protected String serverText   = null;
	private final WorkerThreadDevice0[] threads;
	private final int connectThreadThreshold = 2; // if these number of clients are connected, then all clients will do sth.
	private int maxClientCount;
	public boolean ReadyToProbe = false;  
	private String msg = ""; 
	
	BufferedReader br = null;
	PrintStream os = null;
	
	public WorkerThreadDevice0(Socket clientSocket, WorkerThreadDevice0[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		this.maxClientCount = threads.length;
	}

	public void run() {
		int maxClientsCount = this.maxClientCount;
		WorkerThreadDevice0[] threads = this.threads;
		
		
		try {
			br  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			os = new PrintStream(clientSocket.getOutputStream());
			System.out.println("Device0(commanding unit)" +" Connected");
			sendMsg2Clients("CONNECTED!!!"); 

			while(true)
			{
			
			  //  if(br != null && br.ready())
				synchronized(this)
				{			
					
			    msg = br.readLine();
				
			    
				if(msg.equals("\\q"))
				{
					System.out.println("client wants to quit...");
					break;
				}
				
				if(msg.equals("probe"))
				{
					//notify(); 
					System.out.println("probe activated");
					ReadyToProbe = true; 
					msg = ""; 
					notify(); //used in couple with wait() 
					//break; 
				}
				 if(!msg.equals("")) System.out.println("client: " + msg);
				 
				 
				}
				
				WorkerThreadDevice0.sleep(20); //temperoraliy reqlinquish the control
			}
            
			br.close();
			os.close();
			clientSocket.close();
			System.out.println("one client is quit");

		} catch (IOException e) {
			//report exception somewhere.
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void sendMsg2Clients(String msg)
	{
		if(os != null)
		{
			os.println(msg);
			os.flush();
		}
	}
}
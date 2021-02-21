package mingming.research.soqr.loc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadServer implements Runnable {

	protected int serverPort = 8080;
	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	protected Thread runningThread = null;
	
	

	public int numClients=0; 
	
	private int maxClientCount = 15;	
	
	//private final WorkerThreadDevice0[] thread0 = new WorkerThreadDevice0[1]; 
		
	private final WorkerThread[] threads = new WorkerThread[maxClientCount];// preallocate 15 instances of the same object
	
	
	public MultiThreadServer(int port)
	{
		this.serverPort = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		synchronized(this)
		{
			this.runningThread = Thread.currentThread();
		}

		openServerSocket();

		while(!isStopped())
		{
			Socket clientSocket = null;

			try {
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			
			int i = 0;
			for(i = 0; i < maxClientCount; i++)
			{
				/*
				if (i==0)
				{
					if (thread0[i]==null)
					{
						(thread0[i] = new WorkerThreadDevice0(clientSocket,thread0)).start();//this thread is for device 0
												
						break;
						
					}
						
				}
				*/
				
				if(threads[i] == null)
				{
					
					(threads[i] = new WorkerThread(clientSocket,threads)).start();
					numClients++; 
					threads[i].threadID = i+1; //assign a ID to each client thread
					
					break;
				}
			}
			
			if(i >= maxClientCount)
			{
				try {
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("Server is too busy now. Try later.");
					os.close();
					clientSocket.close();					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}

		System.out.println("Server is stopped");
	}

	private synchronized boolean isStopped(){
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void openServerSocket(){
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMsg2Clients(String msg)
	{
		for(int i = 0; i < threads.length; i++)
		{
			if(threads[i] != null)
			{
				threads[i].sendMsg2Clients(msg);
			}
		}
	}
	

	public void receiveStatus(boolean messageStatus,boolean fileStatus)
	{
		for(int i = 0; i < threads.length; i++)
		{
			if(threads[i] != null)
			{
					
				threads[i].setReceiveStatus(messageStatus,fileStatus); 
				
			}
		}
		
	}
	
	public void setRequiredFileNumber(int fileNumber)
	{
		for(int i = 0; i < threads.length; i++)
		{
			if(threads[i] != null)
			{
				threads[i].setRequiredFileNumber(fileNumber);
				
			}
		}
		
	}
	
	/*
	//initiate a separate thread
	public  boolean ReadyToProbe()
	{
		synchronized(thread0[0])
		{
		   while ((thread0[0].ReadyToProbe)==false) 
		   {
	            try { 
	            	System.out.println("waiting on client0");
	                thread0[0].wait();
	            } catch (InterruptedException e)  {
	            	System.out.println("thread interrupted");
	               // Thread.currentThread().interrupt(); 
	            }
	            
	        }
		   System.out.println("ReadyToProbe set to true");
		   thread0[0].ReadyToProbe=false; //set ready probe back to false
	        return true;
		}
		
	}
	*/
	
   public boolean ReadyToCalc()// need more work
   {
	   //wait until every thread has finished downloading
		for(int i = 0; i < threads.length; i++)
		{
			//System.out.println("Device "+Integer.toString(i+1)+" level 1 reached!!!");	
			if(threads[i] != null) 
			{
			//System.out.println("Device "+Integer.toString(i+1)+" level 2 reached!!!");	
				//System.out.println("This block can be reached");
				synchronized(threads[i]) //synchronized the entire thread object 
				{
					//System.out.println("for some reason this synchronized block was never reached");
					System.out.println("downloadFinish for thread " + (i+1) + threads[i].downloadFinished);
					
					while (threads[i].downloadFinished==false) 
					{
						try {
							System.out.println("waiting on this thread " + Integer.toString(i+1) +" to finish downloading");
							//enable download in here
						    threads[i].setReceiveStatus(true,false); //turn on the message receiver
						    //threads[i].setReceiveStatus(false,false);
							threads[i].wait();
						} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					//	sendMsg2Client0("Device "+ (i+1) + "not responding!");	
						System.out.println("The download process has been interrupted");
						
						e.printStackTrace();
						}
					}
				}
				
				threads[i].downloadFinished=false; 	 
				System.out.println("*****Thread " + Integer.toString(i+1)+" has finished downloading!!!!");
				
	    	}			   
	 
	   }
		
		return true; 
   }
   
   public void setDownload(boolean download)
	{
		for(int i = 0; i < threads.length; i++)
		{
			if(threads[i] != null)
			{
				threads[i].downloadFinished = false; 
				
			}
		}
		
	}
  	
	/*
	public void sendMsg2Client0(String msg)
	{
		
		thread0[0].sendMsg2Clients(msg);
			
	}
	
	*/
	

}

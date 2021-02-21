package mingming.research.soqr.loc;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.SystemException;

/**

 */
public class WorkerThread extends Thread{

	protected Socket clientSocket = null;
	protected String serverText   = null;
	private final WorkerThread[] threads;
	private final int connectThreadThreshold = 2; // if these number of clients are connected, then all clients will do sth.
	private int maxClientCount;
	private int FileCounter=1;//initialize file counter to be 1 
	private int fileSize=0; 
	private int requiredFileNum = 999; //need to be manually calibrated
	
	private String date = "dataCollectionDistanceTestNew"; 
	private String audioType = "FS"; 
	private String duration = "1"; 
	private String distance = "No_distance";
	private String angle = "No_degree";
	private String volume = "100%"; 
	
	
	
	public boolean ReceiveMessage; 
	public boolean ReceiveFile; 
	public boolean downloadFinished=false; 
	public int threadID; 
	
	BufferedReader br = null;
	PrintStream os = null;
	ObjectInputStream ois  = null; 
	InputStream inputStream = null; 
	
	public WorkerThread(Socket clientSocket, WorkerThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		this.maxClientCount = threads.length;
	}

	public void  run() {
		int maxClientsCount = this.maxClientCount;
		WorkerThread[] threads = this.threads;
		
		int sleepTime = 5000; 
		
		try {
			inputStream = clientSocket.getInputStream(); 
			br  = new BufferedReader(new InputStreamReader(inputStream));
			os = new PrintStream(clientSocket.getOutputStream());
			//System.out.println("Bytes to be read at run(): " + Integer.toString((clientSocket.getInputStream()).available()));
			System.out.println("Device " +Integer.toString(threadID)+" Connected");
			sendMsg2Clients("CONNECTED!!!"); 
			//ReceiveMessage = false; //initialize receive message to true
			//ReceiveFile = false;//
			
			while(true)
			{
				
				/*
				 * 
				int cnt = 0;
				for(int i = 0; i < threads.length; i++)
				{
					if(threads[i] != null)
						cnt++;
				}
				if(cnt >= connectThreadThreshold)
				{
					// tell all connected client to prepare to do their job 
					os.println("1");
					os.flush();
				}
				*/
		
				//System.out.println();
				
				char[] msg = new char[7];
				char[] msg2 = new char[7];
			
				//System.out.print("...Device " + threadID + " is running..." );
				
				WorkerThread.sleep(sleepTime);//pause the thread for a bit
			    //if(br != null && br.ready()&&ReceiveMessage==true)
			    if(ReceiveMessage==true)
			    {
			    	System.out.println("Device "+Integer.toString(threadID)+" Ready to reading message");
			    	//if(br != null && br.ready())
			    	//{
				 			    	
			    			int charRead = br.read(msg,0,7);// blocks until size has been read			
			    			
			    			//System.out.println("charRead: "+Integer.toString(charRead));
			    			//System.out.println("Message received");
			    			//System.out.println("msg: "+ String.valueOf(msg));
			    	//}
			    			msg2 = msg; 
			    			
			    		//	System.out.println("The msg is: "+ msg);;
			    	
			               fileSize = Integer.parseInt(String.valueOf(msg2,0,6)); //the last character is /n
			    	//System.out.println("msg: "+msg);
			    	
			    	  //  System.out.println("File length received: "+fileSize+" bytes");
			    	sendMsg2Clients("ready");// tell the client the file receiver is ready to receive files
			    	
			    	ReceiveMessage=false; 
			    	ReceiveFile=true; 
			    	sleepTime = 10; 
			    	
			    }
			
			
				if (ReceiveFile==true)
				{  
					//System.out.println("Start File Reading");			 
					
					//put a guard in here so it won't receive 
					if (FileCounter <= requiredFileNum) {
					
						//System.out.println("FileCounter: "+FileCounter + " requiredFileNum: " + requiredFileNum + "device " + Integer.toString(threadID));
						 receiveFile2();// file receiver on stand by
					}
					
					ReceiveMessage = true;  
					ReceiveFile = false;
					sleepTime = 10; 
			    	
			    }
				  
				  
					if(msg.equals("quit"))
					{
						System.out.println("client wants to quit...");
						break;
					}
					
					  if(!msg.equals(""))
					  {

						  //  System.out.println("Read from Buffer: " + msg);
					  }
					  
					  synchronized(this)
					  {
					  
						  if (FileCounter > requiredFileNum) 
						  {
							downloadFinished=true; 
						  // System.out.println("FileCounter: "+FileCounter); 	
						   //FileCounter=1;// reset file Counter after all the files has been downloaded
						   notify(); 
						   ReceiveMessage = false; //this thread no longer receives message after finishing downloading all the files
						   ReceiveFile = false;//this thread no longer receives file after finishing downloading all the files
						   sleepTime = 5000;//sleep 5 seconds
						  }
					  }
				//	 System.out.println("FileCounter: "+FileCounter); 
			}
			
            
		     br.close();
			 os.close();
		     clientSocket.close();
			 System.out.println("one client has quit");

		} catch (IOException e) {
			//report exception somewhere.
			System.out.println("Jesus1");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Jesus2");
			e.printStackTrace();
		}

	
	}
	
	
	
	public void sendMsg2Clients(String msg)
	{
		if(os != null)
		{
			
			if (msg.equals("stop")) FileCounter=1; //reset the file counter is the command is "stop"
			
			os.println(msg);
			os.flush();
		}
	}
	
	
	public void receiveFile2() throws IOException {
		   int bytesRead=0;
		    int current = 0;
		    FileOutputStream fos = null;
		    BufferedOutputStream bos = null;
		    //Socket sock = null;
		    try {
		     // sock = clientSocket; 
		      System.out.println("Device "+ Integer.toString(threadID) +" Ready to Receive Data");

		      // receive file
		      //444572
		      //byte [] mybytearray  = new byte [444572];//It's very important to know the size of your file
		    
		      byte [] mybytearray  = new byte [fileSize];
		      
		      InputStream is = inputStream;
		      String fileName = distance+"_"+duration+"_"+angle+"_"+ volume +"_"+Integer.toString(FileCounter++)+".wav"; 
		     
		      //fos = new FileOutputStream(fileName);
		      //fos = new FileOutputStream("myData"+"\\"+fileName);
		      String root = new File(".").getAbsolutePath();//get the current directory
		      File filePath = new File(root+"/data"+"/clustering_data"+"/device"+Integer.toString(threadID)); 
		      filePath.mkdirs();
		      
		      File file  = new File(filePath,fileName);
		      fos = new FileOutputStream(file);
		      bos = new BufferedOutputStream(fos);
		      //bytesRead = is.read(mybytearray,0,mybytearray.length);
		      //bytesRead = is.read(mybytearray);
		      //current = bytesRead;
		     // System.out.println("bytesRead 1st time: "+ Integer.toString(bytesRead));

		        
		      
		      do {
		    	  
		          bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
		          
		         // bytesRead = is.read(mybytearray);
		          if(bytesRead >= 0) current += bytesRead;
		       //  System.out.println("bytesRead: "+Integer.toString(bytesRead));
		       } while(bytesRead !=0);
		         
		      bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
		      
             		    	
		     // System.out.println("current: "+Integer.toString(current));
		    //  System.out.println("mybytearray size:"+Integer.toString(mybytearray.length));
		      		      
		      bos.write(mybytearray, 0 , current);
		      
		   //   System.out.println("bytesRead: "+ Integer.toString(bytesRead));
		      
		      bos.flush();
		     // System.out.println("File " + fileName + " downloaded (" + current + " bytes read)" + "by device "+Integer.toString(threadID));
		    }
		    finally {
		      if (fos != null) fos.close();
		      if (bos != null) bos.close();
		    //  if (sock != null) sock.close();
		      
		      System.out.println("Stream " + Integer.toString(threadID) + " closed");// stream closed means one file has finshed downloading
		    }
		  }
	  

		public void setReceiveStatus(boolean messageStatus,boolean fileStatus)
		{
			
				ReceiveMessage = messageStatus;
				ReceiveFile = fileStatus; 
			
		}
		
		public void setRequiredFileNumber(int fileNumber)
		{
			
			requiredFileNum = fileNumber; 
			
		}
}
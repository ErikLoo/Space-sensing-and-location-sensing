package mingming.research.soqr.loc;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.write.Number; 

//this project is clustering phones
//you first connect device 0
//you then connects other devices

public class MainEntrance {

	public static MultiThreadServer server = null;
	private static boolean calc_distance = false; 
	
	//for outputing the data to excel
	private static WritableWorkbook mWorkbook;  
	private static WritableSheet sheet;  
	private static int rowCounter=1; //initialize row counter to 1
	private static int probeCounter=0; 
	private static int probeNumber = 0;
	
	public static void main(String[] argv) throws EngineException, IllegalArgumentException, IllegalStateException, InterruptedException
	{
		server = new MultiThreadServer(9000);
		new Thread(server).start();

		System.out.println("Server starts now\n");
		
		
		MatlabEngine matEng = MatlabEngine.startMatlab(); //this command delays the start of the server for some reason
		System.out.println("MATLAB Engine initialized");
		

		String root = new File(".").getAbsolutePath();//get the current directory
		
		String config = "142-3";
		String date = "2-11"; 
		
		String fileName = root+"/data"+"/"+config+" "+date+".xls";
		
		generateTheExcelFile(fileName); //generate the excel file for data write up if the existing one was not found
		
		System.out.println("Please connect all the devices and press enter to continue");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String status = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		while(true)
		{
			//String data = server.command(); 
			//you can by pass this unit
			//boolean ReadyToProbe = server.ReadyToProbe(); // wait for device 0 to send a "Probe" command
			final Timer timer = new Timer();//create a timerOject which runs on a different thread
			//create a probing status object
			
			if(probeNumber==0)
			{
				if (probeCounter!=0) {
					//server.sendMsg2Client0("Write to Excel");
					System.out.println("Would you like to write to excel (y/N)?");
					String reply = ""; 
					try {
						reply = br.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (reply.equals("y"))
					{
					System.out.println("write to excel and ... done..Excel file close");;
					closeExcel();//close the excel file
					break; 
					}
				}
				
				System.out.println("You have probed "+ probeCounter + " times");
				System.out.println("Please enter the number of probings you want");
				
				
				try {
					probeNumber = Integer.parseInt(br.readLine());//get the number of probings from console
					//server.ReadyToProbe(); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				//probeNumber = 50; 
				
			//	server.ReadyToProbe(); //receive probe command from command unit 0
				
			}
			else //if probeNumber is not zero, start the next probe task
			{
				
				//use a synchronized block
				//wait until notify
				synchronized(server)
				{
					System.out.println("probeNumber: " + probeNumber);
					System.out.println("Waiting on probe # " + (probeCounter+1) + " to finish");
					server.wait(); //wait on timer to notify
				}
			
			}
					
		//	System.out.println("data: "+ data);
			
			

				//start the probing process
			//server.receiveMessage(true);//set the message receiver to stand by	
				if(probeNumber!=0) // the last one does not count
				{
					server.setRequiredFileNumber(server.numClients); // set the number of file to be received by each thread
					//server.receiveStatus(true,false);// set file receiver to stand by
				
			// the server runs on a different thread so it won't interfere with the main thread
			      // commandsFromTimer();
					System.out.println("Number of clients: "+Integer.toString(server.numClients));
					commandsFromTimer(matEng, timer);//probe command
				}	
			//after the beeping stops calculate the distance; 
			       
			
		//	else
		//	{
		//	System.out.println("Invalid Command");
		//	}
		}
		server.stop();
		
		System.out.println("Server stops now.");
		
		Toolkit.getDefaultToolkit().beep();//Computer system beep
	}
	
	
	public static void commandsFromTimer(MatlabEngine matEng,final Timer timer)
	{
		server.sendMsg2Clients("probing");
       // final Timer timer = new Timer();
        //note this timer class is essentially another thread
        timer.scheduleAtFixedRate(new TimerTask() {
            int i = 6*server.numClients-1; // 3 minutes = 180 seconds
            int size = server.numClients; //allocate space for array
            public void run() {
            	
            	
                System.out.println(i);
                	
                i--;
                
                if (i==-1)
                {
      
                	timer.cancel();
                   // server.receiveFile2();    
                    server.setDownload(false);//close the message receiver and file downloader; 
                    server.sendMsg2Clients("stop");//afater receive stop command device will send data
                  //  server.sendMsg2Client0("Files downloaded");
                    //insert a piece of function to compute the distance
            		Toolkit.getDefaultToolkit().beep();
            		
            		server.ReadyToCalc(); //waiting on each thread to finish downloading
            		 
            		        			
            		
            		System.out.println("*****Every thread has finished downloading!!!");
            		
            	                	
                	
            		      		
            		try {
            		//	server.sendMsg2Client0("Waiting on clustering probe " + (probeCounter+1));
						TimeUnit.SECONDS.sleep(3);// have to pause for 2 seconds

					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            		
            				try {
            					CalculateDistance(matEng,size);
            					System.out.println("distance calculation successful");
            					//send to device0 after calculation
            				} catch (InterruptedException e) {
            					// TODO Auto-generated catch block						
            					System.out.println("distance calculation unsuccessful. Recalculating!");
            					e.printStackTrace();
						
            				}
            		
            		probeCounter++; 
        			probeNumber--; 
            				
            		synchronized(server)
            		{
            			System.out.println("end of the timer!");
            			server.notify(); 
            		}
            			
            				
                	}
            }
          
            
        }, 0, 1000);
        
		
	}
	
	
	public static void commandsFromConsole() //This method is not operational for some reason
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			try {
				String data = br.readLine();
				
				if(br.equals("\\q"))
					break;
				else
				{
					server.sendMsg2Clients(data);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void CalculateDistance(MatlabEngine matEng,int size) throws InterruptedException
	{
		
		//double distance = 0;
		int[] index = new int[size*2]; 
		double[] temp; 
		int[] cluster = new int[size]; 
		int[] device_order = new int[size]; 
		boolean distance_calculation = false; 
		
		
		while (distance_calculation==false)
		{
			
		try {
			//distance = matEng.feval("compute_distance");
			//distance = matEng.feval("solr_distance_compute_modified");//need to change to modified version
			temp = matEng.feval("solr_distance_grouping_fun"); // get results from MATLAB
			
			//convert double array to int arry
			for(int i=0;i<temp.length;i++)
			{
				index[i] = (int)temp[i]; 
			}
			
			System.out.println("distance calculation successful");
			distance_calculation = true; 
		} catch (RejectedExecutionException e1) {
			// TODO Auto-generated catch block
			System.out.println("distance calculation unsuccessful. Recalculating!1");
		//	server.sendMsg2Client0("Error1"); 
			closeExcel();//close the excel file if running into an error
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("distance calculation unsuccessful. Recalculating!2");
			//server.sendMsg2Client0("Error2"); 
			closeExcel();//close the excel file if running into an error
			distance_calculation = false; 
			
			try {
				TimeUnit.SECONDS.sleep(2);// have to pause for 2 seconds

			} catch (InterruptedException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		}  
		
		}
		
		cluster = Arrays.copyOfRange(index, 0, size);
		device_order = Arrays.copyOfRange(index, size, size*2);
		
		System.out.println("Index is : "+Arrays.toString(cluster));
		//server.sendMsg2Client0("Clustering: "+Arrays.toString(index)); 
		displayMessageOnDevice(cluster,device_order); 
		
		
	}
	
	public static void displayMessageOnDevice(int[] cluster,int[] device_order)
	{
		String message1 = "";
		String message2 = ""; 
		//int increment = 0; 
		for(int i = 0; i < cluster.length; i=i+1)
		{
			if(cluster[i]==cluster[0])//data[i] can either be 1 or 2. Device 1 is the target and it is always in the 1st cluster
			{
				message1 = message1 + device_order[i];
			}
			else 
			{
				message2 = message2 + device_order[i];
			}
			
		}
	//	server.sendMsg2Client0("Cluster 1:"+ message1); 
		//server.sendMsg2Client0("Cluster 2:"+ message2 ); 
		
		System.out.println("Cluster 1:"+ message1);
		System.out.println("Cluster 2:"+ message2);
		
		//need make this function output some excel
		writeExcel(Integer.parseInt(message1), 0, rowCounter); //write cluster 1 to excel
		writeExcel(Integer.parseInt(message2), 1, rowCounter); //write cluster 2 to excel
		rowCounter++; 
		
		
	}
	
	
	public static void writeExcel(int values, int col, int row)
	{
		Number val = new Number(col,row,values); 
		
		
			try {
				sheet.addCell(val);
			//	mWorkbook.write();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
	
	}
	
	public static void closeExcel()
	{
		try {
			mWorkbook.write();// you only write to file once
			mWorkbook.close(); //you only close the file once
		} catch (IOException | WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void generateTheExcelFile(String fileName)
	{
		
		try {
			mWorkbook = Workbook.createWorkbook(new File(fileName));
			sheet = mWorkbook.createSheet("Sheet1",0);
			
			//adding a label
			Label labelC1 = new Label(0,0,"Cluster 1");
			Label labelC2 = new Label(1,0,"Cluster 2");

			sheet.addCell(labelC1);
			sheet.addCell(labelC2);
			
			System.out.println("Excel file "+ fileName +" has been generated!!!");
			
			
		} catch (IOException | WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
	
	public static boolean openExcelFile(String fileName)
	{
		
		
		try {
			Workbook myWorkbook = Workbook.getWorkbook(new File(fileName));
			 mWorkbook = Workbook.createWorkbook(new File(fileName), myWorkbook);//copy the material to writableworkbook
			 System.out.println(fileName + " has been opend!");
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("input file does not exist. Generate new one");
			return false; 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("workbook does not exist. Generate new one");
			return false;
		}
		
		
		return true; 
	}
	

}

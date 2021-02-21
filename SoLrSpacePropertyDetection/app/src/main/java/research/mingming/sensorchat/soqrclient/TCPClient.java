package research.mingming.sensorchat.soqrclient;

/**
 * Created by Mingming on 3/3/2016.
 */

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPClient {

    private String serverMessage;
    private String readyToSend;
    public static String SERVERIP = "100.64.167.4"; //your computer IP address. Forgot to change?
    public static final int SERVERPORT = 9000;

    public  String clientID;//assign an ID to each object

    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private boolean sendFile = false;
    public boolean testBoolean;

    PrintWriter out; // Writing out
    BufferedReader in; //Writing in

    Socket socket; //declaring the socket

    private OutputStream out2;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            //Socket socket = new Socket(serverAddr, SERVERPORT);
            socket = new Socket(serverAddr, SERVERPORT);//assign values to socket
            //out2 = socket.getOutputStream();//assign values to out2;


            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage + clientID);
                        readyToSend = serverMessage;
                    }
                    serverMessage = null;



                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    //added on 6-26-2018
    //send file to server
    public void sendFile() throws java.io.IOException {

        File file = new File(Environment.getExternalStorageDirectory(), "test.txt");

        if (out2!=null) {
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(bytes, 0, bytes.length);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(bytes);
            out2.flush();
        }
    }

    public  void sendFile2(File myFile) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        Socket sock = null;
        int bytesRead = 0;
        //File myFile = new File(Environment.getExternalStorageDirectory(), "test.txt");
       // File myFile = new File(Environment.getExternalStorageDirectory().toString()+"/probedata","tyu_07-02 14:06:15_No_2.wav");
       // File myFile = new File(Environment.getExternalStorageDirectory(), "test2.txt");
        try {

//            while (true) {
                System.out.println("Waiting...");
                try {
                    sock = socket;
                    System.out.println("Accepted connection : " + sock);
                    // send file
                    byte [] mybytearray  = new byte [(int)myFile.length()];


                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);

                    bis.read(mybytearray,0,mybytearray.length);


                    //bis.read(mybytearray,0,444572); overreading the data will cause exception



                    sendMessage(Integer.toString(mybytearray.length));//this function will send the length of the file to server
                   // sendMessage("abc");

                    System.out.println("file length sent to server");


                    while (sendFile!=true) {

                       // System.out.println("Waiting for server");
                        if (readyToSend!=null) {
                            if (readyToSend.equals("ready")) {
                                sendFile = true; //break the loop if
                                System.out.println("Server ready to receive file");
                            }
                        }
                    }
                    readyToSend = null;
                    sendFile=false;


                    //serverMessage = in.readLine();//wait for message from server this statement will block

                    os = sock.getOutputStream();
                    System.out.println("Sending " + "audio file" + "(" + mybytearray.length + " bytes)");

                   os.write(mybytearray,0,mybytearray.length);
                   os.flush();

                    System.out.println("Done.");
                }
                finally {
                    if (bis != null) bis.close();
                    //if (os != null) os.close();
                    //if (sock!=null) sock.close();
                }
           // }
        }
        finally {
           // if (servsock != null) servsock.close();
        }
    }
    }




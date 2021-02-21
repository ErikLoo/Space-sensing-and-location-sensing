package research.mingming.sensorchat.soqrclient;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import research.mingming.sensorchat.R;
import research.mingming.sensorchat.activeprobing.ExtAudioRecorder;
import research.mingming.sensorchat.activeprobing.Utils;

//this command is just used to test branch

public class MainActivity extends Activity {
    PowerManager.WakeLock wl = null;//A wake lock is to indicate your application needs to have the device stays on
    AudioManager mAudioManager;//Audio manager provides acess to volume and ringer model control
    MediaPlayer mp = null;//can be used to control playback of audio/video files
    ExtAudioRecorder extAudioRecorder = null;// I guess this is for recording audio files
    Timer mTimerPeriodic = null;// used for scheduling tasks for future application
    File myDir;
    String mRecordFileName;  // file name of recorded sound clip
    File mRecordFile = null;
    private int playtime_cnt = 0;
    private int fileCounter ; //This counter is use to sync files between different devices

    private ListView mList;// displays a vertically scrollable collection of views
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPClient mTcpClient;
    private File[] fileArray = new File[40];


    private String currentObjectName = "";

    @Override
    //why is the this onCreate method public? I thought it should be protected
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mywakeuplock");
        wl.acquire();

        arrayList = new ArrayList<String>();

        //By setting variable type to final you are keeping the values constant
        //You are allowed to initialize final variables only once
        //These are interactions with the UI. They are finding the corresponding UI element
        final EditText etIP = (EditText) findViewById(R.id.et_ip);//ip address
        final EditText editText = (EditText) findViewById(R.id.editText);//message
        Button send = (Button) findViewById(R.id.send_button);

        fileCounter = 1;

        //relate the listView from java to the one created in xml
        mList = (ListView) findViewById(R.id.list);// find the corresponding UI element
        mAdapter = new MyCustomAdapter(this, arrayList);//ArrayAdapter is still holding a reference to the orginal list
        //However, it does not know if the you have changed the orginal list in Activity
        mList.setAdapter(mAdapter);// set the data behind this ListView

        //This method handles storage
        if (isExternalStorageWritable()) {
            String root = Environment.getExternalStorageDirectory().toString();
            myDir = new File(root + "/probedata");
            myDir.mkdirs();
        }

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);// Initializing audio manager


        // waiting for send button to be clicked
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String ipaddress = etIP.getText().toString();// waiting for input ip address

             //  ipaddress = "172.20.10.3"; //the ipaddress for my phone
                ipaddress = "100.64.202.175";// the ipaddress of my computer in DGP Lab


                etIP.setText(ipaddress);//clear the input
                //What if ipaddress does not accpet new ipadress
                TCPClient.SERVERIP = ipaddress;// Why not mTCPClient?

                Log.d("DEBUG", "IP: " + TCPClient.SERVERIP);//Log.d sends out a DEBUG log message

                String message = editText.getText().toString();

                currentObjectName = message;


                    new connectTask().execute("");//execute the asychronous task
                    //what if you press send button multiple times which also triggers execute multiple times
                    //parameters of execute() goes to  doInBackground()
                    //add the text in the arrayList
                    arrayList.add("c: " + message);


                //sends the message to the server
                if (mTcpClient != null) {
                   // mTcpClient.sendMessage("damn it");
                  //  sendFileToServer();
                  //  sendFileToServer2();
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();// arrayList, which is now the underlying data of mAdapeter, can be changed by using this function
                editText.setText("");//clear the input
              //  etIP.setText("");//clear the input

            }
        });

    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

//always release resource at the end of the activity's life cycle
    protected void onDestroy() {
        if (wl != null) {
            if (wl.isHeld())
                wl.release();
            wl = null;
        }
        super.onDestroy();
    }

    //AsyncTask run the network operation in the background
    //connectTask is just a subclass of AsynTask
    //Note for some reason some of functions of the AsyncTask subclass are not explicity called
    public class connectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {
        //we would like to maintain the connectino to server in the background
            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    //this method publish (not display) the message to the main UI thread
                    publishProgress(message);
                }
            });

            mTcpClient.clientID = "Client" + currentObjectName;// assign a client ID to each object


            mTcpClient.run();// keep running until the program is terminated



            return null;
        }

        @Override
        //executed on the main UI thread
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();

           // int delay = (currentObjectName-1)*1000;  //create delay for the client

            //if the message is a command, then do the following things
            if (values[0].equals("probing"+"Client"+currentObjectName)) {
                startPeriodProbing();
                fileCounter=1;
            } else if (values[0].equals("stop"+"Client"+currentObjectName)) {
                stopPeriodProbing();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendMultipleFileToServer2(); //send multiple files to server
                    }
                }, 1000);//pause for 1s after the recording before sending files


            }
        }
    }

    protected void startProbing(int type){
        Timer t = new Timer();
        t.schedule(new myTimerTask(), 0);
    }




    protected void startPeriodProbing() {
        if (mTimerPeriodic != null) {
            mTimerPeriodic.cancel();
        }
        Log.d("startPeriodProbing", "before create period timer");
        mTimerPeriodic = new Timer();
        mTimerPeriodic.scheduleAtFixedRate(new myTimerTask(), 0, Utils.repeat_period);
    }


    private void stopPeriodProbing() {
        if (mTimerPeriodic != null) {
            mTimerPeriodic.cancel();
        }
    }

    //set up a timer task for the timer class to excute
    class myTimerTask extends TimerTask {

        @Override
        //The action to be performed by this timer task
        public void run() {
            final Date currentTime = new Date();
            final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);
         mRecordFileName = currentObjectName + "_" + sdf.format(currentTime) + "_" +"Device"+ "_"+Integer.toString(fileCounter) +"beeping"+".wav";
         //mRecordFileName = currentObjectName + ".wav";

          //  mRecordFileName = currentObjectName + "_" + sdf.format(currentTime)+".wav";

            if (isExternalStorageWritable()) {
                mRecordFile = new File(myDir, mRecordFileName);

                Log.d("External Storage","wrtiable");
            }
            else {
                mRecordFile = new File(getApplicationContext().getFilesDir(), mRecordFileName);

                Log.d("External Storage","not wrtiable");
            }

            startRecordSound();// start recording at the same time

          //fileCounter++;
          //  int delayTime =  Integer.parseInt(currentObjectName);
           // int delayTime =  (Integer.parseInt(Utils.targetObject)-1)*3000;//3 second delay

            int delayTime = 1000;

            //if this is the target object, then do the following
            //What if this is not the target object
            if (Utils.targetObject.equals(Integer.toString(fileCounter))) {
                //a bit of explantion would be nice
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSweepSound();
                    }
                }, delayTime);
            }

        }
    }


    private void startRecordSound() {
        // Start recording
        //extAudioRecorder = ExtAudioRecorder.getInstanse(true);	  // Compressed recording (AMR)
        extAudioRecorder = ExtAudioRecorder.getInstanse(false); // Uncompressed recording (WAV)

        extAudioRecorder.setOutputFile(mRecordFile.getAbsolutePath());

       // Log.i("TAG", "after init extAudioRecorder");

        extAudioRecorder.prepare();
       // Log.i("TAG", "after preparing");

        extAudioRecorder.start();
        //Log.d("TAG", "recording start");
        //startTimerTask(Utils.probing_duration);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                     @Override
                                                     public void run() {

                                                         new CountDownTimer(Utils.probing_duration, Utils.probing_duration) {
                                                             public void onTick(long millisUntilFinished) {
                                                             }

                                                             public void onFinish() {
                                                                 StopRecordSound();
                                                                 fileArray[fileCounter-1] = mRecordFile;
                                                                 System.out.println(fileCounter+" th element is " + mRecordFileName );
                                                                 fileCounter++;

                                                                // sendFileToServer2();
                                                                 //after stop recording sound delay 0.5 seconds before sending files
                                                                /*
                                                                 new CountDownTimer(Utils.delay_send, Utils.delay_send)
                                                                 {
                                                                     public void onTick(long millisUntilFinished) {
                                                                     }

                                                                     public void onFinish() {
                                                                         sendFileToServer2();
                                                                     }
                                                                 }.start();
                                                                 */
                                                                 //insert a piece of code that sends the file to server

                                                             }
                                                         }.start();
                                                     }
                                                 }
        );
    }

    private void playSweepSound() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (1*mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
    //mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep20hz20000hz3dbfsdot1s); //full range 0.1sec
     //   mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep20hz20000hz3dbfsdot5s); //full range 0.5sec
    // mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep20hz20000hz3dbfs1s); //full range 1 sec
    // mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep17000hz20000hz3dbfsdot1s);// ultrasonic range 0.1s
        //mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep17000hz20000hz3dbfsdot5s);// ultrasonic range 0.5s
    mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep17000hz20000hz3dbfs1s);// ultrasonic range 1s
       // mp = MediaPlayer.create(getApplicationContext(), R.raw.sweep17000hz22000hz17000hzdot1s);// ultrasonic range 0.2s

        if (mp != null) {
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(final MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    ReleaseMediaPlayer();
                }
            });
        }
    }


    private void ReleaseMediaPlayer() {
        if (mp != null) {
            mp.reset();
            mp.release();
            mp = null;
        }
    }

/*
    private void startTimerTask(int timeinterval)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                new CountDownTimer(timeinterval, timeinterval) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        StopRecordSound();
                    }
                }.start();
            }
        });

    }
*/

    private void StopRecordSound() {
        // Stop recording
        extAudioRecorder.stop();
        extAudioRecorder.reset();
        extAudioRecorder.release();

    }

    //added on 6-26-2018
    //send file to server

  private void sendFileToServer()  {

      try {
          mTcpClient.sendFile();
          Log.e("File", "Sent to server");
      } catch (IOException e) {
          e.printStackTrace();
          Log.e("File", "Fail to send", e);

      }
  }

    private void sendFileToServer2()  {



        try {
           mTcpClient.sendFile2(mRecordFile );
           // mTcpClient.sendFile = false;
           // mTcpClient.sendFile2(fileName);
          //  mTcpClient.sendFile2();

            Log.e("File "+mRecordFileName, "Sent to server");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("File "+mRecordFileName, "Fail to send", e);

        }

    }

    private void sendMultipleFileToServer2()
    {
        int numberOfFiles = fileCounter;

       // System.out.println("The numbers of files are" + numberof);

        for(int i=0; i<numberOfFiles; i++){

            try {
                if (fileArray[i]!=null) mTcpClient.sendFile2(fileArray[i] );
                else {
                    System.out.println("fileArray "+Integer.toString(i+1)+" is null!!! ");
                }
                // mTcpClient.sendFile = false;
                // mTcpClient.sendFile2(fileName);
                //  mTcpClient.sendFile2();

                Log.e("File "+Integer.toString(i+1), "Sent to server");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("File "+Integer.toString(i+1), "Fail to send", e);

            }


        }


    }


}

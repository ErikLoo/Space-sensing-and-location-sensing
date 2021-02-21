/**
 * author: Mingming Fan
   contact at: fmmbupt@gmail.com
 */
package ubicomp.research.mingming.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import ubicomp.research.mingming.fileoperations.FileProcess;
import ubicomp.research.mingming.mymath.MyMath;

public class WekaFeaturesFileCreation {

	
	public static ArrayList<String> ExtractMFCCFeatures(int NumberSamplesPerFrame, int OverlapSamplesNumber, String folder_path, String outputfile, String extension, int ClassIndex, int NumberOfFeatures)
	{
		 ArrayList<String> classes = null;
		 PrintWriter out = null;
		 try {
			 out = new PrintWriter(new FileWriter(folder_path + outputfile));
			 
			 out.println("@relation jAudio");
			 
			 for(int i = 1; i <= NumberOfFeatures; i++ )
			 {
				 out.println("@ATTRIBUTE \"MFCC Overall Average" + i +"\" NUMERIC");
			 }
			 out.println("@DATA");
			 
			 classes = new ArrayList<String>();
			
			 String files;
			 File folder = new File(folder_path);
			 File[] listOfFiles = folder.listFiles();
			 System.out.println("# of files: " + listOfFiles.length);
			 for(int i = 0; i < listOfFiles.length; i++)
			 {
				if(listOfFiles[i].isFile())
				{
					files = listOfFiles[i].getName();
					if(files.endsWith(extension))
					{
						String[] temp = files.split("_");
						String className="";
						if(temp.length > 0)
						{
							className = temp[ClassIndex];
							if(!classes.contains(className))
								classes.add(className);
						}
						
						FeaturesExtractor FE = new FeaturesExtractor(folder_path,new File(folder_path+files));
						FE.Prepare(NumberSamplesPerFrame, OverlapSamplesNumber);
						
						// extract MFCC features
						double[] MFCC = FE.ExtractMFCC(folder_path + outputfile);
						StringBuilder aline = new StringBuilder();
						for(int j = 1; j < MFCC.length; j++)
						{
							aline.append(MFCC[j]+",");
						}
						aline.append(className);
						
						out.println(aline.toString());
					}
				}
			 }
			 
			 out.flush();
			 out.close();
			 			 
		 } catch (IOException e) {
		 	// TODO Auto-generated catch block
		 	e.printStackTrace();
		 }
		 
		 return classes;
	}
	
	//extract decay curve features
	
	public static ArrayList<String> ExtractDecayCurveFeatures(int NumberSamplesPerFrame, int OverlapSamplesNumber, String folder_path, String outputfile, String extension, int ClassIndex, double sweepDuration,int _WhichMethod2UseForModelNoise, int _useSoundPieceBeforeSweep, int useSoundAtTheEndOfSoundClip, int NoiseDetectionWindowSize, int NumOfStd)
	{
		 ArrayList<String> classes = null;
		 PrintWriter out = null;
		 try {
			 out = new PrintWriter(new FileWriter(folder_path + outputfile));
			 	// class information will be extracted from the file's name		 
			 classes = new ArrayList<String>();
			 boolean addwekafileheader = false;
			 
			 String files;
			 File folder = new File(folder_path);
			 File[] listOfFiles = folder.listFiles();
			 System.out.println("# of files: " + listOfFiles.length);
			 for(int i = 0; i < listOfFiles.length; i++)
			 {
				if(listOfFiles[i].isFile())
				{
					files = listOfFiles[i].getName();
					if(files.endsWith(extension))
					{
						String[] temp = files.split("_");
						String className="";
						if(temp.length > 0)
						{
							className = temp[ClassIndex];
							if(!classes.contains(className))
								classes.add(className);
						}
						
						FeaturesExtractor FE = new FeaturesExtractor(folder_path,new File(folder_path+files));
						
						double samplingRate = FE.Prepare(NumberSamplesPerFrame, OverlapSamplesNumber);
						
						//extract decay curve features;
						int[] DecayCurveInFrames = FE.ExtractDecayCurveFeatures(folder_path, sweepDuration, _WhichMethod2UseForModelNoise, _useSoundPieceBeforeSweep, useSoundAtTheEndOfSoundClip, NoiseDetectionWindowSize, NumOfStd);
						
						if(Constants.Debug == 1)
							FileProcess.SaveArray2File(folder_path, "decay"+"_"+i+".csv", DecayCurveInFrames);
						
						
						// mean-filter smoothing
						int mean_smooth_win_size = 10;
						
						int[] mean_smooth_DecayCurveIndexs = MyMath.smooth_MeanFilter(DecayCurveInFrames, mean_smooth_win_size);
						if(Constants.Debug == 1)
							FileProcess.SaveArray2File(folder_path, "decay_mean_smoothed"+"_"+i+".csv", mean_smooth_DecayCurveIndexs);							
						
						/*
						//median-filter smoothing
						int  median_smooth_win_size = 10;
						int[] median_smooth_DecayCurveIndexs = MyMath.smooth_MedianFilter(DecayCurveInFrames, median_smooth_win_size);
						if(Constants.Debug == 1)
							FileProcess.SaveArray2File("E:\\SoundData\\test\\", "decay_median_smoothed.csv", median_smooth_DecayCurveIndexs);							
				        */
						
						/*
						//kalman filter smoothing
						MyMath.smooth_KalmanFilter(DecayCurveIndexs);
						if(Constants.Debug == 1)
							FileProcess.SaveArray2File("E:\\SoundData\\test\\", "decay_kf_smoothed.csv", DecayCurveIndexs);
						*/
						
						//find local maximum of the decay curve
						ArrayList<Integer> indexs = MyMath.findLocalMaximum(mean_smooth_DecayCurveIndexs);
						if(Constants.Debug == 1) 
							FileProcess.SaveArray2File(folder_path, "decay_local_max_indexs"+"_"+i+".csv", indexs);
						
						double[] DecayCurveInTime = new double[DecayCurveInFrames.length];
						double timeInterval = (NumberSamplesPerFrame - OverlapSamplesNumber) / samplingRate; 
						for(int k = 0; k < DecayCurveInFrames.length; k++)
						{
							DecayCurveInTime[k] = (double)DecayCurveInFrames[k] * timeInterval;
						}
																		
						// normalized features;
						MyMath.normalization(DecayCurveInTime);
												
						StringBuilder aline = new StringBuilder();
						
						for(int j = 0; j < DecayCurveInTime.length; j++)
						{
							//System.out.println(DecayCurveInTime[j]+",");
							aline.append(DecayCurveInTime[j]+",");
						}
						aline.append(className);
						
						//begin create weka file
						//header only need to add once
						if(!addwekafileheader)
						{
							out.println("@relation jAudio");
							 
							for(int l = 1; l <= NumberSamplesPerFrame / 2; l++ )
							{
								out.println("@ATTRIBUTE \"Decay Curve Features " + l +"\" NUMERIC");
							}
							out.println("@DATA");
							addwekafileheader = true;
						}
												
						out.println(aline.toString());						
					}
				}
			 }			 
			 out.flush();
			 out.close();
			 			 
		 } catch (IOException e) {
		 	// TODO Auto-generated catch block
		 	e.printStackTrace();
		 }
		 
		 return classes;
	}
	
	
	public static void AddClassesLabel2FeaturesFile(String inputfile, String outputfile, int skipNumOfLines, ArrayList<String> classes)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(outputfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try{
			FileInputStream fstream = new FileInputStream(inputfile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String str1="";
			
			int i = 0;
			while(i < skipNumOfLines)
			{
				str1 = br.readLine();
				out.println(str1);
				i++;
			}
			
			out.print("@ATTRIBUTE Class {");
			for(String s : classes)
			{
				out.print(s + " , ");
			}
			out.println("}");
			
			str1 = br.readLine();
			while(str1 != null)
			{	
				out.println(str1);
				str1 = br.readLine();
			}
			
			if(in != null)
			{
				in.close();
				in = null;
			}
			
			out.flush();
			out.close();
			
		}catch(IOException e)
		{
			
		}
		
	}
}

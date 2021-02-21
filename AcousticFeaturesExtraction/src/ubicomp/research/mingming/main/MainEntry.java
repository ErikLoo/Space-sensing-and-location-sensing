/**
 * author: Mingming Fan
   contact at: fmmbupt@gmail.com
 */
package ubicomp.research.mingming.main;

import java.awt.Toolkit;
import java.util.ArrayList;

/**
 * @author mingming
 * this java file is used for generating Weka files which are then used for training in Weka 
 *
 */
public class MainEntry {
	
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			 String outputfile = "features2.arff";
			 //String folder_path = "C:\\Users\\mingu\\OneDrive - University of Toronto\\SonicObjectFinding\\Data\\spacetypedata\\";
			// String folder_path = "C:\\Users\\gdsyzx\\Desktop\\Space property at different time intervals in one folder\\";
			 //awalys read the data file to the your working place
			// String folder_path = "C:\\Users\\gdsyz\\Desktop\\2018 Thesis Code\\AcousticFeaturesExtraction\\Space property at different time intervals in one folder\\";
			 //String folder_path = "C:\\Users\\gdsyz\\Desktop\\2018 Thesis Code\\AcousticFeaturesExtraction\\Technical Evaluation 2\\";
			 //String folder_path = "C:\\Users\\gdsyz\\Desktop\\2018 Thesis Code\\AcousticFeaturesExtraction\\Night Data\\";
			 String folder_path = "C:\\Users\\gdsyz\\Desktop\\2018 Thesis Code\\AcousticFeaturesExtraction\\Thesis_data\\";

			 //Space property at different time intervals in one folder
			 
			 //String folder_path = "E:\\SoundData\\test2\\";
			 //String folder_path =  "E:\\SoundData\\AllMixed\\";
	        // String folder_path = "E:\\SoundData\\AcrossDevices\\01302014\\Nexus4\\SineSweep\\";
	        // String folder_path = "E:\\SoundData\\AcrossDevices\\01302014\\GalaxyNexus_Amplified\\";
	         
	         
			 int NumberSamplesPerFrame = 512;
			 float overlapRatio = 0.5f;
			 int OverlapSamplesNumber = (int)(NumberSamplesPerFrame * overlapRatio); //NumberSamplesPerFrame>>1; // 50% overlap
			 
			 //int ClassIndex = 2;  // seperating the file name by "_", the ClassIndex means the sequence of the class string
			 int ClassIndex = 0;  // seperating the file name by "_", the ClassIndex means the sequence of the class string
			 String extension = ".wav";  // file extension to process: here we only process .wav files
			 			 
			 double sweepDuration = 0.1;  //sweep sound duration
			 
			 int _WhichMethod2UseForModelNoise = 1;
			 int _useSoundPieceBeforeSweep = 0;
			 int useSoundAtTheEndOfSoundClip = 1;
			 int NoiseDetectionWindowSize = 5;
			 int NumOfStd = 3;			 
			 Constants.Debug = 1;
			 
			 //ArrayList<String>  classes = WekaFeaturesFileCreation.ExtractDecayCurveFeatures(NumberSamplesPerFrame, OverlapSamplesNumber, folder_path, outputfile, extension, ClassIndex, sweepDuration, _WhichMethod2UseForModelNoise, _useSoundPieceBeforeSweep, useSoundAtTheEndOfSoundClip, NoiseDetectionWindowSize, NumOfStd);
			 //.ExtractDecayCurveFeatures(NumberSamplesPerFrame, OverlapSamplesNumber, folder_path, outputfile, extension, ClassIndex, sweepDuration);
			 //int numberOfFeatures = NumberSamplesPerFrame >> 1; //FFT is symmetric
			 		 		 
			 
			 //for MFCC features:
			 int numberOfFeatures = 12;  //  12 mfcc removed the first one;  13 if mfcc; 4 acoustic features
			 ArrayList<String>  classes = WekaFeaturesFileCreation.ExtractMFCCFeatures(NumberSamplesPerFrame, OverlapSamplesNumber,folder_path, outputfile, extension, ClassIndex, numberOfFeatures);
			 
			 
			 String outputfileWithLabels = "features_with_Classes2.arff";
			 int skiplines = numberOfFeatures + 1;  // +1  because the first line of the weka file is "@jAudio"
			 WekaFeaturesFileCreation.AddClassesLabel2FeaturesFile(folder_path + outputfile , folder_path + outputfileWithLabels, skiplines, classes);
		}
		else
		if(args.length == 1 && args[0].equals("help"))
		{
			System.out.println("=================================================================\n" +
					"An example would be like this: \n"+
					"java -jar AFE.jar  E:\\SoundData\\test\\ 2 1 0 1 5 3\n"+
					"0: String_foler: where your wav audio files are: \n"+
					"1: ClassIndex: the index of the class in the filename by separating using \"_\"\n" +
					"\t e.g. if a sound file name is \"SineSweep_School_Bathroom_14-01-30 180512__.wav\", then ClassIndex is 2 \n"+
					"2: _WhichMethod2UseForModelNoise:\n \t1: Model the noise for each frequency separately;\n \t2: Model the noise using all frequencies; \n" +
					"\t3: Model the noise using frequencies above 3K Hz\n" +
					"\t4: Model the noise using frequencies between 7K and 15K Hz\n" +
					"3: _useSoundPieceBeforeSweep: when modeling the noise, whether to use the sound clip before the sweep starts. 0: not use; 1: use\n" +
					"4: useSoundAtTheEndOfSoundClip: when modeling the noise, whether to use the sound clip right before the whole recording ends. 0: not use; 1: use \n"+
					"5: NoiseDetectionWindowSize: the sliding window size that is used to detect the end of decay curve. e.g. 5 \n" +
					"6: NumOfStd: number of standard deviations to use in modeling noise, e.g. 1 or 3\n" + 
					"=================================================================");
					
		}
		else if(args.length == 7)
		{
			/*
			Constants.Debug = Integer.parseInt(args[0]);
			int NumberSamplesPerFrame = Integer.parseInt(args[1]);;
			int OverlapSamplesNumber = (int)(NumberSamplesPerFrame * Float.parseFloat(args[2]));
			String folder_path = args[3];
			String outputfile = args[4] + ".arff";
			String extension = args[5];
			int ClassIndex = Integer.parseInt(args[6]);
			double sweepDuration = Double.parseDouble(args[7]);
			int _WhichMethod2UseForModelNoise = Integer.parseInt(args[8]);
			int _useSoundPieceBeforeSweep = Integer.parseInt(args[9]);
			int useSoundAtTheEndOfSoundClip = Integer.parseInt(args[10]);
			int NoiseDetectionWindowSize = Integer.parseInt(args[11]); 
			int NumOfStd = Integer.parseInt(args[12]);
			*/
			
			Constants.Debug = 1;
			int NumberSamplesPerFrame = 512;
			int OverlapSamplesNumber = 256;
			String folder_path = args[0];
			String outputfile = "feature.arff";
			String extension = ".wav";
			int ClassIndex = Integer.parseInt(args[1]);;
			double sweepDuration = 0.1;
			int _WhichMethod2UseForModelNoise = Integer.parseInt(args[2]);
			int _useSoundPieceBeforeSweep = Integer.parseInt(args[3]);
			int useSoundAtTheEndOfSoundClip = Integer.parseInt(args[4]);
			int NoiseDetectionWindowSize = Integer.parseInt(args[5]); 
			int NumOfStd = Integer.parseInt(args[6]);
			 
			//ArrayList<String>  classes = WekaFeaturesFileCreation.ExtractDecayCurveFeatures(NumberSamplesPerFrame, OverlapSamplesNumber, folder_path, outputfile, extension, ClassIndex, sweepDuration, _WhichMethod2UseForModelNoise, _useSoundPieceBeforeSweep, useSoundAtTheEndOfSoundClip, NoiseDetectionWindowSize, NumOfStd);
			//.ExtractDecayCurveFeatures(NumberSamplesPerFrame, OverlapSamplesNumber, folder_path, outputfile, extension, ClassIndex, sweepDuration);
			//int numberOfFeatures = NumberSamplesPerFrame >> 1; //FFT is symmetric
			 		 		 
			 
			 //for MFCC features:
			 int numberOfFeatures = 12;  //  12 mfcc removed the first one;  13 if mfcc; 4 acoustic features
			 ArrayList<String>  classes = WekaFeaturesFileCreation.ExtractMFCCFeatures(NumberSamplesPerFrame, OverlapSamplesNumber,folder_path, outputfile, extension, ClassIndex, numberOfFeatures);
			 
			 
			 String outputfileWithLabels = "feature_with_Classes" + ".arff";
			 int skiplines = numberOfFeatures + 1;  // +1  because the first line of the weka file is "@jAudio"
			 WekaFeaturesFileCreation.AddClassesLabel2FeaturesFile(folder_path + outputfile , folder_path + outputfileWithLabels, skiplines, classes);
		}
		else
		{
			System.out.println("Sorry, the number parameters are not correct, please re-enter your commend");
		}

		Toolkit.getDefaultToolkit().beep();//Computer system beep to remind the users that the classification has finished running
		 System.out.println("finished...");
	}


}

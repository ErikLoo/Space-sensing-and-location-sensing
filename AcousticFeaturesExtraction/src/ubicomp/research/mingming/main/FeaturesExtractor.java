/**
 * author: Mingming Fan
   contact at: fmmbupt@gmail.com
 */
package ubicomp.research.mingming.main;

import java.io.File;

import ubicomp.research.mingming.audiofile.WavFile;
import ubicomp.research.mingming.fileoperations.FileProcess;
import ubicomp.research.mingming.soundfeatures.BasicFrequencyAnalysis;
import ubicomp.research.mingming.soundfeatures.DecayCurveFeatures;
import ubicomp.research.mingming.soundfeatures.MFCCCalculator;

/**
 * @author mingming
 *
 */
public class FeaturesExtractor {

	BasicFrequencyAnalysis FreqAnalyzer;
	MFCCCalculator MFCCCal;
	DecayCurveFeatures DCF;
	File mFile;
	double[] buffer;
	double samplingRate = 44100;
	double[][] FFTMags;
	int NumberSamplesPerFrame;
	int OverlapSamplesNumber;
	String folder_path;
	
	public FeaturesExtractor(String _folder_path, File _File)
	{
		folder_path = _folder_path;
		mFile = _File;
	}
	
	public double Prepare( int _NumberSamplesPerFrame, int _OverlapSamplesNumber)
	{
		if(mFile != null)
		{
			try{
		         // Open the wav file specified as the first argument
		         WavFile wavFile = WavFile.openWavFile(mFile);

		         // Display information about the wav file
		         wavFile.display();

		         // Get the number of audio channels in the wav file
		         int numChannels = wavFile.getNumChannels();
		         
		         //grab the sampling rate from the file
		         samplingRate = wavFile.getSampleRate();
		         
		         long nFrames = wavFile.getNumFrames();
		       
		         // Create a buffer of FFT_SIZE frames
		         buffer = new double[(int)nFrames * numChannels];
		         
		         NumberSamplesPerFrame = _NumberSamplesPerFrame;
		         OverlapSamplesNumber = _OverlapSamplesNumber;
		         
		        // System.out.println("NumberSamplesPerFrame: " + NumberSamplesPerFrame + ", OverlapSamplesNumber: " + OverlapSamplesNumber);
		         
		         FreqAnalyzer = new BasicFrequencyAnalysis( NumberSamplesPerFrame, OverlapSamplesNumber,  samplingRate);
		         
		         wavFile.readFrames(buffer, (int)nFrames);
		         
		         FFTMags = FreqAnalyzer.getFFTMagnitudesForEntireSoundClip(buffer,samplingRate);
		         /*
					for(int i = 0; i < FFTMags.length; i++)
					{
						for(int j = 0; j < FFTMags[i].length; j++)
						{
							System.out.print(FFTMags[i][j] + ",");
						}
						System.out.println();
					}
				*/
		         //output FFT magnitudes
		         FileProcess.Save2DArray2File(folder_path, "FFTMags.txt", FFTMags);
		         
		         
		         
		         // Close the wavFile
		         wavFile.close();
			}
			catch(Exception e)
			{
				
			}
		}
		
		return samplingRate;
	}
	
	public double[] ExtractMFCC(String folder)
	{
		double[] MeanMFCC = null;
		if(FFTMags != null)
		{			
			MFCCCal = new MFCCCalculator(samplingRate);
			
			double[][] MFCCs = MFCCCal.process(FFTMags);
			
			FileProcess.Save2DArray2File(folder, "MFCCs.csv", MFCCs);
	         
			
	         MeanMFCC = new double[MFCCs[0].length];
	         
	         //do some aggregation process
	         for(int j = 0; j < MFCCs[0].length; j++)
	         {
	        	 double avg = 0;
	        	 for(int i = 0; i < MFCCs.length; i++)
	        	 {
	        		 avg += MFCCs[i][j];
	        	 }		        	 
	        	 avg /= MFCCs.length;
	        	 MeanMFCC[j] = avg;
	         }
		}
		return MeanMFCC;
	}
	
	public int[] ExtractDecayCurveFeatures(String folder_path, double sweepDuration,int _WhichMethod2UseForModelNoise, int _useSoundPieceBeforeSweep, int useSoundAtTheEndOfSoundClip, int NoiseDetectionWindowSize, int NumOfStd)
	{
		int[] decayIndexs = null;
		
		if(FFTMags != null)
		{
			DCF = new DecayCurveFeatures();
			decayIndexs = DCF.ExtractDecayCuveFeature(folder_path, FFTMags, NumberSamplesPerFrame, OverlapSamplesNumber, samplingRate, sweepDuration, _WhichMethod2UseForModelNoise, _useSoundPieceBeforeSweep, useSoundAtTheEndOfSoundClip, NoiseDetectionWindowSize, NumOfStd);
			//decayIndexs = DCF.ExtractDecayCuveFeature(FFTMags, NumberSamplesPerFrame, OverlapSamplesNumber, samplingRate, sweepDuration);
		}
		//return DecayCurveFeatures.ExtractDecayCuveFeature(FFTMags, NumberSamplesPerFrame, OverlapSamplesNumber, samplingRate, sweepDuration);
		return decayIndexs;
	}
}

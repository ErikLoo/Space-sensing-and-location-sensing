/**
 * author: Mingming Fan
   contact at: fmmbupt@gmail.com
 */
package ubicomp.research.mingming.soundfeatures;

import ubicomp.research.mingming.fileoperations.FileProcess;
import ubicomp.research.mingming.main.Constants;

/**
 * @author mingming
 *
 */
public class DecayCurveFeatures {

	public static double startSweepTime = 0;
	public static double endSweepTime = 0;
	public static int startSweepIndex = 0;
	public static int endSweepIndex = 0;
	public static int[] decayCurveIdx = null;
	
	
	/***
	 * folder_path: where to save the noise data file;
	 * FFTMags:  FFT magnitudes for all frames and all frequencies
	 * frameLength: how many sample per FFT
	 * FrameOverlap: the over lap samples of two windows
	 * samplingRate: sampling rate of the sound clip
	 * sweepDuration: sweep duration in seconds
	 * _WhichMethod2UseForModelNoise: which method to use to model the background noise
	 * _useSoundPieceBeforeSweep: use the sound clip before the sweep to model the noise
	 * useSoundAtTheEndOfSoundClip: use the sound clip right before the end of the whole sound lcip to model the noise
	 * NoiseDetectionWindowSize: the window size used to detect whether a decay curve ends: normally set it as 5 
	 * NumOfStd: Number of standard deviations: normally set as 1 or 3
	 * */
	public int[] ExtractDecayCuveFeature(String folder_path, double[][] FFTMags, int frameLength, int FrameOverlap, double samplingRate, double sweepDuration , int _WhichMethod2UseForModelNoise, int _useSoundPieceBeforeSweep, int useSoundAtTheEndOfSoundClip, int NoiseDetectionWindowSize, int NumOfStd)
	{
    	int sweepWindowSize = (int)((samplingRate * sweepDuration) / (frameLength - FrameOverlap)); // number of frames inside of the sweep
    	
    	//System.out.println("sweepWindowSize: " + sweepWindowSize);
    	double max = -100000;
    	int index = -1; 
    	
    	for(int i = 0; i < FFTMags.length; i++)
    	{
    		double sum = 0;
    		for(int j = i; j < Math.min(i + sweepWindowSize,FFTMags.length); j++)
    		{
    			for(int k = 0; k < FFTMags[j].length; k++)
    			{
    				sum += FFTMags[j][k];
    			}
    		}
    		
    		if(sum > max)
    		{
    			max = sum;
    			index = i;
    		}
    	}    	
    	
    	startSweepIndex = index;
    	startSweepTime = index * (frameLength - FrameOverlap) / samplingRate;
    	if(Constants.Debug == 1)
    	{
	    	System.out.println("Start of sweeping index: " + index);
	    	System.out.println("Start of sweeping time: " + startSweepTime  + " s");
    	}
    	
    	endSweepIndex = index + sweepWindowSize;   	
    	endSweepTime = endSweepIndex * (frameLength - FrameOverlap) / samplingRate;
    	if(Constants.Debug == 1)
    	{
	    	System.out.println("End of Sweeping index: " + endSweepIndex);
	    	System.out.println("End of Sweeping time: " + endSweepTime + " s");
    	}
    	        
    	//only return the lower half, because of the symmetry of the FFT 
    	int decayCurveLength = frameLength / 2;
    	
    	decayCurveIdx = new int[decayCurveLength];
    	
    	for(int i = 0; i < decayCurveLength; i++)
    		decayCurveIdx[i] = endSweepIndex;
    	
    	
        BackgroundNoiseModeling BNM = new BackgroundNoiseModeling();
        
        int WhichMethod2UseForModelNoise = _WhichMethod2UseForModelNoise;
    	int winSize = NoiseDetectionWindowSize;
    	
        switch(WhichMethod2UseForModelNoise)
        {
	        case 1: //Model the noise for each frequency separately;
	            BNM.ModelForEachFrequency(frameLength, FFTMags, index, _useSoundPieceBeforeSweep, useSoundAtTheEndOfSoundClip);
	            double[] BkgNoise =  BNM.getNoiseMean();
	        	double[] BkgNoiseSTD = BNM.getNoiseStd();
	        	
	        	if(Constants.Debug == 1)
	        		FileProcess.SaveArray2File(folder_path,"noise.csv",BkgNoise);
	        	
	        	
	        	/////////////////////////////////////
	    		//detect when the decay curves disppear 
	    		//method: detecting when the environment noise coming back again	        	
	        	// calculating the decay curves using the noise model for each individual frequency
	        	for(int j = 0; j < decayCurveLength; j++)
	        		for(int i = endSweepIndex; i < FFTMags.length; i++)
	        		{    			
	        			int k = 0;
	        			int ctn = 0;
	        			while(k < winSize)
	        			{
	        				int idx = Math.min(i+k, FFTMags.length-1);
	    					try
	    					{
	    						
	    		                if((FFTMags[idx][j] > BkgNoise[j] -  NumOfStd* BkgNoiseSTD[j]) &&
	    	                	   (FFTMags[idx][j] < BkgNoise[j] +  NumOfStd* BkgNoiseSTD[j]))
	    	    				{
	    		                	ctn++;
	    	    				}
	    	    				k++;
	    					} catch(Exception x) {}
	        			}
	        			
	        			if(ctn >= winSize/2 )
	        			{
	        				decayCurveIdx[j] = i;
	        				break;
	        			}  			
	        		}	        	
	        	break;
	        
	        case 2: 
	        	// calculating the decay curves using the noise model of all frequencies
	        	BNM.ModelNoiseForAllFrequency(frameLength, FFTMags, index);
	        	double BkgNoise_FromAllFreq_Mean = BNM.getNoiseFromAllFreq_Mean();
	        	double BkgNoise_FromAllFreq_Std = BNM.getNoiseFromAllFreq_Std();
	        	for(int j = 0; j < decayCurveLength; j++)
	        		for(int i = endSweepIndex; i < FFTMags.length; i++)
	        		{    			
	        			int k = 0;
	        			int ctn = 0;
	        			while(k < winSize)
	        			{
	        				int idx = Math.min(i+k, FFTMags.length-1);
	    					try
	    					{
	    						
	    		                if((FFTMags[idx][j] > BkgNoise_FromAllFreq_Mean -  NumOfStd* BkgNoise_FromAllFreq_Std) &&
	    	                	   (FFTMags[idx][j] < BkgNoise_FromAllFreq_Mean +  NumOfStd* BkgNoise_FromAllFreq_Std))
	    						{
	    		                	ctn++;
	    	    				}
	    	    				k++;
	    					} catch(Exception x) {}
	        			}
	        			
	        			if(ctn >= winSize/2 )
	        			{
	        				decayCurveIdx[j] = i;
	        				break;
	        			}  			
	        		}
	        	break;
	        	
	        case 3:
	        	BNM.ModelNoiseFromCertainHzAndAbove(3000, samplingRate, frameLength, FFTMags, index);
	        	double BkgNoise_3KAndAbove_Mean = BNM.getNoiseFromCertainHzAndAbove_Mean();
	        	double BkgNoise_3KAndAbove_Std = BNM.getNoiseFromCertainHzAndAbove_Std();
	        	for(int j = 0; j < decayCurveLength; j++)
	        		for(int i = endSweepIndex; i < FFTMags.length; i++)
	        		{    			
	        			int k = 0;
	        			int ctn = 0;
	        			while(k < winSize)
	        			{
	        				int idx = Math.min(i+k, FFTMags.length-1);
	    					try
	    					{
	    		                if((FFTMags[idx][j] > BkgNoise_3KAndAbove_Mean -  NumOfStd* BkgNoise_3KAndAbove_Std) &&
	    			               (FFTMags[idx][j] < BkgNoise_3KAndAbove_Mean +  NumOfStd* BkgNoise_3KAndAbove_Std))
	    						{
	    		                	ctn++;
	    	    				}
	    	    				k++;
	    					} catch(Exception x) {}
	        			}
	        			
	        			if(ctn >= winSize/2 )
	        			{
	        				decayCurveIdx[j] = i;
	        				break;
	        			}  			
	        		}	        	
	        	break;
	        
	        case 4:
	        	BNM.ModelNoiseFromCertainFreqRange(7000, 15000, samplingRate, frameLength, FFTMags, index);
	        	double BkgNoise_ModelCertainFreqRange_Mean = BNM.getNoiseFromCertainFreqRange_Mean();
	        	double BkgNoise_ModelCertainFreqRange_Std = BNM.getNoiseFromCertainFreqRange_Std();
	        	
	        	for(int j = 0; j < decayCurveLength; j++)
	        		for(int i = endSweepIndex; i < FFTMags.length; i++)
	        		{    			
	        			int k = 0;
	        			int ctn = 0;
	        			while(k < winSize)
	        			{
	        				int idx = Math.min(i+k, FFTMags.length-1);
	    					try
	    					{
	    		                if((FFTMags[idx][j] > BkgNoise_ModelCertainFreqRange_Mean -  NumOfStd* BkgNoise_ModelCertainFreqRange_Std) &&
	    					               (FFTMags[idx][j] < BkgNoise_ModelCertainFreqRange_Mean +  NumOfStd* BkgNoise_ModelCertainFreqRange_Std))
	    						{
	    		                	ctn++;
	    	    				}
	    	    				k++;
	    					} catch(Exception x) {}
	        			}
	        			
	        			if(ctn >= winSize/2 )
	        			{
	        				decayCurveIdx[j] = i;
	        				break;
	        			}  			
	        		}	
	        	
        	default: 
        		break;
        }
        
        
           

    	
    	
    	/*
    	// calculating the decay curves using the noise model of all frequencies
    	BNM.ModelNoiseForAllFrequency(frameLength, FFTMags, index);
    	double BkgNoise_FromAllFreq_Mean = BNM.getNoiseFromAllFreq_Mean();
    	double BkgNoise_FromAllFreq_Std = BNM.getNoiseFromAllFreq_Std();
    	
    	BNM.ModelNoiseFromCertainHzAndAbove(3000, samplingRate, frameLength, FFTMags, index);
    	double BkgNoise_3KAndAbove_Mean = BNM.getNoiseFromCertainHzAndAbove_Mean();
    	double BkgNoise_3KAndAbove_Std = BNM.getNoiseFromCertainHzAndAbove_Std();
    	
    	
    	BNM.ModelNoiseFromCertainFreqRange(7000, 15000, samplingRate, frameLength, FFTMags, index);
    	double BkgNoise_ModelCertainFreqRange_Mean = BNM.getNoiseFromCertainFreqRange_Mean();
    	double BkgNoise_ModelCertainFreqRange_Std = BNM.getNoiseFromCertainFreqRange_Std();
    	
    	for(int j = 0; j < decayCurveLength; j++)
    		for(int i = endSweepIndex; i < FFTMags.length; i++)
    		{    			
    			int k = 0;
    			int ctn = 0;
    			while(k < winSize)
    			{
    				int idx = Math.min(i+k, FFTMags.length-1);
					try
					{
						
		              //  if((FFTMags[idx][j] > BkgNoise_FromAllFreq_Mean -  BkgNoise_FromAllFreq_Std) &&
	                //	   (FFTMags[idx][j] < BkgNoise_FromAllFreq_Mean +  BkgNoise_FromAllFreq_Std))
		            //    if((FFTMags[idx][j] > BkgNoise_3KAndAbove_Mean -  BkgNoise_3KAndAbove_Std) &&
			         //      (FFTMags[idx][j] < BkgNoise_3KAndAbove_Mean +  BkgNoise_3KAndAbove_Std))
		                if((FFTMags[idx][j] > BkgNoise_ModelCertainFreqRange_Mean -  BkgNoise_ModelCertainFreqRange_Std) &&
					               (FFTMags[idx][j] < BkgNoise_ModelCertainFreqRange_Mean +  BkgNoise_ModelCertainFreqRange_Std))
						{
		                	ctn++;
	    				}
	    				k++;
					} catch(Exception x) {}
    			}
    			
    			if(ctn >= winSize/2 )
    			{
    				decayCurveIdx[j] = i;
    				break;
    			}  			
    		}
    	*/
    	
    	
    	
    	
    	/*
    	for(int j = 0; j < decayCurveLength; j++)
    		for(int i = FFTMags.length-1; i >= endSweepIndex; i--)
    		{
    			int k = i;
    			int ctn = 0;
    			while(k >= endSweepIndex)
    			{
	                if((FFTMags[k][j] > BkgNoise[j] -  3* BkgNoiseSTD[j]) &&
		                	   (FFTMags[k][j] < BkgNoise[j] + 3 *  BkgNoiseSTD[j]))
		    				{
			                	ctn++;
		    				}
	                k--;
    			}
    			
    			if(ctn < winSize/2)
    			{
    				decayCurveIdx[j] = i;
    				break;
    			}
    		}
    	*/
        
    	return decayCurveIdx;
	}
}

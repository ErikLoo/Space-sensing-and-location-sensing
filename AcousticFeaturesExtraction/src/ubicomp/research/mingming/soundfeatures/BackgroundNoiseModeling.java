package ubicomp.research.mingming.soundfeatures;

public class BackgroundNoiseModeling {
	double[] BkgNoise;
	double[] BkgNoiseSTD;
	
	public void ModelForEachFrequency(int frameLength, double[][] FFTMags, int startSweepIndex, int _useSoundPieceBeforeSweep, int useSoundAtTheEndOfSoundClip)
	{
		
		int useLastSoundPiece = useSoundAtTheEndOfSoundClip;
		int useSoundPieceBeforeSweep = _useSoundPieceBeforeSweep;
		
		////////
		//background noise modeling
		///////
		BkgNoise = new double[frameLength];  // noise mean for each frequency 
		BkgNoiseSTD = new double[frameLength]; // noise standard deviation for each frequency
		
		if(useSoundPieceBeforeSweep == 1)
		{
			for(int i = 0; i < startSweepIndex; i++) //Math.max(0,index-5)
				for(int j = 0; j < FFTMags[i].length; j++)
				{
					BkgNoise[j] += FFTMags[i][j];
				}
		}
		
		if(useLastSoundPiece == 1)
		{
			// take the last few frames of sound data into environmental noise modeling
			for(int i = FFTMags.length-1; i >= FFTMags.length - startSweepIndex; i--)
				for(int j = 0; j < FFTMags[i].length; j++)
				{
					BkgNoise[j] += FFTMags[i][j];
				}
		}
		
		for(int k = 0; k < BkgNoise.length; k++)
		{
			BkgNoise[k] = Math.floor(BkgNoise[k] / BkgNoise.length *10000) / 10000;   //only keep 4 bits after dot
		}   
		
		if(useSoundPieceBeforeSweep == 1)
		{
			for(int i = 0; i < startSweepIndex; i++)//Math.max(0,index-5)
				for(int j = 0; j < FFTMags[i].length; j++)
				{
					BkgNoiseSTD[j] += Math.pow(FFTMags[i][j] - BkgNoise[j],2);
				}
		}
		
		if(useLastSoundPiece == 1)
		{
			// take the last few frames of sound data into environmental noise modeling
			for(int i = FFTMags.length-1; i >= FFTMags.length - startSweepIndex; i--)
				for(int j = 0; j < FFTMags[i].length; j++)
				{
					BkgNoiseSTD[j] += Math.pow(FFTMags[i][j] - BkgNoise[j],2);
				}
		}
		
		for(int k = 0; k < BkgNoiseSTD.length; k++)
		{
			BkgNoiseSTD[k] = Math.sqrt(BkgNoiseSTD[k] / (BkgNoiseSTD.length-1));
		}
	}

    public double[] getNoiseMean()
    {
    	return BkgNoise;
    }
    
    public double[] getNoiseStd()
    {
    	return BkgNoiseSTD;
    }
    
    private double NoiseFromAllFreq_Mean  = 0;
    private double NoiseFromAllFreq_Std = 0;
    
    public void ModelNoiseForAllFrequency(int frameLength, double[][] FFTMags, int startSweepIndex)
    {
		////////
		//background noise modeling
		///////
		int cnt = 0;
		for(int i = 0; i < startSweepIndex; i++) //Math.max(0,index-5)
			for(int j = 0; j < FFTMags[i].length; j++)
			{
				NoiseFromAllFreq_Mean += FFTMags[i][j];
				cnt ++;
			}
		NoiseFromAllFreq_Mean /= cnt;
		
		for(int i = 0; i < startSweepIndex; i++) //Math.max(0,index-5)
			for(int j = 0; j < FFTMags[i].length; j++)
			{
				NoiseFromAllFreq_Std += Math.pow( FFTMags[i][j] - NoiseFromAllFreq_Mean,2);
			}
		NoiseFromAllFreq_Std = Math.sqrt(NoiseFromAllFreq_Std/cnt);
    }
    
    public double getNoiseFromAllFreq_Mean()
    {
    	return NoiseFromAllFreq_Mean;
    }
    
    public double getNoiseFromAllFreq_Std()
    {
    	return NoiseFromAllFreq_Std;
    }
	
    
    private double NoiseFromCertainHzAndAbove_Mean = 0;
    private double NoiseFromCertainHzAndAbove_Std = 0;
    
    public void ModelNoiseFromCertainHzAndAbove(double startingFreq, double samplingRate, int frameLength, double[][] FFTMags, int startSweepIndex)
    {
		////////
		//background noise modeling
		///////
    	
    	int ThreeKHzFFTBinId = (int)(startingFreq * frameLength / samplingRate); // FFT bin Id of the 3K Hz
    	
		int cnt = 0;
		for(int i = 0; i < startSweepIndex; i++) //Math.max(0,index-5)
			for(int j = ThreeKHzFFTBinId; j < FFTMags[i].length; j++)
			{
				NoiseFromCertainHzAndAbove_Mean += FFTMags[i][j];
				cnt ++;
			}
		NoiseFromCertainHzAndAbove_Mean /= cnt;
		
		for(int i = 0; i < startSweepIndex; i++) //Math.max(0,index-5)
			for(int j = ThreeKHzFFTBinId; j < FFTMags[i].length; j++)
			{
				NoiseFromCertainHzAndAbove_Std += Math.pow( FFTMags[i][j] - NoiseFromCertainHzAndAbove_Mean,2);
			}
		NoiseFromCertainHzAndAbove_Std = Math.sqrt(NoiseFromCertainHzAndAbove_Std/cnt);
    }
    
    public double getNoiseFromCertainHzAndAbove_Mean()
    {
    	return NoiseFromCertainHzAndAbove_Mean;
    }
    
    public double getNoiseFromCertainHzAndAbove_Std()
    {
    	return NoiseFromCertainHzAndAbove_Std;
    }
    
    
    
    
    
    private double NoiseFromCertainFreqRange_Mean = 0;
    private double NoiseFromCertainFreqRange_Std = 0;
    
    public void ModelNoiseFromCertainFreqRange(double startingFreq, double endFreq, double samplingRate, int frameLength, double[][] FFTMags, int startSweepIndex)
    {
		////////
		//background noise modeling
		///////
    	
    	int StartFFTBinId = (int)(startingFreq * frameLength / samplingRate); // FFT bin Id of starting frequency
    	int EndFFTBinId = (int)(endFreq * frameLength / samplingRate); // FFT bin Id of ending frequency
		int cnt = 0;
		for(int i = 0; i < startSweepIndex; i++) //Math.max(0,index-5)
			for(int j = StartFFTBinId; j < EndFFTBinId; j++)
			{
				NoiseFromCertainFreqRange_Mean += FFTMags[i][j];
				cnt ++;
			}
		NoiseFromCertainFreqRange_Mean /= cnt;
		
		for(int i = 0; i < startSweepIndex; i++) //Math.max(0,index-5)
			for(int j = StartFFTBinId; j < EndFFTBinId; j++)
			{
				NoiseFromCertainFreqRange_Std += Math.pow( FFTMags[i][j] - NoiseFromCertainFreqRange_Mean,2);
			}
		NoiseFromCertainFreqRange_Std = Math.sqrt(NoiseFromCertainFreqRange_Std/cnt);
    }
    
    public double getNoiseFromCertainFreqRange_Mean()
    {
    	return NoiseFromCertainFreqRange_Mean;
    }
    
    public double getNoiseFromCertainFreqRange_Std()
    {
    	return NoiseFromCertainFreqRange_Std;
    }
    
    
    
}

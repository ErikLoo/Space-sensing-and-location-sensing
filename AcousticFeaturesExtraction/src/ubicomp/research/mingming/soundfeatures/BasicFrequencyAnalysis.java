/**
 * author: Mingming Fan
   contact at: fmmbupt@gmail.com
   
   Part of the file is adapted from OC Volume package. The copyright of OC Volume is listed below
   /*
OC Volume - Java Speech Recognition Engine
Copyright (c) 2002-2004, OrangeCow organization
All rights reserved.

Redistribution and use in source and binary forms,
with or without modification, are permitted provided
that the following conditions are met:

* Redistributions of source code must retain the
  above copyright notice, this list of conditions
  and the following disclaimer.
* Redistributions in binary form must reproduce the
  above copyright notice, this list of conditions
  and the following disclaimer in the documentation
  and/or other materials provided with the
  distribution.
* Neither the name of the OrangeCow organization
  nor the names of its contributors may be used to
  endorse or promote products derived from this
  software without specific prior written
  permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS
AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

Contact information:
Please visit http://ocvolume.sourceforge.net.
*/


package ubicomp.research.mingming.soundfeatures;

import org.oc.ocvolume.dsp.fft;

/***
 * This class does basic level sound analysis:
 * framing;
 * padding;
 * windowing;
 * FFT
 * */
public class BasicFrequencyAnalysis {

	/**
	 * Number of samples per frame
	 * this will also be the size of the FFT
	 * */
	protected int frameLength = 512;
	
	/**
	 * Number of overlapping samples 
	 * */
	protected int shiftInterval = frameLength /2 ;
	
    /**
     * Pre-Emphasis Alpha (Set to 0 if no pre-emphasis should be performed)
     */
    protected final static double preEmphasisAlpha = 0;
	
    /**
     * sampling rate of the input signal
     * */
	protected double samplingRate = 44100;
	
    /**
     * All the frames of the input signal
     */
    protected double frames[][];
    
    /**
     * hamming window values
     */
    protected double hammingWindow[];
    
    
    public BasicFrequencyAnalysis()
    {
    	
    }
    
    /**
     * constructor
     * set parameters here
     * */
    public BasicFrequencyAnalysis(int NumberSamplesPerFrame, int OverlapSamplesNumber, double _samplingRate)
    {
    	frameLength = NumberSamplesPerFrame;
    	shiftInterval = OverlapSamplesNumber;
    	samplingRate = _samplingRate;
    }
    
    public double[][] getFFTMagnitudesForEntireSoundClip(double inputSignal[],double _samplingRate)
    {
    	samplingRate = _samplingRate;
    	
    	double[][] magnitudes ; 
    	
        // Pre-Emphasis
        double outputSignal[] = preEmphasis(inputSignal);
        
        // Frame Blocking
        framing(outputSignal);

        // apply Hamming Window to ALL frames
        hammingWindow();
        
        magnitudes = new double[frames.length][frameLength];
        
        //
        // Below computations are all based on individual frames with Hamming Window already applied to them
        //
        for (int k = 0; k < frames.length; k++){
            // calculate FFT for current frame
            fft.computeFFT( frames[k] );
            
            // calculate magnitude spectrum
            for (int j = 0; j < frames[k].length; j++){
            	double temp = Math.pow(fft.real[j] * fft.real[j] + fft.imag[j] * fft.imag[j], 0.5);
            	magnitudes[k][j]  = Math.floor(temp * 10000) / 10000;  // only keep 4 numbers after the dot           	
            } 
        }
        
        return magnitudes;
    }
    
    
    /**
     * computes the magnitude spectrum of the input frame<br>
     * @param frame Input frame signal
     * @return Magnitude Spectrum array
     */
    public double[] getFFTMagnitudesForOneFrame(double frame[]){
        double magSpectrum[] = new double[frame.length];
        
        // calculate FFT for current frame
        fft.computeFFT( frame );
        
        // calculate magnitude spectrum
        for (int k = 0; k < frame.length; k++){
            magSpectrum[k] = Math.pow(fft.real[k] * fft.real[k] + fft.imag[k] * fft.imag[k], 0.5);
        }

        return magSpectrum;
    }
    
    /**
     * return the all bins frequencies of FFT
     * */
    public double[] getFFTBinFrequencies()
    {
    	double[] FFTBins = new double[frameLength];
    	double interval = samplingRate / frameLength;
    	for(int i = 0; i < frameLength; i++)
    	{
    		FFTBins[i] = interval * i;
    	}
    	
    	return FFTBins;
    }
    
    /**
     * performs Frame Blocking to break down a speech signal into frames<br>
     * @param inputSignal Speech Signal (16 bit integer data)
     */
    protected void framing(double inputSignal[]){
        double numFrames = (double)inputSignal.length / (double)(frameLength - shiftInterval);
        
        // unconditionally round up
        if ((numFrames / (int)numFrames) != 1){
            numFrames = (int)numFrames + 1;
        }
        
        // use zero padding to fill up frames with not enough samples
        double paddedSignal[] = new double[(int)numFrames * frameLength];
        for (int n = 0; n < inputSignal.length; n++){
            paddedSignal[n] = inputSignal[n];
        }

        frames = new double[(int)numFrames][frameLength];

        // break down speech signal into frames with specified shift interval to create overlap
        for (int m = 0; m < numFrames; m++){
            for (int n = 0; n < frameLength; n++){
                frames[m][n] = paddedSignal[m * (frameLength - shiftInterval) + n];
            }
        }
    }
    
    
    /**
     * performs Hamming Window<br>
     * @param frame A frame
     * @return Processed frame with hamming window applied to it
     */
    private void hammingWindow(){
        double w[] = new double[frameLength];
        for (int n = 0; n < frameLength; n++){
            w[n] = 0.54 - 0.46 * Math.cos( (2 * Math.PI * n) / (frameLength - 1) );
        }

        for (int m = 0; m < frames.length; m++){
            for (int n = 0; n < frameLength; n++){
                frames[m][n] *= w[n];
            }
        }
    }
    
    /**
     * perform pre-emphasis to equalize amplitude of high and low frequency<br>
     * @param inputSignal Speech Signal (16 bit integer data)
     * @return Speech signal after pre-emphasis (16 bit integer data)
     */
    protected static double[] preEmphasis(short inputSignal[]){
        double outputSignal[] = new double[inputSignal.length];
        
        // apply pre-emphasis to each sample
        for (int n = 1; n < inputSignal.length; n++){
            outputSignal[n] = inputSignal[n] - preEmphasisAlpha * inputSignal[n - 1];
        }
        
        return outputSignal;
    }
	
    protected static double[] preEmphasis(double inputSignal[]){
        double outputSignal[] = new double[inputSignal.length];
        
        // apply pre-emphasis to each sample
        for (int n = 1; n < inputSignal.length; n++){
            outputSignal[n] = inputSignal[n] - preEmphasisAlpha * inputSignal[n - 1];
        }
        
        return outputSignal;
    }
}

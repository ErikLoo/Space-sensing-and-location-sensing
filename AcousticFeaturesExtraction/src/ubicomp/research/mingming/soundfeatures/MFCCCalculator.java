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

public class MFCCCalculator {

    /**
     * Number of MFCCs per frame
     * Modifed 4/5/06 to be non final variable - Daniel McEnnnis
     */
    protected int numCepstra = 13;
    /**
     * lower limit of filter (or 64 Hz?)
     */
    protected  double lowerFilterFreq = 0; //1000 //5321;//133.3334; //15000;// protected final
    /**
     * upper limit of filter (or half of sampling freq.?)
     */
    protected  double upperFilterFreq = 22050; // 8000 //9321;  //6855.4976;   // protected final
    /**
     * number of mel filters (SPHINX-III uses 40)
     */
    protected  int numMelFilters = 23;
    
    protected int frameLength = 512;
    
    protected double samplingRate = 44100;
	
	
		
	public MFCCCalculator(double _samplingRate)
	{
		samplingRate = _samplingRate;
		upperFilterFreq = samplingRate/2;
	}
		
	public MFCCCalculator(double _samplingRate, double _lowerFilterFreq, double _upperFilterFreq)
	{
		samplingRate = _samplingRate;
		upperFilterFreq = samplingRate/2;
		lowerFilterFreq = _lowerFilterFreq;
		upperFilterFreq = Math.min(upperFilterFreq,_upperFilterFreq);
	}
	
    /**
     * takes a speech signal and returns the Mel-Frequency Cepstral Coefficient (MFCC)<br>
     */
    public double[][] process(double[][] FFTMags){
    	double[][] MFCC = null;
    	   	
    	if(FFTMags != null)
    	{        	
            // Initializes the MFCC array
            MFCC = new double[FFTMags.length][numCepstra];
            //
            // Below computations are all based on individual frames with Hamming Window already applied to them
            //
            for (int k = 0; k < FFTMags.length; k++){

            	// Magnitude Spectrum from frame k
                double bin[] = FFTMags[k];
                
                //System.out.println("bin size: " + bin.length);
                // Mel Filtering
                int cbin[] = fftBinIndices(samplingRate,frameLength);
                
                //System.out.println("cbin size: " + cbin.length);
                
                // get Mel Filterbank
                double fbank[] = melFilter(bin, cbin);

                // Non-linear transformation
                double f[] = nonLinearTransformation(fbank);

                // Cepstral coefficients
                double cepc[] = cepCoefficients(f);

                // Add resulting MFCC to array
                for (int i = 0; i < numCepstra; i++){
                    MFCC[k][i] = cepc[i];
                }
            }
    	}

        return MFCC;
    }
    
    
    /**
     * calculates the FFT bin indices<br>
     * calls: none<br>
     * called by: featureExtraction
     * 
     * 5-3-05 Daniel MCEnnis paramaterize sampling rate and frameSize
     * 
     * @return array of FFT bin indices
     */
    public int[] fftBinIndices(double samplingRate,int frameSize){
        int cbin[] = new int[numMelFilters + 2];
        
        cbin[0] = (int)Math.round(lowerFilterFreq / samplingRate * frameSize);
        cbin[cbin.length - 1] = (int)(frameSize / 2);
        
        for (int i = 1; i <= numMelFilters; i++){
            double fc = centerFreq(i,samplingRate);

            cbin[i] = (int)Math.round(fc / samplingRate * frameSize);
        }
        
        return cbin;
    }
    /**
     * Calculate the output of the mel filter<br>
     * calls: none
     * called by: featureExtraction
     */
    public double[] melFilter(double bin[], int cbin[]){
        double temp[] = new double[numMelFilters + 2];

        for (int k = 1; k <= numMelFilters; k++){
            double num1 = 0, num2 = 0;

            for (int i = cbin[k - 1]; i <= cbin[k]; i++){
                num1 += ((i - cbin[k - 1] + 1) / (cbin[k] - cbin[k-1] + 1)) * bin[i];
            }

            for (int i = cbin[k] + 1; i <= cbin[k + 1]; i++){
                num2 += (1 - ((i - cbin[k]) / (cbin[k + 1] - cbin[k] + 1))) * bin[i];
            }

            temp[k] = num1 + num2;
        }

        double fbank[] = new double[numMelFilters];
        for (int i = 0; i < numMelFilters; i++){
            fbank[i] = temp[i + 1];
        }

        return fbank;
    }
    /**
     * Cepstral coefficients are calculated from the output of the Non-linear Transformation method<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param f Output of the Non-linear Transformation method
     * @return Cepstral Coefficients
     */
    public double[] cepCoefficients(double f[]){
        double cepc[] = new double[numCepstra];
        
        for (int i = 0; i < cepc.length; i++){
            for (int j = 1; j <= numMelFilters; j++){
                cepc[i] += f[j - 1] * Math.cos(Math.PI * i / numMelFilters * (j - 0.5));
            }
        }
        
        return cepc;
    }
    /**
     * the output of mel filtering is subjected to a logarithm function (natural logarithm)<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param fbank Output of mel filtering
     * @return Natural log of the output of mel filtering
     */
    public double[] nonLinearTransformation(double fbank[]){
        double f[] = new double[fbank.length];
        final double FLOOR = -50;
        
        for (int i = 0; i < fbank.length; i++){
            f[i] = Math.log(fbank[i]);
            
            // check if ln() returns a value less than the floor
            if (f[i] < FLOOR) f[i] = FLOOR;
        }
        
        return f;
    }
    /**
     * calculates logarithm with base 10<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param value Number to take the log of
     * @return base 10 logarithm of the input values
     */
    protected static double log10(double value){
        return Math.log(value) / Math.log(10);
    }
    /**
     * calculates center frequency<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param i Index of mel filters
     * @return Center Frequency
     */
    private  double centerFreq(int i,double samplingRate){
        double mel[] = new double[2];
        mel[0] = freqToMel(lowerFilterFreq);
        mel[1] = freqToMel(upperFilterFreq);  //way up to the highest point that sampling rate can get: samplingRate / 2
        
        // take inverse mel of:
        double temp = mel[0] + ((mel[1] - mel[0]) / (numMelFilters + 1)) * i;
        return inverseMel(temp);
    }
    /**
     * calculates the inverse of Mel Frequency<br>
     * calls: none<br>
     * called by: featureExtraction
     */
    private static double inverseMel(double x){
        double temp = Math.pow(10, x / 2595) - 1;
        return 700 * (temp);
    }
    /**
     * convert frequency to mel-frequency<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param freq Frequency
     * @return Mel-Frequency
     */
    protected static double freqToMel(double freq){
        return 2595 * log10(1 + freq / 700);
    }
}

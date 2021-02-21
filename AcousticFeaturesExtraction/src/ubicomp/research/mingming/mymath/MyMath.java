/**
 * author: Mingming Fan
   contact at: fmmbupt@gmail.com
 */
package ubicomp.research.mingming.mymath;

import java.util.ArrayList;
import java.util.Arrays;

import Jama.Matrix;

/**
 * some standard math used in vector calculations
 * */
public class MyMath {
	
	
	public static void smooth_KalmanFilter(double[] data)
	{
		KalmanFilter KF;  
        double dt = 10; // delta t: time interval
        double processNoiseStdev = 5;  // noise standard deviation
        double measurementNoiseStdev = 50;  // noise standard deviation
        
        //init filter
        KF = KalmanFilter.buildKF(0, 0, dt, Math.pow(processNoiseStdev, 2) / 2, Math.pow(measurementNoiseStdev, 2));
        KF.setX(new Matrix(new double[][]{{0}, {data[0]}, {0}, {0}}));
        for(int i = 1; i < data.length; i++)
        {
            //filter update
            KF.predict();
          
            data[i-1] = (int) KF.getX().get(1, 0);
            KF.correct(new Matrix(new double[][]{{i}, {data[i]}}));
            
        }
	}
	
	public static void smooth_KalmanFilter(int[] data)
	{
		KalmanFilter KF;  
        double dt = 10; // delta t: time interval milliseconds
        double processNoiseStdev = 10;  // noise standard deviation
        double measurementNoiseStdev = 100;  // noise standard deviation
        
        //init filter
        KF = KalmanFilter.buildKF(0, 0, dt, Math.pow(processNoiseStdev, 2) / 2, Math.pow(measurementNoiseStdev, 2));
        KF.setX(new Matrix(new double[][]{{0}, {data[0]}, {0}, {0}}));
        for(int i = 1; i < data.length; i++)
        {
            //filter update
            KF.predict();
          
            data[i-1] = (int) KF.getX().get(1, 0);
            KF.correct(new Matrix(new double[][]{{i}, {data[i]}}));
            
        }
	}
	
	public static void smooth_MeanFilter(double[] data,int winSize)
	{
		int halfWin = winSize/2;
		for(int i = halfWin; i < data.length - halfWin; i++)
		{
			double sum = 0;
			for(int j= i - halfWin; j < i + halfWin; j++)
			{
				sum += data[j];
			}
			
			sum /= winSize;
			
			data[i] = sum;
		}
	}
	
	public static int[] smooth_MeanFilter(int[] data,int winSize)
	{
		int[] result = new int[data.length];
		
		int halfWin = winSize/2;
		for(int i = halfWin; i < data.length - halfWin; i++)
		{
			int sum = 0;
			for(int j= i - halfWin; j < i + halfWin; j++)
			{
				sum += data[j];
			}
			
			sum /= winSize;
			
			result[i] = sum;
		}
		// two ends just filling the same values 
		for(int i = 0; i < halfWin; i++)
			result[i] = result[halfWin];
		for(int i = data.length - halfWin; i < data.length; i++)		
			result[i] = result[data.length - halfWin-1];
		
		return result;
	}
	
	public static void normalization(int[] data)
	{
		int max = Integer.MIN_VALUE;
		for(int d: data)
		{
			if(d > max)
				max = d;
		}
		
		for(int i = 0; i < data.length; i++)
		{
			data[i] /= max;
		}
		return;
	}
	
	public static void normalization(double[] data)
	{
		double max = Double.MIN_VALUE;
		for(double d: data)
		{
			if(d > max)
				max = d;
		}
		
		for(int i = 0; i < data.length; i++)
		{
			data[i] /= max;
		}
		return;
	}
	
	public static void normalization(float[] data)
	{
		float max = Float.MIN_VALUE;
		for(float d: data)
		{
			if(d > max)
				max = d;
		}
		
		for(int i = 0; i < data.length; i++)
		{
			data[i] /= max;
		}
		return;
	}
	
	public static int[] smooth_MedianFilter(int[] data,int winSize)
	{
		int[] result = new int[data.length];
		int halfWin = winSize/2;
		for(int i = halfWin; i < data.length - halfWin; i++)
		{
			int mid = Median(data,i-halfWin,i+halfWin);
			result[i] = mid;
		}
		
		//filling in two ends;
		for(int i = 0; i < halfWin; i++)
			result[i] = result[halfWin];
		for(int i = data.length - halfWin; i < data.length; i++)		
			result[i] = result[data.length - halfWin-1];
		
		return result;
	}
	
	public static int Median(int[] data, int startIdx, int endIdx)
	{
		if(endIdx > startIdx)
		{
			int[] temp = new int[endIdx-startIdx];
			for(int i = 0; i < endIdx-startIdx; i++)
			{
				temp[i] = data[startIdx+i];
			}
			Arrays.sort(temp);
			if((endIdx-startIdx) % 2 == 0)
			{
				return (temp[(endIdx-startIdx)/2 -1] +temp[(endIdx-startIdx)/2 +1])/2;
			}
			else
			{
				return temp[(endIdx-startIdx)/2 ];
			}
		}
		else
			return 0;
	}
	
	public static double[]  abs(double[] data)
	{
		double[] result = new double[data.length];
		for(int i = 0; i < data.length; i++)
		{
			result[i] = Math.abs(data[i]);
		}
		return result;
	}
	
	public static void reverseVectorInPlace(double[] data, int start, int end)
	{
		int i = start;
		int j = end;
		while(i < j)
		{
		   double temp = data[i];
		   data[i] = data[j];
		   data[j] = temp;
		   i++;
		   j--;
		}		
	}
	
	public static void reverseVectorInPlace(double[] data)
	{
		reverseVectorInPlace(data,0,data.length-1);
	}
	
	public static double[] reverseVector(double[] data, int start, int end)
	{
		double[] result = new double[end-start+1];
		for(int i = start; i <= end; i++)
		{
			result[end-i] = data[i];
		}
		
		return result;
	}
	
	/**
	 * mean of a vector data
	 * */
	public static double mean(double[] data)
	{
		double m = 0;
		for(double d : data)
		{
			m +=d;
		}
		
		if(data.length > 0)
		{
			m /= data.length;
		}
		
		return m;
	}
	/**
	 * mean of a vector data given start and end points
	 * */
	public static double mean(double[] data, int start, int end)
	{
		double m = 0;
		for(int i = start; i < end; i++)
		{
			m +=data[i];
		}
		
		if(end - start + 1 > 0)
		{
			m /= (end-start+1);
		}
		
		return m;
	}
	
	/**
	 * vector dot multiply
	 * **/
	public static double[] dotmultiply(double[] x1, double[] x2)
	{
		int length = x1.length;
		double[] temp = new double[length];
		for(int i = 0; i < length; i++)
		{
			temp[i] = x1[i] * x2[i];
		}
		
		return temp;
	}
	

	/**
	 * vector divided by a number
	 * **/
	public static void dotdivide(double[] data, double denominator)
	{
		if(denominator != 0)
		{
			for(int i = 0; i < data.length; i++)
			{
				data[i] = data[i] / denominator;
			}
		}
	}
	
	/**
	 * vector divided by another vector
	 * */
	public static void dotdivide(double[] data, int[] denominator)
	{
		for(int i = 0; i < data.length; i++)
		{
			if(denominator[i] != 0)
				data[i] = data[i] / denominator[i];
		}
	}
	
	/**
	 * cumulative sum of a vector
	 * */
	public static double[] cumsum(double[] data)
	{
		double[] sum = new double[data.length];
		double tempsum = 0;
		for(int i = 0; i < data.length; i++)
		{
			tempsum += data[i];
			sum[i] = tempsum;
		}
		return sum;
	}
	
	/**
	 * vector minus
	 * input: v1, v2
	 * output: v1
	 * */
	public static void vector_minus(double[] v1, double[] v2)
	{
		
		if(v1.length == v2.length)
		{
			for(int i = 0; i < v1.length; i++)
			{
				v1[i] = v1[i] - v2[i];
			}
		}
		else
		{
			System.out.println("vector lengths are not the same...");
		}
	}
	
	/**
	 * linear spacing between d1 and d2 with N points
	 * */
	public static double[] linspace(double d1, double d2, int N)
	{
		double interval = (d2-d1)/ (N-1);
		
		double[] results = new double[N];
		
		for(int i = 0; i < N; i++)
		{
			results[i] = d1 + i * interval;
		}
		return results;
	}
	
	public static double sum(double[] data)
	{
		double result = 0;
		for(double d : data)
		{
			result += d;
		}
		return result;
	}
	
	
	
	public static ArrayList<Integer> findLocalMaximum(int[] data)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		int win = 5;
		for(int i = win/2; i < data.length - win/2; i++)
		{
			// i th data is the local maximum within the window centered at it
			int mid = data[i];
			int j = i - win/2;
			for(; j < i + win/2; j++)
			{
				if(j != i && data[j] >= mid)
					break;
			}			
			if(j == i + win/2)
				result.add(i);
		}
		return result;
	}
	
	/**
	 * works just so so
	 * */
	public static ArrayList<Integer> findLocalMaximum_windowMaxApproach(int[] data)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		int win = 20;
		int overlap = win/2;
		int prevIndex = 0;
		for(int i = 0; i < data.length-win; i+= overlap)
		{
			int idx = i;
			int max = data[idx];
			for(int j = i+1; j < i+win; j++)
			{
				if(data[j] > max)
				{
					max = data[j];
					idx = j;
				}
			}
			
			//keep a distance between local maximums
			if(idx - prevIndex > overlap)
			{
				result.add(idx);
				prevIndex = idx;
			}	
			
		}
		
		return result;
	}
	
	public static ArrayList<Integer> findLocalMaximum(double[] data)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		int win = 20;
		int overlap = win/2;
		
		for(int i = 0; i < data.length-win; i+= overlap)
		{
			int idx = i;
			double max = data[idx];
			for(int j = i+1; j < i+win; j++)
			{
				if(data[j] > max)
				{
					max = data[j];
					idx = j;
				}
			}
			result.add(idx);
		}
		
		return result;
	}
}

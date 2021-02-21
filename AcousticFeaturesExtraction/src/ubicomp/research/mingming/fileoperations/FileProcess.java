/**
 * author: Mingming Fan
   contact at: fmmbupt@gmail.com
 */
package ubicomp.research.mingming.fileoperations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileProcess {
	
	public static void SaveArray2File(String folder_path, String outputfile, int[] data)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(folder_path + outputfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(out != null)
		{
			for(int i = 0; i < data.length; i++)
			{
				double freq = i * 44100 / 512;
				out.println(data[i]+","+ freq + "," +i );
				out.flush();
			}
		}
		
		if(out != null)
		{
			out.flush();
			out.close();
			out = null;
		}
	}
	
	
	public static void SaveArray2File(String folder_path, String outputfile, ArrayList<Integer> data)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(folder_path + outputfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(out != null)
		{
			for(int i = 0; i < data.size(); i++)
			{
				//double freq = i * 44100 / 512;
				out.println(data.get(i));
				out.flush();
			}
		}
		
		if(out != null)
		{
			out.flush();
			out.close();
			out = null;
		}
	}
	
	public static void SaveArray2File(String folder_path, String outputfile, double[] data)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(folder_path + outputfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(out != null)
		{
			for(int i = 0; i < data.length / 2; i++)
			{
				double freq = i * 44100 / 512;
				out.println(data[i]+","+ freq );
				out.flush();
			}
		}
		
		if(out != null)
		{
			out.flush();
			out.close();
			out = null;
		}
	}
	
	
	public static void Save2DArray2File(String folder_path, String outputfile, double[][] data)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(folder_path + outputfile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(out != null)
		{
			for(int i = 0; i < data.length; i++)
			{
				for(int j = 0; j < data[i].length; j++)
				{
					out.print(data[i][j]+",");
				}
				out.println();
				out.flush();
			}
		}
		
		if(out != null)
		{
			out.flush();
			out.close();
			out = null;
		}
	}

	public static void SaveFileNames2File(String folder_path, String extension, String outputfilename)
	{
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(outputfilename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String files;
		File folder = new File(folder_path);
		File[] listOfFiles = folder.listFiles();
		for(int i = 0; i < listOfFiles.length; i++)
		{
			if(listOfFiles[i].isFile())
			{
				files = listOfFiles[i].getName();
				if(files.endsWith(extension))
				{
					if(out != null)
					{
						out.println(files);
					}
					System.out.println(files);
				}
			}
		}
		
		if(out != null)
		{
			out.close();
			out = null;
		}
	}
}

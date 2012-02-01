package com.ibox_ucsc.design.positioning; 

import com.ibox_ucsc.design.util.DeviceReader;
import com.ibox_ucsc.design.util.Util;

public class WifiPositioning 
{
	public static final int WAP_NUMBER = 6; // 5 wireless access points 
	public static final int WAP_COUNT = 4;
	public static final int VALUE_LENGTH = 8; // 6 digits + minus sign '-' + 	decimal point '.'
	public static final String SIMULATION_FILE = "/sdcard/wpirl/simulation_model/WAP";
	public static final int MAX_COUNT = 5;
	private RadioMap[] simulation; // the router simulated map
	private int[] rssi;
	/**
      * Read the simulated model from input file stored
      * in the sdcard at location /sdcard/locus/simulation_model/
      * with name format is WAP# with # is the order number
      */
	public WifiPositioning() 
	{
		simulation = new RadioMap[WAP_NUMBER]; 
		rssi = new int[WAP_NUMBER];
		input();
	}
	
	private void input() 
	{
		// for each simulation file containing the radio map of one wireless access point 
		for (int k = 0; k < WAP_NUMBER; k++) 
		{
			String file = new String(SIMULATION_FILE + String.valueOf(k+1) + ".txt");  // the path of the file
			DeviceReader in = new DeviceReader(file); // open the file to read
			String s = in.readln();  // read the first line
			s = s.toLowerCase(); 
			simulation[k] = new RadioMap();
		    simulation[k].bssid = s;  // the first line is BSSID (MAC address)
		    s = in.readln(); // skip the next line
		
		    for (int i = 0; i < RadioMap.ROW; i++) 
		    { // browse the number of row
		    	s = in.readln(); // read the entire line
		    	int start = 0; 
		    	int j = 0;
		    	
		    	/*
		         * Each value contains exact 8 characters:
		         * 6 digits, 1 minus sign '-', and 1 decimal point '.'
		         * The value will be read from position: 0, 8, 16, etc.
		         */
		    	while (j < RadioMap.COL) 
		    	{
		    		String value = s.substring(start, start + VALUE_LENGTH); // the value at row i column j
		    		start += VALUE_LENGTH;
		    		simulation[k].map[i][j] = Float.valueOf(value); // record the value
		    		j++; 
		    	}
		    		
		    }
		    in.close();
		 }
	}
	/**
	* Return the probability density function of the last sample 
	* based on the simulated propagation models
	*
	* @param rssi
	* @return
	*/
	
	public float[][] correlate() 
	{
		float[][] c = new float[RadioMap.ROW][RadioMap.COL];
		boolean[] highest = Util.highest(rssi, WAP_COUNT); 
		int[] maxpos = new int[2];
		float max = 0;
		for (int i = 0; i < RadioMap.ROW; i++) 
		{
			for (int j = 0; j < RadioMap.COL; j++) 
			{
				c[i][j] = 1;
				for (int k = 0; k < WAP_NUMBER; k++) 
				{
					if ( (rssi[k] > Integer.MIN_VALUE) && (highest[k]) ) 
					{
						float square = Util.square(rssi[k] - simulation[k].map[i][j]);
						c[i][j] *= (double) Math.exp(( square ) / (5*rssi[k]));
						if (c[i][j] > max) 
						{ 
							max = c[i][j]; 
							maxpos[0] = j; 
							maxpos[1] = i;
						} 
					}
						
				} 
			}
		}						     
		addPosition(maxpos);						
		return c; 
	}
	
	public void addMeasurement(int[] rssi) 
	{ 
		Util.copyArray(this.rssi, rssi);
	}
	
	/**
	 * Find the id of the WAP in simulation maps using the
	 * BSSID (physical MAC address of the router). In the WAP
	 * is among the simulated propagation models, return the id of
	 * that WAP, else return -1 if this WAP is not in the model.				
	 *
	 * @param bssid The BSSID of the WAP to search
	 * @return The id of the simulated model of this WAP 
	 */
	
	public int findBSSID(String bssid) 
	{
		for (int k = 0; k < WAP_NUMBER; k++) 
		{
			if (bssid.equals(simulation[k].bssid)) 
			{ 
				return k;
			}
		}; 
		return -1; 
	}
	
	private int[][] position = new int[MAX_COUNT][2]; 
	private double[] average = new double[2];
	private int count = 0;
	
	public void addPosition(int[] newpos) 
	{ 
		double[] d = new double[MAX_COUNT]; 
		double sum = 0;
		position[count][0] = newpos[0]; 
		position[count][1] = newpos[1];
		int length;
		
		if (position[MAX_COUNT-1][0] + position[MAX_COUNT-1][1] == 0) 
			length = count+1;  
		else 
			length = MAX_COUNT; 
		count++;
		if (count == MAX_COUNT) 
			count = 0;
		if (length > 1)
		{
			for (int i = 0; i < length; i++) 
			{
				d[i] = Util.distance(position[i][0], position[i][1], average[0],average[1]);
				sum += d[i];
			}
			average[0] = 0;
			average[1] = 0;
			for (int i = 0; i < length; i++) 
			{
				average[0] += ((double) position[i][0]) * (sum - d[i]) / ((length - 1) * sum);
				average[1] += ((double) position[i][1]) * (sum - d[i]) / ((length - 1) * sum);
			}
		} else 
		{
			average[0] = position[0][0];
			average[1] = position[0][1];
		}
	}
	
	public int[] average()
	{
		int[] value = new int[2]; 
		value[0] = (int) average[0]; 
		value[1] = (int) average[1]; 
		return value;
	}
	public int[] position() 
	{
		int[] value = Util.copyArray(position[count]); 
		return value;
	} 
}
	
	
		
		
		
		
		
		
		
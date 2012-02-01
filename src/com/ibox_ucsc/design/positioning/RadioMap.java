package com.ibox_ucsc.design.positioning; 

import com.ibox_ucsc.design.map.Map;


public class RadioMap 
{
	public static final int RESOLUTION = 2; // 2 sample per meter int LEFT = 74;
	public static final int LEFT = 74;
	public static final int TOP = 188;
	public static final int ROW = 94;
	public static final int COL = 25;
		
	public float[][] map; 
	public String bssid;
	
	public RadioMap() 
	{
		map = new float[ROW][COL];
	}
	
	public String toString() 
	{
		StringBuilder sb = new StringBuilder(); 
		for (int i = 0; i < ROW; i++) 
		{
			for (int j = 0; j < COL; j++) 
				sb.append(String.valueOf(map[i][j]));
			sb.append("\n");
         }
		return sb.toString(); 
	}
	
	
	
	public static float[] toActualScale(int [] pos)
	{
		float[] actual_scale = new float[2];
		
		actual_scale[0] =  pos[0] / 2;
		actual_scale[1] =  pos[1] / 2;
		
		return actual_scale;
	}
}
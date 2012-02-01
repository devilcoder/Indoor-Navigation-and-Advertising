package com.ibox_ucsc.design.map; 
import java.util.ArrayList;
import com.ibox_ucsc.design.util.DeviceReader; 
import com.ibox_ucsc.design.util.Util;

public class Architecture 
{
	public static final String WALLS_FILE = "/sdcard/wpirl/map/walls.txt";
	public static final int ORIGIN_X = 74;
	public static final int ORIGIN_Y = 188;
	private ArrayList<Wall> walls;
	
	public Architecture()
	{
		walls = new ArrayList<Wall>();
		DeviceReader in = new DeviceReader(WALLS_FILE); 
		int n = Integer.valueOf(in.readln());
		for (int i = 0; i < n; i++)
		{
			String s = in.readln();
			float x1 = Float.valueOf(s.substring( 0, 8)); 
			float y1 = Float.valueOf(s.substring( 8, 16)); 
			float x2 = Float.valueOf(s.substring(16, 24));
			float y2 = Float.valueOf(s.substring(24, 32));
			int[] p1 = Util.translate(Map.toMapScale(x1, y1), ORIGIN_X, ORIGIN_Y); 
			int[] p2 = Util.translate(Map.toMapScale(x2, y2), ORIGIN_X, ORIGIN_Y);
			
			walls.add(new Wall(p1, p2, Wall.SORT_X)); 
		}
	} 
}
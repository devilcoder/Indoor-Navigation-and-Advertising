package com.ibox_ucsc.design.map; 

import com.ibox_ucsc.design.util.Util; 

public class Wall 
{
	public static int SORT_X = 1; 
	public static int SORT_Y = 2;
	public int x1;
	public int y1; 
	public int x2; 
	public int y2;
	
	public Wall(int x1, int y1, int x2, int y2, int sort) 
	{ 
		this.x1 = x1;
		this.y1 = y1; 
		this.x2 = x2; 
		this.y2 = y2;
		if (sort == SORT_X) { sortX(); }
		else { sortY(); } 
	}
	public Wall() 
	{
		this(0, 0, 0, 0, 0);
	}
	
	public Wall(int[] p1, int[] p2, int sort) 
	{ 
		this(p1[0], p1[1], p2[0], p2[1], sort);
	}
	
	public float distance() 
	{
		return Util.distance(x1, y1, x2, y2);
	}
	private void sortX() 
	{ 
		if (x1 > x2)
		{
			int xt = x1; x1 = x2; x2 = xt;
			int yt = y1; y1 = y2; y2 = yt; 
		}
	};
	
	private void sortY() 
	{ 
		if (y1 > y2) 
		{
			int xt = x1; x1 = x2; x2 = xt;
			int yt = y1; y1 = y2; y2 = yt;
		}
	};

}
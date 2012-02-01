package com.ibox_ucsc.design.positioning;

import com.ibox_ucsc.design.util.Util;

public class Motion 
{
	public float[] distance;
	public long time;
	
	public Motion() 
	{ 
		this.distance = new float[2];
		this.time = 0;
	}
	
	public Motion(long time) 
	{ 
		this.distance = new float[2]; 
		this.time = time;
	}
	public Motion(Motion m) 
	{
		this.distance = Util.copyArray(m.distance); 
		this.time = m.time;
	} 
}
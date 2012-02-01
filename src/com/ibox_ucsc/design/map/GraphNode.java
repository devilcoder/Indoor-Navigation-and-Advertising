package com.ibox_ucsc.design.map; 

import com.ibox_ucsc.design.util.Util; 


public class GraphNode 
{
	public static final int TYPE_CORRIDOR = 1;
	public static final int TYPE_STAIR = 2;
	public static final int TYPE_ELEVATOR = 3;
	public static final int TYPE_ROOM = 4;
	public static final int TYPE_CUBICLE = 5;
	
	public int id;
	public int[] position; // x y coordination (unit in pixels)
	public int[] adjacent;
	public int type;
	public String room;
	public String person;
	public HeapNode heap;
	
	public GraphNode(int id, int[] position, String type, String room, String person) 
	{
		this.id  = id;
		this.position = new int[2];
		this.position[0]  = position[0];
		this.position[1]  = position[1];
		this.room   = room;
		this.person = person;
		
		if (type.compareToIgnoreCase("room") ==	0) 
		{
			this.type = TYPE_ROOM;
		}
		else if (type.compareToIgnoreCase("elevator") == 0) 
		{
			this.type = TYPE_ELEVATOR;
		} else if (type.compareToIgnoreCase("stair" ) == 0)
		{ 
			this.type = TYPE_STAIR;
		} else if (type.compareToIgnoreCase("corridor") == 0)
		{ 
			this.type = TYPE_CORRIDOR;
		} else if (type.compareToIgnoreCase("cubicle") == 0)
		{ 
			this.type = TYPE_CUBICLE;
		}
		
	}
	
	public GraphNode(int id, int x, int y) 
	{ 
		this.id = id;
		this.position = new int[2];
		this.position[0] = x;
		this.position[1] = y;
	}
		
	public GraphNode(GraphNode node) 
	{
		this.id  = node.id;
		this.position  	= Util.copyArray(node.position); 
		this.adjacent  = Util.copyArray(node.adjacent);
		this.type  = node.type;
		this.room = node.room;
		this.person = node.person;
	}
	
	public GraphNode() 
	{
		
		this.position = new int[2];
		this.room  = new String();
		
		this.person = new String();
		this.adjacent = new int[1]; 
	}
}		
		
		
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
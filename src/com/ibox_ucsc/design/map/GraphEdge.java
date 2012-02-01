package com.ibox_ucsc.design.map;

// import com.wpirl.positioning.RadioMap;

public class GraphEdge{
	
	public final GraphNode target;
    public final double weight;

    
    public final adjacents [] endpoints;
    
   
    public GraphEdge(GraphNode argTarget, double argWeight)
    { 
    	target = argTarget; 
    	weight = argWeight; 
    	endpoints = new adjacents[(int)weight];
    	
    	for(int i=0; i < argTarget.adjacent.length; i++)
    		endpoints[i].id  = argTarget.adjacent[i];    	
    }
    
    public GraphEdge()
    {
		this.target = new GraphNode();
		this.weight  = 0;
		this.endpoints = new adjacents[1]; 
		this.endpoints[0].id = 0;
    }
        
    public GraphEdge(GraphEdge edge)
    {
    	this.target  = new GraphNode(edge.target);
    	this.weight = edge.weight;
    	this.endpoints = edge.endpoints; 
    }
  
    
    public class adjacents
    {
    	public int id;
    	// public final GraphNode node;
    	public adjacents()
    	{
    		id = 0;
    		//node = new GraphNode();
    	}
    	
    }
}




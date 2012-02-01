package com.ibox_ucsc.design.map;

/* This class implements a node of the Fibanacci Heap used in Dijkstra algorith.
 * It holds the data used for maintaining the structure of the heap.
 * It also holds the reference to the key value (which is used to determine the heap structure).
 */

public class HeapNode
{
	GraphNode data;  // note data;
	HeapNode child;  // first child node
	HeapNode left;  // left sibling node
	HeapNode parent; // parent node
	HeapNode right; // right sibling node
	boolean mark; // true if this node has had a child removed since this node was added to its parent
	double key;  // key value for this node
	int degree; // number of children of this node excluding grandchildren
	
	/**
	* Default Constructor. Initializes the right and left pointers, making this 
	* a circular doubly-linked list.
	*
	* @param data data for this node
	* @param key initial key for node
	*/
	
	public HeapNode(GraphNode data, double key) 
	{
		this.right = this; 
		this.left = this;
		this.data = data; 
		this.key = key;
	}
	
	public final double getKey() 
	{ 
		return key;
	}
	
	public GraphNode getData() 
	{ 
		return data;
	}
	public String toString() 
	{
		//  return Double.toString(key);
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("Node=[parent = ");
		if (parent != null) 
			sb.append(Double.toString(parent.key));  
		else 
			sb.append("---"); 
		
		sb.append(", key = "); 
		sb.append(Double.toString(key)); 
		sb.append(", degree = "); 
		sb.append(Integer.toString(degree)); 
		sb.append(", right = ");
	
		if (right != null) 
			sb.append(Double.toString(right.key));
		else
			sb.append("---");
		
		sb.append(", left = ");
		
		if (left != null)
			sb.append(Double.toString(left.key)); 
		else 
			sb.append("---");
		
		sb.append(", child = ");
		
		if (child != null) 
			sb.append(Double.toString(child.key));
		else 
			sb.append("---");
		
		sb.append(']');
		
		return sb.toString();
	}
}
		
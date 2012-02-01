package com.ibox_ucsc.design.map; 

import java.util.ArrayList;
import java.util.Arrays;
import com.ibox_ucsc.design.util.DeviceReader;
import com.ibox_ucsc.design.util.Util;

public class Graph 
{
	public static final String NODES_FILE = "/sdcard/wpirl/map/nodes.txt";
	public static final String EDGES_FILE = "/sdcard/wpirl/map/edges.txt";
	
	private ArrayList<GraphNode> nodes;
	
	private FibonacciHeap dijkstra;
	private int[]  track; // the shortest path backtrack array
	private boolean[]  visited; // flag to mark visited nodes
	private int[] path; // the shortest path
	
	/**
	 * Constructor call when Graph is first initialized
	 */
	
	
	
	public Graph() 
	{
		nodes = new ArrayList<GraphNode>(); // initialize the array list nodes
		/*
		 * Read the nodes from the input text files.
		 * The nodes information includes the position on the graph,
		 * the type of node and data associated with its location
		 */
		DeviceReader in = new DeviceReader(NODES_FILE); // open the text file
		int n = Integer.valueOf(in.readln());  // read the number of nodes
		in.readln();   // skip the first line
		
		/*
         * Add an empty node at the beginning to make the id
         * of the node is also its location on the data structure
         * array list.  This is only for the convenience of accessing
         * the array list
         */
		
		nodes.add(new GraphNode()); 
		
		/*
         * Read the information associated with each node.  All the data
         * of one node are written on one line of text file.
         */
		
		for (int i = 0; i < n; i++) { 
			
			/*
		     * Read the entire line and extract information out of
             * the string by concatting different locations of the string
             */
			String s = in.readln();
			int id = Integer.valueOf(s.substring( 0, 3).trim());
			int x = Integer.valueOf(s.substring( 4, 8).trim());
			int y = Integer.valueOf(s.substring( 9, 13).trim());
			String type = s.substring(14, 22).trim(); 
			int[] pos = {x, y};
			String room = new String();
			if (s.length() > 30) { room = s.substring(23, 31).trim(); }
			String host = new String();
			if (s.length() > 32) { host = s.substring(32, s.length()); }
			
			/*
			 * Add the new nodes into the graph
			 */
			
			nodes.add( new GraphNode(id, pos, type, room, host) ); 
			}
			in.close(); // close the
			
			/*
			 * Read the edges represented by adjacent list of each node from
			 * a text file.  Each line represented all the id of the nodes adjacent
			 * with the node id at the beginning of the line.
			 */
			
			in = new DeviceReader(EDGES_FILE); // open the text file 
			in.readln(); // skip the first line 
			for (int i = 0; i < n; i++) 
			{
			
				/*
			     * Read the entire line and extract information out of
			     * the string by concatting different locations of the string
			     */
				String s = in.readln();
				int id = Integer.valueOf(s.substring(0, 2).trim()); 
				int size = Integer.valueOf(s.substring(3, 5).trim()); 
				int start = 6;
				int[] adjacent = new int[size];
				for (int j = 0; j < size; j++) 
				{
					int k = Integer.valueOf(s.substring(start, start+2 ).trim()); 
					start += 3;
					adjacent[j] = k;
				}
			    nodes.get(id).adjacent = adjacent; // update this adjacent to the node
			  }
			
			
			 in.close(); // close the edges text file
	}
	
	/**
	* Find the nearest GraphNode on the graph
	* for the specified position
	*
	* @param x the x coordination of the position
	* @param y the y coordination of the position
	* @return the GraphNode id of the nearest GraphNode 
	*/
	
	public int matchNode(int x, int y) 
	{
		float minDistance = Float.MAX_VALUE; 
		int minNode = 0;
		for (int i = 0; i < nodes.size(); i++) 
		{
			int pos[] = nodes.get(i).position;
			float d = Util.distance(x, y, pos[0], pos[1]); 
			if (d < minDistance) 
			{
	                minDistance = d;
	                minNode = i; 
	        }
		}
		return minNode;
	}
	
	/**
	* Find the route between two specified GraphNodes
	*
	* @param source the id of the source GraphNode
	* @param destination the id of the destination GraphNode 
	*/
	
	public void route(int source, int destination) 
	{ 
		initDijkstra(source, destination);
		while ( (!dijkstra.isEmpty()) && (!visited[destination]) ) 
		{
			HeapNode uHeapNode = dijkstra.min(); // get the minimum node from the top of the heap at O(1)
			dijkstra.removeMin(); // remove this node from the heap
			GraphNode u = uHeapNode.getData(); // extract the id of this node
			visited[u.id] = true;    // set flag for this GraphNode as visited
			double costu = uHeapNode.getKey();  // path cost from source to u 
			int[] adjacent = u.adjacent; // get all the adjacent nodes
			
			/*
			 * Visit each nodes adjacent with the current evaluating node u
			 */
			for (int i = 0; i < adjacent.length; i++) 
			{
				GraphNode v = nodes.get(adjacent[i]); // get the node v adjacent with u
				double duv = distance(u.id, v.id); // distance between u and v
				
				 /*
			      *  When v has not been in the heap yet, which mean v has not been
			      *  reachable before, we then add v into the heap
			      */
				if (v.heap == null) 
				{
					HeapNode vHeapNode = new HeapNode(v, costu + duv); // the heap node containing the source node
					v.heap = vHeapNode; 
					track[v.id] = u.id; 
					dijkstra.insert(vHeapNode, costu + duv);
					
					/*
					 *  When v has already been in the heap, update the new cost to v through u
					 *  if the new cost (key of the heap) is smaller
					 */
					
				} else 
				{
					double costv = v.heap.getKey(); // current cost from source to v
					if (costv > costu + duv) 
					{ 
						dijkstra.decreaseKey(v.heap, costu + duv); // update the distance in the heap
						track[v.id] = u.id; // update the track
					}
				}
			}
		} // dijkstra loop
		
		 track(source, destination);
	}
	
	
	/*
	 * initialize values used in dijkstra algorithm
	 *
	 * @param source id of the source GraphNode
	 * @param destination id of the destination GraphNode 
	 */
					
	private void initDijkstra(int source, int destination) 
	{			
		int size = nodes.size();
		dijkstra  = new FibonacciHeap();
		track  = new int[size];
		visited  = new boolean[size];
		
		Arrays.fill(track, 0); // clear the track array 
		Arrays.fill(visited, false); // make the visited set empty
		
		 // clear the old heap reference in the nodes list
		for (int i = 0; i < nodes.size(); i++)
		{ 
			nodes.get(i).heap = null;
		}
		
		/*
         * Set the source GraphNode dijkstra value = 0
         * and put the source GraphNode at the top of the heap
         */
		
		  GraphNode sGraphNode = nodes.get(source); // the source node in the graph
		  HeapNode sHeapNode = new HeapNode(sGraphNode, 0); // the heap node containing the source node
		  sGraphNode.heap = sHeapNode; // put a reference of the heap node pointer
		  dijkstra.insert(sHeapNode, 0);
    }
	
	/**
	 * Create the path found by the algorithm from source
	 * to destination using the track array recorded
	 *	
	 * @param source the id of the source node
	 * @param destination the id of the destination node 
	 */
		  
	private void track(int source, int destination)
	{ 
		int[] temp = new int[nodes.size()];
		int count = 0;
		int now = destination;
		
		/*
		 * Get the path of the shortest route using the track array
		 * by tracing reversely from the destination back to the source
		 */
		while (now != 0)
		{ 
			temp[count] = now;
		    now = track[now];
		    count++;
		}
	    
		/*
	     * Reverse the path array to make the path go from source
	     * to destination.
	     */
		path = new int[count];
		for (int i = 0; i < count; i++) 
		       path[i] = temp[count-i-1];
	    
	}
	
	/**
	* Return the distance between two GraphNodes by id 
	* @param source 	the id of the source node
	* @param destination   the id of the destination node
	* @return    the distance between the two nodes 
	*/
	
	private float distance(int source, int destination)
	{ 
		int[] s = nodes.get(source).position;
		int[] d = nodes.get(destination).position;
		return Util.distance(s, d);
	}

	/*
     * ============================================================================
     * get & set methods
     * ============================================================================
     */
	public ArrayList<GraphNode> getGraph()
	{ 
		return nodes;
	}
	
	public int[] getPath() 
	{ 
		return path;
	} 	


	//Added Function for MapActivity class : 

	public void addNode(GraphNode node)
	{
		nodes.add(node);       	
	
	}	
	
}

		
	
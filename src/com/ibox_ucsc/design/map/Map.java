package com.ibox_ucsc.design.map;

import java.util.ArrayList; 
import com.ibox_ucsc.design.util.Util;

public class Map 
{
	public static final float SCALE = 0.033264f; // 1 pixel = 0.1 meter
	public static final float  NORTH = -0.221992086112412f;
	
	public static final int 	DATABASE_PERSON = 1; 
	public static final int		DATABASE_ROOM = 2;
	
	public static int			GRAPH_SIZE = 0;  // Added variable for MapActivity Class
	
	private static Object 		lockGraph;
	private static Graph		graph;
	private static Object  		lockArchitecture; 
	private static Architecture	architecture;
	private static Object 		lockRouting;
	private static boolean 		flagRouting; 
	private static GraphNode 	source;
	private static GraphNode	destination;
		
	/**
	 * Initialize all the variables
	 */
	
	public static void initialize() 
	{
		lockGraph	 		= new Object(); 
		lockArchitecture    = new Object();
		graph				= new Graph();
		architecture 		= new Architecture();
		
		lockRouting = new Object(); 
		flagRouting = false;
		source = new GraphNode(); 
		destination = new GraphNode();
	}
	
	/*
	 * ============================================================================
	 * Graph access and management
	 * ============================================================================
	 */
	
	public static ArrayList<GraphNode> getNodes() 
	{ 
		ArrayList<GraphNode> copy = new ArrayList<GraphNode>();
		synchronized(lockGraph) 
		{
			ArrayList<GraphNode> nodes = graph.getGraph(); 
			for (int i = 0; i < nodes.size(); i++) 
			{
				GraphNode node = new GraphNode(nodes.get(i));
	            copy.add(node);
	        }
			
			GRAPH_SIZE = nodes.size(); // Added for MapActivity Class
		}
		return copy; 
	}
	

// Added Function for MapActivity class 

	public static void addNode(GraphNode node)
	{
		graph.addNode(node);    
		GRAPH_SIZE++;
		
	}	

// Added Function for MapActivity Class
	
	public static int getGraphSize()
	{
		return GRAPH_SIZE;
	}
	
// Added Function for MapActivity Class 
	
	public static void removeLast() 
	{ 
		ArrayList<GraphNode> nodes = getNodes();
		nodes.remove(nodes.size());
	}
	
///////////////////////////	
	public static String[] getDatabase(int key) 
	{ 
		ArrayList<String> s = new ArrayList<String>(); 
		ArrayList<GraphNode> nodes = getNodes();
		for (int i = 1; i < nodes.size(); i++) 
		{
			if ( ((key & DATABASE_ROOM) > 0) && (nodes.get(i).room.length() > 0))
				s.add(nodes.get(i).room);
			if ( ((key & DATABASE_PERSON) > 0) && (nodes.get(i).person.length() > 0))
				s.add(nodes.get(i).person);
		}
		String[] r = new String[s.size()]; 
		s.toArray(r);
		return r;
	}
	
	public static String[] getRooms() 
	{
		return getDatabase(DATABASE_ROOM);
	}
	
	public static String[] getPeople()
	{ 
		return getDatabase(DATABASE_PERSON);
	}
	
	public static GraphNode searchDetail(String key)
	{ 
		ArrayList<GraphNode> nodes = getNodes();
		for (int i = 1; i < nodes.size(); i++) 
		{ 
			GraphNode node = nodes.get(i);
			if ((node.room.indexOf(key) >= 0) || (node.person.indexOf(key) >= 0)) 
				return node;	 
		}
		return null; 
	}
	
	public static GraphNode searchId(int key) 
	{ 
		ArrayList<GraphNode> nodes = getNodes(); 
		for (int i = 1; i < nodes.size(); i++) 
		{	
			GraphNode node = nodes.get(i); 
			if ((node.id == key)) 
				return node; 
		}
		return null; 
	}
	
	/**
	* Map matching algorithm 
	* @param x
	* @param y
	* @return
	*/
	
	public static int[] mapMatching(int x, int y) 
	{ 
		int[] match = new int[2]; 
		ArrayList<GraphNode> nodes = getNodes(); 
		int min = Integer.MAX_VALUE;
		
		for (int i = 1; i < nodes.size(); i++)
		{
			GraphNode a = nodes.get(i);
			for (int j = 0; j < a.adjacent.length; j++) 
			{
				if (a.adjacent[j] > i)
				{
					GraphNode b = nodes.get(a.adjacent[j]);
					int[] online = new int[2];
					int distance = Util.distancePointLine(x, y, a.position,b.position, online);
					if (distance < min) 
					{
						min = distance;
					    match[0] = online[0];
					    match[1] = online[1];
					}
				} 
			}
		}
		return match;
	}
	
	/*
	 * ============================================================================
	 * Synchronized routing
	 * ============================================================================
	 */
	public static boolean isRouting()
	{ 
		boolean b;
		synchronized (lockRouting)
		{ 
			b = flagRouting;
		}
		return b;
	}
	
	public static void enableRouting() 
	{ 
		graph.route(source.id, destination.id); 
		synchronized (lockRouting) 
		{
			flagRouting = true; 
		}
	}
	
	public static void disableRouting() 
	{ 
		synchronized (lockRouting) 
		{
			flagRouting = false; 
		}
	}
	
	public static void setRoutingSource(GraphNode node) 
	{ 
		synchronized (lockRouting) 
		{
			source = node;
		}
	}
	
	public static void setRoutingDestination(GraphNode node) 
	{ 
		synchronized (lockRouting) 
		{
			destination = node; 
		}
	}
	
	public static GraphNode getRoutingSource() 
	{ 
		GraphNode node;
		synchronized (lockRouting) 
		{
			node = new GraphNode(source);
		}
		return node;
	}
	
	public static GraphNode getRoutingDestination() 
	{
		GraphNode node;
		synchronized (lockRouting) 
		{
			node = new GraphNode(destination); 
		}
		return node; 
	}
	
	public static int[] getPath() 
	{
		int[] path = Util.copyArray(graph.getPath()); 
		return path;
	}
	
	/*
	 * ============================================================================
	 * Miscellaneous methods
	 * ============================================================================
	 */
	
	/*
	 * Convert to map coordination system
	 *
	 * Return the distance move in pixel in the frame system * of the current building with specified offset angle
	 *
	 * @param north the distance toward north in meter
	 * @param east the distance toward east in meter
	 * @return the distance in x and y axis in pixel
	 */
	
	public static float[] toMapCS(float[] csEarth) 
	{
		
		// Input coordinate system:
		// ^ y = north
		// |
		// |
		// |
		// |
		// o---------> x = east
		//// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Building coordinate system
		//
		// 		 y \ a   |north/ x
		//			\    |    /
		// 			 \   |	 / 
		//			  \  |  /
		//			   \ | /
		//				\|/
		//				 o
		//
		// Roration Matrix for rotate the coordinate system "a" degree
		//
		//  [   cos a    sin a ] * [ x ]
		//  [ - sin a    cos a ]   [ y ] 
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Map coordinate system
		//				north
		// 		    \ a  |	  /
		// 			 \   |   / x
		//			  \  |  /
		//			   \ | /
		//				\|/
		//				 o
		//				  \
		//				   \
		//					\
		//					 \ y
		//
		// Flip Matrix
		//
		// [ 1   0  ] * [ x ]
		// [ 0  -1  ]   [ y ]
		
		float[] csBuilding  = new float[2]; // building coordinate system
		float[] csMap = new float[2]; // map coordinate system
		float a = -Map.NORTH; // rotation angle (counterclockwise)
		
		// rotate earth coordinate system to get building coordinate system
        // [   cos a     sin a   ] * [ x ]
        // [ - sin a     cos a   ]   [ y ]
		
		csBuilding[0] = (float) ( Math.cos(a)*csEarth[0] + Math.sin(a)*csEarth[1]); csBuilding[1] = (float) (- Math.sin(a)*csEarth[1] + Math.cos(a)*csEarth[1]);
        
		// flip building coordinate system and convert from meters to pixels
        // to get map coordinate system
        // [ 1   0  ] * [ x ]
        // [ 0  -1  ]   [ y ]
        csMap[0] =  csBuilding[0];
        csMap[1] = -csBuilding[1];
        return csMap; 
        
	}
		
	/*
     * Convert the vector from meter to pixels
     * @param meter The vector in meter * @return The vector in pixel
     */

	public static int[] toMapScale(float[] meter) 
	{ 
		return toMapScale(meter[0], meter[1]);
	}
	/*
	 * Convert the vector from meter to pixels
	 *
	 * @param x the x coordination point right in meter * @param y the y coordination point bottom in meter 
	 * 
	 * @return the vector in pixel
	 */

	public static int[] toMapScale(float x, float y) 
	{ 
		int[] pixel = new int[2];
		pixel[0] = (int)( x / Map.SCALE);
		pixel[1] = (int)( y / Map.SCALE);
		return pixel; 
	}
}	
		
		
		
		
		
	
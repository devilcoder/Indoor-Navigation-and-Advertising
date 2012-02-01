package com.ibox_ucsc.design.map;

import java.util.ArrayList;
import java.util.List; 
import java.util.Stack;

/**
 * This class implements a Fibonacci heap data structure used for Dijkstra algorithm
 *
 * The amortized running time of most of these methods is O(1).
 * Several have an actual running time of O(1).
 *
 * removeMin() and delete() have O(log n) amortized running times because they do the heap consolidation.
 */

public class FibonacciHeap 
{
	private static final double oneOverLogPhi = 1.0 / Math.log((1.0 + Math.sqrt(5.0)) / 2.0);
	private HeapNode minNode; // pointer to the minimum node in the heap
	private int nNodes; // number of node in the heap
	
	/**
	 * Constructs a FibonacciHeap object that contains no elements.
	 */
	
	public FibonacciHeap() { } // DijkstraHeap()
   
	/**
     * Tests if the Fibonacci heap is empty or not.  Returns true if the heap is
     * empty, false otherwise.
     *
     * @return true if the heap is empty, false otherwise 
     */
	
	public boolean isEmpty()
	{ 
		return minNode == null;
	} // isEmpty
	   
	/**
	 * Removes all elements from this heap.
	 */
	
	public void clear() 
	{ 
		minNode = null;
		nNodes = 0;
	} // clear
	
	/**
	 * Decreases the key value for a heap node, given the new value to take on. 
	 * The structure of the heap may be changed and will not be consolidated.
	 *
	 * <p>Running time: O(1) amortized<p>
	 *
	 * @param x node to decrease the key of
	 * @param k new key value for node x
	 *
	 * @exception IllegalArgumentException Thrown if k is larger than x.key
	 * value.
	 */
	
	public void decreaseKey(HeapNode x, double k) 
	{
		if (k > x.key) 
			throw new IllegalArgumentException( "decreaseKey() got larger key value");
		x.key = k;
	    HeapNode y = x.parent;
	     
	    if ((y != null) && (x.key < y.key)) 
	    { 
	    	cut(x, y);
	        cascadingCut(y);
	     }
	
	    if (x.key < minNode.key)
	    	minNode = x; 
	
	} // decreaseKey
	
	/**
	* Deletes a node from the heap given the reference to the node. The trees 
	* in the heap will be consolidated, if necessary. This operation may fail 
	* to remove the correct element if there are nodes with key value
	* -Infinity.
	*
	* <p>Running time: O(log n) amortized</p>
	*
	* @param x node to remove from heap
	*/
	
	public void delete(HeapNode x)
	{
		decreaseKey(x, Double.NEGATIVE_INFINITY); // make x as small as possible
		removeMin(); // remove the smallest, which decreases n also
	} // delete
		
	/**
	* Inserts a new data element into the heap. No heap consolidation is
	* performed at this time, the new node is simply inserted into the root 
	* list of this heap.
	*
	* <p>Running time: O(1) actual</p>
	*
	* @param node new node to insert into heap
	* @param key key value associated with data object
	*/
	
	public void insert(HeapNode node, double key) 
	{ 
		node.key = key; 
	    // concatenate node into min list
		if (minNode != null) 
		{ 
			node.left = minNode; 
			node.right = minNode.right; 
			minNode.right = node; 
			node.right.left = node;
			if (key < minNode.key) 
				minNode = node;
		} else 
			minNode = node;
        nNodes++;
	} // insert

	/**
	* Returns the smallest element in the heap. This smallest element is the 
	* one with the minimum key value.
	*
	* <p>Running time: O(1) actual</p>
	*
	* @return heap node with the smallest key
	*/
	
	public HeapNode min() 
	{ 
		return minNode;
	} // min
	
	/**
	* Removes the smallest element from the heap. This will cause the trees in 
	* the heap to be consolidated, if necessary.
	*
	* <p>Running time: O(log n) amortized</p>
	*
	* @return node with the smallest key
	*/
	
	public HeapNode removeMin() 
	{ 
		HeapNode z = minNode;
		if (z != null) 
		{
			int numKids = z.degree; 
			HeapNode x = z.child; 
			HeapNode tempRight;
	        // for each child of z do...
			while (numKids > 0) 
			{ 
				tempRight = x.right;
			    // remove x from child list
	            x.left.right = x.right;
	            x.right.left = x.left;
	            // add x to root list of heap
	            x.left = minNode;
	            x.right = minNode.right;
	            minNode.right = x;
	            x.right.left = x;
	            // set parent[x] to null
	            x.parent = null; 
	            x = tempRight; numKids--;
			}
	        // remove z from root list of heap
	        z.left.right = z.right;
	        z.right.left = z.left;
	        if (z == z.right) 
	        	minNode = null;
	        else 
	        {
	        	minNode = z.right; 
	        	consolidate();
	        }
	        // decrement size of heap
	        nNodes--; 
	    }
		return z; 
	} // removeMin
	
	/**
	* Returns the size of the heap which is measured in the number of elements 
	* contained in the heap.
	*
	* <p>Running time: O(1) actual</p>
	*
	* @return number of elements in the heap
	*/

	public int size() 
	{ 
		return nNodes;
	} // size
	
	/**
	 * Joins two Fibonacci heaps into a new one.  No heap consolidation is
	 * performed at this time.  The two root lists are simply joined together.
	 *
	 * <p>Running time: O(1) actual</p>
	 *
	* @param h1 first heap
	* @param h2 second heap
	*
	* @return new heap containing h1 and h2 */
	
	public static FibonacciHeap union( FibonacciHeap h1, FibonacciHeap h2) 
	{ 
		FibonacciHeap h = new FibonacciHeap();
		if ((h1 != null) && (h2 != null)) 
		{
			h.minNode = h1.minNode;
			if (h.minNode != null) 
			{
				if (h2.minNode != null)
				{
					h.minNode.right.left = h2.minNode.left;
	                h2.minNode.left.right = h.minNode.right;
	                h.minNode.right = h2.minNode;
	                h2.minNode.left = h.minNode;
	                if (h2.minNode.key < h1.minNode.key) 
	                	h.minNode = h2.minNode;
	             } 
			} else 
	        	h.minNode = h2.minNode;
	    	h.nNodes = h1.nNodes + h2.nNodes;
	    }
		return h; 
	} // union
	
	/**
	 * Creates a String representation of this Fibonacci heap.
	 *
	 * @return String of this. 
	 */
	
	public String toString() 
	{
		if (minNode == null) 
			return "FibonacciHeap=[]";
		// create a new stack and put root on it
		
		Stack<HeapNode> stack = new Stack<HeapNode>(); 
		stack.push(minNode);
		StringBuffer buf = new StringBuffer(512); 
		buf.append("FibonacciHeap=[");
		
		// do a simple breadth-first traversal on the tree
		
		while (!stack.empty()) 
		{ 
			HeapNode curr = stack.pop(); 
			buf.append(curr); 
			buf.append(", ");
			if (curr.child != null)
				stack.push(curr.child);
		    HeapNode start = curr;
		    curr = curr.right;
		    while (curr != start) 
		    { 
		    	buf.append(curr); buf.append(", ");
		        if (curr.child != null) { stack.push(curr.child);
		        }
		        curr = curr.right;
		    }
		}
		buf.append(']');
		return buf.toString(); 
	} // toString
	
	/**
	* Performs a cascading cut operation. This cuts y from its parent and then 
	* does the same for its parent, and so on up the tree.
	*
	* <p>Running time: O(log n); O(1) excluding the recursion</p>
	*
	* @param y node to perform cascading cut on
	*/
	
	protected void cascadingCut(HeapNode y) 
	{ 
		HeapNode z = y.parent;
		if (z != null) 
		{ // if there's a parent...
			if (!y.mark)  // if y is unmarked, set it marked
				y.mark = true; 
			else 
			{
				cut(y, z); // it's marked, cut it from parent
				cascadingCut(z); // cut its parent as well
			} 
		}
	} // cascadingCut
		
	protected void consolidate() 
	{
		int arraySize = ((int) Math.floor(Math.log(nNodes) * oneOverLogPhi)) + 1; 
		List<HeapNode> array = new ArrayList<HeapNode>(arraySize);
		
		// Initialize degree array
		
		for (int i = 0; i < arraySize; i++) { array.add(null); }
		// Find the number of root nodes.
		int numRoots = 0; HeapNode x = minNode;
		if (x != null) 
		{ 
			numRoots++;
			x = x.right;
			while (x != minNode) 
			{ 
				numRoots++;
				x = x.right; 
			}
		}
		
		// For each node in root list do...
		while (numRoots > 0) 
		{
			// Access this node's degree.. 
			int d = x.degree;
			HeapNode next = x.right;
		    
			// ..and see if there's another of the same degree.
			for (;;) 
			{
				HeapNode y = array.get(d); 
				if (y == null) 
				{
					// Nope.
					break; 
				}
		
				// There is, make one of the nodes a child of the other.
				// Do this based on the key value.
				if (x.key > y.key) 
				{
		            HeapNode temp = y;
		            y = x;
		            x = temp;
				}
		        // HeapNode y disappears from root list.
			
				link(y, x);
		        
				// We've handled this degree, go to next one.
				
				array.set(d, null);
				d++; 
			}
		    
			// Save this node for later when we might encounter another
		    // of the same degree.
		    
			array.set(d, x);
		    
			// Move forward through list.
			x = next;
		    numRoots--;
		}
		
		// Set min to null (effectively losing the root list) and
		// reconstruct the root list from the array entries in array[].
		
		minNode = null;
		
		for (int i = 0; i < arraySize; i++) 
		{ 
			HeapNode y = array.get(i);
			if (y == null) 
			{ 
				continue;
			}
		    
			// We've got a live one, add it to root list.
			if (minNode != null) 
			{
				// First remove node from root list.
				y.left.right = y.right;
				y.right.left = y.left;
		        
				// Now add to root list, again.
		        y.left = minNode;
		        y.right = minNode.right;
		        minNode.right = y;
		        y.right.left = y;
		       
		        // Check if this is a new min.
		        if (y.key < minNode.key) 
		        { 
		        	minNode = y;
		        }
			} else 
			{
				minNode = y; 
			}
		}
	} // consolidate
		
   /**
	* The reverse of the link operation: removes x from the child list of y.
	* This method assumes that min is non-null.
	*
	* <p>Running time: O(1)</p>
	*
	* @param x child of y to be removed from y's child list 
	* @param y parent of x about to lose a child
	*/
	
	protected void cut(HeapNode x, HeapNode y) 
	{
		// remove x from childlist of y and decrement degree[y]
	    x.left.right = x.right;
	    x.right.left = x.left;
	    y.degree--;
	    // reset y.child if necessary
	    if (y.child == x)
	    { 
	    	y.child = x.right;
	    }
	    if (y.degree == 0) 
	    { 
	    	y.child = null;
	    }
	   
	    // add x to root list of heap
	    x.left = minNode;
	    x.right = minNode.right;
	    minNode.right = x;
	    x.right.left = x;
	   
	    // set parent[x] to nil
	    x.parent = null;
	    
	    // set mark[x] to false
	    x.mark = false; 
	
	} // cut
	
	/**
	* Make node y a child of node x.
	*
	* <p>Running time: O(1) actual</p> *
	* @param y node to become child
	* @param x node to become parent 
	*/
	
	protected void link(HeapNode y, HeapNode x) 
	{
		// remove y from root list of heap y.left.right = y.right;
		y.right.left = y.left;
	   
		// make y a child of x
		y.parent = x;
		if (x.child == null) 
		{ 
			x.child = y;
			y.right = y;
			y.left = y; 
		} else 
		{
	        y.left = x.child;
	        y.right = x.child.right;
	        x.child.right = y;
	        y.right.left = y;
		}
	     
		// increase degree[x]
		x.degree++;
	    
		// set mark[y] false
		y.mark = false; 
		
	} // link

}
	
	
	
	
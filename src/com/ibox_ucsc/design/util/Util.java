package com.ibox_ucsc.design.util; public class Util {
     /*
      * ============================================================================
      * Compiler methods
      * ============================================================================
      */
	  /**
	   * Return the number of count-highest elements of an array. 
	   * The returning array at position i in a is true only when 
	   * a[i] is one of count-highest elements in a
	   *
	   * @param a
	   * @param count
	   * @return 
	   * The input array
	   * The number of highest elements to find
	   * The boolean array indicating highest elements
	   */
	
	public static boolean[] highest(int[] a, int count) 
	{ 	
		int size = a.length;
		boolean[] highest = new boolean[size]; 
		int[] b = new int[size]; 
		int[] c = new int[size];
		
		/*
		 * Initialize b and c and highest
		 * b is a clone array of a
		 * c is an index array to track the changes position
		 * of b when sorting
		 */
		
		for (int i = 0; i < size; i++) 
		{ 
			b[i] = a[i];
			c[i] = i;
			highest[i] = false; 
		}
			
			// sort the b array in ascending order
			for (int i = 0; i < size-1; i++)
				for (int j = i+1; j < size; j++)
					if (b[i] < b[j]) 
					{ 
						int temp;
						temp = b[i]; b[i] = b[j]; b[j] = temp;
						temp = c[i]; c[i] = c[j]; c[j] = temp;
					}
		for (int i = 0; i < count; i++) 
			highest[c[i]] = true; 
		
		return highest;
	}
	
	public static void copyArray(int[] a, int[] b) 
	{ 
		for (int i = 0; i < a.length; i++) 
			a[i] = b[i];
    }

	public static void copyArray(float[] a, float[] b) 
	{
		for (int i = 0; i < a.length; i++) 
			a[i] = b[i]; 
	}

	public static int[] copyArray(int[] a) 
	{
		int[] r = new int[a.length];
		for (int i = 0; i < a.length; i++) 
			r[i] = a[i]; return r;
	}
	
	public static float[] copyArray(float[] a) 
	{
		float[] r = new float[a.length];
		for (int i = 0; i < a.length; i++) 
						r[i] = a[i]; 
		return r;
	}
	
	/*
	 * ============================================================================
	 * Mathematics methods
	 * ============================================================================
	 */

	public static int getMin(int a, int b) 
	{ 
		if (a > b) 
			return b;
		else 
			return a; 
	}
	
	public static int getMax(int a, int b) 
	{ 
		if (a > b) 
			return a;
		else 
			return b; 
		
	}
	
	public static boolean between(int x, int a, int b) 
	{
		int min = getMin(a, b);
		int max = getMax(a, b);
		if ((x>=min) && ( x<= max))  return true; 
		else  return false; 
		
	}
	
	/**
	 * Return the magnitude of the input vector, which is
	 * the squared root of the sum of squared value
	 *
	 * @param v The input vector
	 * @return The magnitude of the vector */

	public static float magnitude(float[] v) 
	{ 
		float result = 0;
		for (int i = 0; i < v.length; i++)  
			result += square(v[i]);
		result = (float) Math.sqrt(result); 
		return result;
	}

	public static double magnitude(double x, double y) 
	{ 
		return Math.sqrt(square(x) + square(y));
	}
	
	public static int[] translate(int[] p, int originX, int originY) 
	{ 
		return translate(p[0], p[1], originX, originY);
		
	}
	
	public static int[] translate(int x, int y, int originX, int originY)
	{ 
		int[] t = new int[2];
		t[0] = x + originX;
		t[1] = y + originY;
		return t; 
	}
	
	public static int distancePointLine(int x, int y, int[] p1, int[] p2, int[] m) 
	{
		int distance = Integer.MAX_VALUE;
		if (p1[0] == p2[0]) 
		{
			if (between(y, p1[1], p2[1])) 
			{ 
				distance = Math.abs(p1[0] - x); 
				m[0] = p1[0];
				m[1] = y;
			} else 
			  {
				if (distance(x, y, p1[0], p1[1]) > distance(x, y, p2[0], p2[1]))
				{	m[0] = p1[0];
					m[1] = p1[1];
					distance = (int) distance(x, y, p1[0], p1[1]);
				} else 
				  {
					m[0] = p2[0];
					m[1] = p2[1];
					distance = (int) distance(x, y, p2[0], p2[1]); 
				  }
			  }
	 	} else if (p1[1] == p2[1]) 
	 			{
	 				if (between(x, p1[0], p2[0])) 
	 				{ 
	 					distance = Math.abs(p1[1] - y);
	 					m[0] = x;
	 					m[1] = p1[1];
	 				} else 
	 				  {
	 					if (distance(x, y, p1[0], p1[1]) > distance(x, y, p2[0], p2[1]))
	 					{
	 						m[0] = p1[0];
	 						m[1] = p1[1];
	 						distance = (int) distance(x, y, p1[0], p1[1]);
	 					} else 
	 					  {
	 						m[0] = p2[0];
	 						m[1] = p2[1];
	 						distance = (int) distance(x, y, p2[0], p2[1]); 
	 					  }
	 				  }
	 			}
		return distance; 
	}
	
	public static float distance(double[] p1, double[] p2) 
	{ 
		return distance(p1[0], p1[1], p2[0], p2[1]);
	}
	
	public static float distance(float[] p1, float[] p2) 
	{ 
		return distance(p1[0], p1[1], p2[0], p2[1]);
	}
	
	public static float distance(int[] p1, int[] p2) 
	{ 
		return distance(p1[0], p1[1], p2[0], p2[1]);
	}
	
	public static float distance(int x1, int y1, int x2, int y2) 
	{ 
		return (float) Math.sqrt(square(x1 - x2) + square(y1 - y2));
	};
	
	public static float distance(float x1, float y1, float x2, float y2) 
	{ 
		return (float) Math.sqrt(square(x1 - x2) + square(y1 - y2));
	};
	
	public static float distance(double x1, double y1, double x2, double y2) 
	{ 
		return (float) Math.sqrt(square(x1 - x2) + square(y1 - y2));
	};
	
	public static int square(int n) 
	{ 
		return n*n;
	}
	
	public static float square(float n) 
	{ 
		return n*n;
	}
	
	public static double square(double n) 
	{ 
		return n*n;
	}
	
	/*
	 * ============================================================================
	 * Other methods
	 * ============================================================================
	 */
	
	/**
	* Adjust angle to be between 0 and 360 degrees * @param degrees
	* @return new angle
	*/
	
	public static float adjustAngle(float degrees) 
	{
		while (degrees < 0) 
		{ 
			degrees += (float) 360.0; 
		} 
		while (degrees > 360) 
		{ 
			degrees -= (float) 360.0; 
		} 
		
		return degrees;
	}
	
	public static boolean inside(int x, int y, int left, int top, int right, int bottom) 
	{
		if ((x < left) || (x > right) || (y < top) || (y > bottom))  return false;
		else  return true; 
		
	}
	
}	

	
	
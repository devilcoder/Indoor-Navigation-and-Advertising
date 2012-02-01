package com.ibox_ucsc.design.positioning;

import com.ibox_ucsc.design.map.GraphEdge; 
import com.ibox_ucsc.design.map.Map; 
import com.ibox_ucsc.design.util.Util;

public class Positioning 
{
	private static final float GAUSSIAN_POLAR_THETA_ERROR = (float) Math.PI / 4.0f;
	private static final float GAUSSIAN_POLAR_RADIUS_ERROR = 1.0f;
	private static final float  GAUSSIAN_CARTESIAN_ERROR = 2.0f; // 2 meters
	private static final int GAUSSIAN_SIZE = 3; // 3 meters
	private static final int CONVERGING_PERIOD = 5000; // 10 seconds
	private static InertialNavigationSystem ins;
	private static WifiPositioning wifi;
	private static Object insLock = new Object();
	private static Object wifiLock = new Object();
	private static float[][] pdf = new float[RadioMap.ROW][RadioMap.COL];
	
	public static void initialize() 
	{
		wifi = new WifiPositioning();
		ins = new InertialNavigationSystem();
	}
	
	/*
	 * ------------------------------------------------------------------------
	 *   WIFI POSITIONING
	 * ------------------------------------------------------------------------
	 */
	
	public static int[] getWifiPosition() 
	{
		synchronized (wifiLock) 
		{ return wifi.position(); }
	}
	
	public static void updateWifi(int[] rssi) 
	{
		synchronized (wifiLock) { wifi.addMeasurement(rssi); }
	}
	
	public static int findBSSID(String bssid) 
	{
	synchronized (wifiLock) { return wifi.findBSSID(bssid); }
	}
	
	/*
	 * ------------------------------------------------------------------------
	 *   INERTIAL NAVIGATION SYSTEM
	 * ------------------------------------------------------------------------
	 */
	
	public static void updateINS() 
	{
		synchronized (insLock) { ins.addMotion(); }
	}
	
	public static float[] getDisplacement() 
	{
		synchronized (insLock) { return ins.displacement(); }
	}
	
	public static void resetINS() 
	{
		synchronized (insLock) { ins.reset(); }
	}
	
	/*
	 * ------------------------------------------------------------------------
	 *   POSITIONING INTEGRATION
	 * ------------------------------------------------------------------------
	 */
	public static void process() 
	{ 
		/*
	     * If the initial position has already been determined,
	     * process the data by fusing different positioning methods
	     */
		
		if (!isConverging()) 
			integrate();
	  
			/*
			 * If the initial position has not yet been determined,
			 * evaluate for the given period of time to estimate
			 * the position
			 */
		else if (timeConverging() == 0) 
		{
				timeConverging(System.currentTimeMillis());
				pdf = wifi.correlate(); 
		} else 
			{
				long now = System.currentTimeMillis();
				if (timeConverging() - now > CONVERGING_PERIOD) 
				{
					stopConverging();
					timeConverging(0); 
				}
				integrate(); 
			}
	} 
	
	public static void integrate() 
	{ 
		/*
	     *  The probability density function (PDF) of the next position
	     *  computed from the PDF of the last position, the new displacement
	     *  vector from the inertial navigation system (INS), and the probability
	     *  model from the simulated wi-fi positioning system (SWP)
	     */
	     // d is the displacement vector returned from the INS
		
		float[] d = getDisplacement(); 
		d = Map.toMapCS(d); 
		displacement(d);
		
		/*
	     * Clear all the INS displacement from the previous PDF. The future INS
	     * displacement will be referenced from this PDF.
	     */
		
		resetINS(); 
		
		/*
	     * Convert the displacement vector from Cartesian coordination system
	     * to the polar coordination system. The angle theta and the radius r
	     * are the component of the displacement vector in polar coordination system
	     */
		
		double theta = Math.atan2(d[1], d[0]); 
		double r = Util.magnitude(d);
		
		/*
		 * The current probability density function PDF has the size
		 * of the building map with the same geometric resolution as
		 * the simulated propagation model
		 */
		
		float[][] newpdf = new float[RadioMap.ROW][RadioMap.COL];
		
		/*
		 *  The effect radius is the size to be computed of the Gaussian
		 *  distribution model in polar coordinate system. The EFFECT_RADIUS
		 *  is measured in meter. The size of the array is equal the radius times
		 *  the resolution, which is the number of samples per meter
		 */
		
		int size = GAUSSIAN_SIZE * RadioMap.RESOLUTION; 
		
		/*
		 * The origin index of the Gaussian probability model. The reason we need this
		 * is because we cannot declare an array with negative index in java.
		 */
		
		int originx = size;
		int originy = size;
		float[][] gauss = new float[size*2+1][size*2+1];
		
		if (r != 0) 
		{
			/*
			 * The variance of the radius and the angle theta of displacement vector
			 * in a Gaussian probability density function
			 */
			
			double varR = Util.square(GAUSSIAN_POLAR_RADIUS_ERROR*r); 
			double varT = Util.square(GAUSSIAN_POLAR_THETA_ERROR);
		
			for (int i = -size; i <= size; i++) 
				for (int j = -size; j <= size; j++) 
				{
					double x = j/2.0; // x is the coordination in meter double 
					double y = i/2.0; // y is the coordination in meter 
					
					double diffsqrR = Util.square(Util.magnitude(x, y) - r); 
					double diffsqrT = Util.square(Math.atan2(y, x) - theta); 
					gauss[i + originy][j + originx] = (float) Math.exp(- diffsqrR/(2*varR + 1) - diffsqrT/(2*varT*r));
				} 
		} else 
		{
			double var = Util.square(GAUSSIAN_CARTESIAN_ERROR); for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) 
			{
				double x = j/2.0; // x is the coordination in meter 
				double y = i/2.0; // y is the coordination in meter 
				gauss[i + originy][j + originx] = (float) Math.exp( - Util.square(x)/(2*var) - Util.square(y)/(2*var));
				
			}
		} 

		float[][] wp = wifi.correlate(); 
		float max = Float.MIN_VALUE; 
		int[] maxpos = new int[2];
		for (int i = 0; i < RadioMap.ROW; i++) 
			for (int j = 0; j < RadioMap.COL; j++) 
			{
		          newpdf[i][j] = 0;
		          
		          /*
		           * The previous PDF is used as the confident of the last position
		           * With the gaussian distribution from the displacement vector,
		           * using the probability from the last PDF as a weight, we then sum
		           * all these gaussian accross the entire old PDF to get new PDF.
		           */
		          
		          for (int ig = -size; ig <= size; ig++) 
		        	  for (int jg = -size; jg <= size; jg++) 
		        		if ((i + ig >= 0) && (i + ig < RadioMap.ROW)) 
		        			if ((j + jg >= 0) && (j + jg < RadioMap.COL)) 
		        				newpdf[i][j] += pdf[i + ig][j + jg] * gauss[originy - ig][originx - jg];
		          
		          /*
		           * Correlate the PDF with the wifi positioning system PDF
		           */
		          
		          newpdf[i][j] *= wp[i][j];
		          
		          if (newpdf[i][j] > max) 
		          { 
		        	  max = newpdf[i][j];
		              maxpos[0] = j;
		              maxpos[1] = i;
		          }
			}
		        	  
		for (int i = 0; i < RadioMap.ROW; i++) 
		{
			for (int j = 0; j < RadioMap.COL; j++)
			{
			          newpdf[i][j] /= max;
			}
		}
		pdf = newpdf;
		
		float[] pos = RadioMap.toActualScale(maxpos); 
		position(pos);
		int[] mappos = Map.toMapScale(pos);
		mappos = Util.translate(mappos, -RadioMap.LEFT, -RadioMap.TOP); 
		Map.mapMatching(mappos[0], mappos[1]);  //  Map.match(mappos[0], mappos[1]); 
		
	  }
	}

	/*
	 * ------------------------------------------------------------------------
	 *   CONVERGEANCE
	 * ------------------------------------------------------------------------
	 */
	
	private static boolean isConverging;
	private static Object isConvergingLock = new Object(); 
	
	public static void startConverging() 
	{
		synchronized (isConvergingLock) { isConverging = true; } 
	}
	
	public static void stopConverging() 
	{
		synchronized (isConvergingLock) { isConverging = false; }
	}
	
	public static boolean isConverging() 
	{
		synchronized (isConvergingLock) { return isConverging; } 
	}
	
	private static long timeConverging;
	private static Object timeConvergingLock = new Object();
		
	public static void timeConverging(long time) 
	{
		synchronized (timeConvergingLock) { timeConverging = time; }
	}
	
	public static long timeConverging() 
	{
		synchronized (timeConvergingLock) { return timeConverging; }
	}
	
	/*
	 * ------------------------------------------------------------------------
	 *   SYNCHRONIZED POSITION
	 * ------------------------------------------------------------------------
	 */
	
	private static float[] position = new float[2]; 
	private static Object positionLock = new Object();
	
	public static void position(float[] p) 
	{
		synchronized (positionLock) { Util.copyArray(position, p); }
	}

	public static float[] position() 
	{
		synchronized (positionLock) { return Util.copyArray(position); }
	}
	private static float[] displacement = new float[2];
	private static Object displacementLock = new Object();
	
	public static void displacement(float[] p) 
	{
		synchronized (displacementLock) { Util.copyArray(displacement, p); }
	}
	
	public static float[] displacement() 
	{
		synchronized (displacementLock) { return Util.copyArray(displacement); }
	}
	
	/*
	 * ------------------------------------------------------------------------
	 *   SYNCHRONIZED RECORDING
	 * ------------------------------------------------------------------------
	 */
	
	private static boolean isRecording;
	private static Object isRecordingLock = new Object(); 
	
	public static void startRecording() 
	{
		synchronized (isRecordingLock) { isRecording = true; }
	}
	
	public static void stopRecording() 
	{
		synchronized (isRecordingLock) { isRecording = false; } 
	}
	
	public static boolean isRecording() 
	{
		synchronized (isRecordingLock) { return isRecording; }
	}
	
	/*
	 * ------------------------------------------------------------------------
	 *   SYNCHRONIZED POSITIONING
	 * ------------------------------------------------------------------------
	 */
	
	private static boolean isPositioning;
	private static Object isPositioningLock = new Object(); 
	
	public static void startPositioning() 
	{
		synchronized (isPositioningLock) { isPositioning = true; } 
	}
	
	public static void stopPositioning() 
	{
		synchronized (isPositioningLock) { isPositioning = false; }
	}

	public static boolean isPositioning() 
	{
		synchronized (isPositioningLock) { return isPositioning; } 
	}
	
	 /*
     * ------------------------------------------------------------------------
     *   MAP MATCHING
     * ------------------------------------------------------------------------
     */
	
	private static int[] matchedPoint = new int[2];
	private static Object matchedPointLock = new Object();
	public static void matchedPoint(int[] point) 
	{
		synchronized (matchedPointLock) 
		{ 
			Util.copyArray(matchedPoint, point); 
		}
	}
	
	public static int[] matchedPoint() 
	{
		synchronized (matchedPointLock) 
		{ 
			return Util.copyArray(matchedPoint); 
		}
	}
	private static GraphEdge matchedEdge = new GraphEdge();
	private static Object matchedEdgeLock = new Object();
	
	public static void matchedEdge(GraphEdge edge) 
	{
		synchronized (matchedEdgeLock) 
		{ 
			matchedEdge = new GraphEdge(edge); 
		}
	}
	public static GraphEdge matchedEdge() 
	{
		synchronized (matchedEdgeLock) 
		{ 
			return new GraphEdge(matchedEdge);
		}
	} 
}
	
	
	
	
	
	
	
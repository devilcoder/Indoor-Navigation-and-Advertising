package com.ibox_ucsc.design.positioning;

import android.hardware.SensorManager; 
import com.ibox_ucsc.design.util.Util;

public class DeviceSensor
{
	private static final float  MOVEMENT = 0.5f;
	private static final int WINDOW_SUMMATION_SIZE = 30; // 50 * 20 ms = 1 second
	private static final float EARTH_GRAVITY = SensorManager.GRAVITY_EARTH; // 9.80755f
	private static final float EARTH_GEOMAGNETIC = 47.0f;
	private static final float THRESHOLD_GRAVITY = 1.0f;
	private static final float THRESHOLD_GEOMAGNETIC = 3.0f;
	
	public static Object lockSensing = new Object();
	public static float[] devA = new float[3];
	public static float[] devM = new float[3];
	public static float[] devO = new float[3];
	public static float[] gravity = new float[3];
	public static float[] geomagnetic = new float[3];
	public static float[] refA = new float[3];
	public static float[] refO = new float[3];
	public static float[] mR = new float[9];
	public static float[] mI = new float[9];
	
	public static Object lockMoving  = new Object();
	public static boolean flagMoving = false;
	public static int wsPosition = 0;
	public static float wsAggregate = 0; 
	public static float[] wsSequence = new float[WINDOW_SUMMATION_SIZE];
	
	/**
	 * Set the accelerometer sample values
	 * and calibrate the data if required
	 *
	 * * @param raw The raw output from accelerometer 
	 */
	
	public static void setDevA(float[] raw) 
	{	
		synchronized (lockSensing) 
		{
			Util.copyArray(devA, raw);
			
			/*
			 * Add new acceleration to the window summation
			 * sequences and find the variance to compare
			 * Note: the sequence is constructed as a rotation array with the
			 * wsPosition
			 * is the position of the latest value
			 */
			aggregrateMotion();
			float magnitude = Util.magnitude(devA); 
			float threshold = THRESHOLD_GRAVITY;
			
			if (Math.abs(magnitude - EARTH_GRAVITY) <= threshold)  
				Util.copyArray(gravity, devA);
			
		} 
	}
			
	/**
	* Set the magnetic sample values
	* and calibrate the data if required
	*
	* @param raw The raw magnetic output from sensor 
	*/
	
	public static void setDevM(float[] raw) 
	{ 
		synchronized (lockSensing) 
		{
			Util.copyArray(devM, raw);
	         /*
	          * Update the geomagnetic vector if this sample
	          * under the threshold
	          */
			float magnitude = Util.magnitude(devM);
			if (Math.abs(magnitude - EARTH_GEOMAGNETIC) <= THRESHOLD_GEOMAGNETIC) 
				Util.copyArray(geomagnetic, devM);
		}
	}
	
	public static void setDevO(float[] raw) 
	{ 
		synchronized (lockSensing) 
		{
			Util.copyArray(devO, raw); 
		}
	}
	
	/**
	 * Convert the sensor data from the device's coordination system
	 * to the world's coordination system including motion and orientation *
	 * @return true if convertible
	 */
	public static boolean toEarthCS() 
	{ 
		synchronized (lockSensing) 
		{
			// When current gravity and current geomagnetic are known
			if ((Util.magnitude(devA) > 0) && (Util.magnitude(devM) > 0)) 
			{
				/*
			      * compute the rotation matrix from the current gravity and
			      * geomanetic
			      * the rotation matrix is used to transpose from the device's
			      * coordination system
			      * to the world's coordination system
			      * 
				*/
				
				SensorManager.getRotationMatrix(mR, mI, gravity, geomagnetic);
				
				/*
				 * compute the orientation of the device from rotation matrix
				 * including azimuth, pitch and roll angles of the device's
				 * current position
				 * in the world's coordination system
				 */
				SensorManager.getOrientation(mR, refO); 
				
				/* compute the acceleration in the world's coordination system
				 * by multiplying the device acceleration with the rotation
				 * matrix
				 * 
				 * | refA0 |   | mR0 mR1 mR2 |   | devA0 |
				 * | refA1 | = | mR3 mR4 mR5 | * | devA1 |
				 * | refA2 |   | mR6 mR7 mR8 |   | devA2 |
				 */
				
				refA[0] = devA[0] * mR[0] + devA[1] * mR[1] + devA[2] * mR[2];
				refA[1] = devA[0] * mR[3] + devA[1] * mR[4] + devA[2] * mR[5];
				refA[2] = devA[0] * mR[6] + devA[1] * mR[7] + devA[2] * mR[8];
				
				return true;
			} else 
				return false;
		}
	}
   
	/*
	 * Synchronized get function to get the reference acceleration 
	 * in the Earth's coordination system
	 *
	 * @return the vector containing the acceleration in (x, y, z) 
	 */
	public static float[] getRefAcceleration() 
	{ 
		float[] result = new float[3]; 
		synchronized (lockSensing) 
		{
			Util.copyArray(result, refA);
		}
		return result; 
	}
	
	/*
	 * ============================================================================
	 * Motion detection methods
	 * ============================================================================
	 */
	
	/*
	 * Return the angle between the device direction
	 * and the north direction in the Earth's coordination system *
	 * @return the heading angle in degrees
	 */
	public static float getHeading() 
	{ 
		float degrees;
		synchronized (lockSensing) 
		{ 
			degrees = (float) devO[0];
		}
		return degrees; 
	}
	
	public static void enableMoving() 
	{
		synchronized (lockMoving) 
		{ 
			flagMoving = true;
		}
	 }
	
	public static void disableMoving() 
	{ 
		synchronized (lockMoving) 
		{
			flagMoving = false; 
		}
	}
		
	public static boolean isMoving()
	{ 
		synchronized (lockMoving) 
		{
			if (flagMoving) return true;
			else return false; 
		}
	}
	
	private static void aggregrateMotion() 
	{
		float magnitude = Util.magnitude(devA);
		 
		 /*
	      * The wsSequence contains the last N samples output from
	      * the sensor.  The sequence is implemented using a rotation array.
	      * The rotation array uses a fixed array and a rotation wsPosition
	      * to indicate the position of the next samples
	      */
	     
		 /*
	      * If this wsPosition is out of bound, go back to the
	      * beginning of the fixed array, thus make the rotation
	      */
		wsPosition++;
		if (wsPosition == WINDOW_SUMMATION_SIZE) wsPosition = 0;
	    
		 /*
	      * The wsAggregate is the sum of all the value in the rotation
	      * array.  A new sum is computed by deducting old value and replace
	      * it with the new value.
	      */
		
		wsAggregate = wsAggregate - wsSequence[wsPosition] + magnitude; wsSequence[wsPosition] = magnitude;
	    
		 /*
	      * The length of the rotation array.  This is not at maximum length
	      * only at the beginning of the sampling period when the program
	      * just launches and the number of samples has not reached the maximum N.
	      * After the first N samples, the length is always N.
	      */
		
		int length;
		if (wsSequence[WINDOW_SUMMATION_SIZE - 1] == 0) 
			length = wsPosition + 1;  
		else 
			length = WINDOW_SUMMATION_SIZE; 
		float mean = wsAggregate / length; // the arithmetic mean (average) 
		float stdev = 0; // the standard deviation
		
		/** Compute the variance of the wsSequence
		 */
		
		for (int i = 0; i < length; i++) 
			stdev += (wsSequence[i] - mean) * (wsSequence[i] - mean);
			
		stdev = (float) Math.sqrt(stdev/length);
		/*
		 * If the standard deviation is below the threshold
		 * the phone is not in motion.
		 */	
		
			if (stdev > MOVEMENT) 
				enableMoving();
			else
				disableMoving();
			
	}
}

		
	
				
	
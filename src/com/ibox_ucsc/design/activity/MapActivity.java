package com.ibox_ucsc.design.activity; 

import java.util.Arrays;
import java.util.List;
import android.app.Activity;
import android.content.BroadcastReceiver; 
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener; 
import android.hardware.SensorManager; 
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.ibox_ucsc.design.map.GraphEdge;
import com.ibox_ucsc.design.map.GraphNode;
import com.ibox_ucsc.design.map.Map;
import com.ibox_ucsc.design.positioning.DeviceSensor; 
import com.ibox_ucsc.design.positioning.Positioning; 
import com.ibox_ucsc.design.positioning.WifiPositioning; 
import com.ibox_ucsc.design.util.DeviceWriter;
import com.ibox_ucsc.design.util.Util;
import com.ibox_ucsc.design.view.MapView;


public class MapActivity extends Activity implements SensorEventListener 
{
	public static final int MENU_MY_LOCATION = Menu.FIRST + 1; 
	public static final int MENU_CHOOSE_DESTINATION = Menu.FIRST + 2;
	public static final int MENU_STOP_RECORDING = Menu.FIRST + 3;
	private MapView view; 
	private SensorManager mSensorManager;
	
	private WifiManager mWifiManager;
	private WifiReceiver mWifiReceiver;
	private DeviceWriter out;
	private long timezero = 0;
	@Override
	
	public void onCreate(Bundle savedInstanceState) 
	{ 
		super.onCreate(savedInstanceState);
		view = new MapView(this); 
		setContentView(view);
		setContentView(view);
		
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); 
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWifiReceiver = new WifiReceiver();
	}
	@Override
	
	public void onPause() 
	{ 
		super.onPause();
		if (Positioning.isPositioning()) 
		{ 
			mSensorManager.unregisterListener(this); 
			unregisterReceiver(mWifiReceiver);
		{
			mSensorManager.unregisterListener(this); 
			unregisterReceiver(mWifiReceiver);
		}
		Positioning.stopPositioning(); }
	}
	@Override
	
	public void onResume() 
	{ 
		super.onResume();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{ 
		menu.setQwertyMode(true);
		menu.add(0, MENU_MY_LOCATION, 0, "My Location"); 
		menu.add(0, MENU_CHOOSE_DESTINATION, 0, "Get Direction"); 
		menu.add(0, MENU_STOP_RECORDING, 0, "Stop Recording"); 
		return super.onCreateOptionsMenu(menu);
	}
	
	  /**
     * This function will be called when a menu button option
     * is selected. If the option is "My Location", the program
     * start the MapActivity and initialize the sensors and WiFi
     * scanning.
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{ 
		int id = item.getItemId();
		switch (id) 
		{
			case MENU_MY_LOCATION:
            
				/*
                 * Turn on the positioning flag to tell other threads
                 * that the user has select positioning.
                 */
				out = new DeviceWriter("positioning.csv"); 
				timezero = System.currentTimeMillis(); 
				Positioning.startRecording();
				Positioning.startPositioning(); 
				Positioning.startConverging();
				Positioning.resetINS(); 
				/*
                 * Initializing all the sensors by registering them
                 * to the phone kernel driver.
                 */
				
				Sensor asensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				Sensor msensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
				Sensor osensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
				mSensorManager.registerListener(this, asensor, SensorManager.SENSOR_DELAY_FASTEST);
				mSensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_FASTEST);
				mSensorManager.registerListener(this, osensor, SensorManager.SENSOR_DELAY_FASTEST);
				
				registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                mWifiManager.startScan();
                break;
			case MENU_CHOOSE_DESTINATION:
					if (Positioning.isPositioning()) 
					{
						if (Map.getGraphSize()-1 > Map.GRAPH_SIZE) 
						{
							Map.removeLast(); 
						}
						GraphNode myNode = new GraphNode();
						myNode.id = Map.GRAPH_SIZE+1;
						myNode.position = Positioning.matchedPoint(); 
						myNode.type = GraphNode.TYPE_CORRIDOR; 
						myNode.room = "";
						myNode.person = "My Location";
						GraphEdge edge = Positioning.matchedEdge(); 
						
						myNode.adjacent = new int[2]; 
						myNode.adjacent[0] = edge.endpoints[0].id;    // modified in GraphEdge
						myNode.adjacent[1] = edge.endpoints[1].id;	  // modified in GraphEdge
						Map.setRoutingSource(myNode); // source(myNode); 
						Map.addNode(myNode); // Map.graph.addNode(myNode);  implemented in Graph Class and accessed in Map Class
						
						startActivity(new Intent(this, RouteActivity.class));
					} else 
					{
						Toast.makeText(this, "Current location is not available", Toast.LENGTH_SHORT).show();
					}
					break;				
			case MENU_STOP_RECORDING:
					Positioning.stopRecording();
					break;
		}
		return super.onOptionsItemSelected(item); 
	}
	@Override
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{ 
		
	}
    /**
     * This function is called whenever a sample is received from the
     * accelerometer or the compass
     */
	@Override

	public void onSensorChanged(SensorEvent event)
	{ 
		int type = event.sensor.getType();
		float[] data = Util.copyArray(event.values);
		if (type == Sensor.TYPE_ACCELEROMETER) 
		{ 
			DeviceSensor.setDevA(data);
			Positioning.updateINS();
		} else if (type == Sensor.TYPE_MAGNETIC_FIELD) 
		{
			DeviceSensor.setDevM(data); 
			DeviceSensor.toEarthCS();
		} else if (type == Sensor.TYPE_ORIENTATION) 
			DeviceSensor.setDevO(data);
		 
	}
    /**
     * This class contains the WiFi thread that will be called
     * when a signal is received from the hardware
     */
	
	private class WifiReceiver extends BroadcastReceiver 
	{
		public void onReceive(Context c, Intent intent) 
		{
			// Get the measurement sample
			List<ScanResult> wl = mWifiManager.getScanResults();
         
			/*
			 * Filter only the signal strength value of known
			 * WAP that is in our simulated propagation by comparing
			 * their BSSID.
			 */
			int[] rssi = new int[WifiPositioning.WAP_NUMBER];
			Arrays.fill(rssi, Integer.MIN_VALUE);
			for (int i = 0; i < wl.size(); i++) 
			{
				int n = Positioning.findBSSID(wl.get(i).BSSID);
				if (n >= 0) 
					rssi[n] = wl.get(i).level; 
			}
			Positioning.updateWifi(rssi);
			Positioning.process();
			if (Positioning.isRecording()) 
			{
				StringBuilder sb = new StringBuilder();
				
				//time
				long now = System.currentTimeMillis();
				sb.append(String.valueOf(now - timezero) + ",");
				
				//displacement
				float[] d = Positioning.displacement(); 
				sb.append(String.valueOf(d[0]) + ","); 
				sb.append(String.valueOf(d[1]) + ",");
				
				//rssi
				for (int i = 0; i < WifiPositioning.WAP_NUMBER; i++) 
					sb.append(String.valueOf(rssi[i]) + ","); 
				
				//position
				float[] p = Positioning.position();
				
				sb.append(String.valueOf(p[0]) + ",");
				sb.append(String.valueOf(p[1]) + ",");
				
				int[] p2 = Positioning.matchedPoint(); 
				
				sb.append(String.valueOf(p2[0]) + ","); 
				sb.append(String.valueOf(p2[1]) + "\n");
				
				out.write(sb.toString());
            }
			mWifiManager.startScan();
		}
		
	} // WifiReceiver Class
	
} // MapActivity Class
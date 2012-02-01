package com.ibox_ucsc.design.view;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory; 
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect; 
import android.graphics.LinearGradient; 
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect; 
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.KeyEvent;
import android.view.View;
import com.ibox_ucsc.design.R;
import com.ibox_ucsc.design.map.GraphNode;
import com.ibox_ucsc.design.map.Map;
import com.ibox_ucsc.design.positioning.DeviceSensor; 
import com.ibox_ucsc.design.positioning.Positioning; 
import com.ibox_ucsc.design.positioning.RadioMap;
import com.ibox_ucsc.design.util.Util;

public class MapView extends View 
{
	private static final int SPACING = 3;
	private static final int COMPASS_RADIUS = 15; 
	private static final int COMPASS_FAN= 60; 
	private static final int POINT_RADIUS = 5;
	private int x; 
	private int y; 
	private float phase;
	private Bitmap map;
	private Paint paintPath; 
	private Paint paintPoint;
	private Paint paintText;
	private Paint paintFrame; 
	private Paint paintCompass;
	private Paint paintFan;

	public MapView(Context context) 
	{ 
		super(context);
		setFocusable(true);
		map = BitmapFactory.decodeResource(getResources(), R.drawable.map_img); 
		x = map.getWidth()/2 - 200;
		y = map.getHeight()/2;
		paintPath = new Paint(Paint.ANTI_ALIAS_FLAG); 
		paintPath.setStyle(Paint.Style.STROKE);
		paintPath.setStrokeWidth(6); 
		paintPath.setColor(Color.argb(100, 0, 0, 255));
		paintFrame = new Paint(); 
		paintFrame.setAntiAlias(true); 
		paintFrame.setStyle(Paint.Style.FILL_AND_STROKE); 
		paintFrame.setStrokeWidth(1);
		paintCompass = new Paint(); 
		paintCompass.setAntiAlias(true); 
		paintCompass.setStyle(Paint.Style.STROKE); 
		paintCompass.setStrokeWidth(2); 
		paintCompass.setColor(Color.argb(100, 0, 0, 255));
		paintFan = new Paint(paintCompass); 
		paintFan.setStyle(Paint.Style.FILL);
		paintPoint = new Paint(); 
		paintPoint.setAntiAlias(true); 
		paintPoint.setStyle(Paint.Style.FILL); 
		paintPoint.setColor(Color.RED);
		paintText = new Paint();
		paintText.setAntiAlias(true); 
		paintText.setStyle(Paint.Style.FILL); 
		paintText.setTextSize(9);
		paintText.setColor(Color.WHITE);
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{ 
		// GET SCREEN INFO
		Rect screen = canvas.getClipBounds(); 
		int sw = screen.width();
		int sh = screen.height();
		/*
		 * When the current mode is positioning
		 */
		if (Positioning.isPositioning())
		{
			// int[] position = Map.toMapScale(Positioning.position());
			// position = Util.translate(position, -RadioMap.LEFT, -RadioMap.TOP);
			// x = position[0];
			// y = position[1];
			
			int[] position = Positioning.matchedPoint(); 
			x = position[0];
			y = position[1];
		}
			
		// The pixel coordinate of the screen top left corner
		
		int left = x - sw/2; 
		int top = y - sh/2;
		
		// draw the map within the screen
		
		canvas.drawBitmap(map, -left, -top, null); 
		ArrayList<GraphNode> nodes = Map.getNodes();
		
		if (Map.isRouting()) 
		{
			int[] path = Map.getPath();  // path();
			Path p = new Path();
			GraphNode n = nodes.get(path[0]); 
			p.moveTo(n.position[0] - left, n.position[1] - top); 
			for (int i = 1; i < path.length; i++) 
			{
				n = nodes.get(path[i]);
			    p.lineTo(n.position[0] - left, n.position[1] - top);
			}
			phase--;
			paintPath.setPathEffect(new ComposePathEffect(new CornerPathEffect(10),new PathDashPathEffect(makePathDash(),12, phase,PathDashPathEffect.Style.ROTATE)));
			canvas.drawPath(p, paintPath);
			GraphNode source = Map.getRoutingSource();  // Map.source();
			Shader shade = new LinearGradient(source.position[0] - left,source.position[1] - top + POINT_RADIUS/2,source.position[0] - left,source.position[1] - top +POINT_RADIUS/2,Color.rgb(225, 32, 33),Color.rgb(189, 26, 25),Shader.TileMode.CLAMP);
			paintPoint.setShader(shade);
			canvas.drawCircle(source.position[0] - left,source.position[1] - top,POINT_RADIUS, paintPoint);
		}
		
		for (int i = 0; i < nodes.size(); i++)
		{
				GraphNode node = nodes.get(i);
				if ((node.type == GraphNode.TYPE_ROOM) || (node.type == GraphNode.TYPE_CUBICLE)) 
				{
					if (Util.inside(node.position[0],node.position[1], x - sw/2, y - sh/2, x + sw/2, y + sh/2))
					{
						ArrayList<String> s = new ArrayList<String>(); int nx = node.position[0] - left;
						int ny = node.position[1] - top;
						int nw = 0;
						int nh = SPACING;
						if (node.room.length() > 0)
						{
							s.add(node.room);
							Rect r = new Rect();
							paintText.getTextBounds(node.room, 0, node.room.length(),r);
							nw = r.right - r.left;
							nh += r.bottom - r.top + SPACING;
							if (node.person.length() > 0) 
							{ 	
								s.add(node.person);
								Rect r2 = new Rect(); 		// Renamed rect from 'r' to 'r2'
								paintText.getTextBounds(node.person,0,node.person.length(), r2);
								if ((r2.right - r2.left) > nw) 
								{
									nw = r2.right - r2.left;
									nh += r2.bottom - r2.top + SPACING; 
								}
								nw += 2*SPACING;
								if (s.size() > 0) 
								{
									Shader shade = new LinearGradient(nx, ny-nh/2, nx, ny+nh/2,Color.rgb(81,169, 231),Color.rgb(44,116, 206),Shader.TileMode.CLAMP);
									paintFrame.setShader(shade);
									canvas.drawRect(nx - nw/2, ny - nh/2, nx + nw/2, ny + nh/2,paintFrame);
									for(int j = 0; j < s.size(); j++) 
										canvas.drawText(s.get(j), nx - nw/2 + SPACING, ny + nh/2 - SPACING - j*10, paintText);
								}
							}
						}
					}
				}
		}
			
			 /*
		      * Drawing the compass using the heading from the compass sensor
		      * The compass is only drawn in positioning mode
		      */
			
			if (Positioning.isPositioning()) 
			{
				float heading = DeviceSensor.getHeading();
				canvas.drawCircle(sw/2, sh/2, COMPASS_RADIUS, paintCompass); 
				canvas.drawCircle(sw/2, sh/2, 2, paintFan);
				RectF oval = new RectF(sw/2 - COMPASS_RADIUS, sh/2 - COMPASS_RADIUS,sw/2 + COMPASS_RADIUS, sh/2 + COMPASS_RADIUS);
				canvas.drawArc(oval, heading - Map.NORTH - 90 - COMPASS_FAN/2,  COMPASS_FAN,true, paintFan);
			} else 
			    canvas.drawCircle(sw/2, sh/2, 2, paintPoint);
		
			  invalidate();
				  
	    }
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{ 
		final int MOVE = 40;
		switch (keyCode) 
		{
		case KeyEvent.KEYCODE_DPAD_UP: 	y += MOVE; break;
		case KeyEvent.KEYCODE_DPAD_LEFT: x += MOVE;	break;
		case KeyEvent.KEYCODE_DPAD_RIGHT: x -= MOVE; break;
		case KeyEvent.KEYCODE_DPAD_DOWN: y -= MOVE; break;
		}
		return super.onKeyUp(keyCode, event); 
	}
		
	private Path makePathDash() 
	{ 
		Path p = new Path(); 
		p.moveTo(0, 0); 
		p.lineTo(0, -4); 
		p.lineTo(8, -4); 
		p.lineTo(12, 0); 
		p.lineTo(8, 4); 
		p.lineTo(0, 4);
		return p;
	}
}
		
		
	
	
	
	
	
	
	
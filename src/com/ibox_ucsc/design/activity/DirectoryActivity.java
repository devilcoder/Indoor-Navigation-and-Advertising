package com.ibox_ucsc.design.activity; 

import java.util.Arrays;
import android.app.ExpandableListActivity; 
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
//import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
//import android.widget.AbsListView;
//import android.widget.BaseExpandableListAdapter; 
import android.widget.ExpandableListAdapter; 
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import com.ibox_ucsc.design.map.GraphNode; 
import com.ibox_ucsc.design.map.Map;
import com.ibox_ucsc.design.activity.DirectoryExpandableListAdapter;

public class DirectoryActivity extends ExpandableListActivity 

{
	private static final int MENU_VIEW_DETAIL = Menu.FIRST + 1; 
	private static final int MENU_FROM_HERE = Menu.FIRST + 2;
	private static final int MENU_TO_HERE = Menu.FIRST + 3;
	
     // Sample data set.  children[i] contains the children (String[]) for groups[i].
	// private final String[] groups = { "Room", "Name" }; 
	private String[][] children = new String[2][];
    static ExpandableListAdapter adapter;
   
    @Override
    
    public void onCreate(Bundle savedInstanceState) 
    { 
    	super.onCreate(savedInstanceState);
           // Set up our adapter
    	adapter = new DirectoryExpandableListAdapter(this); 
    	children[0] = Map.getRooms(); 
    	Arrays.sort(children[0]);
    	children[1] = Map.getPeople(); 
    	Arrays.sort(children[1]); 
    	setListAdapter(adapter); 
    	registerForContextMenu(getExpandableListView());
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
    	menu.setHeaderTitle("Menu");
    	menu.add(0, MENU_VIEW_DETAIL, 0, "View Detail"); 
    	menu.add(0, MENU_FROM_HERE, 0, "From Here"); 
    	menu.add(0, MENU_TO_HERE, 0, "To Here");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) 
    { 
    	
        	
    	ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item .getMenuInfo(); 
    	
    	String title = ((TextView) info.targetView).getText().toString();
    	int type = ExpandableListView.getPackedPositionType(info.packedPosition); 
    	int id = item.getItemId();
    	        /*
    	         * If the menu clicked is the child
    	         */
    	if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) 
    	{ 
    		GraphNode node = Map.searchDetail(title);
    	   	switch (id) 
    	   	{
    	   	case MENU_VIEW_DETAIL:
    	   		StringBuilder sb = new StringBuilder();
    	   		sb.append("Detail information\n");
    	   		sb.append("Room ID: " + node.room + "\n");
    	   		sb.append("Name: " + node.person + "\n");
    	   		Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show(); 
    	   		break;
    	   	case MENU_FROM_HERE:
    	   		Map.setRoutingSource(node);
    	   		startActivity(new Intent(this, RouteActivity.class)); 
    	   		break;
    	   	case MENU_TO_HERE:
    	   		Map.setRoutingDestination(node);
    	   		startActivity(new Intent(this, RouteActivity.class)); 
    	   		break;
    	   	}
    	return true;
    	 /*
         * If the menu clicked is the group
         */
    	} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) 
    	{ 
    		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
    		if (groupPos == 0) 
    			Toast.makeText( this, "This group contains the list of rooms in this building",Toast.LENGTH_SHORT).show();
    		else 
    			Toast.makeText( this, "This group contains the list of names in this building", Toast.LENGTH_SHORT).show();
       		return true; 
    	}
    	return false; 
    	
    }
}


//    Directory Expandable List Adapter





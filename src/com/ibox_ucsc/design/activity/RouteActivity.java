package com.ibox_ucsc.design.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener; 
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView; 
import android.widget.Button;
import android.widget.Toast;
import com.ibox_ucsc.design.R;
import com.ibox_ucsc.design.map.GraphNode;
import com.ibox_ucsc.design.map.Map;

// this class must be included in the SearchActivity class


public class RouteActivity extends Activity implements OnClickListener 
{
	private GraphNode source;
	private GraphNode destination;
	private AutoCompleteTextView textViewSource;
	private AutoCompleteTextView textViewDestination;
	private Button btnSearch;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{ 
		// TODO: must substitute this code with buttons on the layout
		
		super.onCreate(savedInstanceState); 
	//	setContentView(R.layout.route);
	//	setContentView(R.layout.route);
		String[] database = Map.getDatabase(Map.DATABASE_PERSON + Map.DATABASE_ROOM);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, database);
		btnSearch = (Button) findViewById(R.id.btnSearchRoute);
		btnSearch.setOnClickListener(this);
		textViewSource = (AutoCompleteTextView) findViewById(R.id.editSource); 
		textViewSource.setAdapter(adapter);
		textViewDestination = (AutoCompleteTextView) findViewById(R.id.editDestination);
		textViewDestination.setAdapter(adapter); 
		source = Map.getRoutingSource();
		destination = Map.getRoutingDestination();
		
		if (source.person.length() > 0) 
			textViewSource.setText(source.person);
		else
			textViewSource.setText(source.room);
		
		if (destination.person.length() > 0) 
			textViewDestination.setText(destination.person); 
		else 
            textViewDestination.setText(destination.room);
	}
	@Override
	
	public void onClick(View v) 
	{ 
		if (v == btnSearch) 
			if (textViewSource.getText().length() == 0) 
				Toast.makeText(this, "Please input source",Toast.LENGTH_SHORT).show();
			else if (textViewDestination.getText().length() == 0) 
				Toast.makeText(this, "Please input destination", Toast.LENGTH_SHORT).show();
				else 
				{
					Map.setRoutingSource(Map.searchDetail(textViewSource.getText().toString()));
					Map.setRoutingDestination(Map.searchDetail(textViewDestination.getText().toString()));
				}
		Map.enableRouting();
		startActivity(new Intent(this, MapActivity.class)); 
	}
}
      
		
		
				
				
				
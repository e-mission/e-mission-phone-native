package edu.berkeley.eecs.e_mission;

import java.text.ParseException;







import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



//import android.R;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ScrollView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;







import android.annotation.SuppressLint;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


@SuppressLint("NewApi")
public class ConfirmSectionActivity extends Activity implements OnItemSelectedListener {

	TextView autoMode;
	TextView startTimeView;
	TextView endTimeView;
	TextView TextView1;
	TextView TextView2;
	TextView TextView3;
	TextView TextView4;
	TextView TextView5;
	
	RadioGroup userOverrideGroup;
	Spinner spinner1;
	ScrollView userScroll;
	String tripId;
	String sectionId;
	String autoModeClassification;
	RadioButton yesButton;
	RadioButton noButton;
	ModeClassificationHelper dbHelper; 
	String newMode;
	String TAG = "CSA";
	
	//Map Fragment
	GoogleMap map;
	LatLngBounds bounds;
	//Map Fragment
	
	@SuppressLint("NewApi")
	@Override



	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_section);
		
		dbHelper = new ModeClassificationHelper(this);
		
		autoMode = (TextView) findViewById(R.id.autoMode);
		startTimeView = (TextView) findViewById(R.id.startTime);
		endTimeView = (TextView) findViewById(R.id.endTime);
		yesButton = (RadioButton) findViewById(R.id.yesButton);
		noButton = (RadioButton) findViewById(R.id.noButton);
		TextView1 = (TextView) findViewById(R.id.TextView1);
		TextView2 = (TextView) findViewById(R.id.TextView2);
		TextView3 = (TextView) findViewById(R.id.TextView3);
		TextView4 = (TextView) findViewById(R.id.TextView4);
		TextView5 = (TextView) findViewById(R.id.TextView5);

		
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		List<String> list = new ArrayList<String>();
		list.add("Please Specify:");
        list.add("walking");
        list.add("cycling");
        list.add("bus");
        list.add("train");
        list.add("car");
        list.add("air");
        list.add("mixed");
        list.add("not a trip");
		spinner1.setVisibility(View.INVISIBLE);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
					(this, android.R.layout.simple_spinner_item,list);
		dataAdapter.setDropDownViewResource
					(android.R.layout.simple_spinner_dropdown_item);
         
		spinner1.setAdapter(dataAdapter);
		spinner1.setSelection(0, false);
		spinner1.setOnItemSelectedListener(this);
		String text1="<u><b>We detected a trip</u><b>";
		TextView1.setText(Html.fromHtml(text1));
		String text2="<u><b>Is this correct?</u><b>";
		TextView2.setText(Html.fromHtml(text2));
		String text3="<u>Start</u>";
		TextView3.setText(Html.fromHtml(text3));
		String text4="<u>End</u>";
		TextView4.setText(Html.fromHtml(text4));
		String text5="<u>Mode</u>";
		TextView5.setText(Html.fromHtml(text5));
		
		try {
			JSONObject sectionJSON = new JSONObject(getIntent().getExtras().getString("sectionJSON"));
			
			sectionId = sectionJSON.getString("section_id");
			tripId = sectionJSON.getString("trip_id");
			
			UnclassifiedSection currSection = UnclassifiedSection.parse(sectionJSON);
			
			String startTime = sectionJSON.getString("section_start_time");
			String endTime = sectionJSON.getString("section_end_time");
			autoModeClassification = currSection.getMode();
			autoMode.setText(String.format("%-26s", autoModeClassification));
			// This assumes that data from the server is always in UTC
			startTimeView.setText(String.format("%-21s", UnclassifiedSection.parseDateString(startTime)));
			endTimeView.setText(String.format("%-21s", UnclassifiedSection.parseDateString(endTime)));

			/*
			 * We don't want to allow the user to pick transport.
			 * So we disable the yes button, automatically toggle the no button, and display the spinner automatically.
			 */
			if(autoModeClassification.equals("transport")) {
				yesButton.setEnabled(false);
				noButton.toggle();
				spinner1.setVisibility(View.VISIBLE);
			}
			
			//Map Fragment
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			JSONArray trackPointsArray = sectionJSON.getJSONArray("track_points");
			ArrayList<LatLng> trackPoints = new ArrayList<LatLng>();
			
			if (trackPointsArray.length() >= 2) {
				LatLng firstPoint = getLatLng(trackPointsArray.getJSONObject(0));
				LatLng lastPoint = getLatLng(trackPointsArray.getJSONObject(trackPointsArray.length() - 1));
				
				double minLat = firstPoint.latitude;
				double maxLat = firstPoint.latitude;
				double minLng = firstPoint.longitude;
				double maxLng = firstPoint.longitude;
				
				Marker firstMark = map.addMarker(new MarkerOptions().position(firstPoint).title("Starting Point").icon(BitmapDescriptorFactory.fromResource(R.drawable.start_icon)));
				trackPoints.add(firstPoint);
				for (int i = 1; i < trackPointsArray.length(); i++) {
					LatLng currPoint = getLatLng(trackPointsArray.getJSONObject(i));
					trackPoints.add(currPoint);
					map.addMarker(new MarkerOptions().position(currPoint));

					if (currPoint.latitude < minLat) { minLat = currPoint.latitude; }
					if (currPoint.latitude > maxLat) { maxLat = currPoint.latitude; }
					if (currPoint.longitude < minLng) { minLng = currPoint.longitude; }
					if (currPoint.longitude < minLng) { minLng = currPoint.longitude; }
				}
				trackPoints.add(lastPoint);
				Marker lastMark = map.addMarker(new MarkerOptions().position(lastPoint).title("Ending Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.end_icon)));
				
				//Draw Route
				PolylineOptions routeOptions = new PolylineOptions().addAll(trackPoints).color(Color.CYAN);
				Polyline polyline = map.addPolyline(routeOptions);
				
				
				LatLng center = new LatLng((minLat + maxLat)/2, (minLng + maxLng)/2);
				bounds = new LatLngBounds(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
				
				final View mapView = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getView();
			    if (mapView.getViewTreeObserver().isAlive()) {
			    	mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			    		@Override
			    		public void onGlobalLayout() {
							mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
							map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,  50));
			    		}
			    	});
			    }
			}
			//Map Fragment			    
		} catch(JSONException e) {
			Toast.makeText(this, "Unable to display details", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (ParseException e) {
			Toast.makeText(this, "Unable to display details", Toast.LENGTH_LONG).show();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	   
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.confirm_section, menu);
		return true;
	}
	
	public void onModeCorrect(View view) {
		System.out.println("mode was correct!");
		// we are not going to store to the database until the user confirms it
		// dbHelper.storeUserClassification(tripId, sectionId, autoModeClassification);
		dbHelper.storeUserSelection(tripId, sectionId, autoModeClassification);
		finish();
	}
	
	public void onModeWrong(View view) {
		System.out.println("mode was wrong");
		spinner1.setVisibility(View.VISIBLE);
	}


	public LatLng getLatLng(JSONObject trackLocation) throws JSONException {
		JSONArray coords = trackLocation.getJSONObject("track_location").getJSONArray("coordinates");
    // Data from the server is now in GeoJSON (lng, lat) format
		return new LatLng(coords.getDouble(1), coords.getDouble(0));
	}


	public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
		String newMode = String.valueOf(parent.getItemAtPosition(pos).toString());
		Log.d(TAG,"new mode is "+newMode);
		dbHelper.storeUserSelection(tripId, sectionId, newMode);
		finish();
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }
	public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


}


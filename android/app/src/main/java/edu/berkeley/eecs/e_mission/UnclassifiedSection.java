package edu.berkeley.eecs.e_mission;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UnclassifiedSection {
	private String tripId;
	private String sectionId;
	private String sectionBlob;
	private boolean commit;
	private String selMode;
	String TAG = "UnclassifiedSectionsActivity";
	private JSONObject obj;
	private String predMode;
	private double certainty;

	public UnclassifiedSection(String tripId, String sectionId, String selMode,
			String sectionBlob) {
		super();
		Log.d(TAG, "Creating unclassified section with selMode = "+selMode);
		this.tripId = tripId;
		this.sectionId = sectionId;
		this.selMode = selMode;


		this.sectionBlob = sectionBlob;
		try {
			this.obj = new JSONObject(sectionBlob);
            JSONObject predMap = new JSONObject();
            if (obj.has("predicted_mode"))  {
                predMap = obj.getJSONObject("predicted_mode");
            }
			String maxString = "UNKNOWN";
			double maxConf = 0.0;
			Iterator it = predMap.keys();
			while(it.hasNext()) {
				String currKey = (String) it.next();
				double currProb = predMap.getDouble(currKey);
				if (currProb > maxConf) {
					maxConf = currProb;
					maxString = currKey;
				}
			}
			this.predMode = maxString;
			this.certainty = maxConf;

			/*
			 * This is extremely hacky, but needed to get this to work for now.
			 * TODO: Fix this later.
			 * 
			 * The problem is that we don't modify the blob when we set the selMode.
			 * And even if we did, it wouldn't be enough unless we saved it
			 * And we currently don't update the blob, only auxiliary fields.
			 * 
			 * So in the case where the selMode exists, we put it back into the blob in the constructor
			 * to reconstruct what a saved version might be.
			 */
			
			// reconstructing from blob - should be used in detail view
			if (selMode == null && this.obj.has("selected_mode")) {
				this.selMode = this.obj.getString("selected_mode");
			}
			
			// reconstructing the blob from the DB field - should be used in list view
			if (selMode != null) {
				this.obj.put("selected_mode", selMode);
				setSectionBlob(this.obj.toString());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.predMode = "UNKNOWN";
			this.certainty = 0.0;
		}
		this.commit = true;
	}
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
	public String getSectionId() {
		return sectionId;
	}
	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}
	public String getSectionBlob() {
		return sectionBlob;
	}
	
	public void setSectionBlob(String sectionBlob){
		this.sectionBlob = sectionBlob;
		//this.obj = new JSONObject(sectionBlob);
	}
	
	public void setSelMode(String selMode)
	{
		this.selMode = selMode;
		try {
			this.obj.put("selected_mode", selMode);
			setSectionBlob(this.obj.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG,"selMode: "+selMode);
	}
	
	public String getSelMode()
	{
		return selMode;
	}
	
	public boolean getConfirmStatus()
	{
		return commit;
	}
	
	public void setConfirmStatus(boolean commit)
	{
		this.commit = commit;
	}
	

	public String getMode()
	{
		Log.d(TAG, "in getMode, selMode = "+selMode+" predMode = "+this.predMode);
		if(selMode != null){
			return selMode;
		} else {
			return this.predMode;
		}	  
	}
	
	public double getCertainty() throws JSONException 
	{
		if (selMode != null) {
			// If the user has selected it, it must be right!
			return 1.0;
		} else {
			return this.certainty;
		}
	}

	/* This is the text that will be displayed in the list view
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			JSONObject obj = new JSONObject(sectionBlob);
			//return obj.getString("section_start_time") + "*" + obj.getString("section_end_time")+"+"+obj.getString("mode");
			return obj.getString("section_start_time") + "*" + obj.getString("section_end_time")+"+"+this.getMode();
		} catch (JSONException e) {
			e.printStackTrace();
			return "UNKNOWN";
		}
	}

	public static UnclassifiedSection parse(JSONObject obj) throws JSONException {
		return new UnclassifiedSection(obj.getString("trip_id"),
				obj.getString("section_id"), null, obj.toString());
	}

	public static Date parseDateString(String dateStr) throws ParseException {
		/* This is pretty hacky, but I don't really have a better solution. Most of the time,
		 * moves returns a date that is formatted like this:
		 * 20140403T095842-0700
		 * which works perfectly with the SimpleDateFormat pattern that ends with 'Z'.
		 * 
		 * However, occasionally, it will return something like this:
		 * 20140403T002345Z
		 * 
		 * In this case, because the timezone is not an offset from GMT, the parsing fails.
		 * If we were using Java 7, we could use a pattern with "X" which would fix this.
		 * 
		 * However, unfortunately, we are still using Java 6.
		 * 
		 * We could skip the timezone entirely or treat it as a static string (ie 'Z').
		 * However, then we end up with times that appear to be around midnight PST instead of midnight UTC.
		 * 
		 * So we just say that if the dateStr ends with 'Z', then we replace the 'Z' with -0000.
		 * That appears to work in the scala shell...
		 * 
		 * TODO: Fix this once we move to Java 7.
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ",Locale.US);
		if (dateStr.endsWith("Z")) {
			dateStr = dateStr.replace("Z", "-0000");
		}
		return sdf.parse(dateStr);
	}
}

package edu.berkeley.eecs.e_mission;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/*
 * This class is used to instrument the way that the client is used, and push the results
 * to the server as part of our regular sync process. 
 */

public class ClientStatsHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "clientStatsDB";
 
    // Table names
    private static final String TABLE_CLIENT_STATS = "clientStats";
 
    // CLIENT_STATS Table Columns names
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STAT = "stat";
    private static final String KEY_VALUE = "value";

	private static final String TAG = "ClientStatsHelper";
	
	private static final String METADATA_TAG = "Metadata";
	private static final String STATS_TAG = "Readings";
	
	private Context cachedCtx;
    	
    public ClientStatsHelper(Context ctx) {
    	super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    	cachedCtx = ctx;
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CLIENT_STATS_TABLE = "CREATE TABLE " + TABLE_CLIENT_STATS +" (" +
				KEY_TIMESTAMP + " TEXT, "+ KEY_STAT +" TEXT, " +
				KEY_VALUE + " TEXT)";
		System.out.println("CREATE_CLIENT_STATS_TABLE = "+CREATE_CLIENT_STATS_TABLE);
		db.execSQL(CREATE_CLIENT_STATS_TABLE);
	}
	
	public void storeMeasurement(String label, String value, String ts) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_STAT, label);
		newValues.put(KEY_VALUE, value);
		newValues.put(KEY_TIMESTAMP, ts);
		db.insert(TABLE_CLIENT_STATS, null, newValues);
	}
	
	/*
	 * Get and clear are separate calls because in this case, the source of truth is the phone
	 * and not the cloud. So we want to first get the values to send them to the cloud, and
	 * once we know that the call is successful, to delete them locally.
	 */
	public JSONObject getMeasurements() throws JSONException {
		JSONObject retVal = new JSONObject();
		
		JSONObject metadata = new JSONObject();
		metadata.put(cachedCtx.getString(R.string.metadata_os_version),
				android.os.Build.VERSION.RELEASE);
		String appVersion;
		try {
			appVersion = cachedCtx.getPackageManager().getPackageInfo(cachedCtx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			appVersion = "ERROR: "+e.getMessage();
		}
		metadata.put(cachedCtx.getString(R.string.metadata_app_version), appVersion);
		
		SQLiteDatabase db = this.getReadableDatabase();
		// Get the list of keys currently in the database
	    String uniqueKeyQuery = "SELECT DISTINCT " + KEY_STAT +
	    		" FROM " + TABLE_CLIENT_STATS;
	    List<String> keyList = new LinkedList<String>();
	    Cursor keyCursor = db.rawQuery(uniqueKeyQuery, null);
	    if (keyCursor.moveToFirst()) {
	    	do {
	    		keyList.add(keyCursor.getString(0));
	    	} while (keyCursor.moveToNext());
	    }
	    
	    Log.d(TAG, "list of unique keys = "+TextUtils.join(",", keyList));
	    
	    JSONObject stats = new JSONObject();
	    Iterator<String> it = keyList.iterator();
	    while(it.hasNext()) {
	    	String currKey = it.next();
	    	Log.d(TAG, "Getting values for key "+ currKey);
	    	String measurementsForKey = "SELECT "+ KEY_TIMESTAMP +", " +
	    			KEY_VALUE + " FROM " + TABLE_CLIENT_STATS + 
	    			" WHERE ("+KEY_STAT+" = \'" + currKey+"\')";
	    	Cursor valueCursor = db.rawQuery(measurementsForKey, null);
	    	int nValues = valueCursor.getCount();
	    	Log.d(TAG, "Got "+nValues+" matching values to push");
	    	JSONArray valueArr = new JSONArray();
	    	// moveToFirst will return false if the cursor is empty
	    	if (valueCursor.moveToFirst()) {
	    		for (int i = 0; i < nValues; i++) {
	    			JSONArray currValue = new JSONArray();
	    			currValue.put(0, valueCursor.getString(0));
	    			currValue.put(1, valueCursor.getString(1));
	    			valueArr.put(i, currValue);
	    			valueCursor.moveToNext();
	    		}
	    	} else {
	    		Log.w(TAG, "Found key "+currKey+" but no values while reading them?!");
	    	}
	    	if (nValues > 0) {
	    		stats.put(currKey, valueArr);
	    	} else {
	    		Log.d(TAG, "No matches found for key "+currKey+", race condition?");
	    	}
	    }
	    /*
	     * If there are no new stats recorded, we don't need to send any metadata either.
	     * And if we do have stats, then we need to send both
	     */
	    if(keyList.size() > 0) {
	    	retVal.put(METADATA_TAG, metadata);
	    	retVal.put(STATS_TAG, stats);
	    }
	    return retVal;
	}
		
	/*
	 * Clear all entries from the database
	 */
	public void clear() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CLIENT_STATS, null, null);
		db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Delete all existing data and re-create
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_CLIENT_STATS);
		onCreate(db);
	}

}

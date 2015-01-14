package edu.berkeley.eecs.e_mission;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ModeClassificationHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;
 
    // Database Name
    private static final String DATABASE_NAME = "movesConnect";
 
    // Table names
    private static final String TABLE_CURR_TRIPS = "currTrips";
 
    // CURR_TRIPS Table Columns names
    private static final String KEY_TRIP_ID = "tripId";
    private static final String KEY_SECTION_ID = "sectionId";
    private static final String KEY_USER_CLASSIFICATION = "userClassification";
    private static final String KEY_USER_SELECTION = "userSelection";
    private static final String KEY_SECTION_BLOB = "sectionJsonBlob";
    
    private static final String WHERE_ID_CLAUSE = KEY_TRIP_ID+" =? AND "+
    		KEY_SECTION_ID+" =?";
	
    public ModeClassificationHelper(Context ctx) {
    	super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CURR_TRIPS_TABLE = "CREATE TABLE " + TABLE_CURR_TRIPS+" (" +
				KEY_TRIP_ID + ", "+ KEY_SECTION_ID+" TEXT, " +
				KEY_USER_CLASSIFICATION + " TEXT, "+
				KEY_USER_SELECTION + " TEXT, "+
				KEY_SECTION_BLOB + " TEXT)";
		System.out.println("CREATE_UNCLASSIFIED_TABLE = "+CREATE_CURR_TRIPS_TABLE);
		db.execSQL(CREATE_CURR_TRIPS_TABLE);
	}
	
	public void storeUserClassification(String tripId, String sectionId, String userMode) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    
	    ContentValues newValues = new ContentValues();
	    newValues.put(KEY_USER_CLASSIFICATION, userMode);
	    
	    // Updating existing row with the user classification
	    String whereClause = WHERE_ID_CLAUSE;
	    String[] whereArgs = new String[]{tripId, sectionId};
	    
	    db.update(TABLE_CURR_TRIPS, newValues, whereClause, whereArgs);
	    db.close(); // Closing database connection		
	}
	
	public void storeUserSelection(String tripId, String sectionId, String userMode) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    
	    ContentValues newValues = new ContentValues();
	    newValues.put(KEY_USER_SELECTION, userMode);
	    
	    // Updating existing row with the user classification
	    String whereClause = WHERE_ID_CLAUSE;
	    String[] whereArgs = new String[]{tripId, sectionId};
	    
	    db.update(TABLE_CURR_TRIPS, newValues, whereClause, whereArgs);
	    db.close(); // Closing database connection		
	}
	
	public List<UserClassification> getAndDeleteClassifiedSections() {
		List<UserClassification> resultList = new ArrayList<UserClassification>();
	    SQLiteDatabase db = this.getWritableDatabase();
	    String selectQuery = "SELECT  "+KEY_TRIP_ID+", "+KEY_SECTION_ID+" ,"+ KEY_USER_CLASSIFICATION +
	    		" FROM " + TABLE_CURR_TRIPS +
	    		" WHERE "+KEY_USER_CLASSIFICATION+" IS NOT NULL";
	    
	    Cursor cursor = db.rawQuery(selectQuery, null);
	    if(cursor.moveToFirst()) {
	    	do {
	    		UserClassification currClass = new UserClassification(cursor.getString(0),
	    				cursor.getString(1), cursor.getString(2));
	    		resultList.add(currClass);
	    	} while (cursor.moveToNext());
	    }
	    
	    // Delete all the entries that we just read
	    for (int i = 0; i < resultList.size(); i++) {
	    	String[] currArgs = new String[]{resultList.get(i).getTripId(),
	    			resultList.get(i).getSectionId()};
	    	int delCount = db.delete(TABLE_CURR_TRIPS, WHERE_ID_CLAUSE, currArgs);
	    	System.out.println("Deleted "+delCount+" entries");
	    	assert(delCount == 1);
	    }
	    db.close();
	    return resultList;
	}
	
	public void storeNewUnclassifiedTrips(List<UnclassifiedSection> fromServer) {
		// delete old entries that match the new entries
		SQLiteDatabase db = this.getWritableDatabase();
		for (int i = 0; i < fromServer.size(); i++) {
			upsertRecord(db, fromServer.get(i));
		}
		db.close();
	}
	
	/* 
	 * SQLite does not support upsert, so we implement it as delete followed by insert.
	 */
	
	public void upsertRecord(SQLiteDatabase db, UnclassifiedSection currTrip) {
		// Delete existing entry, if any
		String[] currArgs = new String[]{currTrip.getTripId(), currTrip.getSectionId()};
		int delCount = db.delete(TABLE_CURR_TRIPS, WHERE_ID_CLAUSE, currArgs);
		System.out.println("delCount = "+delCount);

		// Now, insert the new entry
		ContentValues values = new ContentValues();
		values.put(KEY_TRIP_ID, currTrip.getTripId());
		values.put(KEY_SECTION_ID, currTrip.getSectionId());
		values.put(KEY_SECTION_BLOB, currTrip.getSectionBlob());
		
		db.insert(TABLE_CURR_TRIPS, null, values);
	}
	
	public List<UnclassifiedSection> getUnclassifiedSections() {
		List<UnclassifiedSection> resultList = new ArrayList<UnclassifiedSection>();
	    SQLiteDatabase db = this.getWritableDatabase();
	    String selectQuery = "SELECT " + KEY_TRIP_ID + ", "+ KEY_SECTION_ID+", " +
	    		KEY_USER_SELECTION + "," + KEY_SECTION_BLOB + 
	    		" FROM " + TABLE_CURR_TRIPS +
	    		" WHERE ("+KEY_USER_CLASSIFICATION+" IS NULL)";
	    // only gets the trips were the confirmation slot is null
	    Cursor cursor = db.rawQuery(selectQuery, null);
	    if(cursor.moveToFirst()) {
	    	do {
	    		UnclassifiedSection currClass = new UnclassifiedSection(cursor.getString(0),
	    				cursor.getString(1), cursor.getString(2), cursor.getString(3));
	    		resultList.add(currClass);
	    	} while (cursor.moveToNext());
	    }
	    db.close();
	    return resultList;
	}

	/*
	 * Clear all entries from the database
	 */
	public void clear() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CURR_TRIPS, null, null);
		db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		// Change from version 1 -> version 2 = adding the USER_SELECTION column
		// Delete all existing data and re-create
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_CURR_TRIPS);
		onCreate(db);
	}

}

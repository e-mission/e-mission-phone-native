/*

package edu.berkeley.eecs.e_mission;

import android.app.Activity;

import org.apache.cordova.CordovaActivity;



import com.couchbase.lite.*;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

*/
/*
This is for couchbase

Commenting it out because the build.gradle is for Azure
*//*



*/
/**
 * Created by RZarrabi on 6/8/2015.
 *//*


public class ListActivity extends Activity{

    private Manager manager;
    private static Context mContext;
    public static final String SYNC_URL = "http://10.10.67.102:4985";
    Database database;
    final String TAG = "TAG";


    public void onCreate() {
        // create a manager
        Manager manager;
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            Log.d (TAG, "Manager created");
        } catch (IOException e) {
            Log.e(TAG, "Cannot create manager object");
            return;
        }

        // create a name for the database and make sure the name is legal
        String dbname = "competitionDB";
        if (!Manager.isValidDatabaseName(dbname)) {
            Log.e(TAG, "Bad database name");
            return;
        }

        // create a new database
        try {
            database = manager.getDatabase(dbname);
            Log.d (TAG, "Database created");

        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get database");
            return;
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("first", "second");

        // create an empty document
        Document document = database.createDocument();
        try {
            document.putProperties(properties);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }

    private void startSync() {

        URL syncUrl;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Replication pullReplication = database.createPullReplication(syncUrl);
        pullReplication.setContinuous(true);

        Replication pushReplication = database.createPushReplication(syncUrl);
        pushReplication.setContinuous(true);

        pullReplication.start();
        pushReplication.start();
*/
/*

        pullReplication.addChangeListener(true);
        pushReplication.addChangeListener(this);
*//*


    }

}

*/

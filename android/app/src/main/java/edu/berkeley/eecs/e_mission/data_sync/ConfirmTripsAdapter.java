/**
 * Creates an adapter to post data to the SMAP server
 */
package edu.berkeley.eecs.e_mission.data_sync;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.berkeley.eecs.e_mission.BatteryUtils;
import edu.berkeley.eecs.e_mission.ClientStatsHelper;
import edu.berkeley.eecs.e_mission.CommunicationHelper;
import edu.berkeley.eecs.e_mission.ModeClassificationHelper;
import edu.berkeley.eecs.e_mission.OnboardingActivity;
import edu.berkeley.eecs.e_mission.R;
import edu.berkeley.eecs.e_mission.UnclassifiedSection;
import edu.berkeley.eecs.e_mission.UserClassification;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

/**
 * @author shankari
 *
 */
public class ConfirmTripsAdapter extends AbstractThreadedSyncAdapter implements SyncingInterface {
	private String userName;

	private SyncingInterface SyncSystem;

	Properties uuidMap;
	boolean syncSkip = false;
	Context cachedContext;
	ModeClassificationHelper dbHelper;
	ClientStatsHelper statsHelper;
	// TODO: Figure out a principled way to do this
	private static int CONFIRM_TRIPS_ID = 99;
	private String service;
	
	public ConfirmTripsAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);

		// Get service somehow

		service = "Ours";

		System.out.println("Creating ConfirmTripsAdapter");
		// Dunno if it is OK to cache the context like this, but there are other
		// people doing it, so let's do it as well.
		// See https://nononsense-notes.googlecode.com/git-history/3716b44b527096066856133bfc8dfa09f9244db8/NoNonsenseNotes/src/com/nononsenseapps/notepad/sync/SyncAdapter.java
		// for an example
		if (service.equals("AzureSync")) {
			SyncSystem = new AzureSync(context, autoInitialize);
		} else if (service.equals("Ours")) {
			SyncSystem = new ShankariSyncService(context, autoInitialize);
		} else if (service.equals("CouchBase")) {
            SyncSystem = new CouchBaseSync(context, autoInitialize);
        } else {
            Log.d("TAG", "No sync service chosen");

        }
			// Our ContentProvider is a dummy so there is nothing else to do here
	}
	
	/* (non-Javadoc)
	 * @see android.content.AbstractThreadedSyncAdapter#onPerformSync(android.accounts.Account, android.os.Bundle, java.lang.String, android.content.ContentProviderClient, android.content.SyncResult)
	 */
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
        Log.i("SYNC", "PERFORMING SYNC");
		SyncSystem.onPerformSync(account, extras, authority, provider, syncResult);
	}
	
	/*
	 * The original thought was that the sectionId would be unique, so we could
	 * use it to uniquely generate an integer that could be used in the notification code.
	 * We also assumed that we could use is as a database key and as the data in
	 * the HTTP post request.
	 * 
	 * However, the current database schema is that the tripID is unique, and the section ID
	 * is unique within a trip.
	 * 
	 * So we need to send a (tripId, sectionId, userClassification) triplet to the server.
	 * So we can not longer use key = sectionId, value = userMode.
	 * 
	 * Also, we need to create a new data structure that we can pass around instead of using a pair.
	 */
	/*
	public JSONArray convertListToJSON(List<UserClassification> resultList) {
		JSONArray retArray = new JSONArray();
		for (int i = 0; i < resultList.size(); i++) {
			JSONObject currObj = new JSONObject();
			UserClassification currClass = resultList.get(i);
			try {
				currObj.put("trip_id", currClass.getTripId());
				currObj.put("section_id", currClass.getSectionId());
				currObj.put("userMode", currClass.getUserMode());
				// System.out.println("currObj = "+currObj);
				retArray.put(currObj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retArray;
	}
	
	public List<UnclassifiedSection> convertJSONToList(JSONArray fromServer) {
		List<UnclassifiedSection> resultList = new ArrayList<UnclassifiedSection>();
		for (int i=0; i < fromServer.length(); i++) {
			try {
				resultList.add(UnclassifiedSection.parse(fromServer.getJSONObject(i)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resultList;
	}
*/
    /*
     * Talks to server to update HTTPResponseCache entry for Results Summary View
     */
    public void updateResultsSummary(String userToken) throws MalformedURLException, IOException{
        Log.d("SYNC", "Updating results summary");
        //String resultHTML = CommunicationHelper.readResults(cachedContext, "max-age=0");
		SyncSystem.UpdateResultsSummary(userToken);

        /*
         * This is not a complete solution because this only pulls changes to the main HTML file -
         * the other files are not reloaded until the HTML is displayed. Attempts to use the
         * webview from this background fetch (see below) fail with the exception
         * "java.lang.IllegalStateException: Calling View methods on another thread than the UI thread."
         *
         * Another option is to try to launch the DisplayResultSummary Activity from here, but
         * I don't think that will work either because you can't launch activities from the background threads.
         * I think that we are going to have to go back to a HTML parsing solution.
         *
         * That is not currently critical, though, and we may want to come up with a generic solution once
         * we decide what to do about webviews for the UI, so let's just push this change for now.
         */

        // We have to display the result in a webview to ensure that all the files are actually retrieved
        /*
        System.out.println("my looper = "+ Looper.myLooper());
        Looper.prepare();
        System.out.println("my looper = "+ Looper.myLooper());
        WebView ignoredView = new WebView(cachedContext);
        ignoredView.getSettings().setJavaScriptEnabled(true);
        ignoredView.loadDataWithBaseURL(ConnectionSettings.getConnectURL(cachedContext), resultHTML,
                null, null, null);
        */
    }


	/*
	 * Generates a notification for the user.
	 */
	
	public void generateNotifications(JSONArray sections) {
		if (sections.length() > 0) {
			String message = "You have "+sections.length()+" pending trips to categorize";
			generateNotification(CONFIRM_TRIPS_ID, message, edu.berkeley.eecs.e_mission.ConfirmSectionListActivity.class);
		} else {
			// no unclassified sections, don't generate a notification
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void generateNotification(int messageId, String message, Class activityToLaunch) {
		System.out.println("While generating notification sectionId = "+messageId);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(cachedContext);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(cachedContext.getString(R.string.app_name));
		builder.setContentText(message);
		
		/*
		 * This is a bit of magic voodoo. The tutorial on launching the activity actually uses a stackbuilder
		 * to create a fake stack for the new activity. However, it looks like the stackbuilder
		 * is only available in more recent versions of the API. So I use the version for a special activity PendingIntent
		 * (since our app currently has only one activity) which resolves that issue.
		 * This also appears to work, at least in the emulator.
		 * 
		 * TODO: Decide what level API we want to support, and whether we want a more comprehensive activity.
		 */
		
		Intent activityIntent = new Intent(cachedContext, activityToLaunch);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent activityPendingIntent = PendingIntent.getActivity(cachedContext, CONFIRM_TRIPS_ID,
				activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(activityPendingIntent);		
		
		NotificationManager nMgr =
				(NotificationManager)cachedContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		nMgr.notify(messageId, builder.build());
	}
	
	public String getPath(String serviceName) {
		return "/"+userName+"/"+serviceName;
	}
}

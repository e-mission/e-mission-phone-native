/**
 * Creates an adapter to post data to the SMAP server
 */
package edu.berkeley.eecs.e_mission.data_sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.eecs.e_mission.AppSettings;
import edu.berkeley.eecs.e_mission.BatteryUtils;
import edu.berkeley.eecs.e_mission.ConnectionSettings;
import edu.berkeley.eecs.e_mission.ModeClassificationHelper;
import edu.berkeley.eecs.e_mission.ClientStatsHelper;
import edu.berkeley.eecs.e_mission.OnboardingActivity;
import edu.berkeley.eecs.e_mission.R;
import edu.berkeley.eecs.e_mission.UnclassifiedSection;
import edu.berkeley.eecs.e_mission.UserClassification;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @author shankari
 *
 */
public class ConfirmTripsAdapter extends AbstractThreadedSyncAdapter {
	private String projectName;
	
	private String userName;

	Properties uuidMap;
	boolean syncSkip = false;
	Context cachedContext;
	ModeClassificationHelper dbHelper;
	ClientStatsHelper statsHelper;
	// TODO: Figure out a principled way to do this
	private static int CONFIRM_TRIPS_ID = 99;
	
	public ConfirmTripsAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		
		System.out.println("Creating ConfirmTripsAdapter");
		// Dunno if it is OK to cache the context like this, but there are other
		// people doing it, so let's do it as well.
		// See https://nononsense-notes.googlecode.com/git-history/3716b44b527096066856133bfc8dfa09f9244db8/NoNonsenseNotes/src/com/nononsenseapps/notepad/sync/SyncAdapter.java
		// for an example
		cachedContext = context;
		dbHelper = new ModeClassificationHelper(context);
		statsHelper = new ClientStatsHelper(context);
		// We read the project name here because that's where we have access to a context
		projectName = context.getString(R.string.app_name);
		// Our ContentProvider is a dummy so there is nothing else to do here
	}
	
	/* (non-Javadoc)
	 * @see android.content.AbstractThreadedSyncAdapter#onPerformSync(android.accounts.Account, android.os.Bundle, java.lang.String, android.content.ContentProviderClient, android.content.SyncResult)
	 */
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
        Log.i("SYNC", "PERFORMING SYNC");
        long msTime = System.currentTimeMillis();
		String syncTs = String.valueOf(msTime);
		statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_launched), null, syncTs);
		
		/*
		 * Read the battery level when the app is being launched anyway.
		 */
		statsHelper.storeMeasurement(cachedContext.getString(R.string.battery_level),
				String.valueOf(BatteryUtils.getBatteryLevel(cachedContext)), syncTs);
				
		if (syncSkip == true) {
			System.err.println("Something is wrong and we have been asked to skip the sync, exiting immediately");
			return;
		}

		if (!OnboardingActivity.getOnboardingComplete(cachedContext)) {
			generateNotification(CONFIRM_TRIPS_ID, "Finish setting up app", edu.berkeley.eecs.e_mission.OnboardingActivity.class);
			return;
		}
		
		System.out.println("Can we use the extras bundle to transfer information? "+extras);
		// Get the list of uncategorized trips from the server
		// hardcoding the URL and the userID for now since we are still using fake data
		String userName = UserProfile.getInstance(cachedContext).getUserEmail();
		System.out.println("real user name = "+userName);

		if (userName == null || userName.trim().length() == 0) {
			System.out.println("we don't know who we are, so we can't get our data");
			return;
		}
		// First, get a token so that we can make the authorized calls to the server
		String userToken = GoogleAccountManagerAuth.getServerToken(cachedContext, userName);
		
		// TODO: Use ORM to convert this to a Section[]
		try {
			// First push and then pull, so that we don't end up re-reading data that the user has just classified	
			JSONArray classifiedSections = convertListToJSON(dbHelper.getAndDeleteClassifiedSections());
			statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_push_list_size),
					String.valueOf(classifiedSections.length()), syncTs);
			// System.out.println("classifiedSections = "+classifiedSections);
			if (classifiedSections.length() > 0) {
				pushClassifications(ConnectionSettings.getConnectURL(cachedContext),
									userToken, classifiedSections);
			} else {
				System.out.println("No user classified sections, skipping push to server");
			}
			
			/*
			 * Originally, we just added the classified sections to the database. But this gives rise
			 * to synchronization errors if the user is accessing the data from multiple devices.
			 * This is probably not a huge issue initially for most people, but some cases that 
			 * I can think of are:
			 * - two people sharing the same account like Tom and me
			 * - people using a different device for the data collection and a different one
			 * (google watch) for the classification.
			 * 
			 * The database on the server is the source of truth, so we can always just synchronize to it.
			 * So let's clear our local cached copy before pulling data from the server.
			 */
			
			dbHelper.clear();
			
			JSONArray unclassifiedSections = getUnclassifiedSections(
					ConnectionSettings.getConnectURL(cachedContext), userToken);
			statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_pull_list_size),
					String.valueOf(unclassifiedSections.length()), syncTs);
			// save the unclassified sections to the database
			dbHelper.storeNewUnclassifiedTrips(convertJSONToList(unclassifiedSections));
			// We should be able to figure out which sections are already in the notification bar and filter those out
			// But let's do something lame and easy for now
			// TODO: Optimize this later
			// Also, since we are already using a database, we may want to consider
			// using a content provider, since that is the recommended solution
			// TODO: Simplify this later, once we are sure that the list stuff works
			generateNotifications(unclassifiedSections);
			
			// Now, we push the stats and clear it
			// Note that the database ensures that we have a blank document if there are no stats
			// by skipping the metadata in that case.
			JSONObject freshStats = statsHelper.getMeasurements();
			if (freshStats.length() > 0) {
				pushStats(ConnectionSettings.getConnectURL(cachedContext), userToken, freshStats);
				statsHelper.clear();
			}
			statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_duration),
					String.valueOf(System.currentTimeMillis() - msTime), syncTs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try {
            updateResultsSummary(userToken);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

	/*
	 * Talks to the server and gets the list of unclassified sections
	 */
	public JSONArray getUnclassifiedSections(String commuteTrackerHost, String userToken)
			throws IOException, JSONException {
		HttpPost msg = new HttpPost(commuteTrackerHost+"/tripManager/getUnclassifiedSections");
		System.out.println("Posting data to "+msg.getURI());
		msg.setHeader("Content-Type", "application/json");
		
		JSONObject toPush = new JSONObject();

		toPush.put("user", userToken);
		msg.setEntity(new StringEntity(toPush.toString()));
		
		AndroidHttpClient connection = AndroidHttpClient.newInstance(projectName);
		HttpResponse response = connection.execute(msg);
		System.out.println("Got response "+response+" with status "+response.getStatusLine());
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		// We assume that the data is all in a single line
		// TODO: Verify this assumption
		String rawJSON = in.readLine();
		// System.out.println("Raw JSON = "+rawJSON);
		in.close();
		connection.close();
		JSONObject parentObj = new JSONObject(rawJSON);
		return parentObj.getJSONArray("sections");
	}


    /*
     * Talks to server to update HTTPResponseCache entry for Results Summary View
     */
    public void updateResultsSummary(String userToken) throws MalformedURLException, IOException{
        Log.d("SYNC", "Updating results summary");
        final String result_url = AppSettings.getResultUrl(cachedContext);
        URL url = new URL(result_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setUseCaches(true);
        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setReadTimeout(10000 /*milliseconds*/);
        connection.setConnectTimeout(15000 /* milliseconds */);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("user", "" + userToken);

        /* Force the invalidation of the results summary cache entry */
        connection.addRequestProperty("Cache-Control", "max-age=0");

        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.connect();

        InputStream inputStream = connection.getInputStream();
        int code = connection.getResponseCode();
        Log.d("SYNC", "Update Connection response status "+connection.getResponseCode());
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }
        BufferedReader  in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String currLine = null;
        while ((currLine = in.readLine()) != null) {
            builder.append(currLine+"\n");
        }
        String rawHTML = builder.toString();
        in.close();
        connection.disconnect();
    }


	/*
	 * Pushes the classifications to the host.
	 */
	public void pushClassifications(String commuteTrackerHost, String userToken, JSONArray userClassification) throws IOException, JSONException {
		pushJSON(commuteTrackerHost+"/tripManager/setSectionClassification", userToken,
				"updates", userClassification);
	}
	
	/*
	 * Pushes the classifications to the host.
	 */
	public void pushStats(String commuteTrackerHost, String userToken,
						  JSONObject appStats) throws IOException, JSONException {
		pushJSON(commuteTrackerHost+"/stats/set", userToken, "stats", appStats);
	}
	
	public void pushJSON(String fullURL, String userToken,
						 String objectLabel, Object jsonObjectOrArray) throws IOException, JSONException {
		HttpPost msg = new HttpPost(fullURL);
		System.out.println("Posting data to "+msg.getURI());
		msg.setHeader("Content-Type", "application/json");
		JSONObject toPush = new JSONObject();

		toPush.put("user", userToken);
		toPush.put(objectLabel, jsonObjectOrArray);
		msg.setEntity(new StringEntity(toPush.toString()));
		AndroidHttpClient connection = AndroidHttpClient.newInstance(projectName);
		HttpResponse response = connection.execute(msg);
		System.out.println("Got response "+response+" with status "+response.getStatusLine());
		connection.close();		
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

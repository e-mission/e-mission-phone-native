/**
 * Creates an adapter to post data to the SMAP server
 */
package edu.berkeley.eecs.e_mission.data_sync;

import com.couchbase.lite.*;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.WebView;


import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.*;
//import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.replicator.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
/*
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;
*/

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;

import edu.berkeley.eecs.e_mission.BatteryUtils;
import edu.berkeley.eecs.e_mission.ClientStatsHelper;
import edu.berkeley.eecs.e_mission.CommunicationHelper;
import edu.berkeley.eecs.e_mission.ConnectionSettings;
import edu.berkeley.eecs.e_mission.ModeClassificationHelper;
import edu.berkeley.eecs.e_mission.OnboardingActivity;
import edu.berkeley.eecs.e_mission.R;
import edu.berkeley.eecs.e_mission.ScoreActivity;
import edu.berkeley.eecs.e_mission.UnclassifiedSection;
import edu.berkeley.eecs.e_mission.UserClassification;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

import static com.couchbase.lite.auth.AuthenticatorFactory.createPersonaAuthenticator;

/**
 * @author shankari
 *
 */
public class ConfirmTripsAdapter extends AbstractThreadedSyncAdapter implements Replication.ChangeListener {
	private String userName;

	Properties uuidMap;
	boolean syncSkip = false;
	Context cachedContext;
	ModeClassificationHelper dbHelper;
	ClientStatsHelper statsHelper;
	// TODO: Figure out a principled way to do this
	private static int CONFIRM_TRIPS_ID = 99;
	private String Service;
/*	private MobileServiceClient mClient;
	private MobileServiceSyncTable<JSONArray> mToDoTable;*/
	private Query mPullQuery;
	private Manager manager;

	private Database database;
	private static URL SYNC_URL;
	final String TAG = "TAG";


	private boolean iAmDebugging;

	private static final String DATABASE_NAME = "our_db"; // Only lower case characters and numbers
    private OnSyncUnauthorizedObservable onSyncUnauthorizedObservable;
    private OnSyncProgressChangeObservable onSyncProgressChangeObservable;


	public ConfirmTripsAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		Service = "CouchBase";
		iAmDebugging = true;
        final String userToken = GoogleAccountManagerAuth.getServerToken(context, userName);
        onSyncUnauthorizedObservable = new OnSyncUnauthorizedObservable();
        onSyncProgressChangeObservable = new OnSyncProgressChangeObservable();

		System.out.println("Creating ConfirmTripsAdapter");
		// Dunno if it is OK to cache the context like this, but there are other
		// people doing it, so let's do it as well.
		// See https://nononsense-notes.googlecode.com/git-history/3716b44b527096066856133bfc8dfa09f9244db8/NoNonsenseNotes/src/com/nononsenseapps/notepad/sync/SyncAdapter.java
		// for an example
		cachedContext = context;
		if (Service.equals("Ours")) {
			dbHelper = new ModeClassificationHelper(context);
			statsHelper = new ClientStatsHelper(context);
		}
		try {
			if (Service.equals("Axure")) {
/*					mClient = new MobileServiceClient(
							"https://e-mission.azure-mobile.net/",
							"aBNpasaSXoYAhvstmXYvtkEfFYudbt33",
							context
					);
					mPullQuery = mClient.getTable(JSONArray.class).where().field("score").gt(0);

					authenticate();*/
			} else if (Service.equals("CouchBase")) {
				try {
/*					Manager.enableLogging(Log.TAG, Log.VERBOSE);
					Manager.enableLogging(Log.TAG_SYNC, Log.DEBUG);
					Manager.enableLogging(Log.TAG_QUERY, Log.DEBUG);
					Manager.enableLogging(Log.TAG_VIEW, Log.DEBUG);
					Manager.enableLogging(Log.TAG_DATABASE, Log.DEBUG);*/
					System.out.println("Are we here 1");
					manager = new Manager(new AndroidContext(getContext()), Manager.DEFAULT_OPTIONS);
					System.out.println("Created Manager");

				} catch (IOException e) {
					Log.e(TAG, "Cannot create Manager object", e);
					return;
				}
				try {
					System.out.println("Inside of database try statement");
					database = manager.getDatabase(DATABASE_NAME);
					System.out.println("database set to something");

				} catch (CouchbaseLiteException e) {
					//Log.e(TAG, "Cannot get Database", e);
					System.out.println("Database failed");
					e.printStackTrace();
					return;
				} catch (Exception e) {
					System.out.println("Other exception");
					e.printStackTrace();
				}
				SYNC_URL = new URL("http://50.17.111.19:4984/db");
				System.out.println("SYNC_URL set");


			}
		} catch (Exception e) {
			//
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
		//if (Service.equals("Ours")) {
			long msTime = System.currentTimeMillis();
			String syncTs = String.valueOf(msTime);

		if (Service.equals("Ours")) {
			statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_launched), null, syncTs);
		}
			/*
			 * Read the battery level when the app is being launched anyway.
			 */
		if (Service.equals("Ours")) {
			statsHelper.storeMeasurement(cachedContext.getString(R.string.battery_level),
					String.valueOf(BatteryUtils.getBatteryLevel(cachedContext)), syncTs);
		} else if (Service.equals("CouchBase")) {
			Document document = database.createDocument();
			Map<String, Object> properties = new HashMap<String, Object>();
            String userName = UserProfile.getInstance(cachedContext).getUserEmail();
            properties.put("user_id", userName);
			properties.put(cachedContext.getString(R.string.battery_level),String.valueOf(BatteryUtils.getBatteryLevel(cachedContext)));
			properties.put("time", System.currentTimeMillis());
			try {
				document.putProperties(properties);
			} catch (CouchbaseLiteException e) {
				e.printStackTrace();
			}
		}
			if (syncSkip == true) {
				System.err.println("Something is wrong and we have been asked to skip the sync, exiting immediately");
				return;
			}

				if (!OnboardingActivity.getOnboardingComplete(cachedContext)) {
				generateNotification(CONFIRM_TRIPS_ID, "Finish setting up app", edu.berkeley.eecs.e_mission.OnboardingActivity.class);
				return;
			}

			System.out.println("Can we use the extras bundle to transfer information? " + extras);
			// Get the list of uncategorized trips from the server
			// hardcoding the URL and the userID for now since we are still using fake data
			String userName = UserProfile.getInstance(cachedContext).getUserEmail();
			System.out.println("real user name = " + userName);

			if (userName == null || userName.trim().length() == 0) {
				System.out.println("we don't know who we are, so we can't get our data");
				return;
			}
			// First, get a token so that we can make the authorized calls to the server
			String userToken = GoogleAccountManagerAuth.getServerToken(cachedContext, userName);

			// TODO: Use ORM to convert this to a Section[]
			try {
				// First push and then pull, so that we don't end up re-reading data that the user has just classified
				if (Service.equals("Ours")) {
				JSONArray classifiedSections = convertListToJSON(dbHelper.getAndDeleteClassifiedSections());
					statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_push_list_size),
						String.valueOf(classifiedSections.length()), syncTs);
				// System.out.println("classifiedSections = "+classifiedSections);
					if (classifiedSections.length() > 0) {
						CommunicationHelper.pushClassifications(cachedContext, userToken, classifiedSections);
					} else {
						System.out.println("No user classified sections, skipping push to server");
					}
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

				if (Service.equals("Ours")) {
					dbHelper.clear();

					JSONArray unclassifiedSections;
					unclassifiedSections = CommunicationHelper.getUnclassifiedSections(
							cachedContext, userToken);
					statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_pull_list_size),
							String.valueOf(unclassifiedSections.length()), syncTs);

					// save the unclassified sections to the database
					dbHelper.storeNewUnclassifiedTrips(convertJSONToList(unclassifiedSections));
					generateNotifications(unclassifiedSections);

				} else if (Service.equals("CouchBase")) {
/*					Document document = database.createDocument();
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put("unclassifiedSections", convertJSONToList(unclassifiedSections));
					try {
						document.putProperties(properties);
					} catch (CouchbaseLiteException e) {
						e.printStackTrace();
					}*/
				}
				// We should be able to figure out which sections are already in the notification bar and filter those out
				// But let's do something lame and easy for now
				// TODO: Optimize this later
				// Also, since we are already using a database, we may want to consider
				// using a content provider, since that is the recommended solution
				// TODO: Simplify this later, once we are sure that the list stuff works

				// Now, we push the stats and clear it
				// Note that the database ensures that we have a blank document if there are no stats
				// by skipping the metadata in that case.
				if (Service.equals("Ours")) {
					JSONObject freshStats = statsHelper.getMeasurements();
					if (freshStats.length() > 0) {
						CommunicationHelper.pushStats(cachedContext, userToken, freshStats);
						statsHelper.clear();
					}
					statsHelper.storeMeasurement(cachedContext.getString(R.string.sync_duration),
							String.valueOf(System.currentTimeMillis() - msTime), syncTs);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (Service.equals("Ours")) {
				try {
					updateResultsSummary(userToken);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		//} else if (Service.equals("Azure")) {
			///
		if (Service.equals("CouchBase")) {
			// Couchbase
			System.out.println("CB SYNC");
			doCouchBaseSync();
            System.out.println("Why is it getting here???");
			if (iAmDebugging) {
                List l = database.getAllViews();
                System.out.println("list size = " + l.size());
                System.out.println("doc count = " + database.getDocumentCount());
				Document yoloDoc = database.getExistingDocument("123");
                //QueryOptions query = new QueryOptions();
                //database.getAllDocs(query);
                Document test = database.getExistingDocument("TEST");
                if (test == null) {
                    System.out.println("Test failed");
                } else {
                    System.out.println("Test passed!");
                }
                System.out.println("123 = " + yoloDoc);
                //System.out.println(yoloDoc.getId());
                Document battery = database.getExistingDocument("0151173a-596d-41c2-a3df-b4a0a5b5521e");
                if (battery != null) {
                    System.out.println("should be 70 : " + battery.getProperty("battery_level"));
                } else {
                    System.out.println("wrong");
                }
                Query q = database.createAllDocumentsQuery();



                //String name = yoloDoc.getProperty("IS").toString();
				//assert name.equals("Josh");
				//System.out.println("Assert was true, name = " + name);
			}
		}
	}

    @Override
    public void changed(Replication.ChangeEvent event) {

        Replication replication = event.getSource();
        Log.d(TAG, "Replication : " + replication + " changed.");
        if (!replication.isRunning()) {
            String msg = String.format("Replicator %s not running", replication);
            Log.d(TAG, msg);
        }
        else {
            int processed = replication.getCompletedChangesCount();
            int total = replication.getChangesCount();
            String msg = String.format("Replicator processed %d / %d", processed, total);
            Log.d(TAG, msg);
        }

        if (event.getError() != null) {
            //showError("Sync error", event.getError());
            System.out.println("Error at line 388");
        }

    }


    public void pushSetUp() {
        Replication push = database.createPushReplication(SYNC_URL);
        push.setContinuous(true);
        push.start();
        String userName = UserProfile.getInstance(cachedContext).getUserEmail();
        String password = "password"; // Horrible security flaw but I just want to get
        push.setAuthenticator(new BasicAuthenticator(userName, password));
        push.addChangeListener(this);
    }

    public void pullSetUp() {
        Replication pull = database.createPullReplication(SYNC_URL);
        pull.setContinuous(true);
        pull.start();
        String userName = UserProfile.getInstance(cachedContext).getUserEmail();
        String password = "password"; // Horrible security flaw but I just want to get
        pull.setAuthenticator(new BasicAuthenticator(userName, password));
        pull.addChangeListener(this);
    }


	private void doCouchBaseSync() {

        pushSetUp();
        pullSetUp();

//        System.out.println("In doCouchBaseSync");
//		Replication push = database.createPushReplication(SYNC_URL);
//        System.out.println("Replication push = database.createPushReplication(SYNC_URL);");
//        Replication pull = database.createPullReplication(SYNC_URL);
//        System.out.println("Replication pull = database.createPullReplication(SYNC_URL);");
//        push.setContinuous(true);
//        System.out.println("push.setContinuous(true);");
//        pull.setContinuous(true);
//        System.out.println("pull.setContinuous(true);");
//		push.start();
//        System.out.println("got past push.start()");
//		pull.start();
//        System.out.println("got past pull.start()");
//
//        String userName = UserProfile.getInstance(cachedContext).getUserEmail();
//        String password = "password"; // Horrible security flaw but I just want to get this out for now...
//
//        pull.setAuthenticator(new BasicAuthenticator(userName, password));
//        push.setAuthenticator(new BasicAuthenticator(userName, password));
//
//        pull.addChangeListener(this);
//        push.addChangeListener(this);




        //com.couchbase.lite.auth.Authenticator ourAuth = AuthenticatorFactory.createFacebookAuthenticator();

/*
        String cookieName = "SyncGatewaySession";
        boolean isSecure = true;
        boolean httpOnly = true;

        // expiration date - 1 day from now
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int numDaysToAdd = 1;
        cal.add(Calendar.DATE, numDaysToAdd);
        Date expirationDate = cal.getTime();

        String cookieValue = GoogleAccountManagerAuth.getServerToken(cachedContext, userName);

        System.out.println(cookieValue);

        pull.setCookie(cookieName, cookieValue, "/", expirationDate, isSecure, httpOnly);
        push.setCookie(cookieName, cookieValue, "/", expirationDate, isSecure, httpOnly);
*/


        //public static Authenticator a=  createFacebookAuthenticator();




        //String ours =

        //com.couchbase.lite.auth.Authenticator ourAuth = new BasicAuthenticator("")



        //Authorizer
/*        try {
            login();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
/*
        pull.setAuthenticator(new BasicAuthenticator(userName, GoogleAccountManagerAuth.getServerToken(cachedContext, userName)));
        push.setAuthenticator(new BasicAuthenticator(userName, GoogleAccountManagerAuth.getServerToken(cachedContext, userName)));*/

        //startReplicationSyncWithCustomCookie();


/*        pull.addChangeListener(this);
        push.addChangeListener(this);*/

	}

    public void startReplicationSyncWithCustomCookie() {

        String cookieValue = GoogleAccountManagerAuth.getServerToken(cachedContext, userName);
        System.out.println("SYNC: " + SYNC_URL.toString());
        Synchronize sync = new Synchronize.Builder(this.database, SYNC_URL.toString(), true)
                .cookieAuth(cookieValue)
                .addChangeListener(getReplicationChangeListener())
                .build();
        sync.start();
    }

    static class OnSyncUnauthorizedObservable extends Observable {
        private void notifyChanges() {
            setChanged();
            notifyObservers();
        }
    }

    private Replication.ChangeListener getReplicationChangeListener() {
        return new Replication.ChangeListener() {

            @Override
            public void changed(Replication.ChangeEvent event) {
                Replication replication = event.getSource();
                if (event.getError() != null) {
                    Throwable lastError = event.getError();
                    if (lastError.getMessage().contains("existing change tracker")) {
                        //pushLocalNotification("Replication Event", String.format("Sync error: %s:", lastError.getMessage()));
                    }
                    if (lastError instanceof HttpResponseException) {
                        HttpResponseException responseException = (HttpResponseException) lastError;
                        System.out.println("Response code : " + responseException.getStatusCode());
                        if (responseException.getStatusCode() == 401) {
                            onSyncUnauthorizedObservable.notifyChanges();
                            System.out.println("401 Error code");
                        }
                    }
                }
                Log.d(TAG, event.toString());
                updateSyncProgress(
                        replication.getCompletedChangesCount(),
                        replication.getChangesCount(),
                        replication.getStatus()
                );
            }
        };
    }

    private synchronized void updateSyncProgress(int completedCount, int totalCount, Replication.ReplicationStatus status) {
        onSyncProgressChangeObservable.notifyChanges(completedCount, totalCount, status);
    }

    private void login() throws IOException, JSONException{

        String email = UserProfile.getInstance(cachedContext).getUserEmail();
        String fullURL = "http://localhost:4985/db/_user/" + email;

        HttpPost msg = new HttpPost(fullURL);
        msg.setHeader("Content-Type", "application/json");

        JSONObject toPush = new JSONObject();
        toPush.put("name", email);
        toPush.put("email", email);
        toPush.put("password", GoogleAccountManagerAuth.getServerToken(cachedContext, userName));
        msg.setEntity(new StringEntity(toPush.toString()));

        System.out.println("Posting data to " + msg.getURI());

        //create connection
        AndroidHttpClient connection = AndroidHttpClient.newInstance(R.class.toString());
        HttpResponse response = connection.execute(msg);
        StatusLine statusLine = response.getStatusLine();


    }


/*




		private void createTable() {

		// Get the Mobile Service Table instance to use

		System.out.println("Create table");

		SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "JSONArrays", null, 1);
		//MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();
		MobileServiceSyncContext syncContext = mClient.getSyncContext();
		System.out.println("opened a bunch of stuff");

		System.out.println("mpullQuery");

		Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
		try {
			localStore.defineTable("JSONArrays", tableDefinition);
			//syncContext.initialize(localStore, handler).get();
		} catch (Exception e){

			System.out.println("Error Initializing sync context");
			e.printStackTrace();

		}
		System.out.println("Sync context initialized");
		// Get the Mobile Service Table instance to use
		mToDoTable = mClient.getSyncTable(JSONArray.class);




		//resfreshItemsFromTable();


		//sc.mId = 1;
		//addItem(v, num);
*/
/*		mClient.getTable(ScoreActivity.class).insert(sc, new TableOperationCallback<ScoreActivity>() {
			public void onCompleted(ScoreActivity entity, Exception exception, ServiceFilterResponse response) {
				if (exception == null) {
					// Insert succeeded
					System.out.println("Succeeded");
				} else {
					// Insert failed
					exception.printStackTrace();
				}
			}
		});*//*


	}
*/


/*
	private void refreshItemsFromTable() {

		// Get the items that weren't marked as completed and add them in the
		// adapter
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					final MobileServiceList<JSONArray> result = mToDoTable.read(mPullQuery).get();
					//runOnUiThread(new Runnable() {

						//@Override
						//public void run() {
					//mAdapter.clear();

					for (JSONArray item : result) {
								mAdapter.add(item);
							}

				} catch (Exception exception) {
					createAndShowDialog(exception, "Error");
				}
				return null;
			}
		}.execute();
	}
*/


/*

	private void authenticate() {
		// Login using the Google provider.

		ListenableFuture<MobileServiceUser> mLogin = mClient.login(MobileServiceAuthenticationProvider.Google);

		Futures.addCallback(mLogin, new FutureCallback<MobileServiceUser>() {
			@Override
			public void onFailure(Throwable exc) {
				//createAndShowDialog((Exception) exc, "Error");
			}

			@Override
			public void onSuccess(MobileServiceUser user) {
				//createAndShowDialog(String.format(
				//"You are now logged in - %1$2s",
				//user.getUserId()), "Success");
				System.out.println("Is this happenening??????");
				createTable();
			}
		});
	}
*/

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
     * Talks to server to update HTTPResponseCache entry for Results Summary View
     */
	public void updateResultsSummary(String userToken) throws MalformedURLException, IOException{
		Log.d("SYNC", "Updating results summary");
		String resultHTML = CommunicationHelper.readResults(cachedContext, "max-age=0");

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
        System.out.println("my looper = "+ Looper.myLooper());
        Looper.prepare();
        System.out.println("my looper = "+ Looper.myLooper());
        WebView ignoredView = new WebView(cachedContext);
        ignoredView.getSettings().setJavaScriptEnabled(true);
        ignoredView.loadDataWithBaseURL(ConnectionSettings.getConnectURL(cachedContext), resultHTML,
                null, null, null);
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
    static class OnSyncProgressChangeObservable extends Observable {
        private void notifyChanges(int completedCount, int totalCount, Replication.ReplicationStatus status) {
            SyncProgress progress = new SyncProgress();
            progress.completedCount = completedCount;
            progress.totalCount = totalCount;
            progress.status = status;
            setChanged();
            notifyObservers(progress);
        }
    }

        static class SyncProgress {
            public int completedCount;
            public int totalCount;
            public Replication.ReplicationStatus status;
        }



	public String getPath(String serviceName) {
		return "/"+userName+"/"+serviceName;
	}
}

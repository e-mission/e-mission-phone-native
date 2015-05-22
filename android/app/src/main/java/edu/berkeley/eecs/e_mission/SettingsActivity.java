package edu.berkeley.eecs.e_mission;



import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;
// import edu.berkeley.eecs.e_mission.auth.GoogleBrowserAuth;

/**
 * Demonstrates app-to-app and browser-app-browser integration with Moves API authorize flow.
 */
public class SettingsActivity extends Activity {

    private static final String TAG = "SettingsActivity";

    // BEGIN: variables to set up the authentication with moves
    private static String CLIENT_ID;

    /** the uri must match one of the callback uris registered for your app, 
     * but you may include additional query parameters in the uri. 
     * If omitted, the default callback uri is used. 
     * You can define the default redirect uri in app info's development tab. */
    
    private static String REDIRECT_URI;

    private static final int REQUEST_AUTHORIZE = 1;
	private static final int REQUEST_CODE_PICK_ACCOUNT = 1000; 
    
    private TextView userNameField;
    private TextView authStatusField;
    private Button linkToMoves;
    private TextView linkToMovesStatusField;
    private TextView tokenView;
    private ClientStatsHelper statsHelper;
    //private CheckBox lcView;
    //private static boolean onlyUnsure = false;
    // END: variables to set up the authentication with moves
    
    // BEGIN: variables to set up the automatic syncing
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "edu.berkeley.eecs.e_mission.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "eecs.berkeley.edu";
    // The account name
    public static final String ACCOUNT = "dummy_account";
    private Account mAccount;  
    // END: variables to set up the automatic syncing


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().getBooleanExtra("EXIT", false)) {
        	finish();
        }

        REDIRECT_URI = ConnectionSettings.getConnectURL(this) + "/movesCallback";
        CLIENT_ID = ConnectionSettings.getMovesClientID(this);
        setContentView(R.layout.activity_main);

        
        userNameField = (TextView) findViewById(R.id.userName);
        authStatusField = (TextView) findViewById(R.id.authStatus);
        linkToMoves = (Button) findViewById(R.id.linkToMoves);
        linkToMovesStatusField = (TextView) findViewById(R.id.linkToMovesStatus);
        tokenView = (TextView)findViewById(R.id.tokenView);
        
		statsHelper = new ClientStatsHelper(this);
		
        /*
        lcView = (CheckBox)findViewById(R.id.checkBox1);
        lcView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				onlyUnsure = true;
				Log.d(TAG,"Checked!");
				Log.d(TAG,"onlyUnsure: "+ onlyUnsure);
				
			}

        });
        */
        findViewById(R.id.linkToMoves).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            	statsHelper.storeMeasurement(getString(R.string.button_moves_linked), null,
            			String.valueOf(System.currentTimeMillis()));
                doRequestAuthInApp();
            }
        });
	    
        mAccount = GetOrCreateSyncAccount(this);
		CookieSyncManager.createInstance(getApplicationContext());
		System.out.println("At the end of the constructor");
    }

    public static Account GetOrCreateSyncAccount(Context context) {
    	// Get an instance of the Android account manager
    	AccountManager accountManager =
    			(AccountManager) context.getSystemService(
    					ACCOUNT_SERVICE);
    	Account[] existingAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
    	assert(existingAccounts.length <= 1);
    	if (existingAccounts.length == 1) {
    		return existingAccounts[0];
    	}

    	// Create the account type and default account
    	Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);	  
    	/*
    	 * Add the account and account type, no password or user data
    	 * If successful, return the Account object, otherwise report an error.
    	 */
    	if (accountManager.addAccountExplicitly(newAccount, null, null)) {
    		return newAccount;
    	} else {
    		System.err.println("Unable to create a dummy account to sync with!");
    		return null;
    	}
    }
    
    // get settings every time, update
	public void onResume()
    {
    	super.onResume();
    	syncUserSettings();
    }
    
    public String getEulaVersion() {
        ApplicationInfo ai = null;
        String eulaVersion = null;
        try {
             ai = getPackageManager().getApplicationInfo(this.getPackageName(),PackageManager.GET_META_DATA);
             Bundle bundle = ai.metaData;
             eulaVersion = bundle.get("EulaVersion").toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return eulaVersion;
    }

    /**
     * App-to-app. Creates an intent with data uri starting moves://app/authorize/xxx (for more
     * details, see documentation link below) to be handled by Moves app. When Moves receives this
     * Intent it opens up a dialog asking for user to accept the requested permission for your app.
     * The result of this user interaction is delivered to 
     * {@link #onActivityResult(int, int, android.content.Intent) }
     *
     * More details: https://dev.moves-app.com/docs/api
     */
    private void doRequestAuthInApp() {
    	String userName = UserProfile.getInstance(this).getUserEmail();
    	System.out.println("userName = "+userName+" with length "+userName.length());
    	// If the user email is not there, the connection to moves for authorization cannot happen so the method returns.
    	if (userName.length() == 0) {
    		Toast.makeText(this, "Please enter a userName", Toast.LENGTH_SHORT).show();
    		System.out.println("Returning due to lack of username");
    		return;
    	}
    	//the auth URI is created using the helper function to createAuthUri()
        Uri uri = createAuthUri("moves", "app", "/authorize").build();
        //System.out.println("URI is",+uri.toString);
        //New intent is created, and the uri is sent to moves app
        //new intent is created and the uri is sent to moves app
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
        	// the startActivityForResult() launches the activity with the specified intent.
        	// When that activity exits, we get back a result
        	// the onActivityResult() function is called with a result code
            startActivityForResult(intent, REQUEST_AUTHORIZE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Moves app not installed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the result from Moves authorization flow. The result is delivered as an uri documented
     * on the developer docs (see link below).
     *
     * More details: https://dev.moves-app.com/docs/api
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	//First we are processing requestCode
    	System.out.println("in MainActivity, requestCode = "+requestCode+" resultCode = "+resultCode);
    	if (requestCode == REQUEST_AUTHORIZE) {
    		if (data == null) {
    			Toast.makeText(this, "Moves app not installed. Are you running in the emulator?", Toast.LENGTH_SHORT).show();
    			return;
    		}
    		//Now we are processing resultCode
    		//resultUri is the Uri of the data this intent is targetting
    		Uri resultUri = data.getData();
    		UserProfile.getInstance(this).setLinkWithMovesDone(true);
    		System.out.println("resultUri = "+resultUri);
    		//setting the boolean in the profile to say the link to Moves was successful
    		if (resultCode != RESULT_OK) {
    			// populating textView to say the authentication with moves had trouble 
    			linkToMovesStatusField.setText("Error "+data+" while authenticating with moves");
    		}
    		saveMovesAuthToServer(resultUri);
    		// Have the activity widgets reflect the state of the user session
    		syncUserSettings();
    		// new DataUtils(this).saveUserName(getUserName());
    	} else if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
    		if (resultCode == Activity.RESULT_OK) {
    			String userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
    			Toast.makeText(this, userEmail, Toast.LENGTH_SHORT).show();
    			UserProfile.getInstance(this).setUserEmail(userEmail);
    			UserProfile.getInstance(this).setGoogleAuthDone(true);
    			syncUserSettings();
    			// new DataUtils(this).saveUserName(userEmail);
    		} else if (resultCode == Activity.RESULT_CANCELED) {
    			Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
    		}
    	}
    }
    
    //saving moves Authorization to http server
    private void saveMovesAuthToServer(final Uri resultUri) {
    	
    	//gets the user's email
    	final Context thisContext = this;
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
		        try {
		        	String code = resultUri.getQueryParameter("code");
		        	String state = resultUri.getQueryParameter("state");
		        	System.out.println("code = "+code+" state = "+state);

		        	JSONObject movesAuth = new JSONObject();
		        	movesAuth.put("code", code);
		        	movesAuth.put("state", state);
                    CommunicationHelper.saveMovesAuth(thisContext, movesAuth);
                    return "success";
		        } catch (IOException e) {
		        	// TODO Auto-generated catch block
		        	e.printStackTrace();
		        } catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String taskResult) {
				linkToMovesStatusField.setText(taskResult);
			}
		};
		task.execute((Void) null);	
    }
    
    
    //Get and update widgets with profile information    
    private void syncUserSettings() {
    	UserProfile profile = UserProfile.getInstance(this);
    	if(profile.isGoogleAuthDone()) {
    		userNameField.setText(profile.getUserEmail());
    		authStatusField.setText(R.string.googleAuthDoneString);
    		linkToMoves.setEnabled(true);
    	} else {
    		userNameField.setText("UNKNOWN");
    		authStatusField.setText(R.string.noGoogleAuthString);
    		linkToMoves.setEnabled(false);
    	}
    	if(profile.isLinkWithMovesDone()) {
    		linkToMovesStatusField.setText(R.string.linkToMovesDoneString);
    	} else {
    		linkToMovesStatusField.setText(R.string.noLinkToMovesString);
    	}
    }
    
    /**
     * Helper method for building a valid Moves authorize uri.
     */
    private Uri.Builder createAuthUri(String scheme, String authority, String path) {
        return new Uri.Builder()
                .scheme(scheme)
                .authority(authority)
                .path(path)
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("scope", "location activity")
                .appendQueryParameter("state", String.valueOf(SystemClock.uptimeMillis()));
    }
    


    //called when schedule sync button is clicked
    public void onScheduleSync(View view) {
		System.out.println("MainActivity forcing sync");
    	statsHelper.storeMeasurement(getString(R.string.button_sync_forced), null,
    			String.valueOf(System.currentTimeMillis()));
		Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(mAccount, AUTHORITY, b);
    }
    
    //called when force sync button is clicked 
    public void onForceSync(View view) {
    	statsHelper.storeMeasurement(getString(R.string.button_sync_forced), null,
    			String.valueOf(System.currentTimeMillis()));
		AsyncTask<Context, Void, Void> task = new AsyncTask<Context, Void, Void>() {
			protected Void doInBackground(Context... ctxt) {
				edu.berkeley.eecs.e_mission.data_sync.ConfirmTripsAdapter cta = new edu.berkeley.eecs.e_mission.data_sync.ConfirmTripsAdapter(ctxt[0], true);
				cta.onPerformSync(mAccount, null, AUTHORITY,
						null, null);
				return null;
			}
		};
		task.execute(this);
    }
    
    /*
    public void googleBrowserSignIn(View view) {
    	new GoogleBrowserAuth(this).getLoginEmail();
    }*/
    
    //called when get signin with Google button is clicked 
    public void googleSignIn(View view) {
    	//returns username
    	statsHelper.storeMeasurement(getString(R.string.button_account_changed), null,
    			String.valueOf(System.currentTimeMillis()));
    	new GoogleAccountManagerAuth(this, REQUEST_CODE_PICK_ACCOUNT).getUserName();
    }
    
    // called when server button is clicked
    public void getServerToken(View view) {
    	final String userName = UserProfile.getInstance(this).getUserEmail();
		AsyncTask<Context, Void, String> task = new AsyncTask<Context, Void, String>() {
			@Override
			protected String doInBackground(Context... ctxt) {
				// apparently, for doInBackground, ctxt is an array of Contexts
				// i.e. Context[]
				// Since we have only one context here, hardcoding this to read the first one
				if (ctxt.length == 0) {
					return null;
				} else {
					return GoogleAccountManagerAuth.getServerToken(ctxt[0], userName);
				}
			}
			
			@Override
			protected void onPostExecute(String taskResult) {
				//setting text to the value of taskResult
				Log.d(TAG,"Task Result: "+taskResult);
				tokenView.setText(taskResult);
			}

		};
		//execute
		task.execute(this);
    }
    
    public void showList(View view)
    {
    	Intent intent = new Intent(this,
				edu.berkeley.eecs.e_mission.ConfirmSectionListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("onlyUnsure", true);
		startActivity(intent);
    }
    
    public void showAllTrips(View view)
    {
    	Intent intent = new Intent(this,
				edu.berkeley.eecs.e_mission.ConfirmSectionListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
    }
}


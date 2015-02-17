package edu.berkeley.eecs.e_mission;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;
import edu.berkeley.eecs.e_mission.EulaFragment.OnActionListener;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

public class OnboardingActivity extends Activity implements OnActionListener {
	
	public static boolean introComplete, eulaComplete, signUpComplete, linkToMovesComplete, onboardingComplete;
	private Intent intent;
	
	/** For Signup */
	private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	UserProfile profile;
	
	
	/** For Link To Moves */
	private static String CLIENT_ID;

    /** the uri must match one of the callback uris registered for your app, 
     * but you may include additional query parameters in the uri. 
     * If omitted, the default callback uri is used. 
     * You can define the default redirect uri in app info's development tab. */
    private static String REDIRECT_URI;

	private static final int REQUEST_AUTHORIZE = 1; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);	    
	    setContentView(R.layout.activity_onboarding);
	    REDIRECT_URI = ConnectionSettings.getConnectURL(this) + "/movesCallback";
	    CLIENT_ID = ConnectionSettings.getMovesClientID(this);
	    intent = new Intent(this, ConfirmSectionListActivity.class);
	    
	    // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }
        
        showNextFragment();
	}
	
	/*
	 * Without this, pressing the "back" key while navigating through the workflow goes back
	 * through the fragments. This is particularly annoying because we don't have a "skip" option,
	 * so hitting "back" from the list view brings us to the "Link To Moves" view, at which
	 * point, we have to link again to moves and so on.
	 */
	@Override
	public void onBackPressed() {
		// We do NOT call the method in the super class because we don't want to be able
		// to go back through the fragments
		// http://stackoverflow.com/questions/17738966/how-to-disable-back-button-pressed-in-android-fragment-class
	}
	
	/** This method determines which fragment to show next and if onboarding complete, starts next activity. */
	private void showNextFragment() {
		getIntroComplete();
		getEulaComplete();
	    getSignUpComplete();
	    getLinkToMovesComplete();
	    
	    onboardingComplete = introComplete && eulaComplete && signUpComplete && linkToMovesComplete;

        if (!onboardingComplete) {
        	if (!introComplete) {
        		loadIntroFragment();
        	} else if (!eulaComplete) {
        		loadEulaFragment();
            } else if (!signUpComplete) {
        		loadSignupFragment();
            } else if (!linkToMovesComplete) {
            	loadLinkToMovesFragment();
            }
        } else {
        	//Can check anywhere in the app if Onboarding has been completed.
        	 setOnboardingComplete(true);
        	 //Start the next Activity (set in onCreate() )
             startActivity(intent);
             // Finish this activity so that it doesn't show up if you go back
             // from the next activity started
             finish();
        }
	}
	
	/** General Logic 
	 * If one wants to display EULA, Sign Up With Google, or Install/Link With Moves. 
	 * They can set the corresponding key to false in SharedPreferences and the screen will show
	 * next time the user opens the app as OnboardingActivity is the Launching Activity.
	 * */

	@Override
	public void onButtonClick(String buttonType) {
		if (buttonType.equals("getStarted")) {
				getStarted();
		} else if (buttonType.equals("acceptEula")) {
				acceptEula();
		} else if (buttonType.equals("declineEula")) {
				declineEula();
		} else if (buttonType.equals("signUp")) {
				signUp();
		} else if (buttonType.equals("linkToMoves")) {
				linkToMoves();
		} else if (buttonType.equals("installMoves")) {
				installMoves();
		}
	}

	private void getStarted() {
		setIntroComplete(true);
		showNextFragment();
	}
	
	public void getIntroComplete() {
		final String key = "intro";
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	introComplete = prefs.getBoolean(key, false);
	}
	
	public void getEulaComplete() {
		final String key = getEulaKey();
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	eulaComplete = prefs.getBoolean(key, false);
	}
	
	public void getSignUpComplete() {
		final String key = "signUp";
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	signUpComplete = prefs.getBoolean(key, false);
	}
	
	public void getLinkToMovesComplete() {
        if (Build.FINGERPRINT.startsWith("generic")) {
            // We are running in an emulator
            // TODO: Populate the database with some dummy data here to allow easier testing
            linkToMovesComplete = true;
        } else {
            // We are running in a real device, so we want to link to moves to collect data
            final String key = "linkToMoves";
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            linkToMovesComplete = prefs.getBoolean(key, false);
        }
	}
	
	public void getOnboardingComplete() {
		final String key = "onboarding";
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	onboardingComplete = prefs.getBoolean(key, false);
	}
	
	/*
	 * The previous getOnboarding non-static method is not easily usable outside this class
	 * since it requires the caller to create an instance of this class. So we define a static method
	 * that can work without instantiating the class.
	 */
	public static boolean getOnboardingComplete(Context ctxt) {
		final String key = "onboarding";
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
    	return prefs.getBoolean(key, false);
	}
	
	public void setIntroComplete(boolean val) {
		introComplete = val;
		final String key = "intro";
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(key, val);
		e.commit();
	}
	
	public void setEulaComplete(boolean val) {
		eulaComplete = val;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(getEulaKey(), val);
		e.commit();
	}
	
	public void setSignUpComplete(boolean val) {
		signUpComplete = val;
		final String key = "signUp";
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(key, val);
		e.commit();
	}
	
	public void setLinkToMovesComplete(boolean val) {
		linkToMovesComplete = val;
		final String key = "linkToMoves";
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(key, val);
		e.commit();
	}
	
	public void setOnboardingComplete(boolean val) {
		onboardingComplete = val;
		final String key = "onboarding";
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(key, val);
		e.commit();
	}
	
	private void loadEulaFragment() {
		// Create new fragment and transaction
    	Fragment newFragment = new EulaFragment();
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();

    	// Replace whatever is in the fragment_container view with this fragment,
    	// and add the transaction to the back stack
    	transaction.replace(R.id.fragment_container, newFragment);
    	transaction.addToBackStack(null);

    	// Commit the transaction
    	transaction.commit();	
	}
	
	private void loadIntroFragment(){
		// Create new fragment and transaction
    	Fragment newFragment = new IntroFragment();
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();
    	transaction.add(R.id.fragment_container, newFragment);
    	transaction.addToBackStack(null);
    	// Commit the transaction
    	transaction.commit();
	}
	
	/**
	 * Accept EULA and proceed with main application.
	 */
	public void acceptEula() {
		
		setEulaComplete(true);
		showNextFragment();	
	}
	
	/**
	 * Decline EULA.
	 */
	public void declineEula() {
		setEulaComplete(false);
		/* Make sure to exit the activity if they decline -
		 * this is not allowed on iOS, but is allowed on android
		 */
		finish();
	}

	/** Sets the accepted Eula Version in the server. */
	public void saveEulaVerToServer() {
    	//gets the user's email
    	final String userName = UserProfile.getInstance(this).getUserEmail();
    	final String eulaVer = getEulaVersion();
    	final Context thisContext = this;
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String REDIRECT_URI = getString(R.string.connect_url) + "/profile/consent";
		    	HttpPost msg = new HttpPost(REDIRECT_URI);
		    	msg.setHeader("Content-Type", "application/json");
		    	String userToken = GoogleAccountManagerAuth.getServerToken(thisContext, userName);
		    
		    	try {
		    		
		    		JSONObject toPush = new JSONObject();
					toPush.put("user", userToken);
					toPush.put("version", eulaVer);

		    	
		    	//set the value of the httppost object called msg to the JSON object called toPush
		    	//toPush has all the values taken from the uri
				msg.setEntity(new StringEntity(toPush.toString()));
		    	
		    	System.out.println("Posting data to "+msg.getURI());
		    	
		    	//create connection
		    	AndroidHttpClient connection = AndroidHttpClient.newInstance(R.class.toString());
		    	
		    	//execute httpresponse
		    	HttpResponse response;
				response = connection.execute(msg);
		    	System.out.println("Got response "+response+" with status "+response.getStatusLine());
		    	connection.close();
		    	
		    	} catch (JSONException e) {
					e.printStackTrace();
		    	} catch (UnsupportedEncodingException e){
		    		e.printStackTrace();
		    	} catch (IOException e) {
		    		e.printStackTrace();
		    	}
		    	return null;
			}
			@Override
			protected void onPostExecute(String taskResult) {
				/** To test if call is being made */
				//Toast.makeText(getApplicationContext(),eulaVer, 
		                //Toast.LENGTH_LONG).show();
			}
		};
		task.execute((Void) null);	
    }
	
	public String getEulaKey() {
		return "eula_"+ getEulaVersion();
	}
	
	//Gets the Eula Version stated in the metadata tag in the Android Manifest.
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
	 * 
	 * For Signup
	 *
	 *              */
	
	/** Called when the user clicks the "Sign Up with Google" button */
	public void signUp() {
	    // Creates a new GoogleAccountManagerAuth object which authenticates and returns a resultCode 
		new GoogleAccountManagerAuth(this, REQUEST_CODE_PICK_ACCOUNT).getUserName();
	}
	
	private void loadSignupFragment() {
		// Create new fragment and transaction
    	Fragment newFragment = new SignupFragment();
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();

    	// Replace whatever is in the fragment_container view with this fragment,
    	// and add the transaction to the back stack
    	transaction.replace(R.id.fragment_container, newFragment);
    	transaction.addToBackStack(null);

    	// Commit the transaction
    	transaction.commit();	
	}
	
	/**
     * Handle the result from Moves authorization flow. The result is delivered as an uri documented
     * on the developer docs (see link below).
     *
     * @see https://dev.moves-app.com/docs/api
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//When the GoogleAccountManagerAuth returns the resultCode, the system will call the onActivityResult
    	System.out.println("in MainActivity, requestCode = "+requestCode+" resultCode = "+resultCode);
     	if (requestCode == REQUEST_AUTHORIZE) {
    		if (data == null) {
    			Toast.makeText(this, "Please sign into the Moves app first.", Toast.LENGTH_SHORT).show();
    			return;
    		}
    		Uri resultUri = data.getData();
    		UserProfile.getInstance(this).setLinkWithMovesDone(true);
    		System.out.println("resultUri = "+resultUri);
    		if (resultCode != RESULT_OK) {
    			intent.putExtra("MOVES_STATUS_FIELD", "Error "+data+" while authenticating with moves");
    		}
    		saveMovesAuthToServer(resultUri);
    		// Have the activity widgets reflect the state of the user session
    		syncUserSettings();
    		// new DataUtils(this).saveUserName(getUserName());
    		showNextFragment();
    	} else if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
    		//if it is the right request code
    		if (resultCode == Activity.RESULT_OK) {
    			// the result code is correct
    			String userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
    			Toast.makeText(this, userEmail, Toast.LENGTH_SHORT).show();
    			UserProfile.getInstance(this).setUserEmail(userEmail);
    			UserProfile.getInstance(this).setGoogleAuthDone(true);
    			registerUser();
    			//syncUserSettingsSignUp();
    			saveEulaVerToServer();
    			showNextFragment();
    			// new DataUtils(this).saveUserName(userEmail);
    		} else if (resultCode == Activity.RESULT_CANCELED) {
    			Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
    		}
    	}
    }
    
    private void registerUser() {
    	profile = UserProfile.getInstance(this);
    	if(profile.isGoogleAuthDone()) {
    		intent.putExtra("USERNAME", profile.getUserEmail());
    		intent.putExtra("AUTH_STATUS_FIELD", R.string.googleAuthDoneString);
    		/* 
    		 * This is a little tricky since we don't want to get the settings until the signup
    		 * has completed successfully because calling settings before the registration is complete will fail.
    		 * So we defined a callback method that is invoked when the registration is complete, and that reads the settings.
    		 */
    		profile.registerUser(new UserProfile.RegisterUserResult() {
    			public void registerComplete(String result) {
    				syncUserSettingsSignUp();
    			}
    		});
    		//Set Profile Settings
    		AppSettings.setProfileSettings(this);
    		//Set Sign up Complete.
    		setSignUpComplete(true);
    	} else {
    		intent.putExtra("USERNAME", "UNKNOWN");
    		intent.putExtra("AUTH_STATUS_FIELD", R.string.noGoogleAuthString);
    	} 
    }

    private void syncUserSettingsSignUp() {
    	profile = UserProfile.getInstance(this);
    	if(profile.isGoogleAuthDone()) {
    		intent.putExtra("USERNAME", profile.getUserEmail());
    		intent.putExtra("AUTH_STATUS_FIELD", R.string.googleAuthDoneString);
    		//Set Profile Settings
    		AppSettings.setProfileSettings(this);
    		//Set Sign up Complete.
    		setSignUpComplete(true);
    	} else {
    		intent.putExtra("USERNAME", "UNKNOWN");
    		intent.putExtra("AUTH_STATUS_FIELD", R.string.noGoogleAuthString);
    	}
    }
    
    /** For LinkToMoves Fragment */
    
    private void loadLinkToMovesFragment() {
    	// Create new fragment and transaction
    	Fragment newFragment = new LinkToMovesFragment();
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();

    	// Replace whatever is in the fragment_container view with this fragment,
    	// and add the transaction to the back stack
    	transaction.replace(R.id.fragment_container, newFragment);
    	transaction.addToBackStack(null);

    	// Commit the transaction
    	transaction.commit();	
	}
    
    /** Called when the user clicks the "Link With Moves" button, must have signed in to Moves to work. */
	public void linkToMoves() {
		doRequestAuthInApp();
	}
	
	/** Called when the user clicks the "Install Moves" button */
	public void installMoves() {
	    // Launch Dialog before directing to Market
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.after_install_moves);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	 //Direct User to Android Market to Download Moves
	       		Intent goToMarket = new Intent(Intent.ACTION_VIEW)
	       	    .setData(Uri.parse("market://details?id=com.protogeo.moves"))
	       	    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	       		startActivity(goToMarket);
	           }
	       });
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               // User cancelled the dialog
	           }
	       });
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
     * App-to-app. Creates an intent with data uri starting moves://app/authorize/xxx (for more
     * details, see documentation link below) to be handled by Moves app. When Moves receives this
     * Intent it opens up a dialog asking for user to accept the requested permission for your app.
     * The result of this user interaction is delivered to 
     * {@link #onActivityResult(int, int, android.content.Intent) }
     *
     * @see https://dev.moves-app.com/docs/api
     */
    private void doRequestAuthInApp() {
    	String userName = UserProfile.getInstance(this).getUserEmail();
    	System.out.println("userName = "+userName+" with length "+userName.length());
    	if (userName.length() == 0) {
    		Toast.makeText(this, "Please enter a userName", Toast.LENGTH_SHORT).show();
    		System.out.println("Returning due to lack of username");
    		return;
    	}
        Uri uri = createAuthUri("moves", "app", "/authorize").build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivityForResult(intent, REQUEST_AUTHORIZE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Moves app not installed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveMovesAuthToServer(final Uri resultUri) {
    	final String userName = UserProfile.getInstance(this).getUserEmail();
    	final Context thisContext = this;
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
		        try {
		        	String userToken = GoogleAccountManagerAuth.getServerToken(thisContext, userName);
		        	String code = resultUri.getQueryParameter("code");
		        	String state = resultUri.getQueryParameter("state");
		        	System.out.println("userToken = "+userToken+" code = "+code+" state = "+state);
		        			    		
		        	HttpPost msg = new HttpPost(REDIRECT_URI);
		        	msg.setHeader("Content-Type", "application/json");
		        	
		        	JSONObject toPush = new JSONObject();
		        	toPush.put("user", userToken);
		        	toPush.put("code", code);
		        	toPush.put("state", state);
		        	msg.setEntity(new StringEntity(toPush.toString()));
		        	
		        	System.out.println("Posting data to "+msg.getURI());
		        	AndroidHttpClient connection = AndroidHttpClient.newInstance(R.class.toString());
		        	HttpResponse response = connection.execute(msg);
		        	System.out.println("Got response "+response+" with status "+response.getStatusLine());
		        	connection.close();
		        	return response.getStatusLine().toString();
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
				intent.putExtra("MOVES_STATUS_FIELD", taskResult);
			}
		};
		task.execute((Void) null);	
    }
    
    private void syncUserSettings() {
    	UserProfile profile = UserProfile.getInstance(this);
    	if(profile.isLinkWithMovesDone()) {
    		intent.putExtra("MOVES_STATUS_FIELD", R.string.linkToMovesDoneString);
    		setLinkToMovesComplete(true);
    	} else {
    		intent.putExtra("MOVES_STATUS_FIELD", R.string.noLinkToMovesString);
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
}

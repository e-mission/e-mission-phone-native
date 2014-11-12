package edu.berkeley.eecs.e_mission;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

public class PreferencesActivity extends Activity {
	
	private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	UserProfile profile;
	Intent intent;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        intent = new Intent(this, SettingsActivity.class);
	}
	
	/** Called when the user clicks the "Sign Up with Google" button */
	public void signUp(View view) {
	    // Do something in response to button
		new GoogleAccountManagerAuth(this, REQUEST_CODE_PICK_ACCOUNT).getUserName();
		
		// Launch Link to Moves Activity
		// Launch Preferences
		startActivity(intent);
		finish();
	}
	
	/**
     * Handle the result from Moves authorization flow. The result is delivered as an uri documented
     * on the developer docs (see link below).
     *
     * @see https://dev.moves-app.com/docs/api
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	System.out.println("in MainActivity, requestCode = "+requestCode+" resultCode = "+resultCode);
    	if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
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
    
    //TODO: Discuss syncUserSettings behavior. Make sure to enable linktoMoves button. If Auth refactored, override syncUsers method in MainActivity
    private void syncUserSettings() {
    	profile = UserProfile.getInstance(this);
    	if(profile.isGoogleAuthDone()) {
    		intent.putExtra("USERNAME", profile.getUserEmail());
    		intent.putExtra("AUTH_STATUS_FIELD", R.string.googleAuthDoneString);
    		//linkToMoves.setEnabled(true);
    	} else {
    		intent.putExtra("USERNAME", "UNKNOWN");
    		intent.putExtra("AUTH_STATUS_FIELD", R.string.noGoogleAuthString);
    		//linkToMoves.setEnabled(false);
    	}
/*    	if(profile.isLinkWithMovesDone()) {
    		linkToMovesStatusField.setText(R.string.linkToMovesDoneString);
    	} else {
    		linkToMovesStatusField.setText(R.string.noLinkToMovesString);
    	}*/
    }

}


package edu.berkeley.eecs.e_mission.auth;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;

public class GoogleBrowserAuth {
	private static final int LOGIN_REQUEST = 2;
	private Activity mCtxt;

	public GoogleBrowserAuth(Activity ctxt) {
		mCtxt = ctxt;
	}
	
	// TODO: Figure out way to call startActivityForResult without passing in the parent activity
	public void getLoginEmail() {
    	List<NameValuePair> getParams = new ArrayList<NameValuePair>();
    	getParams.add(new BasicNameValuePair("response_type", "code"));
    	getParams.add(new BasicNameValuePair("client_id", "97387382925-ik6180v9qbbgs33lcql4nhln25u7rpdp.apps.googleusercontent.com"));
    	getParams.add(new BasicNameValuePair("redirect_uri", "http://localhost"));
    	getParams.add(new BasicNameValuePair("scope", "profile"));
    	// TODO: Replace this with some form of UUID to avoid cross-site-request-forgery
    	getParams.add(new BasicNameValuePair("state", "currently_unused"));
    	webSignIn("https://accounts.google.com/o/oauth2/auth", getParams);
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {    
        switch (requestCode) {
            case LOGIN_REQUEST:
            	System.out.println("After login, token = "+data);
            	break;
        }
    }

	// TODO: Move this to a common class when we get the next browser based auth
    private void webSignIn(String primaryURL, List<NameValuePair> getParams) {
    	Intent activityIntent = new Intent(mCtxt, edu.berkeley.eecs.e_mission.auth.WebLoginActivity.class);
    	activityIntent.putExtra("url", primaryURL);
    	String paramsString = URLEncodedUtils.format(getParams, "UTF-8");
    	activityIntent.putExtra("params", paramsString);
    	mCtxt.startActivityForResult(activityIntent, LOGIN_REQUEST);
    }
}

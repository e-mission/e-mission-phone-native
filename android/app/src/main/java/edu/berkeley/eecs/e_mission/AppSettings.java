package edu.berkeley.eecs.e_mission;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/*
 * Single class that returns all the connection level settings that need to be customized
 * when 
 */

public class AppSettings {
	
	public static void putResultUrl(Context ctxt, String url) {
		SharedPreferences prefs = ctxt.getSharedPreferences("profileSettings",ctxt.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("result_url", url);
		editor.commit();
		String result = prefs.getString("result_url", null);
	}
	
	public static String getResultUrl(Context ctxt) {
		SharedPreferences prefs = ctxt.getSharedPreferences("profileSettings",ctxt.MODE_PRIVATE);
		String result_url = ConnectionSettings.getConnectURL(ctxt)+"/compare";
		return result_url;
	}
	
	public static void setProfileSettings(Context ctxt){
    	//gets the user's email
    	final Context thisContext = ctxt;
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
		    	try {
                    return CommunicationHelper.getUserSettings(thisContext);
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

				if (taskResult != null) {
					System.out.println("Here's the result JSON String "+taskResult);
					setAppSettings(thisContext, taskResult);
					
				}
			}
		};
		task.execute((Void) null);	
}
	public static void setAppSettings(Context ctxt, String result) {
		String url = null;
		try {
			JSONObject obj = new JSONObject(result);
			url = obj.getString("result_url");
			putResultUrl(ctxt, url);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}

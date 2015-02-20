package edu.berkeley.eecs.e_mission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

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
    	final String userName = UserProfile.getInstance(ctxt).getUserEmail();
    	final Context thisContext = ctxt;
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
		    	
		    	String userToken = GoogleAccountManagerAuth.getServerToken(thisContext, userName);
				// TODO: Restructure this later to combine with the data sync class
				HttpPost msg = new HttpPost(ConnectionSettings.getConnectURL(thisContext)+
											"/profile/settings");
				msg.setHeader("Content-Type", "application/json");
				String result = null;
		    	try {
		    		//String result;
		    		JSONObject toPush = new JSONObject();
					toPush.put("user", userToken);

					msg.setEntity(new StringEntity(toPush.toString()));
			    	
			    	System.out.println("Posting data to "+msg.getURI());
			    	
			    	//create connection
			    	AndroidHttpClient connection = AndroidHttpClient.newInstance(R.class.toString());
				    HttpResponse response = connection.execute(msg);
				    StatusLine statusLine = response.getStatusLine();
				    System.out.println("Got response "+response+" with status "+statusLine);
				    int statusCode = statusLine.getStatusCode();
				    
				    //String json = EntityUtils.toString(response.getEntity());
				    
		    		if(statusCode == 200){
		    			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					    StringBuilder builder = new StringBuilder();
					    String currLine = null;   
					    while ((currLine = in.readLine()) != null) {
					    	builder.append(currLine+"\n");
					    }
					    result = builder.toString();
					    System.out.println("Result Summary JSON = "+result);
					    in.close();
					    
					    /** How it is done in ConfirmTripsAdapter */
					    /*String rawJSON = in.readLine();
						// System.out.println("Raw JSON = "+rawJSON);
						in.close();
						connection.close();
						JSONObject parentObj = new JSONObject(rawJSON);
						return parentObj.getJSONArray("sections");*/
		    			
		    		} else {
		    			Log.e(R.class.toString(),"Failed to get JSON object");
		    		}
				    connection.close();
				    return result;
		    	
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

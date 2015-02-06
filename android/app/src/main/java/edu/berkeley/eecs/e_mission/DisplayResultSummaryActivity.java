package edu.berkeley.eecs.e_mission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

@SuppressLint("SetJavaScriptEnabled")
public class DisplayResultSummaryActivity extends Activity {


	private WebView displaySummaryView;
	private ClientStatsHelper statsHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		statsHelper = new ClientStatsHelper(this);
		setContentView(R.layout.activity_display_result_summary);
		displaySummaryView = (WebView) findViewById(R.id.displayResultSummaryView);
		displaySummary();
	}
	

	void displaySummary() {
		final long startMs = System.currentTimeMillis();
		final Context thisContext = this;
		final String userName = UserProfile.getInstance(this).getUserEmail();
		final String result_url = AppSettings.getResultUrl(this);
		
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {				
				try {
					String userToken = GoogleAccountManagerAuth.getServerToken(thisContext, userName);
					// TODO: Restructure this later to combine with the data sync class
					HttpPost msg = new HttpPost(result_url);
					msg.setHeader("Content-Type", "application/json");
					
					JSONObject toPush = new JSONObject();
					toPush.put("user", userToken);
					msg.setEntity(new StringEntity(toPush.toString()));
					
				    AndroidHttpClient connection = AndroidHttpClient.newInstance("E-Mission");
				    HttpResponse response = connection.execute(msg);
				    System.out.println("Got response "+response+" with status "+response.getStatusLine());
				    BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				    StringBuilder builder = new StringBuilder();
				    String currLine = null;   
				    while ((currLine = in.readLine()) != null) {
				    	builder.append(currLine+"\n");
				    }
				    String rawHTML = builder.toString();
				    // System.out.println("Raw HTML = "+rawHTML);
				    in.close();
				    connection.close();
				    return rawHTML;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "<html><body>"+e.getLocalizedMessage()+"</body></html>";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "<html><body>"+e.getLocalizedMessage()+"</body></html>";
				}
			}

			@Override
			protected void onPostExecute(String taskResult) {
				if (taskResult != null) {
					displaySummaryView.getSettings().setJavaScriptEnabled(true);
					displaySummaryView.loadDataWithBaseURL(ConnectionSettings.getConnectURL(thisContext),
							taskResult, null, null, null);
				} else {
					long endMs = System.currentTimeMillis();
					statsHelper.storeMeasurement(thisContext.getString(R.string.result_display_failed),
							null, String.valueOf(endMs));					
				}
				long endMs = System.currentTimeMillis();
				statsHelper.storeMeasurement(thisContext.getString(R.string.result_display_duration),
						String.valueOf(endMs - startMs), String.valueOf(endMs));

			}

		};
		task.execute((Void)null);
	}
}

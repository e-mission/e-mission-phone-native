package edu.berkeley.eecs.e_mission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
                    URL url = new URL(result_url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setUseCaches(true);
                    connection.setDoOutput(false);
                    connection.setDoInput(true);
                    connection.setReadTimeout( 10000 /*milliseconds*/ );
                    connection.setConnectTimeout(15000 /* milliseconds */);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("user", "" + userToken);

                    int maxStale = 60 * 60 * 6; // tolerate 6-hours stale
                    connection.addRequestProperty("Cache-Control", "max-stale=" + maxStale);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    int code = connection.getResponseCode();
                    System.out.println("Connection response status "+connection.getResponseCode());
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
                    return rawHTML;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return "<html><body>"+e.getLocalizedMessage()+"</body></html>";
                } catch (IOException e) {
                    e.printStackTrace();
                    return "<html><body>" + e.getLocalizedMessage() + "</body></html>";
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

    public void onDestroy() {
        super.onDestroy();

        EMission app = (EMission) getApplication();
        app.flushCache();
    }
}

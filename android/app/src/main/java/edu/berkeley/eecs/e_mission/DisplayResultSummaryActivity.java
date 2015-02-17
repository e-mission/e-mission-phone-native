package edu.berkeley.eecs.e_mission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

@SuppressLint("SetJavaScriptEnabled")
public class DisplayResultSummaryActivity extends Activity {
    
    
    private WebView displaySummaryView;
    private ClientStatsHelper statsHelper;
    
    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        final Context thisContext = this;
        super.onCreate(savedInstanceBundle);
        
        // create a cache at application start up
        try {
            File httpCacheDir = new File(thisContext.getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        }
        catch(IOException e){
            Log.i("HTTPCACHE", "HTTP response cache installation failed:" + e);
        }
        
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
                    System.out.println("http post result_url before httpPost " + result_url);
                    // TODO: Restructure this later to combine with the data sync class
                    
                    URL url = new URL(result_url);
                    //can we preload this url?
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    // allow for httpresponse caching here
                    connection.setUseCaches(true);
                    connection.setReadTimeout( 10000 /*milliseconds*/ );
                    connection.setConnectTimeout(15000 /* milliseconds */);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    // allow for httpresponse caching here
                    connection.setUseCaches(true);
                    
                    // force a cache response
                    connection.addRequestProperty("Cache-Control", "only-if-cached");
                    
                    InputStream inputStream;
                    
                    try {
                        // cached inputstream
                        inputStream = connection.getInputStream();
                    }
                    catch (FileNotFoundException e) {
                        // Resource not cached -- fallback
                        System.out.println("Cache miss, execute httpconnection for display summary");
                        connection.connect();
                        // output stream expects to work with bytes
                        byte[] outputBytes = "{'user': userToken}".getBytes("UTF-8");
                        OutputStream os = connection.getOutputStream();
                        os.write(outputBytes);
                        os.flush();
                        
                        if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
                        }
                        System.out.println("Connection response status "+connection.getResponseCode());
                        
                        inputStream = connection.getInputStream();
                    }
                    
                    
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String currLine = null;
                    while ((currLine = in.readLine()) != null) {
                        builder.append(currLine+"\n");
                    }
                    String rawHTML = builder.toString();
                    // System.out.println("Raw HTML = "+rawHTML);
                    in.close();
                    connection.disconnect();
                    return rawHTML;
                } catch (MalformedURLException e) {
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
    /*
     protected void onStop() {
     // requires a minsdkversion 14 (current is 11 in androidmanifest.xml
     
     HttpResponseCache cache = HttpResponseCache.getInstalled();
     if (cache != null) {
     cache.flush();
     }
     } */
}

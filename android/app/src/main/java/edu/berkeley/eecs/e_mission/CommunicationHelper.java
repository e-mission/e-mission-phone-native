package edu.berkeley.eecs.e_mission;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.berkeley.eecs.e_mission.auth.GoogleAccountManagerAuth;
import edu.berkeley.eecs.e_mission.auth.UserProfile;

public class CommunicationHelper {
    public static final String TAG = "CommunicationHelper";

    public static String readResults(Context ctxt, String cacheControlProperty)
                throws MalformedURLException, IOException {
        final String result_url = AppSettings.getResultUrl(ctxt);
        final String userName = UserProfile.getInstance(ctxt).getUserEmail();
        final String userToken = GoogleAccountManagerAuth.getServerToken(ctxt, userName);

        final URL url = new URL(result_url);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setUseCaches(true);
        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setReadTimeout(10000 /*milliseconds*/);
        connection.setConnectTimeout(15000 /* milliseconds */);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User", "" + userToken);

        /* Force the invalidation of the results summary cache entry */
        connection.addRequestProperty("Cache-Control", cacheControlProperty);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.connect();

        final InputStream inputStream = connection.getInputStream();
        final int code = connection.getResponseCode();
        Log.d(TAG, "Update Connection response status " + connection.getResponseCode());
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }
        final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder builder = new StringBuilder();
        String currLine = null;
        while ((currLine = in.readLine()) != null) {
            builder.append(currLine+"\n");
        }
        final String rawHTML = builder.toString();
        in.close();
        connection.disconnect();
        return rawHTML;
    }
}

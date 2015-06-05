package edu.berkeley.eecs.e_mission;

/**
 * Created by RZarrabi on 6/5/2015.
 */


import java.net.MalformedURLException;
import android.os.AsyncTask;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import org.apache.cordova.CordovaActivity;

public class CompetitionActivity extends CordovaActivity {

    private MobileServiceClient mClient;
    private MobileServiceTable<ScoreActivity> mToDoTable;

    public boolean onCreate() {
        try {
// Create the Mobile Service Client instance, using the provided
// Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://e-mission.azure-mobile.net/",
                    "aBNpasaSXoYAhvstmXYvtkEfFYudbt33", this
            );
            // Get the Mobile Service Table instance to use
            mToDoTable = mClient.getTable(ScoreActivity.class);
            return true;
        } catch (MalformedURLException e) {
            System.out.print("There was an error creating the Mobile Service. Verify the URL");
        }
        return false;
    }


    public void addItem(ScoreActivity thing) {
        final ScoreActivity a = thing;
        new AsyncTask<Void, Void, Void>() {

	        @Override
	        protected Void doInBackground(Void... params) {
	            try {
	                mToDoTable.insert(a).get();
	            } catch (Exception exception) {
	               // createAndShowDialog(exception, "Error");
                 }
	            return null;
	        }
	    }.execute();
        }
    }























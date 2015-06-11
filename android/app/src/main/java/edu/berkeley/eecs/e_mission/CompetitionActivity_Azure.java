package edu.berkeley.eecs.e_mission;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.ListView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import org.apache.cordova.CordovaActivity;

import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.*;
import com.microsoft.windowsazure.mobileservices.table.*;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.operations.RemoteTableOperationProcessor;
import com.microsoft.windowsazure.mobileservices.table.sync.operations.TableOperation;
import com.microsoft.windowsazure.mobileservices.table.sync.push.MobileServicePushCompletionResult;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandlerException;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;


public class CompetitionActivity_Azure extends Activity {

    private MobileServiceClient mClient;
    private MobileServiceSyncTable<ScoreActivity> mToDoTable;
    private Query mPullQuery;
    private EditText editTheText;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        System.out.println("Doing Something");

        setContentView(R.layout.mpg);
        try {

            System.out.println("Passed try statement");
            mClient = new MobileServiceClient(
                    "https://e-mission.azure-mobile.net/",
                    "aBNpasaSXoYAhvstmXYvtkEfFYudbt33",
                    this
            );

            authenticate();


        } catch (MalformedURLException e) {
            System.out.println("Failed to connect");
        } catch (Exception e) {
            System.out.println("Exception");
        }
    }

    private void createTable() {

        // Get the Mobile Service Table instance to use

        System.out.println("Create table");

        SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "Item", null, 1);
        MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();
        MobileServiceSyncContext syncContext = mClient.getSyncContext();
        System.out.println("opened a bunch of stuff");

        mPullQuery = mClient.getTable(ScoreActivity.class).where().field("score").gt(0);
        System.out.println("mpullQuery");

        Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
        try {
            localStore.defineTable("Score Item", tableDefinition);
            syncContext.initialize(localStore, handler).get();
        } catch (Exception e){

        }
        System.out.println("Sync context initialized");
        // Get the Mobile Service Table instance to use
        mToDoTable = mClient.getSyncTable(ScoreActivity.class);
        editTheText = (EditText) findViewById(R.id.editText);

        editTheText.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                System.out.println("onKey");
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            // Perform action on key press
                        System.out.println("ENTER");

                    return true;
                }
                return false;
            }
        });
        System.out.println(editTheText);

        refreshItemsFromTable();
    }

    private void authenticate() {
        // Login using the Google provider.

        ListenableFuture<MobileServiceUser> mLogin = mClient.login(MobileServiceAuthenticationProvider.Google);

        Futures.addCallback(mLogin, new FutureCallback<MobileServiceUser>() {
            @Override
            public void onFailure(Throwable exc) {
                //createAndShowDialog((Exception) exc, "Error");
            }

            @Override
            public void onSuccess(MobileServiceUser user) {
                //createAndShowDialog(String.format(
                        //"You are now logged in - %1$2s",
                        //user.getUserId()), "Success");
                System.out.println("Is this happenening??????");
                createTable();
            }
        });
    }

    public void addItem(View view) {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final ScoreActivity item = new ScoreActivity();
        //item.mText =

        // Insert the new item
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ScoreActivity entity = mToDoTable.insert(item).get();
                    if (true) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //mAdapter.add(entity);
                                mToDoTable.insert(entity);
                            }
                        });
                    }
                } catch (Exception exception) {
                    //createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();

    }

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final MobileServiceList<ScoreActivity> result = mToDoTable.read(mPullQuery).get();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //mAdapter.clear();

                            for (ScoreActivity item : result) {
                                //mAdapter.add(item);
                            }
                        }
                    });
                } catch (Exception exception) {
                    //createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();
    }

    private class ConflictResolvingSyncHandler implements MobileServiceSyncHandler {

        @Override
        public JsonObject executeTableOperation(
                RemoteTableOperationProcessor processor, TableOperation operation)
                throws MobileServiceSyncHandlerException {

            MobileServicePreconditionFailedException ex = null;
            JsonObject result = null;
            try {
                result = operation.accept(processor);
            } catch (MobileServicePreconditionFailedException e) {
                ex = e;
            } catch (Throwable e) {
                ex = (MobileServicePreconditionFailedException) e.getCause();
            }

            if (ex != null) {
                // A conflict was detected; let's force the server to "win"
                // by discarding the client version of the item
                // Other policies could be used, such as prompt the user for
                // which version to maintain.
                JsonObject serverItem = null;

                if (serverItem == null) {
                    // Item not returned in the exception, retrieving it from the server
                    try {
                        serverItem = mClient.getTable(operation.getTableName()).lookUp(operation.getItemId()).get();
                    } catch (Exception e) {
                        throw new MobileServiceSyncHandlerException(e);
                    }
                }

                result = serverItem;
            }

            return result;
        }

        @Override
        public void onPushComplete(MobileServicePushCompletionResult result)
                throws MobileServiceSyncHandlerException {
        }
    }


}
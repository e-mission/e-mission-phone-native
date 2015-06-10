package edu.berkeley.eecs.e_mission;
import android.os.AsyncTask;
import android.view.View;

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




public class CompetitionActivity_Azure extends CordovaActivity {

    private MobileServiceClient mClient;
    private MobileServiceSyncTable<ScoreActivity> mToDoTable;
    private Query mPullQuery;



    public void onCreate() {

        try {
            mClient = new MobileServiceClient(
                    "https://e-mission.azure-mobile.net/",
                    "aBNpasaSXoYAhvstmXYvtkEfFYudbt33",
                    this
            );
            SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "Item", null, 1);
            MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();
            MobileServiceSyncContext syncContext = mClient.getSyncContext();

            mPullQuery = mClient.getTable(ScoreActivity.class).where().field("score").gt(0);

            Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();

            localStore.defineTable("Score Item", tableDefinition);
            syncContext.initialize(localStore, handler).get();

            // Get the Mobile Service Table instance to use
            mToDoTable = mClient.getSyncTable(ScoreActivity.class);

            refreshItemsFromTable();


        } catch (MalformedURLException e) {
            System.out.println("Failed to connect");
        } catch (Exception e) {
            System.out.println("Exception");
        }

    }

    public void addItem(View view) {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final ScoreActivity item = new ScoreActivity();

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
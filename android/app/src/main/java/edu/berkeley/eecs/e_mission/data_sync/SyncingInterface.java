package edu.berkeley.eecs.e_mission.data_sync;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.SyncResult;
import android.os.Bundle;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by RZarrabi on 6/15/2015.
 */
public interface SyncingInterface {
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult);

    public void updateResultsSummary(String userToken) throws MalformedURLException, IOException;


}

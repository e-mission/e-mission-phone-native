/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package edu.berkeley.eecs.e_mission.cordova;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.cordova.CordovaActivity;

import edu.berkeley.eecs.e_mission.R;

public class DiaryActivity extends CordovaActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();
        // Set by <content src="index.html" /> in config.xml
        loadUrl("file:///android_asset/www/listview.html");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "Clicked on settings", Toast.LENGTH_LONG);
                return true;
            case R.id.action_result_summary:
                Toast.makeText(this, "Clicked on result", Toast.LENGTH_LONG);
                return true;
            case R.id.action_launch_cordova_tabs:
                Toast.makeText(this, "Clicked on diary", Toast.LENGTH_LONG);
                return true;



            // case R.id.action_launch_cordova_sidebar:
            //     launchCordovaSidebarActivity();
            //     return true;
        }
        return false;
    }

}

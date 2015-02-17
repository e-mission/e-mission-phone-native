package edu.berkeley.eecs.e_mission;

import android.app.Application;
import android.net.http.HttpResponseCache;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * A base class for all of EMission. Any code that should be
 * run upon startup (and never again) can be put here.
 * Created by zrfield on 2/16/15.
 */
public class EMission extends Application {

    public EMission() {
        // this method fires only once per application start.

        Log.i("main", "Application initiated");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // this method fires once as well as constructor
        // but also application has context here

        Log.i("main", "Application onCreate fired");

        // create a cache at application start up
        try {
            File httpCacheDir = new File(this.getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        }
        catch(IOException e){
            Log.i("HTTPCACHE", "HTTP response cache installation failed:" + e);
        }
    }


    // method to flush cache contents to the filesystem
    public void flushCache() {
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }
}

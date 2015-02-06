package edu.berkeley.eecs.e_mission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.webkit.WebView;

/** Eula displays on every new installation or update. */

public class Eula {

	private Activity mActivity;
	
	public Eula(Activity context) {
        mActivity = context;
    }
	
	/**
	 * Get information collected from Android Manifest.xml 
	 */
	
	public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
             info = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }
	
    public void show() {
    	PackageInfo versionInfo = getPackageInfo();
    	final String key = "eula_" + versionInfo.versionCode;
    	// get prefs
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
    	boolean hasBeenShown = prefs.getBoolean(key, false);
    	// only show if hasnt been shown before
        if (hasBeenShown == false){
	        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
	        builder.setTitle("Consent to Participate in Research");
	        builder.setCancelable(false);
	        WebView webView = new WebView(mActivity);
	        webView.loadUrl(ConnectionSettings.getConnectURL(mActivity) + "/consent");
	        builder.setView(webView);
        
	        // set each button to be a listener and proceed forward if they press agree or quit if not.
	        builder.setPositiveButton("I AGREE", new Dialog.OnClickListener() {
        	 
	            @Override
	            public void onClick(DialogInterface dialogInterface, int i) {
	                //mark as shown.
	                SharedPreferences.Editor editor = prefs.edit();
	                editor.putBoolean(key, true);
	                editor.commit();
	                dialogInterface.dismiss();
	            }
	        });
	        builder.setNegativeButton("I DISAGREE", new Dialog.OnClickListener() {
	        	 
	            @Override
	            public void onClick(DialogInterface dialogInterface, int i) {
	                mActivity.finish();
	            }
	        });
	        builder.create().show();
         } 
    }
}

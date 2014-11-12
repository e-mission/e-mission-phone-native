package edu.berkeley.eecs.e_mission;

import android.content.Context;

/*
 * Single class that returns all the connection level settings that need to be customized
 * when 
 */

public class ConnectionSettings {
	public static String getConnectURL(Context ctxt) {
		return ctxt.getString(R.string.connect_url);
	}
	
	public static String getGoogleWebAppClientID(Context ctxt) {
		return ctxt.getString(R.string.google_webapp_client_id);
	}
	
	public static String getMovesClientID(Context ctxt) {
		return ctxt.getString(R.string.moves_client_id);
	}
}

package edu.berkeley.eecs.e_mission.auth;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import edu.berkeley.eecs.e_mission.R;

public class WebLoginActivity extends Activity {
	private WebView wv;
	
	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.activity_web_login);
		wv = (WebView)findViewById(R.id.webloginview);
		String uriToDisplay = getIntent().getExtras().getString("url");
		String getParams = getIntent().getExtras().getString("params");
		wv.loadUrl(uriToDisplay + "?" + getParams);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebChromeClient(new WebChromeClient() {
			    // Show loading progress in activity's title bar
			@Override
			public void onProgressChanged(WebView view, int progress) {
			setProgress(progress * 100);
			}
		});
		wv.setWebViewClient(new WebViewClient() {
			// When start to load page, show url in activity's title bar
			@Override
			public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
				System.out.println("Starting to load "+url);
				setTitle(url);
				if (url.startsWith("http://localhost/")) {
					System.out.println("Found local redirect, parsing for code");
					try {
						List<NameValuePair> queryResult = URLEncodedUtils.parse(new URI(url), "UTF-8");
						for (int i = 0; i < queryResult.size(); i++) {
							if (queryResult.get(i).getName().equals("code")) {
								returnResult(queryResult.get(i).getValue());
							}
						}
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				System.out.println("Finished loading "+url);
				CookieSyncManager.getInstance().sync();
				// Get the cookie from cookie jar.
				String cookie = CookieManager.getInstance().getCookie(url);
				if (cookie == null) {
					return;
				}
				// Cookie is a string like NAME=VALUE [; NAME=VALUE]
				String[] pairs = cookie.split(";");
				for (int i = 0; i < pairs.length; ++i) {
					String[] parts = pairs[i].split("=", 2);
					// If token is found, return it to the calling activity.
					if (parts.length == 2 &&
							parts[0].equalsIgnoreCase("oauth_token")) {
						returnResult(parts[1]);
					}
				}
			}
		});
	}
	
	public void returnResult(String resultToken) {
		System.out.println("Returning result "+resultToken);
		Intent result = new Intent();
		result.putExtra("token", resultToken);
		setResult(RESULT_OK, result);
		finish();
	}
	
	 @Override
	 protected void onPause() {
	   super.onPause();
	   CookieSyncManager.getInstance().stopSync();
	 }

	 @Override
	 protected void onResume() {
	   super.onResume();
	   CookieSyncManager.getInstance().startSync();
	 }
}

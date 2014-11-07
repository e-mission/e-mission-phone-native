package edu.berkeley.eecs.e_mission;


import android.os.Bundle;
import android.webkit.WebView;

import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EulaFragment extends Fragment {
	
	private OnActionListener mCallback;
	private View ll;
	private Activity fa;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fa = super.getActivity();
		ll = inflater.inflate(R.layout.activity_eula, container, false);

        //Displays eula in web view
        WebView mWebView = (WebView) ll.findViewById(R.id.webview);
    	mWebView.loadUrl(ConnectionSettings.getConnectURL(fa) + "/consent");
        
        Button mAgree = (Button) ll.findViewById(R.id.accept_button);
		mAgree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				acceptEula();
			}
		});

		Button mDisagree = (Button) ll.findViewById(R.id.reject_button);
		mDisagree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				refuseEula();
			}
		});
		
		return ll;
	}
	
	// Container Activity must implement this interface
    public interface OnActionListener {
        public void onButtonClick(String buttonType);
    }
    
    /** Checks to see if associated Activity has implemented the callback interface */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActionListener");
        }
    }
	
	/**
	 * Accept EULA and proceed with main application.
	 */
	public void acceptEula() {
		mCallback.onButtonClick("acceptEula");
	}

	/**
	 * Refuse EULA.
	 */
	public void refuseEula() {
		mCallback.onButtonClick("declineEula");
	}
}

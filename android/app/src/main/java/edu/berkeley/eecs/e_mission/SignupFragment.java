package edu.berkeley.eecs.e_mission;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import edu.berkeley.eecs.e_mission.EulaFragment.OnActionListener;

public class SignupFragment extends Fragment {
	
	private LinearLayout ll;
	
	private OnActionListener mCallback;
	
    
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
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ll = (LinearLayout) inflater.inflate(R.layout.activity_signup, container, false);
		
		Button mSignup = (Button) ll.findViewById(R.id.SignUpWithGoogle);
		mSignup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				signUp();
			}
		});
		
		return ll;
	}
	
	/** Called when the user clicks the "Sign Up with Google" button */
	public void signUp() {
	    mCallback.onButtonClick("signUp");
	}
}

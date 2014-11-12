package edu.berkeley.eecs.e_mission;

import edu.berkeley.eecs.e_mission.EulaFragment.OnActionListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class LinkToMovesFragment extends Fragment {
	
	private Button linkToMoves, installMoves;
	
	Intent intent;
	
	private OnActionListener mCallback;
	private View ll;
	private Activity fa;
	private boolean viewCreated;
	
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
		fa = super.getActivity();
		ll = inflater.inflate(R.layout.activity_install_moves, container, false);
		
		linkToMoves = (Button) ll.findViewById(R.id.link);
		linkToMoves.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				linkToMoves();
			}
		});
        installMoves = (Button) ll.findViewById(R.id.install);
        installMoves.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				installMoves();
			}
		});
        
        setButtons();
        setViewCreated();
		
		return ll;
	}
	
	private void setViewCreated() {
		viewCreated = true;
		
	}
	
	public void onPause() {
		super.onPause();
		if (viewCreated) {
			setButtons();
		}
	}
	
	public void onResume() {
		super.onResume();
		if (viewCreated) {
			setButtons();
		}
	}

	/** Called when the user clicks the "Sign Up with Google" button */
	public void linkToMoves() {
	    mCallback.onButtonClick("linkToMoves");
	}
	
	/** Called when the user clicks the "Sign Up with Google" button */
	public void installMoves() {
	    mCallback.onButtonClick("installMoves");
	}
	
	
	private void setButtons() {
		/**
		 * If the packages for moves app are found on the phone, we do not need to install moves again so the install moves button 
		 * is disabled and a link to moves is enabled. otherwise it is the other way around.
		 */
		boolean movesInstalled = isPackageInstalled("com.protogeo.moves", fa);
        if (movesInstalled) {
        	linkToMoves.setEnabled(true);
        	installMoves.setEnabled(false);
        	installMoves.setBackgroundColor(Color.parseColor("#808080"));
        	installMoves.setTextColor(Color.parseColor("#bdbdbd"));
        	linkToMoves.setBackgroundColor(Color.parseColor("#00D55A"));
        	linkToMoves.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
        	installMoves.setEnabled(true);
        	linkToMoves.setEnabled(false);
        	linkToMoves.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        	linkToMoves.setBackgroundColor(Color.parseColor("#808080"));
        	linkToMoves.setTextColor(Color.parseColor("#bdbdbd"));
        	installMoves.setBackgroundColor(Color.parseColor("#00D55A"));
        	installMoves.setTextColor(Color.parseColor("#FFFFFF"));
        }
	}
	
/*	public void onResume() {
		if (viewCreated) {
			setButtons();
		}
	}*/
	
	/** Called when checking if Moves package is installed. */
	private boolean isPackageInstalled(String packagename, Context context) {
	    PackageManager pm = context.getPackageManager();
	    try {
	        pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
	        return true;
	    } catch (NameNotFoundException e) {
	        return false;
	    }
	}
}

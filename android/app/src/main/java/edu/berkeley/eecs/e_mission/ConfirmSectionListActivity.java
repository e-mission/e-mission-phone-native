package edu.berkeley.eecs.e_mission;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import android.view.View;
import edu.berkeley.eecs.e_mission.auth.UserProfile;
//import android.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog; 
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class ConfirmSectionListActivity extends Activity {
	private ListView lv;
	private boolean hasShownResults = false;

	public static boolean[] arrBoolean = null;
	CustomAdapter adapter;
	CheckBox checkbox;
	Button confirmAll;
	List<UnclassifiedSection> ucs;

	private String TAG = "CSLA";
	// private ArrayAdapter<UnclassifiedSection> ucsArray;
	private ModeClassificationHelper dbHelper;
	private ClientStatsHelper statsHelper;
	private boolean onlyUnsure = false;
	
    // BEGIN: variables to set up the automatic syncing
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "edu.berkeley.eecs.e_mission.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "eecs.berkeley.edu";
    // The account name
    public static final String ACCOUNT = "dummy_account";
    private Account mAccount;
    
    private static final long SYNC_INTERVAL = 2 * 60 * 60; // Changed to 10 secs to debug syncing issues on some android versions
    // END: variables to set up the automatic syncing

	protected void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.activity_confirm_section_list_1);
		lv = (ListView) findViewById(R.id.listView);
		confirmAll = (Button) findViewById(R.id.confirmAll);

		Intent intent = getIntent();

		onlyUnsure = intent.getBooleanExtra("onlyUnsure", onlyUnsure);
		Log.d(TAG, "onlyUnsure = " + onlyUnsure);
		
		// TODO: Determine whether this is the right place to create this.  This
		// will work for now because we launch the activity on reboot, but we need
		// to figure out our UI story and see if this will always be true. If not,
		// we need to move it (and the alarm setup) to some other location.
		mAccount = GetOrCreateSyncAccount(this); 
		System.out.println("mAccount = "+mAccount);
	    // Get the content resolver for your app
	    // Turn on automatic syncing for the default account and authority
		ContentResolver.setIsSyncable(mAccount, AUTHORITY, 1);
	    ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
	    ContentResolver.addPeriodicSync(mAccount, AUTHORITY, new Bundle(), SYNC_INTERVAL);
	    
	    // Let us also do a force sync to ensure that the service is created. This is needed on Android 4.1.2
		// Doing this all the time seems to lead to a vicious cycle in which it is invoked every time that
	    // the activity is launched
	    /*
	    Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(mAccount, AUTHORITY, b);
		*/
	    
		// Tell the list view which view to display when the list is empty
		// lv.setEmptyView(findViewById(R.id.empty));
		dbHelper = new ModeClassificationHelper(this);
		statsHelper = new ClientStatsHelper(this);
		
		hasShownResults = false;
		/*
		 * Context context = getApplicationContext(); final View view =
		 * findViewById(R.id.listView); //
		 * view.setBackgroundColor(Color.rgb(238,232,170));
		 * view.setOnTouchListener(new OnSwipeTouchListener(context) { public
		 * void onSwipeTop() {
		 * 
		 * }
		 * 
		 * public void onSwipeRight() { toast("right"); }
		 * 
		 * public void onSwipeLeft() { toast("left"); }
		 * 
		 * public void onSwipeBottom() {
		 * 
		 * }
		 * 
		 * public boolean onTouch(View v, MotionEvent event) { return
		 * gestureDetector.onTouchEvent(event); } });
		 */
	}
	
    public static Account GetOrCreateSyncAccount(Context context) {
    	// Get an instance of the Android account manager
    	AccountManager accountManager =
    			(AccountManager) context.getSystemService(
    					ACCOUNT_SERVICE);
    	Account[] existingAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
    	assert(existingAccounts.length <= 1);
    	if (existingAccounts.length == 1) {
    		return existingAccounts[0];
    	}

    	// Create the account type and default account
    	Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);	  
    	/*
    	 * Add the account and account type, no password or user data
    	 * If successful, return the Account object, otherwise report an error.
    	 */
    	if (accountManager.addAccountExplicitly(newAccount, null, null)) {
    		return newAccount;
    	} else {
    		System.err.println("Unable to create a dummy account to sync with!");
    		return null;
    	}
    }


	// Re-read the list every time we are re-launched
	protected void onResume() {
		super.onResume();
		
		String resumeTs = String.valueOf(System.currentTimeMillis());
		
		statsHelper.storeMeasurement(getString(R.string.battery_level),
				String.valueOf(BatteryUtils.getBatteryLevel(this)), resumeTs);
		statsHelper.storeMeasurement(getString(R.string.confirmlist_resume), null, resumeTs);
		
		// Check to see if we are authenticated
		// If not, we need to pop-up the MainActivity to allow the user to
		// re-authenticate
		if (!UserProfile.getInstance(this).isGoogleAuthDone()) {
			statsHelper.storeMeasurement(getString(R.string.confirmlist_auth_not_done), null, resumeTs);
			launchSettingsActivity();
			return;
		}

		ucs = dbHelper.getUnclassifiedSections();
		if (onlyUnsure == true) {
			showLowConfidence(ucs);
		}
		if (ucs.size() > 0) {
			confirmAll.setEnabled(true);
		} else {
			confirmAll.setEnabled(false);
		}
		Log.d(TAG, "created ucs");
		Log.d(TAG, "ucs.size(): " + ucs.size());
		statsHelper.storeMeasurement(getString(R.string.confirmlist_ucs_size), String.valueOf(ucs.size()), resumeTs);
		System.out.println("ucs = " + ucs);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		Log.d(TAG, "made listView multiple");
		lv.setItemsCanFocus(false);
		adapter = new CustomAdapter(this, ucs);
		lv.setAdapter(adapter);
		Log.d(TAG, "set adapter");
		// lv.setOnItemClickListener(this);
		arrBoolean = new boolean[ucs.size()];
		Log.d(TAG, "arrboolean.length: " + arrBoolean.length);

		System.out.println("ucs = " + ucs);
		// ucsArray = new ArrayAdapter<UnclassifiedSection>(this,
		// android.R.layout.simple_list_item_1, ucs);
		// System.out.println("ucsArray = "+ucsArray);
		// lv.setAdapter(ucsArray);
		// lv.setAdapter(new CustomAdapter(this, ucs));
		// lv.setOnItemClickListener(this);
		Log.d(TAG, "Size = " + ucs.size());
		if (ucs.size() == 0 && !hasShownResults) {
			hasShownResults = true;
			launchResultSummary();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	public void showLowConfidence(List<UnclassifiedSection> ucs) {
		int counter = 0;
		for (int i = 0; i < ucs.size(); i++) {
			double prob = 0;
			try {
				prob = ucs.get(i).getCertainty();
				Log.d(TAG, "in try");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG, "certainty: " + prob);
			if (prob >= 0.95) {
				counter++;
				Log.d(TAG, "removed");
				ucs.remove(i);
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ConfirmSectionListActivity.this);
		builder.setMessage(
				"When trips have over a 95% chance of being correct, we assume that our predictions are correct and do not show them here. "
						+ "Select 'All Trips' in Settings if you would like to see all your trips.")
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});

		builder.create().show();
		/*
		 * toast(
		 * "When trips have over a 95% chance of being correct, we assume that our predictions are correct."
		 * ); if (counter > 0) { toast("We have confirmed " + counter +
		 * " trips!");
		 * toast("Select 'All Trips' in Settings if you would like to see the "
		 * +counter+ " autoconfirmed trips as well."); } else {
		 * toast("We have not confirmed any trips"); }
		 */
		counter = 0;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			launchSettingsActivity();
			return true;
		case R.id.action_result_summary:
			launchResultSummary();
			return true;
		}
		return false;
	}

	public void launchSettingsActivity() {
		Intent settingsIntent = new Intent(this,
				edu.berkeley.eecs.e_mission.SettingsActivity.class);
		settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(settingsIntent);
	}

	public void launchResultSummary() {
		Intent resultIntent = new Intent(this,
				edu.berkeley.eecs.e_mission.DisplayResultSummaryActivity.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(resultIntent);
	}

	public void toast(CharSequence prompt) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, prompt, duration);
		toast.show();
	}

	public void confirmAll(View view) {
		/*
		 * Log.d(TAG, "confirm all clicked"); SparseBooleanArray checked =
		 * lv.getCheckedItemPositions();
		 * Log.d(TAG,"checked size = "+checked.size());
		 */
		String confirmTs = String.valueOf(System.currentTimeMillis());
		int size = ucs.size();
		statsHelper.storeMeasurement(getString(R.string.button_confirm_all), String.valueOf(size), confirmTs);
		Log.d(TAG, "in confirmAll: ucs.size() = " + size);
		int skippedCount = 0;
		for (int i = 0; i < size; i++) {
			UnclassifiedSection item = (UnclassifiedSection) ucs.get(i);
			boolean status = item.getConfirmStatus();
			Log.d(TAG, "position: " + i);
			Log.d(TAG, "status: " + status);
			if (status == true) {
				Log.d(TAG, "status is TRUE!, will confirm");
				Log.d(TAG, "Got click at position " + i);
				// get unclassified trip
				Log.d(TAG, "ucsection " + item);
				String tripId = item.getTripId();
				Log.d(TAG, "tripID: " + tripId);
				String sectionId = item.getSectionId();
				Log.d(TAG, "sectionID: " + sectionId);
				String autoModeClassification = item.getMode();
				Log.d(TAG, "autoModeC: " + autoModeClassification);

				Log.d(TAG, "mode was correct!");
				// create the dbHelper to confirm the trip
				/*
				dbHelper = new ModeClassificationHelper(this);
				if (dbHelper == null) {
					Log.d(TAG, "helper null");
				} else {
				*/
					// give the dbHelper the tripID , section ID and
					// mode classification.
					dbHelper.storeUserClassification(tripId, sectionId,
							autoModeClassification);
				// }
			} else {
				Log.d(TAG, "status is FALSE!, will not confirm");
				skippedCount++;

			}

			statsHelper.storeMeasurement(getString(R.string.button_confirm_all_skipped), String.valueOf(skippedCount), confirmTs);

        }

        Intent intent = new Intent(ConfirmSectionListActivity.this, ConfirmSectionListActivity.class);
        startActivity(intent);
	}




	public class CustomAdapter extends BaseAdapter{
		protected static final String TAG = "ADAPTER";
		List<UnclassifiedSection> USC;
		Context context;
		ModeClassificationHelper dbHelper;

		// private static LayoutInflater inflater=null;
		public CustomAdapter(ConfirmSectionListActivity mainActivity,
				List<UnclassifiedSection> ucs) {
			// TODO Auto-generated constructor stub
			USC = ucs;
			context = mainActivity;
			// inflater = ( LayoutInflater )context.
			// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return USC.size();
		}
		
	    public void updateResults(List<UnclassifiedSection> results) {
	        USC = results;
	        //Triggers the list update
	        notifyDataSetChanged();
	    }

		@Override
		public UnclassifiedSection getItem(int position) {
			// TODO Auto-generated method stub
			return USC.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}


		public class Holder {
			TextView textmode;
			TextView confidence;
			TextView textduration;
			TextView textstart;
			TextView textday;
			ImageView img;
			CheckBox confirm;
			Spinner spinner;
			// boolean commit;
		}
		


		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			Holder holder = null;
			View rowView = convertView;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater
						.inflate(R.layout.program_list, parent, false);
				holder = new Holder();
				holder.textmode = (TextView) rowView.findViewById(R.id.mode);
				holder.textduration = (TextView) rowView
						.findViewById(R.id.duration);
				holder.textstart = (TextView) rowView.findViewById(R.id.start);
				holder.textday = (TextView) rowView.findViewById(R.id.day);
				holder.confidence = (TextView) rowView
						.findViewById(R.id.confidence);
				holder.img = (ImageView) rowView.findViewById(R.id.imageView1);
				holder.confirm = (CheckBox) rowView
						.findViewById(R.id.checkBox1);
				holder.spinner = (Spinner) rowView.findViewById(R.id.spinner1);
				List<String> list = new ArrayList<String>();
				list.add("");
				list.add("walking");
				list.add("cycling");
				list.add("bus");
				list.add("train");
				list.add("car");
				list.add("air");
				list.add("mixed");
				list.add("not a trip");
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list);
				dataAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				holder.spinner.setAdapter(dataAdapter);
				holder.spinner.setSelection(0, false);

				rowView.setTag(holder);
			} else {
				holder = (Holder) rowView.getTag();
			}

			UnclassifiedSection section = USC.get(position);
			String selMode = null;
			Log.d(TAG, "Position: " + position);
			String mode = section.getMode();
			Log.d(TAG, "selMode: " + section.getSelMode());
			Log.d(TAG, "mode: " + mode);
			System.out.println("modeinadapter " + mode);
			String starttime = section.toString().substring(0,
					section.toString().lastIndexOf("*"));
			System.out.println("starttimeinadapter " + starttime);
			String endtime = section.toString().substring(
					section.toString().lastIndexOf("*") + 1,
					section.toString().lastIndexOf("+"));
			System.out.println("endtimeinadapter " + endtime);
			String date = "";
			double num = 0;
			try {
				num = section.getCertainty() * 100;
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String percent = num + " % ";
			holder.confidence.setText(percent);

			try {
				Date startd = UnclassifiedSection.parseDateString(starttime);
				Date endd = UnclassifiedSection.parseDateString(endtime);
				String tripduration = String
						.valueOf(Math.round((endd.getTime() - startd.getTime()) / 1000 / 60));
				Calendar rightnow = Calendar.getInstance();
				Calendar yesterday = Calendar.getInstance();
				yesterday.add(Calendar.DAY_OF_YEAR, -1);
				Calendar startCal = Calendar.getInstance();
				startCal.setTime(startd);
				if (startCal.get(Calendar.YEAR) == rightnow.get(Calendar.YEAR)
						&& startCal.get(Calendar.DAY_OF_YEAR) == rightnow
								.get(Calendar.DAY_OF_YEAR)) {
					date = "today";
				} else if (startCal.get(Calendar.YEAR) == yesterday
						.get(Calendar.YEAR)
						&& startCal.get(Calendar.DAY_OF_YEAR) == yesterday
								.get(Calendar.DAY_OF_YEAR)) {
					date = "yesterday";
				} else {
					date = starttime.substring(4, 6) + "/"
							+ starttime.substring(6, 8);
				}
				
				holder.textmode.setText(mode);
				holder.textduration.setText(tripduration + " min");
				holder.textstart.setText(starttime.substring(9, 11) + ":"
						+ starttime.substring(11, 13));
				holder.textday.setText(date);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			int value = (int) num;
			Log.d(TAG, "value: " + value);
			if (mode.equals("bus")) {
				holder.img.setImageResource(R.drawable.bus);
				// holder.img.setColorFilter(Color.rgb(0,128 - value,0),
				// PorterDuff.Mode.SCREEN);
				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}
			} else if (mode.equals("train")) {
				holder.img.setImageResource(R.drawable.train);
				// holder.img.setColorFilter(Color.rgb(0,128 - value,0),
				// PorterDuff.Mode.SCREEN);
				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}
			} else if (mode.equals("walking")) {
				holder.img.setImageResource(R.drawable.walking);
				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}
			} else if (mode.equals("cycling")) {
				holder.img.setImageResource(R.drawable.cycling);
				// holder.img.setColorFilter(Color.rgb(0,128 - value,0),
				// PorterDuff.Mode.SCREEN);
				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}
			} else if (mode.equals("car")) {
				holder.img.setImageResource(R.drawable.car);
				// holder.img.setColorFilter(Color.rgb(0,128 - value,0),
				// PorterDuff.Mode.SCREEN);

				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}

			} else if (mode.equals("running")) {
				holder.img.setImageResource(R.drawable.running);
				// holder.img.setColorFilter(Color.rgb(0,128 - value,0),
				// PorterDuff.Mode.SCREEN);
				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}
			} else if (mode.equals("air")) {
				holder.img.setImageResource(R.drawable.air);
				// holder.img.setColorFilter(Color.rgb(0,128 - value,0),
				// PorterDuff.Mode.SCREEN);
				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}
			} else if (mode.equals("transport")) {
				holder.img.setImageResource(R.drawable.train);
				// holder.img.setColorFilter(Color.rgb(0,128 - value,0),
				// PorterDuff.Mode.SCREEN);
				if (num >= 80.0) {
					holder.img.setColorFilter(Color.rgb(0, 128, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "greater than 80");
				}
				if (num >= 70.0 && num < 80.0) {
					holder.img.setColorFilter(Color.rgb(255, 215, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "between 70 and 80");
				}
				if (num < 70) {
					holder.img.setColorFilter(Color.rgb(255, 0, 0),
							PorterDuff.Mode.SCREEN);
					Log.d(TAG, "else");
				}
			}
			// made the checkbox a listener, it reacts whenever it is clicked
		
			
			holder.confirm
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub

							UnclassifiedSection item = (UnclassifiedSection) USC
									.get(position);
							String tripId = item.getTripId();
							Log.d(TAG, "tripID: " + tripId);
							String sectionId = item.getSectionId();
							Log.d(TAG, "sectionID: " + sectionId);
							String autoModeClassification = item.getMode();
							Log.d(TAG, "autoModeC: " + autoModeClassification);

							Log.d(TAG, "mode was correct!");
							// create the dbHelper to confirm the trip
							dbHelper = new ModeClassificationHelper(context);
							if (dbHelper == null) {
								Log.d(TAG, "helper null");
							} else {
								// give the dbHelper the tripID , section ID and
								// mode classification.
								dbHelper.storeUserClassification(tripId,
										sectionId, autoModeClassification);
								// Now that the user has confirmed this, and it is saved to the
								// database, we don't want them to be able to deselect things!
								buttonView.setEnabled(false);
							}

						}

					});

			// made the mode image a listener, when tapped the detail view is
			// shown.
			holder.img.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.d(TAG, "imageclicked");

					Log.d(TAG, "Got click at position " + position);
					final UnclassifiedSection item = (UnclassifiedSection) USC
							.get(position);
					Log.d(TAG, "ucsection " + item);
					Intent activityIntent = new Intent(
							context,
							edu.berkeley.eecs.e_mission.ConfirmSectionActivity.class);
					Log.d(TAG, "done with activityIntent");
					activityIntent.putExtra("sectionJSON", item
							.getSectionBlob().toString());
					activityIntent.putExtra("position", position);
					Log.d(TAG, "done with putExtra");
					activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Log.d(TAG, "done with setFlags");
					context.startActivity(activityIntent);
					Log.d(TAG, "done with startActivity");
				}

			});
			
			holder.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, 
			            int pos, long id) {
					if (pos == 0) {
						// The first element is blank, and the corresponding selected element is "".
						// we don't ever want users to be able to return an null value, so let's bail
						// out if that is the case
						return;
					}
					Log.d(TAG,"spinner item selected!");
					final UnclassifiedSection item = (UnclassifiedSection) USC.get(position);
					Log.d(TAG,"created item");
					String selMode = String.valueOf(parent.getItemAtPosition(pos).toString());
					item.setSelMode(selMode);
					/* 
					 * It is not sufficient to set the selMode in memory because when the process is
					 * suspended and resumed (for example, when a detail view is launched, or the
					 * screen locks) then the in-memory datastructures are all cleared out and recreated.
					 * We really need to store this in the database too.
					 * On the other hand, I don't want to add DB code UnclassifiedSection, which is
					 * intended as a lightweight wrapper class. So for now, we make two calls to update
					 * in memory and in the DB.
					 */
					dbHelper = new ModeClassificationHelper(context);
					dbHelper.storeUserSelection(item.getTripId(), item.getSectionId(), selMode);
					Log.d(TAG,"set selMode to: "+selMode);
					// After this is done, reset the selection to blank, because otherwise it just 
					// looks ugly 
					parent.setSelection(0);
					updateResults(USC);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			return rowView;
		}

	}
}

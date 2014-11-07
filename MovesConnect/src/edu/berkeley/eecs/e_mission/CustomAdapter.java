package edu.berkeley.eecs.e_mission;

// tester code
/*
//import edu.berkeley.eecs.e_mission.R;
public class CustomAdapter extends BaseAdapter {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder = null;
		View rowView = convertView;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.program_list, parent, false);
			holder = new Holder();
			holder.textmode = (TextView) rowView.findViewById(R.id.mode);
			holder.textduration = (TextView) rowView
					.findViewById(R.id.duration);
			holder.textstart = (TextView) rowView.findViewById(R.id.start);
			holder.textday = (TextView) rowView.findViewById(R.id.day);
			holder.confidence = (TextView) rowView
					.findViewById(R.id.confidence);
			holder.img = (ImageView) rowView.findViewById(R.id.imageView1);
			registerForContextMenu(holder.img); 
			holder.confirm = (CheckBox) rowView.findViewById(R.id.checkBox1);
			holder.spinner = (Spinner) rowView.findViewById(R.id.spinner1);

			rowView.setTag(holder);
		} else {
			holder = (Holder) rowView.getTag();
		}

		UnclassifiedSection section = USC.get(position);
		// String mode =
		// section.toString().substring(section.toString().lastIndexOf("+") +
		// 1);
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
			holder.textduration.setText(tripduration + " minutes");
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
							dbHelper.storeUserClassification(tripId, sectionId,
									autoModeClassification);
						}

					}

				});



		// made the mode image a listener, when tapped the detail view is shown.
		holder.img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "image clicked");

				Log.d(TAG, "Got click at position " + position);
				final UnclassifiedSection item = (UnclassifiedSection) USC
						.get(position);
				Log.d(TAG, "ucsection " + item);
				Intent activityIntent = new Intent(
						context,
						edu.berkeley.eecs.e_mission.ConfirmSectionActivity.class);
				Log.d(TAG, "done with activityIntent");
				activityIntent.putExtra("sectionJSON", item.getSectionBlob()
						.toString());
				activityIntent.putExtra("position", position);
				Log.d(TAG, "done with putExtra");
				activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Log.d(TAG, "done with setFlags");
				context.startActivity(activityIntent);
				Log.d(TAG, "done with startActivity");
			}

		});
		
		
		/*
		holder.img.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				Log.d(TAG,"longClicked!");
				

				return false;
			}
		
		});


		return rowView;
	}

}
*/


package edu.berkeley.eecs.e_mission.test;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.eecs.e_mission.ConfirmSectionListActivity;
import edu.berkeley.eecs.e_mission.ModeClassificationHelper;
import edu.berkeley.eecs.e_mission.UnclassifiedSection;
import android.test.ActivityInstrumentationTestCase2;
import android.test.RenamingDelegatingContext;

public class ConfirmSectionListActivityTest extends
		ActivityInstrumentationTestCase2<ConfirmSectionListActivity> {
	ModeClassificationHelper dbHelper;
	private ConfirmSectionListActivity mActivity;
	
	public ConfirmSectionListActivityTest() {
		super(ConfirmSectionListActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();	
		RenamingDelegatingContext context = 
				new RenamingDelegatingContext(mActivity, "test_");
		dbHelper = new ModeClassificationHelper(context);
	}
	
	public void testLaunchActivity() {
		mActivity.finish();
		mActivity = (ConfirmSectionListActivity) this.getActivity();
		assert(mActivity.findViewById(edu.berkeley.eecs.e_mission.R.id.listView) != null);
	}
	
	public void testZTimeParse() {
		String testSectionBlob = "{'group': 4, 'section_end_time': '20140402T152854Z'," +
				"'source': 'Shankari', 'type': 'move', 'manual': false, 'section_id': 0, "+
				"'track_points': [{'track_location': {'type': 'Point', 'coordinates': [37.391, -122.08632]}, 'time': '20140402T152001Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.39115, -122.08649]}, 'time': '20140402T152102Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.39234, -122.08686]}, 'time': '20140402T152123Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.39309, -122.08639]}, 'time': '20140402T152146Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.39644, -122.0825]}, 'time': '20140402T152452Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.39833, -122.08141]}, 'time': '20140402T152511Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.40096, -122.07981]}, 'time': '20140402T152601Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.40283, -122.08278]}, 'time': '20140402T152734Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.40299, -122.08316]}, 'time': '20140402T152754Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.40299, -122.08316]}, 'time': '20140402T152812Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.40299, -122.08316]}, 'time': '20140402T152832Z'},"+
				                 "{'track_location': {'type': 'Point', 'coordinates': [37.40299, -122.08316]}, 'time': '20140402T152854Z'}],"+
				                 "'mode': 'transport', 'user_id': 'tamtom2000@gmail.com', 'section_start_time': '20140402T152001Z',"+
				                 "'_id': 'tamtom2000@gmail.com_20140402T152001Z', 'trip_id': '20140402T152001Z', 'confirmed_mode': ''}";
		UnclassifiedSection testSec = new UnclassifiedSection("20140402T152001Z", "0", null, testSectionBlob);
		
		List<UnclassifiedSection> secList = new ArrayList<UnclassifiedSection>();
		secList.add(testSec);
		dbHelper.storeNewUnclassifiedTrips(secList);
		mActivity.finish();
		mActivity = (ConfirmSectionListActivity) this.getActivity();
	}

	/*
	public void testOnListItemClicked() {
		return;
	}
	*/
}

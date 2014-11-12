package edu.berkeley.eecs.e_mission.test;

import java.util.Date;

import android.test.AndroidTestCase;
import edu.berkeley.eecs.e_mission.UnclassifiedSection;

public class UnclassifiedSectionTest extends AndroidTestCase {

	public UnclassifiedSectionTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Ensure that dates in both formats returned to us by moves are handled correctly.
	public void testDateEndingInZ() throws Exception {
		/*
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
		UnclassifiedSection testSec = new UnclassifiedSection("20140402T152001Z", "0", testSectionBlob);
		*/
		Date parsedDate = UnclassifiedSection.parseDateString("20140403T002345Z");
		assertEquals(parsedDate, new Date(1396484625000L));
	}
	
	public void testDateEndingInTZ() throws Exception {
		Date parsedDate = UnclassifiedSection.parseDateString("20140403T095842-0700");
		assertEquals(parsedDate, new Date(1396544322000L));
	}
}

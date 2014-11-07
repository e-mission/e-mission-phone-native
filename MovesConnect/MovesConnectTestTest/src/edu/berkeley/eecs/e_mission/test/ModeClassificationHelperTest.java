package edu.berkeley.eecs.e_mission.test;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import edu.berkeley.eecs.e_mission.ModeClassificationHelper;
import edu.berkeley.eecs.e_mission.UnclassifiedSection;
import edu.berkeley.eecs.e_mission.UserClassification;

public class ModeClassificationHelperTest extends AndroidTestCase {
	ModeClassificationHelper dbHelper;
	List<UnclassifiedSection> testSections = new ArrayList<UnclassifiedSection>();
	
	public ModeClassificationHelperTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context = 
				new RenamingDelegatingContext(getContext(), "test_");
		dbHelper = new ModeClassificationHelper(context);
		testSections.add(new UnclassifiedSection("test_trip_1", "test_section_1", null, "test_json_1"));
		testSections.add(new UnclassifiedSection("test_trip_2", "test_section_1", null, "test_json_2"));
		testSections.add(new UnclassifiedSection("test_trip_2", "test_section_2", null, "test_json_3"));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testUnclassified() throws Exception {
		dbHelper.storeNewUnclassifiedTrips(testSections);
		List<UnclassifiedSection> retSections = dbHelper.getUnclassifiedSections();
		assertEquals(retSections.size(), 3);
		assertEquals(retSections.get(0).getTripId(), "test_trip_1");
		assertEquals(retSections.get(0).getSectionId(), "test_section_1");
		assertEquals(retSections.get(0).getSectionBlob(), "test_json_1");
	}
	
	public void testClassified() throws Exception {
		dbHelper.storeNewUnclassifiedTrips(testSections);
		List<UnclassifiedSection> unclassifiedStep1 = dbHelper.getUnclassifiedSections();
		assertEquals(unclassifiedStep1.size(), 3);
		
		dbHelper.storeUserClassification("test_trip_1", "test_section_1", "walking");
		dbHelper.storeUserClassification("test_trip_2", "test_section_1", "cycling");
		
		List<UnclassifiedSection> unclassifiedStep2 = dbHelper.getUnclassifiedSections();
		assertEquals(unclassifiedStep2.size(), 1);
		
		List<UserClassification> classifiedStep1 = dbHelper.getAndDeleteClassifiedSections();
		assertEquals(classifiedStep1.size(), 2);

		// We just deleted the two classified trips, so we don't expect to see them again
		List<UserClassification> classifiedStep2 = dbHelper.getAndDeleteClassifiedSections();
		assertEquals(classifiedStep2.size(), 0);
		
		// Now, we classify the last one
		dbHelper.storeUserClassification("test_trip_2", "test_section_2", "bus");
		
		// So we see it in the list
		List<UserClassification> classifiedStep3 = dbHelper.getAndDeleteClassifiedSections();
		assertEquals(classifiedStep3.size(), 1);
	}
	
	public void testClear() throws Exception {
		dbHelper.storeNewUnclassifiedTrips(testSections);
		dbHelper.clear();
		List<UnclassifiedSection> unclassifiedStep1 = dbHelper.getUnclassifiedSections();
		assertEquals(unclassifiedStep1.size(), 0);
	}
}

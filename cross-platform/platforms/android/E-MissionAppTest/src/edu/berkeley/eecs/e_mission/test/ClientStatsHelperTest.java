package edu.berkeley.eecs.e_mission.test;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import edu.berkeley.eecs.e_mission.ClientStatsHelper;

public class ClientStatsHelperTest extends AndroidTestCase {
	ClientStatsHelper dbHelper;
	
	public ClientStatsHelperTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context = 
				new RenamingDelegatingContext(getContext(), "test_");
		dbHelper = new ClientStatsHelper(context);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testOneValue() throws Exception {
		dbHelper.storeMeasurement("testlabel1", "10000", "11111");
		dbHelper.storeMeasurement("testlabel2", "20000", "22222");
		dbHelper.storeMeasurement("testlabel3", "30000", "33333");
		JSONObject retVal = dbHelper.getMeasurements();
		assertEquals(retVal.length(), 2);
		JSONObject statsVal = retVal.getJSONObject("Readings");
		JSONObject metadataVal = retVal.getJSONObject("Metadata");
		
		assertEquals(metadataVal.length(), 2);
		assertEquals(statsVal.length(), 3);
		// statsVal.get returns a JSONArray
		assertEquals(statsVal.getJSONArray("testlabel1").length(), 1);
		assertEquals(statsVal.getJSONArray("testlabel2").length(), 1);
		assertEquals(statsVal.getJSONArray("testlabel3").length(), 1);
		// Now try for a label that doesn't exist
		try {
			statsVal.getJSONArray("unknownlabel");
			fail("didn't throw expected exception");
		} catch (JSONException e) { }
	}
	
	public void testMultipleValues() throws Exception {
		dbHelper.storeMeasurement("testlabel1", "10000", "11111");
		dbHelper.storeMeasurement("testlabel2", "20000", "22221");
		dbHelper.storeMeasurement("testlabel3", "30000", "33331");
		
		/* Now store some more stuff */
		dbHelper.storeMeasurement("testlabel1", "10001", "11112");
		dbHelper.storeMeasurement("testlabel2", "20001", "22222");
		dbHelper.storeMeasurement("testlabel3", "30001", "33332");
		
		dbHelper.storeMeasurement("testlabel1", "10002", "11113");
		dbHelper.storeMeasurement("testlabel2", "20002", "22223");
		dbHelper.storeMeasurement("testlabel3", "30002", "33333");		

		JSONObject retVal = dbHelper.getMeasurements();
		assertEquals(retVal.length(), 2);
		JSONObject statsVal = retVal.getJSONObject("Readings");
		JSONObject metadataVal = retVal.getJSONObject("Metadata");
		
		assertEquals(metadataVal.length(), 2);
		assertEquals(statsVal.length(), 3);		

		assertEquals(statsVal.getJSONArray("testlabel1").length(), 3);
		assertEquals(statsVal.getJSONArray("testlabel2").length(), 3);
		assertEquals(statsVal.getJSONArray("testlabel3").length(), 3);
		
		System.out.println(statsVal.getJSONArray("testlabel1"));
		assertEquals(statsVal.getJSONArray("testlabel1").getJSONArray(0).getString(0), "11111");
		assertEquals(statsVal.getJSONArray("testlabel1").getJSONArray(0).getString(1), "10000");
		assertEquals(statsVal.getJSONArray("testlabel1").getJSONArray(2).getString(0), "11113");
		assertEquals(statsVal.getJSONArray("testlabel1").getJSONArray(2).getString(1), "10002");		
	}
	
	public void testClear() throws Exception {
		dbHelper.storeMeasurement("testlabel1", "10000", "11111");
		dbHelper.storeMeasurement("testlabel2", "20000", "22222");
		dbHelper.storeMeasurement("testlabel3", "30000", "33333");
		
		JSONObject retVal = dbHelper.getMeasurements();
		assertEquals(retVal.length(), 2);	
		
		dbHelper.clear();
		retVal = dbHelper.getMeasurements();
		assertEquals(retVal.length(), 0);
	}
}

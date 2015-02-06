package edu.berkeley.eecs.e_mission.test;

import java.io.File;

import android.test.AndroidTestCase;
import edu.berkeley.eecs.e_mission.DataUtils;

public class UserNameTest extends AndroidTestCase {

	private DataUtils du;
	
	public UserNameTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		du = new DataUtils(getContext());
		// It looks like the app is just reinstalled on the development environment
		// and the files are not actually deleted.
		// So we will manually delete the file in the setup phase
		File[] initFile = getContext().getFilesDir().listFiles();
		for (int i = 0; i < initFile.length; i++) {
			initFile[i].delete();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSaveAndReadUserName() throws Exception {
		File[] beforeFiles = getContext().getFilesDir().listFiles();
		if (beforeFiles.length > 0) {
			System.out.println("beforeFiles[0] = "+beforeFiles[0].getName());
		}
		assertEquals(beforeFiles.length, 0);
		du.saveUserName("testuser");
		File[] afterFiles = getContext().getFilesDir().listFiles();
		assertEquals(afterFiles.length, 1);
		assertEquals(afterFiles[0].getName(), "userName");
		String realUser = du.getUserName();
		assertEquals(realUser, "testuser");
	}
	
}

var dbHelperTests = {
    doAllTests: function() {
        QUnit.test ("hello test", function(assert) {
            assert.ok(1 == "1", "Passed!");
        });

        // QUnit.asyncTest ("testPragmaExample", function(assert) {
        //     console.log(dbHelper);
        //     dbHelper.pragmaExample();
        //     assert.ok(1 == "1", "Passed!");
        // });

    	QUnit.asyncTest ("testTripSections", function(assert) {
            console.log("Testing console logs");
    	    console.log(tripSectionDbHelper);
    	    tripSectionDbHelper.getUncommitedSections();
    	    assert.ok(1 == "1", "Passed!");
    	});
    }
}

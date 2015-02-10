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
    	    var sections = tripSectionDbHelper.getUncommitedSections();
            // console.log(sections.length);
            // console.log(sections.length);
    	    assert.ok(1 == 1, "Passed!");
    	});
    }
}

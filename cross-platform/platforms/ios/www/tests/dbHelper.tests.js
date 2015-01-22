sampleJSON1 = ["{\"userSelection\":\"Gautham\",\"sectionJsonBlob\":\"{\\\"source\\\":\\\"Shankari\\\",\\\"section_id\\\":0,\\\"duration\\\":645,\\\"type\\\":\\\"move\\\",\\\"confirmed_mode\\\":\\\"\\\",\\\"predicted_mode\\\":{\\\"walking\\\":1},\\\"mode\\\":\\\"walking\\\",\\\"section_start_time\\\":\\\"20141121T110333-0800\\\",\\\"_id\\\":\\\"7327a0b3-4959-31f5-86cb-ea0c59fd0c83_20141121T110333-0800_0\\\",\\\"distance\\\":724,\\\"trip_id\\\":\\\"20141121T110333-0800\\\",\\\"section_end_time\\\":\\\"20141121T111418-0800\\\",\\\"track_points\\\":[{\\\"track_location\\\":{\\\"type\\\":\\\"Point\\\",\\\"coordinates\\\":[-122.2632823787,37.865492072]},\\\"time\\\":\\\"20141121T110333-0800\\\"}],\\\"manual\\\":false}\",\"tripId\":\"20141121T110333-0800\",\"userClassification\":null,\"sectionId\":\"0\"}"];
sampleJSON1[0] = JSON.parse(sampleJSON1[0]);

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

        QUnit.asyncTest ("testing database and wrappers", function(assert) {
            console.log("Opening database");
            // this doesn't cause a problem no matter what database name I use. Why is this?
            tripSectionDbHelper.getJSON({name: "TripSections.db"}, function(jsonTripList) {
                tripList = tripSectionDbHelper.getUncommitedSections(jsonTripList);
                assert.equal(tripList.length, 31);
            });

            console.log("Testing custom JSON");
            testSections = tripSectionDbHelper.getUncommitedSections(sampleJSON1);
            assert.equal(testSections[0].tripId, "20141121T110333-0800");
            assert.equal(testSections[0].sectionId, 0);
            assert.equal(testSections[0].trackPoints.length, 1);
            assert.equal(testSections[0].startTime.date.toString(), "Sun Dec 21 2014 11:03:33 GMT-0800 (PST)");
            assert.equal(testSections[0].endTime.date.toString(), "Sun Dec 21 2014 11:14:18 GMT-0800 (PST)");
            assert.equal(testSections[0].autoMode, "walking");
            assert.equal(testSections[0].confidence, 1);
            assert.equal(testSections[0].selMode, "");
            assert.equal(testSections[0].userMode, "");
        });
    }
}

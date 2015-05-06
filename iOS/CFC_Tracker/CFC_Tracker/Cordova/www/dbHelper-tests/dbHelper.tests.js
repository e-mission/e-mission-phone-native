var dbHelperTests = {
    doAllTests: function() {
        QUnit.test ("hello test", function(assert) {
            assert.ok(1 == "1", "Passed!");
        });

        QUnit.test ("hello test 2", function(assert) {
            assert.ok(1 == "1", "Passed!");
        });

        QUnit.test ("hello test 3", function(assert) {
            assert.ok(1 == "1", "Passed!");
        });

        // EXAMPLE TEST:
        // QUnit.asyncTest ("testPragmaExample", function(assert) {
        //     console.log(dbHelper);
        //     dbHelper.pragmaExample();
        //     assert.ok(1 == "1", "Passed!");
        // });
        QUnit.test("default sqlite plugin test", function(assert) {
            assert.expect(2);

            var insertIdDone = assert.async();
            var countDone = assert.async();

            console.log("Opening database");
            var db = window.sqlitePlugin.openDatabase("demodb", "1.0", "Demo", -1);

            db.transaction(function(tx) {
              tx.executeSql('DROP TABLE IF EXISTS test_table');
              tx.executeSql('CREATE TABLE IF NOT EXISTS test_table (id integer primary key, data text, data_num integer)');

              tx.executeSql("INSERT INTO test_table (data, data_num) VALUES (?,?)", ["test", 100], function(tx, res) {
              console.log("insertId: " + res.insertId + " -- probably 1"); // check #18/#38 is fixed
              assert.equal(res.insertId, 1);
              insertIdDone();

                db.transaction(function(tx) {
                  tx.executeSql("SELECT data_num from test_table;", [], function(tx, res) {
                    console.log("res.rows.length: " + res.rows.length + " -- should be 1");
                    assert.equal(res.rows.length, 1);
                    countDone();
                  });
                });

              }, function(e) {
                console.log("ERROR: " + e.message);
              });
            });
        });

        QUnit.test("open database twice", function(assert) {
            assert.expect(6);

            var insert100Done = assert.async();
            var insert200Done = assert.async();

            var countDone = assert.async();
            var count1Done = assert.async();

            var insertDb = window.sqlitePlugin.openDatabase("demodb", "1.0", "Demo", -1);
            insertDb.transaction(function(tx) {
              tx.executeSql('DROP TABLE IF EXISTS test_table');
              tx.executeSql('CREATE TABLE IF NOT EXISTS test_table (id integer primary key, data text, data_num integer)');

              tx.executeSql("INSERT INTO test_table (data, data_num) VALUES (?,?)", ["test", 100], function(tx, res) {
                  console.log("insertId: " + res.insertId + " -- probably 1"); // check #18/#38 is fixed
                  assert.equal(res.insertId, 1);
                  insert100Done();
              });
            });

            insertDb.transaction(function(tx) {
              tx.executeSql("INSERT INTO test_table (data, data_num) VALUES (?,?)", ["test1", 200], function(tx, res) {
                  console.log("insertId: " + res.insertId + " -- probably 2"); // check #18/#38 is fixed
                  assert.equal(res.insertId, 2);
                  insert200Done();
              });
            });

            console.log("Opening database");
            var db = window.sqlitePlugin.openDatabase("demodb", "1.0", "Demo", -1);
            db.transaction(function(tx) {
              tx.executeSql("SELECT data_num from test_table;", [], function(tx, res) {
                console.log("res.rows.length: " + res.rows.length + " -- should be 2");
                assert.equal(res.rows.length, 2);
                assert.equal(res.rows.item(0).data_num, 100);
                countDone();
              });
            });
            var db1 = window.sqlitePlugin.openDatabase("demodb", "1.0", "Demo", -1);
            db1.transaction(function(tx) {
              tx.executeSql("SELECT data_num from test_table where data_num = 200", [], function(tx, res) {
                console.log("res.rows.length: " + res.rows.length + " -- should be 1");
                assert.equal(res.rows.length, 1);
                assert.equal(res.rows.item(0).data_num, 200);
                count1Done();
              });
            });
        });

        QUnit.test ("testing database no library", function(assert) {
            assert.expect(1)

            var KEY_SECTION_BLOB = "sectionJsonBlob";
            var TABLE_CURR_TRIPS = "currTrips";
            var KEY_USER_CLASSIFICATION = "userClassification";

            var done = assert.async();
            var db = window.sqlitePlugin.openDatabase({name: "TripSections.db", location: 2, createFromLocation: 1});
            db.transaction(function(tx) {
              tx.executeSql("select " + KEY_SECTION_BLOB + " from " + TABLE_CURR_TRIPS + " where " + KEY_USER_CLASSIFICATION + " is null", [], function(tx, tempTripList) {
                var jsonTripList = [];
                for (k=0; k < tempTripList.rows.length; k++) {
                  jsonTripList.push(tempTripList.rows.item(k));
                }
                assert.equal(jsonTripList.length, 27);
                done();
              }, function(e) {
                console.log("ERROR: " + e.message);
              });
            });
        });

        QUnit.test ("testing database and wrappers", function(assert) {
            sampleJSON1Obj = {"source":"Shankari","section_id":0,"duration":645,"type":"move","confirmed_mode":"","predicted_mode":{"walking":1},"mode":"walking","section_start_time":"20141121T110333-0800","_id":"7327a0b3-4959-31f5-86cb-ea0c59fd0c83_20141121T110333-0800_0","distance":724,"trip_id":"20141121T110333-0800","section_end_time":"20141121T111418-0800","track_points":[{"track_location":{"type":"Point","coordinates":[-122.2632823787,37.865492072]},"time":"20141121T110333-0800"}],"manual":false,"tripId":"20141121T110333-0800","userClassification":null,"sectionId":"0"};
            sampleJSON1 = [{"sectionJsonBlob": btoa(JSON.stringify(sampleJSON1Obj))}];

            assert.expect(10);

            var realDbQueryDone = assert.async();
            console.log("Opening database");
            var db = window.sqlitePlugin.openDatabase({name: "TripSections.db", location: 2, createFromLocation: 1});
            tripSectionDbHelper.getJSON(db, function(jsonTripList) {
                tripList = tripSectionDbHelper.getUncommitedSections(jsonTripList);
                assert.equal(tripList.length, 27);
                realDbQueryDone();
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

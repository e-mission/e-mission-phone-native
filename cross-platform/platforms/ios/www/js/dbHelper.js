// CONSTANTS
var KEY_SECTION_BLOB = "sectionJsonBlob";
var TABLE_CURR_TRIPS = "currTrips";
var KEY_USER_CLASSIFICATION = "userClassification";

var dbHelper = {
    pragmaExample: function() {
        var db = window.sqlitePlugin.openDatabase({name: "my.db"});

        db.transaction(function(tx) {
          tx.executeSql('DROP TABLE IF EXISTS test_table');
          tx.executeSql('CREATE TABLE IF NOT EXISTS test_table (id integer primary key, data text, data_num integer)');

          // demonstrate PRAGMA:
          db.executeSql("pragma table_info (test_table);", [], function(res) {
            console.log("PRAGMA res: " + JSON.stringify(res));
            alert("PRAGMA res: " + JSON.stringify(res));
          });

          tx.executeSql("INSERT INTO test_table (data, data_num) VALUES (?,?)", ["test", 100], function(tx, res) {
            console.log("insertId: " + res.insertId + " -- probably 1");
            console.log("rowsAffected: " + res.rowsAffected + " -- should be 1");

            alert("insertId: " + res.insertId + " -- probably 1");
            alert("rowsAffected: " + res.rowsAffected + " -- should be 1");

            db.transaction(function(tx) {
              tx.executeSql("select count(id) as cnt from test_table;", [], function(tx, res) {
                console.log("res.rows.length: " + res.rows.length + " -- should be 1");
                console.log("res.rows.item(0).cnt: " + res.rows.item(0).cnt + " -- should be 1");

                alert("res.rows.length: " + res.rows.length + " -- should be 1");
                alert("res.rows.item(0).cnt: " + res.rows.item(0).cnt + " -- should be 1");
              });
            });

          }, function(e) {
            console.log("ERROR: " + e.message);
          });
        });
    }
}

var tripSectionDbHelper = {
  getUncommitedSections: function() {
    var db = window.sqlitePlugin.openDatabase({name: "TripSections.db"});

    db.transaction(function(tx) {
      tx.executeSql("select " + KEY_SECTION_BLOB + " from " + TABLE_CURR_TRIPS + " where " + KEY_USER_CLASSIFICATION + " is null", [], function(tx, tripList) {
        console.log("number of rows in tripList: " + tripList.rows.length);
        console.log("Printing Trips:");
        for (i = 0; i < tripList.rows.length; i++) {
          console.log("Trip: " + JSON.stringify(tripList.rows.item(i)));
        }
        console.log("Done printing");
        // use an alert in the place where you call this function so that you can see if these objects work in javascript
        return tripList;
      }, function(e) {
        console.log("ERROR: " + e.message);
      });
    });
  }
}

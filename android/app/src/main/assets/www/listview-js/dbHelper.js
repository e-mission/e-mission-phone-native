// CONSTANTS
var KEY_SECTION_BLOB = "sectionJsonBlob";
var TABLE_CURR_TRIPS = "currTrips";
var KEY_USER_CLASSIFICATION = "userClassification";

// EXAMPLE CODE:

// var dbHelper = {
//     pragmaExample: function() {
//         var db = window.sqlitePlugin.openDatabase({name: "my.db"});

//         db.transaction(function(tx) {
//           tx.executeSql('DROP TABLE IF EXISTS test_table');
//           tx.executeSql('CREATE TABLE IF NOT EXISTS test_table (id integer primary key, data text, data_num integer)');

//           // demonstrate PRAGMA:
//           db.executeSql("pragma table_info (test_table);", [], function(res) {
//             console.log("PRAGMA res: " + JSON.stringify(res));
//             alert("PRAGMA res: " + JSON.stringify(res));
//           });

//           tx.executeSql("INSERT INTO test_table (data, data_num) VALUES (?,?)", ["test", 100], function(tx, res) {
//             console.log("insertId: " + res.insertId + " -- probably 1");
//             console.log("rowsAffected: " + res.rowsAffected + " -- should be 1");

//             alert("insertId: " + res.insertId + " -- probably 1");
//             alert("rowsAffected: " + res.rowsAffected + " -- should be 1");

//             db.transaction(function(tx) {
//               tx.executeSql("select count(id) as cnt from test_table;", [], function(tx, res) {
//                 console.log("res.rows.length: " + res.rows.length + " -- should be 1");
//                 console.log("res.rows.item(0).cnt: " + res.rows.item(0).cnt + " -- should be 1");

//                 alert("res.rows.length: " + res.rows.length + " -- should be 1");
//                 alert("res.rows.item(0).cnt: " + res.rows.item(0).cnt + " -- should be 1");
//               });
//             });

//           }, function(e) {
//             console.log("ERROR: " + e.message);
//           });
//         });
//     }
// }

var tripSectionDbHelper = {
  // callBack must be a function that takes the list of uncommitedSections
  getJSON: function(db, callBack) {
    db.transaction(function(tx) {
      tx.executeSql("select " + KEY_SECTION_BLOB + " from " + TABLE_CURR_TRIPS + " where " + KEY_USER_CLASSIFICATION + " is null", [], function(tx, tempTripList) {
        var jsonTripList = [];
        for (k=0; k < tempTripList.rows.length; k++) {
          jsonTripList.push(tempTripList.rows.item(k));
        }
        callBack(jsonTripList);
      }, function(e) {
        console.log("ERROR: " + e.message);
      });
    });
  },
  getUncommitedSections: function(jsonTripList) {
    var tripList = [];
    for (j=0; j < jsonTripList.length; j++) {
      try {
          var trip = new tripSection();
          trip.loadFromJSON(jsonTripList[j]);
          tripList.push(trip);
      } catch (e) {
          console.log("error while parsing trip string"+jsonTripList[j]);
          alert("error while parsing trip string "+jsonTripList[j]);
      }
    }
    return tripList;
  }
}

function tripSection() {
  this.tripId = "";
  this.sectionId = "";
  this.trackPoints = [];
  this.startTime = new customDate();
  this.endTime = new customDate();
  this.autoMode = "";
  this.selMode = ""; // selMode is only set when the user selects a mode
  this.confidence = -1.0;
  this.userMode = "";

  this.loadFromJSON = function(jsonObject) {
    jsonObject = JSON.parse(atob(jsonObject.sectionJsonBlob));
    this.tripId = jsonObject.trip_id;
    this.sectionId = jsonObject.section_id;
    this.startTime.loadFromDateString(jsonObject.section_start_time);
    this.endTime.loadFromDateString(jsonObject.section_end_time);
    this.userMode = jsonObject.confirmed_mode;

    var predictedMode = jsonObject.mode; // set to mode by default and then prediction if it exists
    var highestConfidence = 0;
    for (var key in jsonObject.predicted_mode) {
      var currentConfidence = jsonObject.predicted_mode[key];
      if (currentConfidence >= highestConfidence) {
        predictedMode = key;
        highestConfidence = currentConfidence;
      }
    }
    this.confidence = highestConfidence;
    this.autoMode = predictedMode;

    for (i=0; i < jsonObject.track_points.length; i++) {
      var tempTrackLoc = new trackLocation();
      tempTrackLoc.loadFromJSON(jsonObject.track_points[i]);
      this.trackPoints.push(tempTrackLoc);
    }
  };
}

// FORMAT: yyyyMMddTHHmmssZ
// EXAMPLE: 20141116T100919-0800
function customDate() {
  this.date = new Date();
  this.zone = "";

  this.loadFromDateString = function(dateString) {
    this.date.setFullYear(parseInt(dateString.substring(0, 4)));;
    this.date.setMonth(parseInt(dateString.substring(4, 6)));
    this.date.setDate(parseInt(dateString.substring(6, 8)));
    this.date.setHours(parseInt(dateString.substring(9, 11)));
    this.date.setMinutes(parseInt(dateString.substring(11, 13)));
    this.date.setSeconds(parseInt(dateString.substring(13, 15)));
    this.zone = dateString.substring(16);
  }
}

// EXAMPLE: {"track_location": {"type":"Point","coordinates":[-122.2940501821,37.9250113486]},
//           "time":"20141116T100054-0800"
//           }
function trackLocation() {
  this.sampleTime = new customDate();
  this.sampleType = "";
  this.coordinate = null;

  this.first = false; // responsibility of caller to set these
  this.last = false;

  this.loadFromJSON = function(jsonObject) {
    var locationInfo = jsonObject.track_location;
    this.sampleTime.loadFromDateString(jsonObject.time);
    this.sampleType = locationInfo.type;
    this.coordinate = locationInfo.coordinates;
  }
}

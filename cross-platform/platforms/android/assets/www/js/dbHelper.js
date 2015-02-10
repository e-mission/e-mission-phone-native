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

    var tripWrapperList = [];

    db.transaction(function(tx) {
      tx.executeSql("select " + KEY_SECTION_BLOB + " from " + TABLE_CURR_TRIPS + " where " + KEY_USER_CLASSIFICATION + " is null", [], function(tx, tripList) {
      // tx.executeSql("select " + "*" + " from " + TABLE_CURR_TRIPS , [], function(tx, tripList) {
        //console.log("number of rows in tripList: " + tripList.rows.length);
        //console.log("Printing Trips:");
        for (i = 0; i < tripList.rows.length; i++) {  
          // console.log("Trip: " + JSON.stringify(tripList.rows.item(i)));
          console.log("marker 1");
          var trip = new tripSection();
          console.log("marker 2");
          trip.loadFromJSON(tripList.rows.item(i));
          console.log("marker 3");
          tripWrapperList.push(trip);
        }
        console.log("Done printing");
        // use an alert in the place where you call this function so that you can see if these objects work in javascript
      }, function(e) {
        console.log("ERROR: " + e.message);
      });
    });
  
  setTimeout(function(){
    console.log(tripWrapperList.length)
    return tripWrapperList;
  }, 500);

  }
}

//[{"track_location":{"type":"Point","coordinates":[-122.2940501821,37.9250113486]},"time":"20141116T100054-0800"},{"track_location":{"type":"Point","coordinates":[-122.2944105623,37.9251194763]},"time":"20141116T100132-0800"},{"track_location":{"type":"Point","coordinates":[-122.294729509,37.9250114113]},"time":"20141116T100154-0800"},{"track_location":{"type":"Point","coordinates":[-122.2948631259,37.9249539313]},"time":"20141116T100210-0800"},{"track_location":{"type":"Point","coordinates":[-122.2950303485,37.9246061106]},"time":"20141116T100233-0800"},{"track_location":{"type":"Point","coordinates":[-122.2951354687,37.9243027568]},"time":"20141116T100252-0800"},{"track_location":{"type":"Point","coordinates":[-122.295232153,37.9241266164]},"time":"20141116T100311-0800"},{"track_location":{"type":"Point","coordinates":[-122.2954467642,37.9238511575]},"time":"20141116T100334-0800"},{"track_location":{"type":"Point","coordinates":[-122.2955252215,37.9237799199]},"time":"20141116T100350-0800"},{"track_location":{"type":"Point","coordinates":[-122.2957380836,37.9236763728]},"time":"20141116T100406-0800"},{"track_location":{"type":"Point","coordinates":[-122.2968968743,37.9235649295]},"time":"20141116T100519-0800"},{"track_location":{"type":"Point","coordinates":[-122.2971837149,37.9237397214]},"time":"20141116T100541-0800"},{"track_location":{"type":"Point","coordinates":[-122.2973710724,37.9239336475]},"time":"20141116T100559-0800"},{"track_location":{"type":"Point","coordinates":[-122.2980347459,37.9242457398]},"time":"20141116T100652-0800"},{"track_location":{"type":"Point","coordinates":[-122.29969366,37.9268944314]},"time":"20141116T100820-0800"},{"track_location":{"type":"Point","coordinates":[-122.2993079473,37.9240372207]},"time":"20141116T100919-0800"}]

function tripSection() {
  this.tripId = "";
  this.sectionId = "";
  this.trackPoints = [];
  this.startTime = null;
  this.endTime = null;
  this.autoMode = "";
  this.selMode = "";
  this.confidence = -1.0;
  this.userMode = "";
  this.loadFromJSON = function(jsonObject) {
    jsonObject = JSON.parse(jsonObject.sectionJsonBlob);
    // console.log("Trip: " + JSON.stringify(jsonObject.sectionJsonBlob));
    // console.log(typeof jsonObject);
    this.tripId = jsonObject.trip_id;
    this.sectionId = jsonObject.section_id;
    console.log("before printing date");
    var now = moment();
    console.log("printing date");
    console.log(now.format());
    // console.log("marker 2.1");
    // this.startTime = $filter('date')(jsonObject.section_start_time,"yyyyMMddTHHmmssZ"); // I don't know if I'm using angular correctly
    // console.log("marker 2.2");
    // this.endTime = $filter('date')(jsonObject.section_end_time, "yyyyMMddTHHmmssZ");
    // console.log("marker 2.3");
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
    // selMode is only set when the user selects a mode
    this.userMode = jsonObject.confirmed_mode;
  };
}

// var tripSection = {
//   tripId: "",
//   sectionId: "",
//   trackPoints: [],
//   startTime: null,
//   endTime: null,
//   autoMode: "",
//   selMode: "",               // the mode the user will select
//   confidence: -1.0,
//   userMode: "",
//   loadFromJSON: function(jsonObject) {
//     this.tripId = jsonObject.trip_id;
//     this.sectionId = jsonObject.section_id;
    
//     this.startTime = $filter('date')(jsonObject.section_start_time,"yyyyMMddTHHmmssZ"); // I don't know if I'm using angular correctly
//     this.endTime = $filter('date')(jsonObject.section_end_time, "yyyyMMddTHHmmssZ");
//     var predictedMode = jsonObject.mode; // set to mode by default and then prediction if it exists
//     var highestConfidence = 0;
//     for (var key in jsonObject.predicted_mode) {
//       var currentConfidence = jsonObject.predicted_mode[key];
//       if (currentConfidence >= highestConfidence) {
//         predictedMode = key;
//         highestConfidence = currentConfidence;
//       }
//     }
//     this.confidence = highestConfidence;
//     this.autoMode = predictedMode;
//     // selMode is only set when the user selects a mode
//     this.userMode = jsonObject.confirmed_mode;
//   },
// }

// var trackLocation = {
//   sampleTime: null,
//   latitude: null,
//   longitude: null,
//   first: false,
//   last: false,
//   coordinate: null,
//   loadFromJSON: function(jsonObject) {
    
//   }
// }

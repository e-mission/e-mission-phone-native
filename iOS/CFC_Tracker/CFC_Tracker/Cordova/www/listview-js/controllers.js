angular.module('starter.controllers', ['ionic'])

.controller("TripsCtrl", function($scope, $ionicPlatform, $state, $ionicSlideBoxDelegate) {
  console.log("controller TripsCtrl called");

  //DATA: Gautham, this is where you link the data.
  /*
  $scope.trips = [
       {mode:'walking',confidence:1},
       {mode:'car',confidence:0.5},
       {mode:'walking',confidence:1},
       {mode:'cycling',confidence:0.3},
       {predictedMode:'cycling',confidence:0.3}
  ];
  */

  /*
   * I think that this may be a cause of a controller trying to do too much,
   * and should probably be moved into a service.
   */

  // code to get trips from most recent day only
  var db = window.sqlitePlugin.openDatabase({
    name: "TripSections.db",
    location: 2,
    createFromLocation: 1
  });
  tripSectionDbHelper.getJSON(db, function(jsonTripList) {
    $scope.$apply(function() {
      // console.log(jsonTripList);
      //$scope.trips = tripSectionDbHelper.getUncommitedSections(jsonTripList);

      var last_five_trips = [];
      var dic = {}
      var sec = tripSectionDbHelper.getUncommitedSections(jsonTripList);

      // get all sections for the last five days
      for (var j = 0; j < 5; j++) {
        var mr_trip = sec.pop();
        var mr_trips = [mr_trip];
        var today = new Date(mr_trip.startTime.date);
        var key_date = getDateOfTrip(today);

        for (var i = sec.length - 1; i >= 0;) {
          var trip = sec[i];
          // hacky way to check if date is the same
          var tripDate = new Date(trip.startTime.date)
          if (tripDate.getMonth() == today.getMonth()) {
            if (tripDate.getDate() == today.getDate()) {
              if (tripDate.getFullYear() == today.getFullYear()) {
                sec.splice(i, 1);
                mr_trips.unshift(trip);

              }
            }
          }
          i--;
        }
        dic['date_key'] = key_date;
        dic['trip_val'] = mr_trips;
        last_five_trips.push(dic);
        dic = {}
          // last five trips: [ {date: date, trips: [trip1, trip2]} ]
      }
      $scope.data = {};
      $scope.data.slides = last_five_trips;
      $ionicSlideBoxDelegate.update();

      $scope.last_five_trips = last_five_trips;
      console.log(last_five_trips);
    });
  });


  var getDateOfTrip = function(date) {
    var month;
    var date;

    switch (date.getMonth() + 1) {
      case 1:
        month = "January";
        break;
      case 2:
        month = "February";
        break;
      case 3:
        month = "March";
        break;
      case 4:
        month = "April";
        break;
      case 5:
        month = "May";
        break;
      case 6:
        month = "June";
        break;
      case 7:
        month = "July";
        break;
      case 8:
        month = "August";
        break;
      case 9:
        month = "September";
        break;
      case 10:
        month = "October";
        break;
      case 11:
        month = "November";
        break;
      case 12:
        month = "December";
        break;

    };

    switch (date.getDay() + 1) {
      case 1:
        day = "Sunday";
        break;
      case 2:
        day = "Monday";
        break;
      case 3:
        day = "Tuesday";
        break;
      case 4:
        day = "Wednesday";
        break;
      case 5:
        day = "Thursday";
        break;
      case 6:
        day = "Friday";
        break;
      case 7:
        day = "Saturday";
        break;
    };

    return (day + ", " + month + " " + date.getDate() + ", " + date.getFullYear());
  };


  $scope.getTime = function(date) {
    var min = (date.getMinutes() < 10 ? '0' : '') + date.getMinutes();
    return ("" + date.getHours() + ":" + min);
  };

  $scope.notSingleOrLast = function(index, list) {
    if (index == list.length - 1) {
      return false;
    } else {
      return true;
    }
  };

  $scope.slideHasChanged = function(index) {
    console.log("slide changed to index: " + index);
    var trip = $scope.data.slides[index];
    $scope.setupMap(trip["trip_val"][0]);
  }

  $scope.mapCreated = function(map) {
    console.log("maps here");
    console.log(map)
    $scope.map = map;
    $scope.setupMap($scope.data.slides[0]["trip_val"][0]);
  };

  $scope.centerOnMe = function() {
    console.log("Centering");
    if (!$scope.map) {
      return;
    }

    $scope.loading = $ionicLoading.show({
      content: 'Getting current location...',
      showBackdrop: false
    });

    navigator.geolocation.getCurrentPosition(function(pos) {
      console.log('Got pos', pos);
      $scope.map.setCenter(new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude));
      $scope.loading.hide();
    }, function(error) {
      alert('Unable to get location: ' + error.message);
    });
  };

  $scope.getDisplayName = function(item) {
    var coordinatesString = item["trackPoints"][0]["coordinate"][1] + "," + item["trackPoints"][0]["coordinate"][0]
    var xmlHttp = new XMLHttpRequest();
    var url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + coordinatesString + "&key=AIzaSyD8BQ3-gIFLsqR324AmQfGK6wCSauVAcEo"
    console.log(url)
    xmlHttp.open("GET", url, false);
    xmlHttp.send();
    var results = JSON.parse(xmlHttp.response)["results"];
    if (results.length > 0) {
      var address_components = results[0]["address_components"];
      var name = ""
      for (var i = 0; i < address_components.length; i++) {
        var component = address_components[i]
        var types = component["types"]
        if (types.indexOf("neighborhood") > -1) {
          name = component["short_name"];
          break;
        } else if (types.indexOf("establishment") > -1) {
          name = component["short_name"];
          break;
        } else if (types.indexOf("locality") > -1) {
          name = component["short_name"];
          break;
        }
      }
      return name
    } else {
      return "over limit";
    }
  };

  /*
  var db = $cordovaSQLite.openDB({name: "TripSections.db"});
  tripSectionDbHelper.getJSON({name: "TripSections.db"}, function(jsonTripList){
      alert("this is actually happening");
      console.log("testing other things");
      $scope.trips = tripSectionDbHelper.getUncommittedSections(jsonTripList);
      console.log($scope.trips.length + "trips have been loaded");
  });
  */
  $scope.nextSlide = function() {
    console.log("next");
    $ionicSlideBoxDelegate.next();
  }

  $scope.pickImage = function(item) {
    if (item.predictedMode != null) {
      var item_mode = item.predictedMode;
    } else {
      var item_mode = item.autoMode;
    }

    if (item_mode == 'walking') {
      return 'img/walking.jpg';
    }
    if (item_mode == 'car') {
      return 'img/car.jpg';
    }
    if (item_mode == 'cycling') {
      return 'img/cycling.jpg';
    }
    if (item_mode == 'air') {
      return 'img/air.jpg';
    }
    if (item_mode == 'bus') {
      return 'img/bus.jpg';
    }
    if (item_mode == 'train') {
      return 'img/train.jpg';
    }
  };

  $scope.setupMap = function(item) {
    console.log(JSON.stringify(item));
    if ($scope.path) {
      $scope.path.setMap(null)
    }
    if ($scope.startMarker) {
      $scope.startMarker.setMap(null)
    }
    if ($scope.endMarker) {
      $scope.endMarker.setMap(null)
    }
    var points = item["trackPoints"]
    var latitude = points[0]["coordinate"][1]
    var longitude = points[0]["coordinate"][0]
    var endLat = points[points.length - 1]["coordinate"][1]
    var endLng = points[points.length - 1]["coordinate"][0]
    $scope.startMarker = new google.maps.Marker({
      position: new google.maps.LatLng(latitude, longitude),
      icon: 'img/maps-markera.png'
    });
    $scope.startMarker.setMap($scope.map)
    $scope.endMarker = new google.maps.Marker({
      position: new google.maps.LatLng(endLat, endLng),
      icon: 'img/maps-markerb.png'
    });
    $scope.endMarker.setMap($scope.map)
    $scope.map.setCenter({
      lat: latitude,
      lng: longitude
    })
    var coordinates = [];
    for (var i = 0; i < points.length; i++) {
      coordinates.push(new google.maps.LatLng(points[i]["coordinate"][1], points[i]["coordinate"][0]))
    }
    var path = new google.maps.Polyline({
      path: coordinates,
      geodesic: true,
      strokeColor: '#FF0000',
      strokeOpacity: 1.0,
      strokeWeight: 2
    });
    $scope.path = path
    path.setMap($scope.map)
    var bounds = new google.maps.LatLngBounds();
    for (var i = 0; i < coordinates.length; i++) {
      bounds.extend(coordinates[i]);
    }
    $scope.map.fitBounds(bounds);
  }

  //Change according to datatype in actual data object and the intervals set in the app.
  // Intervals: Green - confidence > 80 ; Yellow: 80 > confidence > 70; Red: 70 > confidence
  $scope.getConfidenceColor = function(item) {
    if (item.confidence >= 0.9) {
      return "confidence-certain";
    } else if (item.confidence >= 0.7) {
      return "confidence-medium";
    } else {
      return "confidence-low";
    }
  };

  $scope.getDisplayMode = function(item) {
    if (item.predictedMode != null) {
      var item_mode = item.predictedMode;
    } else {
      var item_mode = item.autoMode;
    }
    if (item_mode == 'walking') {
      return 'ion-android-walk'
    }
    if (item_mode == 'car') {
      return 'ion-android-car';
    }
    if (item_mode == 'cycling') {
      return 'ion-android-bicycle';
    }
    if (item_mode == 'air') {
      return 'ion-android-plane';
    }
    if (item_mode == 'bus') {
      return 'ion-android-bus';
    }
    if (item_mode == 'train') {
      return 'ion-android-subway';
    }
  };

  $scope.modes = [{
    mode: "walking",
    show: "Walk"
  }, {
    mode: "cycling",
    show: "Bike"
  }, {
    mode: "car",
    show: "Car"
  }, {
    mode: "air",
    show: "Fly"
  }, {
    mode: "bus",
    show: "Bus"
  }, {
    mode: "train",
    show: "Train"
  }];

  $scope.modeUpdate = function(newMode) {
         console.log("selected new mode " + newMode)
     };

  $scope.modeChange = function(newMode) {
    $scope.modes[0].newMode;
  };

})

.controller('PlaylistsCtrl', function($scope) {
  $scope.playlists = [{
    title: 'Reggae',
    id: 1
  }, {
    title: 'Chill',
    id: 2
  }, {
    title: 'Dubstep',
    id: 3
  }, {
    title: 'Indie',
    id: 4
  }, {
    title: 'Rap',
    id: 5
  }, {
    title: 'Cowbell',
    id: 6
  }];
})
angular.module('starter.controllers', [])

/*.controller('TripsCtrl', function($scope, Trips) {
  $scope.trips = Trips.all();
})

.controller('TripDetailCtrl', function($scope, $stateParams, Trips) {
  $scope.trip = Trips.get($stateParams.tripId);
})*/

.controller('ListCtrl', function($scope) {
  $scope.id = "10001";
  $scope.message = "This works!"
  $scope.cool = function() {
    $scope.message = "I've been clicked!"
  };
})

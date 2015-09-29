/**
 * Created by harsh on 22/6/15.
 */


/* Controllers */
var reportsAppControllers = angular.module('reportsAppControllers',[])

    .controller('HomeController', function($rootScope,
                                        $scope, goToService
                                        ){

        $scope.goTo = function (place) {

            goToService.goTo(place);
        };
});
/**
 * Created by harsh on 22/6/15.
 */


reportsApp.controller('LeftBarMenuController',
    function ($rootScope,$scope,goToService) {


        $scope.goTo = function (place) {

           goToService.goTo(place);
        };


    });
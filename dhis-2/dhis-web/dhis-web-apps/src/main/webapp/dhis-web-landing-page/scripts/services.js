/**
 * Created by harsh on 22/6/15.
 */


var reportsAppServices = angular.module('reportsAppServices',[])


    .service('goToService', function(){

return {
    goTo: function (place) {

        switch (place) {
            case 'tracker-capture' :
                window.location.href = '../dhis-web-tracker-capture/index.html#/';
                break;


            case 'aggregate-data-entry' :
                window.location.href = '../dhis-web-dataentry/index.action';
                break;

            case 'report' :
                window.location.href = '../dhis-web-reporting/index.action';
                break;


            case 'dashboard' :
                window.location.href = '../dhis-web-dashboard-integration/index.action';
                break;
        }

    }
}


});
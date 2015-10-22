'use strict';

/* Directives */

var eventCaptureDirectives = angular.module('eventCaptureDirectives', [])

.directive('selectedOrgUnit', function ($timeout, OrgUnitService, SessionStorageService) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            //once ou tree is loaded, start meta-data download
            //$(function () {
                $( '#orgUnitTree' ).one( 'ouwtLoaded', function( event, ids, names ){
                    console.log('Finished loading orgunit tree');
                    //Disable ou selection until meta-data has downloaded
                    $("#orgUnitTree").addClass("disable-clicks");

                    $timeout(function () {
                        scope.treeLoaded = true;
                        scope.$apply();
                    });

                    downloadMetaData();
                });
            //});

            //listen to user selection, and inform angular         
            selection.setListenerFunction(setSelectedOu, true);
            function setSelectedOu(ids, names) {
                var ou = {id: ids[0], name: names[0]};
                $timeout(function () {
                    scope.selectedOrgUnit = ou;
                    scope.$apply();
                });
            }
        }
    };
})
;
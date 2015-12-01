/* global selection, angular */

'use strict';

/* Directives */

var eventCaptureDirectives = angular.module('eventCaptureDirectives', [])

.directive('selectedOrgUnit', function ($timeout) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            $("#orgUnitTree").one("ouwtLoaded", function (event, ids, names) {                                   
                console.log('Finished loading orgunit tree');                        
                $("#orgUnitTree").addClass("disable-clicks"); //Disable ou selection until meta-data has downloaded
                $timeout(function () {
                    scope.treeLoaded = true;
                    scope.$apply();
                });
                downloadMetaData();                
            });            

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

.directive('d2FileInput', function(DHIS2EventService, DHIS2EventFactory, FileService, DialogService){
    
    return {
        restrict: "A",
        scope: {
            d2FileInputList: '=',
            d2FileInput: '=',
            d2FileInputName: '=',
            d2FileInputCurrentName: '=',
            d2FileInputPs: '='
        },
        link: function (scope, element, attrs) {
            
            var de = attrs.inputFieldId;
            
            var updateModel = function () {
                
                var update = attrs.d2FileInputInstant;
                
                FileService.upload(element[0].files[0]).then(function(data){
                    
                    if(data && data.status === 'OK' && data.response && data.response.fileResource && data.response.fileResource.id && data.response.fileResource.name){
                                            
                        scope.d2FileInput[de] = data.response.fileResource.id;   
                        scope.d2FileInputCurrentName[de] = data.response.fileResource.name;
                        if( update === 'true' ){                            
                            if(!scope.d2FileInputName[scope.d2FileInput.event]){
                                scope.d2FileInputName[scope.d2FileInput.event] = [];
                            }                            
                            scope.d2FileInputName[scope.d2FileInput.event][de] = data.response.fileResource.name;
                            
                            var updatedSingleValueEvent = {event: scope.d2FileInput.event, dataValues: [{value: data.response.fileResource.id, dataElement:  de}]};
                            var updatedFullValueEvent = DHIS2EventService.reconstructEvent(scope.d2FileInput, scope.d2FileInputPs.programStageDataElements);
                            DHIS2EventFactory.updateForSingleValue(updatedSingleValueEvent, updatedFullValueEvent).then(function(data){
                                scope.d2FileInputList = DHIS2EventService.refreshList(scope.d2FileInputList, scope.d2FileInput);
                            });
                        }
                    }
                    else{
                        var dialogOptions = {
                            headerText: 'error',
                            bodyText: 'file_upload_failed'
                        };		
                        DialogService.showDialog({}, dialogOptions);
                    }
                    
                });                 
            };             
            element.bind('change', updateModel);            
        }
    };    
})

.directive('d2FileInputDelete', function($parse, $timeout, FileService, DialogService){
    
    return {
        restrict: "A",
        link: function (scope, element, attrs) {
            var valueGetter = $parse(attrs.d2FileInputDelete);
            var nameGetter = $parse(attrs.d2FileInputName);
            var nameSetter = nameGetter.assign;
            
            if(valueGetter(scope)) {
                FileService.get(valueGetter(scope)).then(function(data){
                    if(data && data.name && data.id){
                        $timeout(function(){
                            nameSetter(scope, data.name);
                            scope.$apply();
                        });
                    }
                    else{
                        var dialogOptions = {
                            headerText: 'error',
                            bodyText: 'file_missing'
                        };		
                        DialogService.showDialog({}, dialogOptions);
                    }                    
                });                 
            }
        }
    };    
});


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

.directive('d2FileInputField', function () {

    return {
        restrict: 'E',
        templateUrl: 'views/file-input.html',
        link: function (scope, element, attrs) {}
    };
})

.directive('d2FileInput', function($parse, $timeout, FileService, DialogService){
    
    return {
        restrict: "A",
        link: function (scope, element, attrs) { 
            var modelGetter = $parse(attrs.d2FileInput);
            var modelSetter = modelGetter.assign;
            
            var nameGetter = $parse(attrs.d2FileInputName);
            var nameSetter = nameGetter.assign;
 
            var updateModel = function () {
                FileService.upload(element[0].files[0]).then(function(data){
                    if(data && data.status === 'OK' && data.response && data.response.fileResource && data.response.fileResource.id){
                        $timeout(function(){
                            modelSetter(scope, data.response.fileResource.id);
                            nameSetter(scope, element[0].files[0].name);
                            scope.$apply();
                        });
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

.directive('d2FileInputDelete', function($parse, $timeout, ModalService, FileService, DialogService){
    
    return {
        restrict: "A",
        link: function (scope, element, attrs) {
            var modelGetter = $parse(attrs.d2FileInputDelete);
            var modelSetter = modelGetter.assign;
            var nameGetter = $parse(attrs.d2FileInputName);
            var nameSetter = nameGetter.assign;
            
            if(modelGetter(scope)) {
                FileService.get(modelGetter(scope)).then(function(data){
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
            
            var deleteFile = function () {
                var modalOptions = {
                    closeButtonText: 'cancel',
                    actionButtonText: 'remove',
                    headerText: 'remove',
                    bodyText: 'are_you_sure_to_remove'
                };

                ModalService.showModal({}, modalOptions).then(function(result){
                    $timeout(function(){
                        modelSetter(scope, '');
                        nameSetter(scope, '');
                        scope.$apply();
                    });
                });
            };
             
            element.bind('click', deleteFile);            
        }
    };    
});


'use strict';

/* Directives */

var eventCaptureDirectives = angular.module('eventCaptureDirectives', [])

/*.directive('d2Options', function ($parse) {
    var linker = function (scope, element, attr) {

        scope.$watchCollection(['ngModel', 'options'], function () {
            element.trigger('chosen:updated');
        });


        var allowSingleDeselect = $parse(attr.allowSingleDeselect)(scope);
        var options = {
            no_results_text: "Oops, nothing found!",
            allow_single_deselect: allowSingleDeselect ? allowSingleDeselect : false
        };
        
        element.chosen(options);
    };

    return {
        restrict: 'A',
        scope: {// isolate scope 
            ngModel: '=',
            options: '='
        },
        link: linker
    };
})*/;
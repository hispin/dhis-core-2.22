/* global trackerCapture, angular */

trackerCapture.controller('ActiveProgramsController',
        function($scope, 
        $location,
        CurrentSelection) {
    //listen for the selected items
    $scope.$on('selectedItems', function(event, args) {        
        var selections = CurrentSelection.get();
        $scope.selectedTeiId = selections.tei ? selections.tei.trackedEntityInstance : null;
        $scope.activeEnrollments =  [];        
        angular.forEach(selections.enrollments, function(en){
            if(en.status === "ACTIVE" && selections.pr && selections.pr.id !== en.program){
                $scope.activeEnrollments.push(en);
            }
        });
    });
    
    $scope.changeProgram = function(program){
        $location.path('/dashboard').search({tei: $scope.selectedTeiId, program: program});
    };
});
/* global trackerCapture, angular */

trackerCapture.controller('ActiveProgramsController',
        function($scope, 
        $location,
        $translate,
        CurrentSelection) {
    //listen for the selected items
    $scope.emptyActiveProgramLabel = $translate.instant('no_active_program');
    
    $scope.$on('selectedItems', function(event, args) {        
        var selections = CurrentSelection.get();
        $scope.selectedTeiId = selections.tei ? selections.tei.trackedEntityInstance : null;
        $scope.activeEnrollments =  [];        
        angular.forEach(selections.enrollments, function(en){
            if(en.status === "ACTIVE"){
                if( !selections.pr ){
                    $scope.activeEnrollments.push(en);
                }
                if( selections.pr && selections.pr.id ){
                    if(selections.pr.id !== en.program){
                        $scope.activeEnrollments.push(en);
                    }
                    else{
                        $scope.emptyActiveProgramLabel = $translate.instant('no_active_program_than_selected');
                    }
                }                
            }
        });
    });
    
    $scope.changeProgram = function(program){
        $location.path('/dashboard').search({tei: $scope.selectedTeiId, program: program});
    };
});
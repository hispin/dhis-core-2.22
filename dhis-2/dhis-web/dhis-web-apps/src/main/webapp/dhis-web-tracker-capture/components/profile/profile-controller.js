trackerCapture.controller('ProfileController',
        function($rootScope,
                $scope,     
                CurrentSelection,
                CustomFormService,
                TEFormService,
                TEIService,
                DialogService,
                AttributesFactory) {    
    
    $scope.editingDisabled = true;
    $scope.enrollmentEditing = false;
    
    //attributes for profile    
    $scope.attributes = []; 
    $scope.attributesById = [];
    AttributesFactory.getAll().then(function(atts){
        angular.forEach(atts, function(att){
            $scope.attributesById[att.id] = att;
        });
    });
    
    //listen for the selected entity
    var selections = {};
    $scope.$on('dashboardWidgets', function(event, args) { 
        selections = CurrentSelection.get();
        $scope.selectedTei = angular.copy(selections.tei);
        $scope.trackedEntity = selections.te;
        $scope.selectedProgram = selections.pr;   
        $scope.selectedEnrollment = selections.selectedEnrollment;
        $scope.optionSets = selections.optionSets;
        $scope.trackedEntityForm = null;
        $scope.customForm = null;
        $scope.attributes = [];

        //display only those attributes that belong to the selected program
        //if no program, display attributesInNoProgram        
        angular.forEach($scope.selectedTei.attributes, function(att){
            $scope.selectedTei[att.attribute] = att.value;
        });
        delete $scope.selectedTei.attributes;
        
        if($scope.selectedProgram && $scope.selectedProgram.id){            
            AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){
                $scope.attributes = atts;

                $scope.selectedProgram.hasCustomForm = false;               
                TEFormService.getByProgram($scope.selectedProgram, atts).then(function(teForm){                    
                    if(angular.isObject(teForm)){                        
                        $scope.selectedProgram.hasCustomForm = true;
                        $scope.selectedProgram.displayCustomForm = $scope.selectedProgram.hasCustomForm ? true:false;
                        $scope.trackedEntityForm = teForm;
                        $scope.customForm = CustomFormService.getForTrackedEntity($scope.trackedEntityForm, 'PROFILE');
                    }                    
                });
            });                
        }
        else{            
            AttributesFactory.getWithoutProgram().then(function(atts){
                $scope.attributes = atts;
            });
        }
    });
    
    //listen for enrollment editing
    $scope.$on('enrollmentEditing', function(event, args) { 
        $scope.enrollmentEditing = args.enrollmentEditing;
    });
    
    $scope.enableEdit = function(){
        $scope.teiOriginal = angular.copy($scope.selectedTei);
        $scope.editingDisabled = !$scope.editingDisabled; 
        $rootScope.profileWidget.expand = true;
    };
    
    $scope.save = function(){
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        }

        //form is valid, continue the update process        
        //get tei attributes and their values
        //but there could be a case where attributes are non-mandatory and
        //form comes empty, in this case enforce at least one value        
        $scope.formEmpty = true;
        var tei = angular.copy(selections.tei);
        tei.attributes = [];
        for(var k in $scope.attributesById){
            if( $scope.selectedTei[k] ){
                tei.attributes.push({attribute: $scope.attributesById[k].id, value: $scope.selectedTei[k], type: $scope.attributesById[k].valueType});
                $scope.formEmpty = false;
            }
        }
        
        if($scope.formEmpty){//form is empty
            return false;
        }
                
        TEIService.update(tei, $scope.optionSets).then(function(updateResponse){
            
            if(updateResponse.status !== 'SUCCESS'){//update has failed
                var dialogOptions = {
                        headerText: 'update_error',
                        bodyText: updateResponse.description
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
            
            $scope.editingDisabled = !$scope.editingDisabled;
            CurrentSelection.set({tei: tei, te: $scope.trackedEntity, pr: $scope.selectedProgram, enrollment: $scope.selectedEnrollment, optionSets: $scope.optionSets});   
            $scope.outerForm.submitted = false; 
        });
    };
    
    $scope.cancel = function(){
        $scope.selectedTei = $scope.teiOriginal;  
        $scope.editingDisabled = !$scope.editingDisabled;
    };
    
    $scope.switchRegistrationForm = function(){
        $scope.selectedProgram.displayCustomForm = !$scope.selectedProgram.displayCustomForm;
    };    
});
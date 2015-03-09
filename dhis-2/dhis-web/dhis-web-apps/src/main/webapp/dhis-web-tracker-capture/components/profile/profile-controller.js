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
    $scope.widget = 'PROFILE';
    
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
        $scope.attributesById = CurrentSelection.getAttributesById();
        
        //display only those attributes that belong to the selected program
        //if no program, display attributesInNoProgram        
        angular.forEach($scope.selectedTei.attributes, function(att){
            $scope.selectedTei[att.attribute] = att.value;
        });
        
        delete $scope.selectedTei.attributes;
        
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){
            $scope.attributes = atts;          
            $scope.customFormExists = false;
            if($scope.selectedProgram && $scope.selectedProgram.id){
                TEFormService.getByProgram($scope.selectedProgram, atts).then(function(teForm){                    
                    if(angular.isObject(teForm)){                        
                        $scope.customFormExists = true;
                        $scope.trackedEntityForm = teForm;
                        $scope.customForm = CustomFormService.getForTrackedEntity($scope.trackedEntityForm, 'PROFILE');
                    }                    
                }); 
            }           
        });
    });
    
    //listen for enrollment editing
    $scope.$on('enrollmentEditing', function(event, args){
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
            if( $scope.selectedTei.hasOwnProperty(k) && $scope.selectedTei[k] ){
                tei.attributes.push({attribute: $scope.attributesById[k].id, value: $scope.selectedTei[k], type: $scope.attributesById[k].valueType});
                $scope.formEmpty = false;
            }
        }
        
        if($scope.formEmpty){//form is empty
            return false;
        }
                
        TEIService.update(tei, $scope.optionSets, $scope.attributesById).then(function(updateResponse){
            
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
        $scope.customFormExists = !$scope.customFormExists;
    };    
});
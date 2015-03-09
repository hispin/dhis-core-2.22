trackerCapture.controller('RegistrationController', 
        function($rootScope,
                $scope,
                $location,
                $timeout,
                AttributesFactory,
                DHIS2EventFactory,
                TEService,
                TEIService,
                TEFormService,
                CustomFormService,
                EnrollmentService,
                DialogService,
                CurrentSelection,
                OptionSetService,
                EventUtils,
                DateUtils,
                storage) {
    
    $scope.today = DateUtils.getToday();
    $scope.trackedEntityForm = null;
    $scope.customForm = null;    
    $scope.selectedTei = {};    
    
    $scope.attributesById = CurrentSelection.getAttributesById();
    if(!$scope.attributesById){
        $scope.attributesById = [];
        AttributesFactory.getAll().then(function(atts){
            angular.forEach(atts, function(att){
                $scope.attributesById[att.id] = att;
            });
            
            CurrentSelection.setAttributesById($scope.attributesById);
        });
    }    
    
    $scope.optionSets = CurrentSelection.getOptionSets();        
    if(!$scope.optionSets){
        $scope.optionSets = [];
        OptionSetService.getAll().then(function(optionSets){
            angular.forEach(optionSets, function(optionSet){                        
                $scope.optionSets[optionSet.id] = optionSet;
            });

            CurrentSelection.setOptionSets($scope.optionSets);
        });
    }
    
    $scope.selectedOrgUnit = storage.get('SELECTED_OU');
    $scope.selectedEnrollment = {dateOfEnrollment: '', dateOfIncident: ''};   
            
    $scope.trackedEntities = {available: []};
    TEService.getAll().then(function(entities){
        $scope.trackedEntities.available = entities;   
        $scope.trackedEntities.selected = $scope.trackedEntities.available[0];
    });
    
    //watch for selection of program
    $scope.$watch('selectedProgram', function() {        
        $scope.trackedEntityForm = null;
        $scope.customForm = null;
        $scope.getAttributes();
    });    
        
    $scope.getAttributes = function(){
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){
            $scope.attributes = atts;
            $scope.customFormExists = false;               
            TEFormService.getByProgram($scope.selectedProgram, $scope.attributes).then(function(teForm){
                if(angular.isObject(teForm)){                        
                    $scope.customFormExists = true;
                    $scope.trackedEntityForm = teForm;                      
                    $scope.customForm = CustomFormService.getForTrackedEntity($scope.trackedEntityForm, 'ENROLLMENT');
                }                    
            });  
        });        
    };
    
    $scope.registerEntity = function(destination){        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid ){
            return false;
        }
        
        //form is valid, continue the registration
        //get selected entity
        var selectedTrackedEntity = $scope.trackedEntities.selected.id; 
        if($scope.selectedProgram){
            selectedTrackedEntity = $scope.selectedProgram.trackedEntity.id;
        }
        
        //get tei attributes and their values
        //but there could be a case where attributes are non-mandatory and
        //registration form comes empty, in this case enforce at least one value        
        $scope.formEmpty = true;        
        $scope.tei = {trackedEntity: selectedTrackedEntity, orgUnit: $scope.selectedOrgUnit.id, attributes: [] };
        for(var k in $scope.attributesById){
            if( $scope.selectedTei.hasOwnProperty(k) && $scope.selectedTei[k] ){
                var val = $scope.selectedTei[k];
                $scope.tei.attributes.push({attribute: $scope.attributesById[k].id, value: val, type: $scope.attributesById[k].valueType});
                $scope.formEmpty = false;
            }
        }
        
        if($scope.formEmpty){//registration form is empty
            return false;
        }
        
        var teiId = '';
        TEIService.register($scope.tei, $scope.optionSets, $scope.attributesById).then(function(response){
            
            if(response.status === 'SUCCESS'){
                
                teiId = response.reference;
                
                //registration is successful and check for enrollment
                if($scope.selectedProgram){    
                    //enroll TEI
                    var enrollment = {trackedEntityInstance: teiId,
                                program: $scope.selectedProgram.id,
                                status: 'ACTIVE',
                                dateOfEnrollment: $scope.selectedEnrollment.dateOfEnrollment,
                                dateOfIncident: $scope.selectedEnrollment.dateOfIncident === '' ? $scope.selectedEnrollment.dateOfEnrollment : $scope.selectedEnrollment.dateOfIncident
                            };                           
                    EnrollmentService.enroll(enrollment).then(function(data){
                        if(data.status !== 'SUCCESS'){
                            //enrollment has failed
                            var dialogOptions = {
                                    headerText: 'enrollment_error',
                                    bodyText: data.description
                                };
                            DialogService.showDialog({}, dialogOptions);
                            return;
                        }
                        else{
                            enrollment.enrollment = data.reference;
                            var dhis2Events = EventUtils.autoGenerateEvents(teiId, $scope.selectedProgram, $scope.selectedOrgUnit, enrollment);
                            if(dhis2Events.events.length > 0){
                                DHIS2EventFactory.create(dhis2Events).then(function(data){
                                    goToDashboard(destination, teiId);
                                });
                            }else{
                                goToDashboard(destination, teiId);
                            }                            
                        }
                    });
                }
                else{
                    goToDashboard(destination, teiId);
                }
            }
            else{
                //registration has failed
                var dialogOptions = {
                        headerText: 'registration_error',
                        bodyText: response.description
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }            
        });
    };
    
    $scope.resetRelationshipSource = function(){
        $scope.selectedRelationshipSource = '';        
    };
    
    $scope.broadCastSelections = function(){
        angular.forEach($scope.tei.attributes, function(att){
            $scope.tei[att.attribute] = att.value;
        });
        
        $scope.tei.orgUnitName = $scope.selectedOrgUnit.name;
        $scope.tei.created = DateUtils.formatFromApiToUser(new Date());
        CurrentSelection.setRelationshipInfo({tei: $scope.tei, src: $scope.selectedRelationshipSource});
        $timeout(function() { 
            $rootScope.$broadcast('relationship', {});
        }, 100);
    };
    
    var goToDashboard = function(destination, teiId){
        //reset form
        $scope.selectedTei = {};
        $scope.selectedEnrollment = {};
        $scope.outerForm.submitted = false;

        if(destination === 'DASHBOARD') {
            $location.path('/dashboard').search({tei: teiId,                                            
                                    program: $scope.selectedProgram ? $scope.selectedProgram.id: null});
        }            
        else if(destination === 'RELATIONSHIP' ){
            $scope.tei.trackedEntityInstance = teiId;
            $scope.broadCastSelections();
        }
    };
});
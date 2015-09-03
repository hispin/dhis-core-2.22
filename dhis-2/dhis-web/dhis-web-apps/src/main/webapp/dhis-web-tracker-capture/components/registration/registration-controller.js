/* global trackerCapture, angular */

trackerCapture.controller('RegistrationController', 
        function($rootScope,
                $scope,
                $location,
                $timeout,
                $modal,
                AttributesFactory,
                DHIS2EventFactory,
                TEService,
                CustomFormService,
                EnrollmentService,
                DialogService,
                CurrentSelection,
                MetaDataFactory,
                EventUtils,
                RegistrationService,
                DateUtils,
                SessionStorageService,
                TrackerRulesFactory,
                TrackerRulesExecutionService) {
    
    $scope.today = DateUtils.getToday();
    $scope.trackedEntityForm = null;
    $scope.customForm = null;    
    $scope.selectedTei = {};
    $scope.tei = {};
    $scope.registrationMode = 'REGISTRATION';    
    $scope.hiddenFields = {};
    $scope.errorMessages = {};
    $scope.warningMessages = {};
    
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
        MetaDataFactory.getAll('optionSets').then(function(optionSets){
            angular.forEach(optionSets, function(optionSet){                        
                $scope.optionSets[optionSet.id] = optionSet;
            });

            CurrentSelection.setOptionSets($scope.optionSets);
        });
    }
    
    $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
    $scope.selectedEnrollment = {dateOfEnrollment: $scope.today, dateOfIncident: $scope.today, orgUnitName: $scope.selectedOrgUnit.name};   
            
    $scope.trackedEntities = {available: []};
    TEService.getAll().then(function(entities){
        $scope.trackedEntities.available = entities;   
        $scope.trackedEntities.selected = $scope.trackedEntities.available[0];
    });

    //watch for selection of program
    $scope.$watch('selectedProgram', function() {        
        $scope.trackedEntityForm = null;
        $scope.customForm = null;
        
        $scope.allProgramRules = [];
        if( angular.isObject($scope.selectedProgram) && $scope.selectedProgram.id ){
			TrackerRulesFactory.getRules($scope.selectedProgram.id).then(function(rules){                    
				$scope.allProgramRules = rules;
			});
		}        
        
        if($scope.registrationMode === 'REGISTRATION'){
            $scope.getAttributes($scope.registrationMode);
        }        
    }); 
    
    //listen to modes of registration
    $scope.$on('registrationWidget', function(event, args){
        $scope.selectedTei = {};
        $scope.tei = {};
        $scope.registrationMode = args.registrationMode;
        $scope.getAttributes($scope.registrationMode);
        
        if($scope.registrationMode !== 'REGISTRATION'){
            $scope.selectedTei = args.selectedTei;            
            $scope.tei = angular.copy(args.selectedTei);            
        }
        
        if($scope.registrationMode === 'PROFILE'){
            $scope.selectedEnrollment = args.enrollment;
        }
    });
        
    $scope.getAttributes = function(_mode){        
        var mode = _mode ? _mode : 'ENROLLMENT';
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){            
            $scope.attributes = atts;
            $scope.customFormExists = false;
            if($scope.selectedProgram && $scope.selectedProgram.id && $scope.selectedProgram.dataEntryForm && $scope.selectedProgram.dataEntryForm.htmlCode){
                $scope.customFormExists = true;
                $scope.trackedEntityForm = $scope.selectedProgram.dataEntryForm;  
                $scope.trackedEntityForm.attributes = $scope.attributes;
                $scope.trackedEntityForm.selectIncidentDatesInFuture = $scope.selectedProgram.selectIncidentDatesInFuture;
                $scope.trackedEntityForm.selectEnrollmentDatesInFuture = $scope.selectedProgram.selectEnrollmentDatesInFuture;
                $scope.trackedEntityForm.displayIncidentDate = $scope.selectedProgram.displayIncidentDate;
                $scope.customForm = CustomFormService.getForTrackedEntity($scope.trackedEntityForm, mode);
            }
        });
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
    
    var reloadProfileWidget = function(){
        var selections = CurrentSelection.get();
        CurrentSelection.set({tei: $scope.selectedTei, te: $scope.selectedTei.trackedEntity, prs: selections.prs, pr: $scope.selectedProgram, prNames: selections.prNames, prStNames: selections.prStNames, enrollments: selections.enrollments, selectedEnrollment: $scope.selectedEnrollment, optionSets: selections.optionSets});        
        $timeout(function() { 
            $rootScope.$broadcast('profileWidget', {});            
        }, 100);
    };
    
    var notifyRegistrtaionCompletion = function(destination, teiId){
        goToDashboard( destination ? destination : 'DASHBOARD', teiId );
    };
    
    var performRegistration = function(destination){
        
        RegistrationService.registerOrUpdate($scope.tei, $scope.optionSets, $scope.attributesById).then(function(registrationResponse){
            var reg = registrationResponse.response ? registrationResponse.response : {};            
            if(reg.reference && reg.status === 'SUCCESS'){                
                $scope.tei.trackedEntityInstance = reg.reference;
                
                if( $scope.registrationMode === 'PROFILE' ){                    
                    reloadProfileWidget();
                }
                else{
                    if( $scope.selectedProgram ){
                        //enroll TEI
                        var enrollment = {};
                        enrollment.trackedEntityInstance = $scope.tei.trackedEntityInstance;
                        enrollment.program = $scope.selectedProgram.id;
                        enrollment.status = 'ACTIVE';
                        enrollment.orgUnit = $scope.selectedOrgUnit.id;
                        enrollment.dateOfEnrollment = $scope.selectedEnrollment.dateOfEnrollment;
                        enrollment.dateOfIncident = $scope.selectedEnrollment.dateOfIncident === '' ? $scope.selectedEnrollment.dateOfEnrollment : $scope.selectedEnrollment.dateOfIncident;

                        EnrollmentService.enroll(enrollment).then(function(enrollmentResponse){
                            var en = enrollmentResponse.response && enrollmentResponse.response.importSummaries && enrollmentResponse.response.importSummaries[0] ? enrollmentResponse.response.importSummaries[0] : {};
                            if(en.reference && en.status === 'SUCCESS'){                                
                                enrollment.enrollment = en.reference;
                                $scope.selectedEnrollment = enrollment;                                                                
                                var dhis2Events = EventUtils.autoGenerateEvents($scope.tei.trackedEntityInstance, $scope.selectedProgram, $scope.selectedOrgUnit, enrollment);
                                if(dhis2Events.events.length > 0){
                                    DHIS2EventFactory.create(dhis2Events).then(function(){
                                        notifyRegistrtaionCompletion(destination, $scope.tei.trackedEntityInstance);
                                    });
                                }else{
                                    notifyRegistrtaionCompletion(destination, $scope.tei.trackedEntityInstance);
                                }                                
                            }
                            else{
                                //enrollment has failed
                                var dialogOptions = {
                                        headerText: 'enrollment_error',
                                        bodyText: enrollmentResponse.message
                                    };
                                DialogService.showDialog({}, dialogOptions);
                                return;                                                            
                            }
                        });
                    }
                    else{
                       notifyRegistrtaionCompletion(destination, $scope.tei.trackedEntityInstance); 
                    }
                }                
            }
            else{//update/registration has failed
                var dialogOptions = {
                        headerText: $scope.tei && $scope.tei.trackedEntityInstance ? 'update_error' : 'registration_error',
                        bodyText: registrationResponse.message
                    };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
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
        if(!$scope.selectedTei.trackedEntityInstance){
            $scope.selectedTei.trackedEntity = $scope.tei.trackedEntity = $scope.selectedProgram && $scope.selectedProgram.trackedEntity && $scope.selectedProgram.trackedEntity.id ? $scope.selectedProgram.trackedEntity.id : $scope.trackedEntities.selected.id;
            $scope.selectedTei.orgUnit = $scope.tei.orgUnit = $scope.selectedOrgUnit.id;
            $scope.selectedTei.attributes = $scope.selectedTei.attributes = [];
        }
        
        //get tei attributes and their values
        //but there could be a case where attributes are non-mandatory and
        //registration form comes empty, in this case enforce at least one value        
        
        var result = RegistrationService.processForm($scope.tei, $scope.selectedTei, $scope.attributesById, $scope.selectedProgram);
        $scope.formEmpty = result.formEmpty;
        $scope.tei = result.tei;
        
        if($scope.formEmpty){//registration form is empty
            return false;
        }
        
        if(!result.validation.valid){//validation exists           
            var modalInstance = $modal.open({
                templateUrl: 'components/registration/validation-message.html',
                controller: 'ValidationMessageController',
                resolve: {
                    validation: function () {
                        return result.validation;
                    }
                }
            });

            modalInstance.result.then(function (res) {
                if(!res) {//strict validation
                    return false;
                }
                else{//not-strict validation
                    performRegistration(destination);
                }
            }, function () {
            });        
        }
        else{//no validation
            performRegistration(destination);
        }        
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
    
    var processRuleEffect = function(){

        angular.forEach($rootScope.ruleeffects['registration'], function (effect) {
            if (effect.trackedEntityAttribute) {
                //in the data entry controller we only care about the "hidefield", showerror and showwarning actions
                if (effect.action === "HIDEFIELD") {
                    if (effect.trackedEntityAttribute) {
                        if (effect.ineffect && $scope.selectedTei[effect.trackedEntityAttribute.id]) {
                            //If a field is going to be hidden, but contains a value, we need to take action;
                            if (effect.content) {
                                //TODO: Alerts is going to be replaced with a proper display mecanism.
                                alert(effect.content);
                            }
                            else {
                                //TODO: Alerts is going to be replaced with a proper display mecanism.
                                alert($scope.attributesById[effect.trackedEntityAttribute.id].name + "Was blanked out and hidden by your last action");
                            }

                            //Blank out the value:
                            $scope.selectedTei[effect.trackedEntityAttribute.id] = "";
                        }

                        $scope.hiddenFields[effect.trackedEntityAttribute.id] = effect.ineffect;
                    }
                    else {
                        $log.warn("ProgramRuleAction " + effect.id + " is of type HIDEFIELD, bot does not have an attribute defined");
                    }
                } else if (effect.action === "SHOWERROR") {
                    if (effect.trackedEntityAttribute) {
                        
                        if(effect.ineffect) {
                            $scope.errorMessages[effect.trackedEntityAttribute.id] = effect.content;
                        } else {
                            $scope.errorMessages[effect.trackedEntityAttribute.id] = false;
                        }
                    }
                    else {
                        $log.warn("ProgramRuleAction " + effect.id + " is of type HIDEFIELD, bot does not have an attribute defined");
                    }
                } else if (effect.action === "SHOWWARNING") {
                    if (effect.trackedEntityAttribute) {
                        if(effect.ineffect) {
                            $scope.warningMessages[effect.trackedEntityAttribute.id] = effect.content;
                        } else {
                            $scope.warningMessages[effect.trackedEntityAttribute.id] = false;
                        }
                    }
                    else {
                        $log.warn("ProgramRuleAction " + effect.id + " is of type HIDEFIELD, bot does not have an attribute defined");
                    }
                }
            }
        });
    };
    
    $scope.executeRules = function () {   
        var flag = {debug: true, verbose: false};
        
        //repopulate attributes with updated values
        $scope.selectedTei.attributes = [];
        
        angular.forEach($scope.attributes, function(metaAttribute){
            var newAttributeInArray = {attribute:metaAttribute.id,
                code:metaAttribute.code,
                displayName:metaAttribute.displayName,
                type:metaAttribute.valueType
            };
            if($scope.selectedTei[newAttributeInArray.attribute]){
                newAttributeInArray.value = $scope.selectedTei[newAttributeInArray.attribute];
            }
            
           $scope.selectedTei.attributes.push(newAttributeInArray);
        });
        TrackerRulesExecutionService.executeRules($scope.allProgramRules, 'registration', null, null, $scope.selectedTei, $scope.selectedEnrollment, flag);

    };
    
    //check if field is hidden
    $scope.isHidden = function (id) {
        //In case the field contains a value, we cant hide it. 
        //If we hid a field with a value, it would falsely seem the user was aware that the value was entered in the UI.        
        return $scope.selectedTei[id] ? false : $scope.hiddenFields[id];
    };
    
    $scope.teiValueUpdated = function(tei, field){
        $scope.executeRules();
    };
    
    //listen for rule effect changes
    $scope.$on('ruleeffectsupdated', function (event, args) {
        processRuleEffect(args.event);
    });


    $scope.interacted = function(field) {
        var status = false;
        if(field){            
            status = $scope.outerForm.submitted || field.$dirty;
        }
        return status;        
    };
})

.controller('ValidationMessageController',
        function ($scope,
                $modalInstance,                
                validation) {
                    
    $scope.validationResult = validation;

    $scope.proceed = function () {
        $modalInstance.close(true);
    };

    $scope.cancel = function () {
        $modalInstance.close(false);
    };
});

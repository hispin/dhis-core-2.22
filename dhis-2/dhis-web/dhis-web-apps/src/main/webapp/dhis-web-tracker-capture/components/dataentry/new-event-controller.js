
/* global trackerCapture, angular */

trackerCapture.controller('EventCreationController',
        function ($scope,
                $modalInstance,
                $timeout,
                DateUtils,
                DHIS2EventFactory,
                OrgUnitFactory,
                DialogService,
                EventCreationService,
                eventsByStage,
                stage,
                stages,
                tei,
                program,
                orgUnit,
                enrollment,                
                eventCreationAction,
                autoCreate,
                EventUtils) {
    $scope.stages = stages;
    $scope.eventCreationAction = eventCreationAction;
    $scope.eventCreationActions = EventCreationService.eventCreationActions;
    $scope.isNewEvent = (eventCreationAction === $scope.eventCreationActions.add);
    $scope.isScheduleEvent = (eventCreationAction === $scope.eventCreationActions.schedule || eventCreationAction === $scope.eventCreationActions.referral);
    $scope.isReferralEvent = (eventCreationAction === $scope.eventCreationActions.referral);
    $scope.model = {selectedStage: stage, dueDateInvalid: false, eventDateInvalid: false};
    var orgPath = [];    
    var dummyEvent = {};
    
    function prepareEvent(){
        
        dummyEvent = EventUtils.createDummyEvent(eventsByStage[stage.id], tei, program, stage, orgUnit, enrollment);
        
        $scope.newEvent = {programStage: stage};
        $scope.dhis2Event = {eventDate: $scope.isScheduleEvent ? '' : DateUtils.getToday(), dueDate: dummyEvent.dueDate, excecutionDateLabel : dummyEvent.excecutionDateLabel, name: dummyEvent.name, invalid: true};

        //custom code for folkehelsa. Set empty eventDate if selectedStage is previous pregnancies
        if($scope.model.selectedStage.id === 'PUZaKR0Jh2k'){
            $scope.dhis2Event.eventDate = '';
        }

        if ($scope.model.selectedStage.periodType) {
            $scope.dhis2Event.eventDate = dummyEvent.dueDate;
            $scope.dhis2Event.periodName = dummyEvent.periodName;
            $scope.dhis2Event.periods = dummyEvent.periods;
            $scope.dhis2Event.selectedPeriod = dummyEvent.periods[0];
        }
        
        orgPath.push(dummyEvent.orgUnit);
    };
    
    if($scope.model.selectedStage){
        prepareEvent();
    }
    
    $scope.$watch('model.selectedStage', function(){       
        if(angular.isObject($scope.model.selectedStage)){
            stage = $scope.model.selectedStage;
            prepareEvent();
        }
    });

    //watch for changes in due/event-date
    $scope.$watchCollection('[dhis2Event.dueDate, dhis2Event.eventDate]', function () {
        if (angular.isObject($scope.dhis2Event)) {
            if (!$scope.dhis2Event.dueDate) {
                $scope.model.dueDateInvalid = true;
                return;
            }

            if ($scope.dhis2Event.dueDate) {
                var rDueDate = $scope.dhis2Event.dueDate;
                var cDueDate = DateUtils.format($scope.dhis2Event.dueDate);
                $scope.model.dueDateInvalid = rDueDate !== cDueDate;
            }

            if ($scope.dhis2Event.eventDate) {
                var rEventDate = $scope.dhis2Event.eventDate;
                var cEventDate = DateUtils.format($scope.dhis2Event.eventDate);
                $scope.model.eventDateInvalid = rEventDate !== cEventDate;
            }
        }
    });

    $scope.save = function () {
        //check for form validity
        if ($scope.model.dueDateInvalid || $scope.model.eventDateInvalid) {
            return false;
        }
        if($scope.isReferralEvent && !$scope.selectedSearchingOrgUnit){
            $scope.orgUnitError = true;
            return false;
        }
        $scope.orgUnitError =  false;
        
        if ($scope.model.selectedStage.periodType) {
            $scope.dhis2Event.eventDate = $scope.dhis2Event.selectedPeriod.endDate;
            $scope.dhis2Event.dueDate = $scope.dhis2Event.selectedPeriod.endDate;
        }
        
        var eventDate = DateUtils.formatFromUserToApi($scope.dhis2Event.eventDate);
        var dueDate = DateUtils.formatFromUserToApi($scope.dhis2Event.dueDate);
        var newEvents = {events: []};
        var newEvent = {
            trackedEntityInstance: dummyEvent.trackedEntityInstance,
            program: dummyEvent.program,
            programStage: dummyEvent.programStage,
            enrollment: dummyEvent.enrollment,
            orgUnit: dummyEvent.orgUnit,
            dueDate: dueDate,
            eventDate: eventDate,
            notes: [],
            dataValues: [],
            status: 'ACTIVE'
        };

        newEvent.status = newEvent.eventDate ? 'ACTIVE' : 'SCHEDULE';

        newEvents.events.push(newEvent);
        DHIS2EventFactory.create(newEvents).then(function (response) {
            if (response.response && response.response.importSummaries[0].status === 'SUCCESS') {
                newEvent.event = response.response.importSummaries[0].reference;
                $modalInstance.close(newEvent);
            }
            else {
                var dialogOptions = {
                    headerText: 'event_creation_error',
                    bodyText: response.message
                };

                DialogService.showDialog({}, dialogOptions);
            }
        });
    };

    //Start referral logic
    $scope.setSelectedSearchingOrgUnit = function(orgUnit){
        $scope.selectedSearchingOrgUnit = orgUnit;
        dummyEvent.orgUnit = orgUnit.id;
        dummyEvent.orgUnitName = orgUnit.name;
    };
    
    
    function initOrgUnits(uid){
        $scope.orgUnitsLoading =true;
        $timeout(function(){
            OrgUnitFactory.getWithParents(uid).then(function(ou){
                if(ou.organisationUnits && ou.organisationUnits[0] && ou.organisationUnits[0].parent){
                    orgPath.push(ou.organisationUnits[0].parent.id);
                    var parent = ou.organisationUnits[0].parent;
                    var lastId = ou.organisationUnits[0].id;
                    while(parent){
                        orgPath.push(parent.id);
                        lastId = parent.id;
                        parent = parent.parent;
                    }
                    initOrgUnits(lastId);
                }else{
                    OrgUnitFactory.getSearchTreeRoot().then(function(response) {
                        $scope.orgUnits = response.organisationUnits;
                        angular.forEach($scope.orgUnits, function(ou){
                            ou.show = true;
                            angular.forEach(ou.children, function(o){
                                o.hasChildren = o.children && o.children.length > 0 ? true : false;
                                initExpand(o);
                            });
                        });
                    });
                }
            });
        },150);
        
    };
    
    function initExpand(orgUnit){
        if(orgPath.indexOf(orgUnit.id)>-1){
            if(orgUnit.hasChildren){
                //Get children for the selected orgUnit
                OrgUnitFactory.get(orgUnit.id).then(function(ou) {     
                    orgUnit.show = true;
                    orgUnit.hasChildren = false;
                    orgUnit.children = ou.organisationUnits[0].children;
                    angular.forEach(orgUnit.children, function(ou){            
                        ou.hasChildren = ou.children && ou.children.length > 0 ? true : false;
                        initExpand(ou);
                    });    
                });
            }else{
                setDefaultOrgUnit();
            }
        }
    };
    
    var defaultOrgUnitGroup = 'hrc';
    function setDefaultOrgUnit(){
        if(orgPath && orgPath.length>1){
            OrgUnitFactory.getWithGroups(orgPath[1]).then(function(ou){
                if(ou.organisationUnits && ou.organisationUnits[0]){
                    var o = ou.organisationUnits[0];
                    angular.forEach(o.children, function(oo){
                        angular.forEach(oo.organisationUnitGroups, function(oug){
                            var shortNameLC = oug.shortName.toLowerCase();
                            if(shortNameLC === defaultOrgUnitGroup){
                                $scope.setSelectedSearchingOrgUnit(oo);
                            }
                        });
                    });

                }
                $scope.orgUnitsLoading = false;
            });   
        }else{
            $scope.orgUnitsLoading = false;
        }
    };
    
    if($scope.isReferralEvent){
        initOrgUnits(orgPath[0]);
    }
           
    $scope.expandCollapse = function(orgUnit) {
        if( orgUnit.hasChildren ){            
            //Get children for the selected orgUnit
            OrgUnitFactory.get(orgUnit.id).then(function(ou) {                
                orgUnit.show = !orgUnit.show;
                orgUnit.hasChildren = false;
                orgUnit.children = ou.organisationUnits[0].children;                
                angular.forEach(orgUnit.children, function(ou){                    
                    ou.hasChildren = ou.children && ou.children.length > 0 ? true : false;
                });                
            });           
        }
        else{
            orgUnit.show = !orgUnit.show;   
        }
    };
    

    /*if($scope.isReferralEvent){
        OrgUnitFactory.getSearchTreeRoot().then(function(response) {
            $scope.orgUnits = response.organisationUnits;
            angular.forEach($scope.orgUnits, function(ou){
                ou.show = true;
                angular.forEach(ou.children, function(o){
                    o.hasChildren = o.children && o.children.length > 0 ? true : false;
                });
            });
        });
    }*/
    //end referral logic
    
    
    //If the caller wants to create right away, go ahead and save.
    if (autoCreate) {
        $scope.save();
    };

    $scope.cancel = function () {
        $modalInstance.close();
    };
});

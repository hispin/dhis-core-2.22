trackerCapture.controller('ReportController',
        function($scope,
                CurrentSelection,
                storage,
                DateUtils,
                EventUtils,
                TEIService,
                TEIGridService,
                TranslationService,
                AttributesFactory,
                DHIS2EventFactory) {

    TranslationService.translate();
    
    $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];         
    $scope.selectedOuMode = $scope.ouModes[0];
    $scope.report = {};
    
    $scope.generateReport = function(){
        
        $scope.dataReady = false;
        
        //check for form validity
        $scope.outerForm.submitted = true;        
        if( $scope.outerForm.$invalid || !$scope.selectedProgram){
            return false;
        }
        
        $scope.programStages = [];
        angular.forEach($scope.selectedProgram.programStages, function(stage){
            $scope.programStages[stage.id] = stage;
        });
            
        AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){            
            $scope.gridColumns = TEIGridService.generateGridColumns(atts, $scope.selectedOuMode.name);      
        });  
        
        //fetch TEIs for the selected program and orgunit/mode
        TEIService.search($scope.selectedOrgUnit.id, 
                            $scope.selectedOuMode.name,
                            null,
                            'program=' + $scope.selectedProgram.id,
                            null,
                            $scope.pager,
                            false).then(function(data){
            if(data.rows){
                $scope.teiCount = data.rows.length;
                $scope.dataReady = true;
            }
            
            
            //process tei grid
            $scope.teiList = TEIGridService.format(data);          
            
            DHIS2EventFactory.getByOrgUnitAndProgram($scope.selectedOrgUnit.id, $scope.selectedOuMode.name, $scope.selectedProgram.id).then(function(eventList){
                $scope.dhis2Events = [];
                angular.forEach(eventList, function(ev){
                    if(ev.trackedEntityInstance){
                        ev.name = $scope.programStages[ev.programStage].name;
                        ev.statusColor = EventUtils.getEventStatusColor(ev); 
                        ev.eventDate = DateUtils.format(ev.eventDate);
                        
                        if($scope.dhis2Events[ev.trackedEntityInstance]){
                            $scope.dhis2Events[ev.trackedEntityInstance].push(ev);
                        }
                        else{
                            $scope.dhis2Events[ev.trackedEntityInstance] = [ev];
                        }
                        ev = EventUtils.setEventOrgUnitName(ev);
                    }
                });
            });
        });
    };
    
    $scope.showEventDetails = function(dhis2Event){
        console.log('I will show details of...:  ', dhis2Event);
    };    
})

//conroller for tei report
.controller('TeiReportController',
        function($scope,
                CurrentSelection,
                storage,
                DateUtils,
                EventUtils,
                TranslationService,
                DHIS2EventFactory) {

    TranslationService.translate();
    
    $scope.$on('dashboard', function(event, args) {
        var selections = CurrentSelection.get();
        $scope.selectedOrgUnit = storage.get('SELECTED_OU');
        $scope.selectedEntity = selections.tei;      
        $scope.selectedProgram = selections.pr;        
        $scope.selectedEnrollment = selections.enrollment; 
        
        if($scope.selectedOrgUnit && 
                $scope.selectedProgram && 
                $scope.selectedEntity && 
                $scope.selectedEnrollment){
            
            $scope.getEvents();
        }       
    });
    
    $scope.getEvents = function(){
        
        $scope.dhis2Events = '';
        $scope.dataExists = false;
        DHIS2EventFactory.getEventsByProgram($scope.selectedEntity.trackedEntityInstance, $scope.selectedOrgUnit.id, $scope.selectedProgram.id).then(function(data){
            $scope.dhis2Events = data;     
            if(!angular.isUndefined($scope.dhis2Events) ){                
                for(var i=0; i<$scope.dhis2Events.length && !$scope.dataExists; i++){
                    if(!angular.isUndefined($scope.dhis2Events[i].dataValues)){
                        $scope.dataExists = true;
                    }                    
                }                    
            }
        });          
    };
});
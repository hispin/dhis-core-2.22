trackerCapture.controller('OverdueEventsController',
         function($scope,
                $modal,
                $location,
                $translate,
                orderByFilter,
                DateUtils,
                Paginator,
                EventReportService,
                TEIGridService,
                AttributesFactory,
                ProgramFactory,
                CurrentSelection,
                OptionSetService,
                storage) {    
    $scope.today = DateUtils.getToday();
    
    $scope.selectedOuMode = 'SELECTED';
    $scope.report = {};
    $scope.displayMode = {};
    $scope.printMode = false;
    
    //get optionsets
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
    
    //get attributes
    $scope.attributesById = CurrentSelection.getAttributesById();
    if(!$scope.attributesById){
        AttributesFactory.getAll().then(function(atts){
            $scope.attributes = [];  
            $scope.attributesById = [];
            angular.forEach(atts, function(att){
                $scope.attributesById[att.id] = att;
            });
            CurrentSelection.setAttributesById($scope.attributesById);
        });
    }    

    //Paging
    $scope.pager = {pageSize: 50, page: 1, toolBarDisplay: 5};   
    
    //watch for selection of org unit from tree
    $scope.$watch('selectedOrgUnit', function() {
        $scope.reportFinished = false;
        $scope.reportStarted = false;
        $scope.selectedProgram = null;
        if( angular.isObject($scope.selectedOrgUnit)){            
            storage.set('SELECTED_OU', $scope.selectedOrgUnit);            
            $scope.loadPrograms($scope.selectedOrgUnit);
        }
    });
    
    //load programs associated with the selected org unit.
    $scope.loadPrograms = function(orgUnit) {        
        $scope.selectedOrgUnit = orgUnit;        
        if (angular.isObject($scope.selectedOrgUnit)){
            ProgramFactory.getProgramsByOu($scope.selectedOrgUnit, $scope.selectedProgram).then(function(response){
                $scope.programs = response.programs;
                $scope.selectedProgram = response.selectedProgram;
            });
        }
    };
    
    //watch for selection of program
    $scope.$watch('selectedProgram', function() {   
        $scope.reportFinished = false;
        $scope.reportStarted = false;        
        if (angular.isObject($scope.selectedProgram)){
            $scope.generateGridHeader();
            $scope.generateReport();
        }
    });
    
    //watch for selection of ouMode
    $scope.$watch('selectedOuMode', function() {   
        $scope.reportFinished = false;
        $scope.reportStarted = false;
        if (angular.isObject($scope.selectedProgram)){
            $scope.generateGridHeader();
            $scope.generateReport();
        }
    });    
    
    $scope.generateReport = function(){
        
        if($scope.selectedProgram && $scope.selectedOuMode){
            
            $scope.reportFinished = false;
            $scope.reportStarted = true;            
            $scope.overdueEvents = [];
            
            EventReportService.getEventReport($scope.selectedOrgUnit.id, $scope.selectedOuMode, $scope.selectedProgram.id, null, null, 'ACTIVE','OVERDUE', $scope.pager).then(function(data){                     
                
                if( data.pager ){
                    $scope.pager = data.pager;
                    $scope.pager.toolBarDisplay = 5;

                    Paginator.setPage($scope.pager.page);
                    Paginator.setPageCount($scope.pager.pageCount);
                    Paginator.setPageSize($scope.pager.pageSize);
                    Paginator.setItemCount($scope.pager.total);                    
                }
                    
                angular.forEach(data.eventRows, function(row){
                    var overdueEvent = {};                    
                    angular.forEach(row.attributes, function(att){
                        var val = AttributesFactory.formatAttributeValue(att, $scope.attributesById, $scope.optionSets, 'USER');                        
                        overdueEvent[att.attribute] = val;                        
                    });
                    
                    overdueEvent.dueDate = DateUtils.formatFromApiToUser(row.dueDate);
                    overdueEvent.event = row.event;
                    overdueEvent.eventName = $scope.programStages[row.programStage].name;
                    overdueEvent.orgUnitName = row.eventOrgUnitName;                    
                    overdueEvent.followup = row.followup;
                    overdueEvent.program = row.program;
                    overdueEvent.programStage = row.programStage;
                    overdueEvent.trackedEntityInstance = row.trackedEntityInstance;
                    overdueEvent.created = DateUtils.formatFromApiToUser(row.registrationDate);;
                    $scope.overdueEvents.push(overdueEvent);
                    
                });
                
                //sort overdue events by their due dates - this is default
                $scope.overdueEvents = orderByFilter($scope.overdueEvents, '-dueDate');
                $scope.overdueEvents.reverse();

                $scope.reportFinished = true;
                $scope.reportStarted = false;
            });
        }
    };    
    
    $scope.generateGridHeader = function(){
        
        if (angular.isObject($scope.selectedProgram)){
            
            $scope.programStages = [];
            $scope.filterTypes = {};
            $scope.filterText = {};

            angular.forEach($scope.selectedProgram.programStages, function(stage){
                $scope.programStages[stage.id] = stage;
            });

            AttributesFactory.getByProgram($scope.selectedProgram).then(function(atts){            
                var grid = TEIGridService.generateGridColumns(atts, $scope.selectedOuMode);
                $scope.gridColumns = grid.columns;

                $scope.gridColumns.push({name: $translate('event_orgunit_name'), id: 'orgUnitName', type: 'string', displayInListNoProgram: false, showFilter: false, show: true});
                $scope.filterTypes['orgUnitName'] = 'string';
                $scope.gridColumns.push({name: $translate('event_name'), id: 'eventName', type: 'string', displayInListNoProgram: false, showFilter: false, show: true});
                $scope.filterTypes['eventName'] = 'string';
                $scope.gridColumns.push({name: $translate('due_date'), id: 'dueDate', type: 'date', displayInListNoProgram: false, showFilter: false, show: true});
                $scope.filterTypes['dueDate'] = 'date';
                $scope.filterText['dueDate']= {};                
            });
            
        }      
    };
    
    $scope.showHideColumns = function(){
        
        $scope.hiddenGridColumns = 0;
        
        angular.forEach($scope.gridColumns, function(gridColumn){
            if(!gridColumn.show){
                $scope.hiddenGridColumns++;
            }
        });
        
        var modalInstance = $modal.open({
            templateUrl: 'views/column-modal.html',
            controller: 'ColumnDisplayController',
            resolve: {
                gridColumns: function () {
                    return $scope.gridColumns;
                },
                hiddenGridColumns: function(){
                    return $scope.hiddenGridColumns;
                }
            }
        });

        modalInstance.result.then(function (gridColumns) {
            $scope.gridColumns = gridColumns;
        }, function () {
        });
    };
    
    $scope.sortGrid = function(gridHeader){
        if ($scope.sortHeader === gridHeader.id){
            $scope.reverse = !$scope.reverse;
            return;
        }        
        $scope.sortHeader = gridHeader.id;
        $scope.reverse = false;
        
        $scope.overdueEvents = orderByFilter($scope.overdueEvents, $scope.sortHeader);
        
        if($scope.reverse){
            $scope.overdueEvents.reverse();
        }
    };
    
    $scope.searchInGrid = function(gridColumn){
        
        $scope.currentFilter = gridColumn;
       
        for(var i=0; i<$scope.gridColumns.length; i++){
            
            //toggle the selected grid column's filter
            if($scope.gridColumns[i].id === gridColumn.id){
                $scope.gridColumns[i].showFilter = !$scope.gridColumns[i].showFilter;
            }            
            else{
                $scope.gridColumns[i].showFilter = false;
            }
        }
    };    
    
    $scope.removeStartFilterText = function(gridColumnId){
        $scope.filterText[gridColumnId].start = undefined;
    };
    
    $scope.removeEndFilterText = function(gridColumnId){
        $scope.filterText[gridColumnId].end = undefined;
    };
    
    $scope.showDashboard = function(tei){
        $location.path('/dashboard').search({tei: tei,                                            
                                            program: $scope.selectedProgram ? $scope.selectedProgram.id: null});
    };
    
    $scope.generateReportData = function(){
        return TEIGridService.getData($scope.overdueEvents, $scope.gridColumns);
    };
    
    $scope.generateReportHeader = function(){
        return TEIGridService.getHeader($scope.gridColumns);
    };
    
    $scope.jumpToPage = function(){
        $scope.generateReport();
    };
    
    $scope.resetPageSize = function(){
        $scope.pager.page = 1;        
        $scope.generateReport();
    };
    
    $scope.getPage = function(page){    
        $scope.pager.page = page;
        $scope.generateReport();
    };
});
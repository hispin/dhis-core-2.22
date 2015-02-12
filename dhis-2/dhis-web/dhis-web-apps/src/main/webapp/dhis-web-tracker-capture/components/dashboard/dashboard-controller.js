//Controller for dashboard
trackerCapture.controller('DashboardController',
        function($rootScope,
                $scope,
                $location,
                $route,
                $modal,
                $timeout,
                $filter,
                orderByFilter,
                storage,
                TEIService, 
                TEService,
                OptionSetService,
                EnrollmentService,
                ProgramFactory,
                DashboardLayoutService,
                CurrentSelection) {
    //selections
    $scope.selectedTeiId = ($location.search()).tei; 
    $scope.selectedProgramId = ($location.search()).program; 
    $scope.selectedOrgUnit = storage.get('SELECTED_OU');
    $scope.selectedProgram;    
    $scope.selectedTei;
    
    //dashboard items   
    var getDashboardLayout = function(){        
        $rootScope.dashboardWidgets = [];    
        $scope.widgetsChanged = [];
        $scope.dashboardStatus = [];
        $scope.dashboardWidgetsOrder = {biggerWidgets: [], smallerWidgets: []};
        $scope.orderChanged = false;
        
        DashboardLayoutService.get().then(function(response){
            $scope.dashboardLayouts = response;
            
            var selectedLayout = $scope.dashboardLayouts ['DEFAULT'];            
            if($scope.selectedProgram && $scope.selectedProgram.id){
                selectedLayout = $scope.dashboardLayouts [$scope.selectedProgram.id] ? $scope.dashboardLayouts [$scope.selectedProgram.id] : selectedLayout;
            }

            for(var widget in selectedLayout.widgets){
                switch(selectedLayout.widgets[widget].title){
                    case 'enrollment':
                        $rootScope.enrollmentWidget = selectedLayout.widgets[widget];
                        $rootScope.dashboardWidgets.push($rootScope.enrollmentWidget);
                        $scope.dashboardStatus[widget] = selectedLayout.widgets[widget];
                        break;
                    case 'dataentry':
                        $rootScope.dataentryWidget = selectedLayout.widgets[widget];
                        $rootScope.dashboardWidgets.push($rootScope.dataentryWidget);
                        $scope.dashboardStatus[widget] = selectedLayout.widgets[widget];
                        break;
                    case 'report':
                        $rootScope.reportWidget = selectedLayout.widgets[widget];
                        $rootScope.dashboardWidgets.push($rootScope.reportWidget);
                        $scope.dashboardStatus[widget] = selectedLayout.widgets[widget];
                        break;
                    case 'current_selections':
                        $rootScope.selectedWidget = selectedLayout.widgets[widget];
                        $rootScope.dashboardWidgets.push($rootScope.selectedWidget);
                        $scope.dashboardStatus[widget] = selectedLayout.widgets[widget];
                        break;
                    case 'profile':
                        $rootScope.profileWidget = selectedLayout.widgets[widget];
                        $rootScope.dashboardWidgets.push($rootScope.profileWidget);
                        $scope.dashboardStatus[widget] = selectedLayout.widgets[widget];
                        break;
                    case 'relationships':
                        $rootScope.relationshipWidget = selectedLayout.widgets[widget];
                        $rootScope.dashboardWidgets.push($rootScope.relationshipWidget);
                        $scope.dashboardStatus[widget] = selectedLayout.widgets[widget];
                        break;    
                    case 'notes':
                        $rootScope.notesWidget = selectedLayout.widgets[widget];
                        $rootScope.dashboardWidgets.push($rootScope.notesWidget);
                        $scope.dashboardStatus[widget] = selectedLayout.widgets[widget];
                        break;    
                }
            }

            angular.forEach(orderByFilter($filter('filter')($scope.dashboardWidgets, {parent: "biggerWidget"}), 'order'), function(w){
                $scope.dashboardWidgetsOrder.biggerWidgets.push(w.title);
            });

            angular.forEach(orderByFilter($filter('filter')($scope.dashboardWidgets, {parent: "smallerWidget"}), 'order'), function(w){
                $scope.dashboardWidgetsOrder.smallerWidgets.push(w.title);
            });
        });        
    };
    
    if($scope.selectedTeiId){
        
        //get option sets
        $scope.optionSets = [];
        OptionSetService.getAll().then(function(optionSets){
            
            angular.forEach(optionSets, function(optionSet){                            
                $scope.optionSets[optionSet.id] = optionSet;
            });
        
            //Fetch the selected entity
            TEIService.get($scope.selectedTeiId, $scope.optionSets).then(function(response){
                $scope.selectedTei = response.data;

                //get the entity type
                TEService.get($scope.selectedTei.trackedEntity).then(function(te){                    
                    $scope.trackedEntity = te;

                    //get enrollments for the selected tei
                    EnrollmentService.getByEntity($scope.selectedTeiId).then(function(response){                    

                        var selectedEnrollment = null;
                        if(angular.isObject(response) && response.enrollments && response.enrollments.length === 1 && response.enrollments[0].status === 'ACTIVE'){
                            selectedEnrollment = response.enrollments[0];
                        }
                        
                        ProgramFactory.getAll().then(function(programs){
                            $scope.programs = [];

                            $scope.programNames = [];  
                            $scope.programStageNames = [];                            
                            //get programs valid for the selected ou and tei
                            angular.forEach(programs, function(program){
                                if(program.organisationUnits.hasOwnProperty($scope.selectedOrgUnit.id) &&
                                   program.trackedEntity.id === $scope.selectedTei.trackedEntity){
                                    $scope.programs.push(program);
                                    $scope.programNames[program.id] = {id: program.id, name: program.name};
                                    angular.forEach(program.programStages, function(stage){                
                                        $scope.programStageNames[stage.id] = {id: stage.id, name: stage.name};
                                    });
                                }

                                if($scope.selectedProgramId && program.id === $scope.selectedProgramId || selectedEnrollment && selectedEnrollment.program === program.id){
                                    $scope.selectedProgram = program;
                                }
                            });
                            
                            getDashboardLayout();                            

                            //broadcast selected items for dashboard controllers
                            CurrentSelection.set({tei: $scope.selectedTei, te: $scope.trackedEntity, prs: $scope.programs, pr: $scope.selectedProgram, prNames: $scope.programNames, prStNames: $scope.programStageNames, enrollments: response.enrollments, selectedEnrollment: selectedEnrollment, optionSets: $scope.optionSets});
                            $scope.broadCastSelections();                        
                        });
                    });
                });            
            });    
        });
    }    
    
    //listen for any change to program selection
    //it is possible that such could happen during enrollment.
    $scope.$on('mainDashboard', function(event, args) {
        var selections = CurrentSelection.get();
        $scope.selectedProgram = null;
        angular.forEach($scope.programs, function(pr){
            if(pr.id === selections.pr){
                $scope.selectedProgram = pr;
            }
        });
    }); 
    
    //watch for widget sorting    
    $scope.$watch('widgetsOrder', function() {        
        if(angular.isObject($scope.widgetsOrder)){
            $scope.orderChanged = false;
            for(var i=0; i<$scope.widgetsOrder.smallerWidgets.length; i++){
                if($scope.widgetsOrder.smallerWidgets.length === $scope.dashboardWidgetsOrder.smallerWidgets.length && $scope.widgetsOrder.smallerWidgets[i] !== $scope.dashboardWidgetsOrder.smallerWidgets[i]){
                    $scope.orderChanged = true;
                }
                
                if($scope.widgetsOrder.smallerWidgets.length !== $scope.dashboardWidgetsOrder.smallerWidgets.length){
                    $scope.orderChanged = true;
                }
            }
            
            for(var i=0; i<$scope.widgetsOrder.biggerWidgets.length; i++){
                if($scope.widgetsOrder.biggerWidgets.length === $scope.dashboardWidgetsOrder.biggerWidgets.length && $scope.widgetsOrder.biggerWidgets[i] !== $scope.dashboardWidgetsOrder.biggerWidgets[i]){
                    $scope.orderChanged = true;
                }
                
                if($scope.widgetsOrder.biggerWidgets.length !== $scope.dashboardWidgetsOrder.biggerWidgets.length){
                    $scope.orderChanged = true;
                }
            }
        }
    });
    
    $scope.applySelectedProgram = function(){
        getDashboardLayout();
        $scope.broadCastSelections(); 
    };
    
    $scope.broadCastSelections = function(){
        
        var selections = CurrentSelection.get();
        $scope.selectedTei = selections.tei;
        $scope.trackedEntity = selections.te;
        $scope.optionSets = selections.optionSets;
        
        CurrentSelection.set({tei: $scope.selectedTei, te: $scope.trackedEntity, prs: $scope.programs, pr: $scope.selectedProgram, prNames: $scope.programNames, prStNames: $scope.programStageNames, enrollments: selections.enrollments, selectedEnrollment: null, optionSets: $scope.optionSets});
        $timeout(function() { 
            $rootScope.$broadcast('selectedItems', {programExists: $scope.programs.length > 0});            
        }, 100); 
    };     
    
    $scope.back = function(){
        $location.path('/').search({program: $scope.selectedProgramId});                   
    };
    
    $scope.displayEnrollment = false;
    $scope.showEnrollment = function(){
        $scope.displayEnrollment = true;
    };
    
    $scope.removeWidget = function(widget){        
        widget.show = false;
        trackWidgetStatusChange(widget);
    };
    
    $scope.expandCollapse = function(widget){
        widget.expand = !widget.expand;
        trackWidgetStatusChange(widget);
    };
    
    var trackWidgetStatusChange = function(widget){
        var w = $scope.dashboardStatus[widget.title];
        
        if(!angular.equals(w, widget) && $scope.widgetsChanged.indexOf(widget.title) === -1){
            $scope.widgetsChanged.push(widget.title);
        }        
        if(angular.equals(w, widget) && $scope.widgetsChanged.indexOf(widget.title) !== -1){
            var idx = $scope.widgetsChanged.indexOf(widget.title);
            $scope.widgetsChanged.splice(idx,1);
        }
    };
    
    $scope.saveDashboardLayout = function(){        
        var widgets = [];
        angular.forEach($rootScope.dashboardWidgets, function(widget){
            var w = angular.copy(widget);            
            if($scope.orderChanged){
                if($scope.widgetsOrder.biggerWidgets.indexOf(w.title) !== -1){
                    w.parent = 'biggerWidget';
                    w.order = $scope.widgetsOrder.biggerWidgets.indexOf(w.title);
                }
                
                if($scope.widgetsOrder.smallerWidgets.indexOf(w.title) !== -1){
                    w.parent = 'smallerWidget';
                    w.order = $scope.widgetsOrder.smallerWidgets.indexOf(w.title);
                }
            }            
            widgets.push(w);
        });
        
        //$scope.dashboardLayouts 
            
        if($scope.selectedProgram && $scope.selectedProgram.id){
            $scope.dashboardLayouts[$scope.selectedProgram.id] = {widgets: widgets, program: $scope.selectedProgram.id};
        }
        
        DashboardLayoutService.saveLayout($scope.dashboardLayouts).then(function(){            
            if($scope.selectedProgramId && $scope.selectedProgram && $scope.selectedProgramId === $scope.selectedProgram.id ||
                    !$scope.selectedProgramId && !$scope.selectedProgram){
                $route.reload();
            }
            else{
                $location.path('/dashboard').search({tei: $scope.selectedTeiId,                                            
                                            program: $scope.selectedProgram ? $scope.selectedProgram.id: null});
            }            
        });
    };
    
    $scope.showHideWidgets = function(){
        var modalInstance = $modal.open({
            templateUrl: "components/dashboard/dashboard-widgets.html",
            controller: "DashboardWidgetsController"
        });

        modalInstance.result.then(function () {
        });
    };
    
    $rootScope.closeOpenWidget = function(widget){
        trackWidgetStatusChange(widget);
    };
});

//Controller for dashboard
trackerCapture.controller('DashboardController',
        function($rootScope,
                $scope,
                $location,
                $modal,
                $timeout,
                storage,
                TEIService, 
                TEService,
                OptionSetService,
                EnrollmentService,
                ProgramFactory,
                CurrentSelection) {
    //dashboard items   
    $rootScope.dashboardWidgets = [];
    $rootScope.dashboardStatus = [];
    $scope.widgetsChanged = [];
    
    $rootScope.enrollmentWidget = {title: 'enrollment', view: "components/enrollment/enrollment.html", show: true, expand: true, parent: 'biggerWidget', order: 0};
    $rootScope.dataentryWidget = {title: 'dataentry', view: "components/dataentry/dataentry.html", show: true, expand: true, parent: 'biggerWidget', order: 1};
    $rootScope.reportWidget = {title: 'report', view: "components/report/tei-report.html", show: true, expand: true, parent: 'biggerWidget', order: 2};
    $rootScope.selectedWidget = {title: 'current_selections', view: "components/selected/selected.html", show: false, expand: true, parent: 'smallerWidget', order: 0};
    $rootScope.profileWidget = {title: 'profile', view: "components/profile/profile.html", show: true, expand: true, parent: 'smallerWidget', order: 1};
    $rootScope.relationshipWidget = {title: 'relationships', view: "components/relationship/relationship.html", show: true, expand: true, parent: 'smallerWidget', order: 2};
    $rootScope.notesWidget = {title: 'notes', view: "components/notes/notes.html", show: true, expand: true, parent: 'smallerWidget', order: 3};  
    
    $scope.dashboardStatus['enrollment'] = {title: 'enrollment', view: "components/enrollment/enrollment.html", show: true, expand: true, parent: 'biggerWidget', order: 0};
    $scope.dashboardStatus['dataentry'] = {title: 'dataentry', view: "components/dataentry/dataentry.html", show: true, expand: true, parent: 'biggerWidget', order: 1};
    $scope.dashboardStatus['report'] = {title: 'report', view: "components/report/tei-report.html", show: true, expand: true, parent: 'biggerWidget', order: 2};
    $scope.dashboardStatus['current_selections'] = {title: 'current_selections', view: "components/selected/selected.html", show: false, expand: true, parent: 'smallerWidget', order: 0};
    $scope.dashboardStatus['profile'] = {title: 'profile', view: "components/profile/profile.html", show: true, expand: true, parent: 'smallerWidget', order: 1};
    $scope.dashboardStatus['relationships'] = {title: 'relationships', view: "components/relationship/relationship.html", show: true, expand: true, parent: 'smallerWidget', order: 2};
    $scope.dashboardStatus['notes'] = {title: 'notes', view: "components/notes/notes.html", show: true, expand: true, parent: 'smallerWidget', order: 3};  
    
    $scope.dashboardWidgetsOrder = {biggerWidgets: ['enrollment', 'dataentry', 'report'], smallerWidgets: ['current_selections', 'profile', 'relationships', 'notes']};
    
    $rootScope.dashboardWidgets.push($rootScope.enrollmentWidget);
    $rootScope.dashboardWidgets.push($rootScope.dataentryWidget);
    $rootScope.dashboardWidgets.push($rootScope.reportWidget);
    $rootScope.dashboardWidgets.push($rootScope.selectedWidget);
    $rootScope.dashboardWidgets.push($rootScope.profileWidget);
    $rootScope.dashboardWidgets.push($rootScope.relationshipWidget);
    $rootScope.dashboardWidgets.push($rootScope.notesWidget);
    
    //selections
    $scope.selectedTeiId = ($location.search()).tei; 
    $scope.selectedProgramId = ($location.search()).program; 
    $scope.selectedOrgUnit = storage.get('SELECTED_OU');

    $scope.selectedProgram;    
    $scope.selectedTei;    
    
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

                            //get programs valid for the selected ou and tei
                            angular.forEach(programs, function(program){
                                if(program.organisationUnits.hasOwnProperty($scope.selectedOrgUnit.id) &&
                                   program.trackedEntity.id === $scope.selectedTei.trackedEntity){
                                    $scope.programs.push(program);
                                }

                                if($scope.selectedProgramId && program.id === $scope.selectedProgramId || selectedEnrollment && selectedEnrollment.program === program.id){
                                    $scope.selectedProgram = program;
                                }
                            }); 

                            //broadcast selected items for dashboard controllers
                            CurrentSelection.set({tei: $scope.selectedTei, te: $scope.trackedEntity, prs: $scope.programs, pr: $scope.selectedProgram, enrollments: response.enrollments, selectedEnrollment: selectedEnrollment, optionSets: $scope.optionSets});
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
        $scope.broadCastSelections(); 
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
    
    $scope.broadCastSelections = function(){
        
        var selections = CurrentSelection.get();
        $scope.selectedTei = selections.tei;
        $scope.trackedEntity = selections.te;
        $scope.optionSets = selections.optionSets;
      
        CurrentSelection.set({tei: $scope.selectedTei, te: $scope.trackedEntity, prs: $scope.programs, pr: $scope.selectedProgram, enrollments: selections.enrollments, selectedEnrollment: null, optionSets: $scope.optionSets});
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
        if($scope.widgetsChanged.length > 0){
            console.log('dashboardWidgets:  ', $rootScope.dashboardWidgets);
        }
        
        if($scope.orderChanged){
            console.log('order:  ', $scope.widgetsOrder);
        }
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

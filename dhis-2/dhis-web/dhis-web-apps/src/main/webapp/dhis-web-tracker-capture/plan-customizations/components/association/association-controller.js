trackerCapture.controller('EventToTEIAssociations',
    function ($rootScope,
              $scope,
              $modal,
              $timeout,
              AjaxCalls,
              utilityService) {

        $scope.teiAttributesMap = [];
        $scope.trackedEntityMap = [];


        $scope.$on('association-widget', function (event, args) {

            $scope.isAssociation = true;
            //
            //if (args.event.event == "s9b0ZMF7QZU")
            //{
            //    $scope.isAssociation = false;
            //}


            $scope.TEtoEventTEIMap = [];
            $scope.TEWiseEventTEIs = [];
            if (args.show){

                if (args.event.program == "nJNF0T9BSDg")
                {
                    $scope.isAssociation = false;
                    $scope.selectedEvent = undefined;
                }

                else
                {
                    $scope.isAssociation = true;
                }


                $scope.eventSelected = true
                AjaxCalls.getEventbyId(args.event.event).then(function(event){
                    $scope.selectedEvent = event;

                    if (event.eventMembers)
                    for (var i=0;i<event.eventMembers.length;i++){
                        if (!$scope.TEtoEventTEIMap[event.eventMembers[i].trackedEntity]){
                            $scope.TEtoEventTEIMap[event.eventMembers[i].trackedEntity] = [];
                        }
                            $scope.TEtoEventTEIMap[event.eventMembers[i].trackedEntity].push(event.eventMembers[i]);

                    }
                    for (key in $scope.TEtoEventTEIMap){
                        var TEIList = [];
                        for (var j=0;j<$scope.TEtoEventTEIMap[key].length;j++) {
                            updateMap($scope.TEtoEventTEIMap[key][j]);
                            TEIList.push($scope.TEtoEventTEIMap[key][j])
                        }
                        $scope.TEWiseEventTEIs.push({
                            id: key,
                            trackedEntity: $scope.trackedEntityMap[key].displayName,
                            TEIList :TEIList});
                    }
                })
            }else {
                $scope.selectedEvent = undefined;
            }
        });

        // get all no program attributes
        //AjaxCalls.getNoProgramAttributes().then(function(data){
        //    $scope.noProgramAttributes = data.trackedEntityAttributes;
        //})

        //get attributes for display in association widget
        AjaxCalls.getAssociationWidgetAttributes().then(function(associationWidgetAttributes){
            $scope.associationWidgetAttributes = associationWidgetAttributes;
        });

        // get all tracked entities
        AjaxCalls.getTrackedEntities().then(function(data){
            if (data.trackedEntities)
            $scope.trackedEntityMap = utilityService.prepareIdToObjectMap(data.trackedEntities,"id");
        });
        $scope.showHomeScreen = function () {

            var modalInstance = $modal.open({
                templateUrl: 'greenstar-customizations/components/association/addAssociation.html',
                controller: 'AddAssociationController',
                windowClass: 'modal-full-window',
                resolve: {

                }
            });
            modalInstance.selectedEvent = $scope.selectedEvent;
            modalInstance.result.then(function () {

            }, function () {
            });
        };

        updateMap = function(tei){

            for (var i=0;i<tei.attributes.length;i++){

                if (!$scope.teiAttributesMap[tei.trackedEntityInstance]){
                    $scope.teiAttributesMap[tei.trackedEntityInstance] = []
                }
                $scope.teiAttributesMap[tei.trackedEntityInstance][tei.attributes[i].attribute] = tei.attributes[i].value;
            }
        }
    });
trackerCapture.controller('EventToTEIAssociations',
    function ($rootScope,
              $scope,
              $modal,
              $timeout,
              AjaxCalls,
              ModalService,
              DHIS2EventFactory,
              utilityService) {

        $scope.teiAttributesMap = [];
        $scope.trackedEntityMap = [];

        $scope.isAssociation = true;

        $scope.$on('association-widget', function (event, args) {

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
                    //$scope.selectedEvent = undefined;
                }

                else
                {
                    $scope.isAssociation = true;

                    // fro show all programs except contact moment
                    AjaxCalls.getAssociationWidgetAttributes().then(function(associationWidgetAttributes){
                        $scope.associationWidgetAttributes = associationWidgetAttributes;
                    });

                    // get all tracked entities
                    AjaxCalls.getTrackedEntities().then(function(data){
                        if (data.trackedEntities)
                            $scope.trackedEntityMap = utilityService.prepareIdToObjectMap(data.trackedEntities,"id");
                    });

                    $scope.eventSelected = true;
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
                }

            }

            else

            {
                $scope.selectedEvent = undefined;
            }
        });


        //console.log( "isAssociation  === " + $scope.isAssociation)

        // get all no program attributes
        //AjaxCalls.getNoProgramAttributes().then(function(data){
        //    $scope.noProgramAttributes = data.trackedEntityAttributes;
        //})

        //get attributes for display in association widget

        /*
        AjaxCalls.getAssociationWidgetAttributes().then(function(associationWidgetAttributes){
            $scope.associationWidgetAttributes = associationWidgetAttributes;
        });
        */

        // get all tracked entities
        /*
        AjaxCalls.getTrackedEntities().then(function(data){
            if (data.trackedEntities)
            $scope.trackedEntityMap = utilityService.prepareIdToObjectMap(data.trackedEntities,"id");
        });
        */

        $scope.showHomeScreen = function () {

            var modalInstance = $modal.open({
                templateUrl: 'plan-customizations/components/association/addAssociation.html',
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
        };


        // delete Tracked Entity Instance From Event Invitation
        $scope.deleteTrackedEntityInstanceFromEventAssociation = function(trackedEntityInstance, selectedEventAssociation){

            var modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'delete',
                headerText: 'delete',
                bodyText: 'are_you_sure_to_delete'
            };

            ModalService.showModal({}, modalOptions).then(function(result){
                //alert( trackedEntityInstance  + "--" + selectedEventAssociation.eventMembers.length );
                if (selectedEventAssociation.eventMembers.length)
                {
                    for ( var i=0;i<selectedEventAssociation.eventMembers.length;i++ )
                    {
                        if (selectedEventAssociation.eventMembers[i].trackedEntityInstance == trackedEntityInstance)
                        {
                            selectedEventAssociation.eventMembers.splice(i,1);
                        }
                    }
                }

                if (selectedEventAssociation.eventMembers.length == 0)
                {
                    delete(selectedEventAssociation.eventMembers);
                }

                //update events list after delete tei

                DHIS2EventFactory.update(selectedEventAssociation).then(function(response)
                {
                    if (response.httpStatus == "OK")
                    {
                        $timeout(function () {
                            $rootScope.$broadcast('association-widget', {event : selectedEventAssociation, show :true});
                        }, 200);
                    }
                    else
                    {
                        alert("An unexpected thing occurred.");
                    }
                });

            });
        };

    });
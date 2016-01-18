trackerCapture.controller('InvitationController',
    function ($rootScope,
              $scope,
              $modal,
              $timeout,
              AjaxCalls,
              utilityService) {

        $scope.teiAttributesMapInvitation = [];
        $scope.trackedEntityMap = [];


        $scope.$on('invitation-div', function (event, args) {

            $scope.TEtoEventTEIMap = [];
            $scope.TEWiseEventTEIs = [];

            //console.log("Current Event - " + event + " 2nd Argu "+ args );
            //console.log("Current Event 2- " + args.event.event + " 2nd Argu 2 "+ args.show );

            if (args.show)
            {
                $scope.eventSelected = true;
                AjaxCalls.getEventbyId(args.event.event).then(function(event){
                    $scope.selectedEventInvitation = event;

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
                });
            }
            else
            {
                $scope.selectedEventInvitation = undefined;
            }


        });




        /*
        $scope.$on('association-widget', function (event, args) {

            $scope.TEtoEventTEIMap = [];
            $scope.TEWiseEventTEIs = [];
            if (args.show){
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
        */

        // get all no program attributes
        //AjaxCalls.getNoProgramAttributes().then(function(data){
        //    $scope.noProgramAttributes = data.trackedEntityAttributes;
        //})

        //get attributes for display in association widget
        AjaxCalls.getInvitationAndAttendedWidgetAttributes().then(function(invitationAttributes){
            $scope.invitationAttributes = invitationAttributes;
        });

        // get all tracked entities

        AjaxCalls.getTrackedEntities().then(function(data){
            if (data.trackedEntities)
            $scope.trackedEntityMap = utilityService.prepareIdToObjectMap(data.trackedEntities,"id");
        });


        $scope.showInvitationSelectionScreen = function () {

            var modalInstance = $modal.open({
                templateUrl: 'plan-customizations/components/contact_moment/invitation/addInvitation.html',
                controller: 'AddInvitationController',
                windowClass: 'modal-full-window',
                resolve: {

                }
            });
            modalInstance.selectedEventInvitation = $scope.selectedEventInvitation;
            modalInstance.result.then(function () {

            }, function () {
            });
        };

        updateMap = function(tei){

            for (var i=0;i<tei.attributes.length;i++){

                if (!$scope.teiAttributesMapInvitation[tei.trackedEntityInstance]){
                    $scope.teiAttributesMapInvitation[tei.trackedEntityInstance] = []
                }
                $scope.teiAttributesMapInvitation[tei.trackedEntityInstance][tei.attributes[i].attribute] = tei.attributes[i].value;
            }
        }
    });
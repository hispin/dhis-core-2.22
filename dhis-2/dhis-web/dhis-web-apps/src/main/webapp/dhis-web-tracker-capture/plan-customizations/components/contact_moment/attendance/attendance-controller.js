trackerCapture.controller('AttendanceController',
    function ($rootScope,
              $scope,
              $modal,
              $timeout,
              AjaxCalls,
              SessionStorageService,
              CurrentSelection,
              ModalService,
              DHIS2EventFactory,
              utilityService) {


        $scope.teiAttributesMapAttendance = [];
        $scope.trackedEntityMap = [];

        $scope.$on('attendance-div', function (event, args) {

            $scope.TEtoEventTEIMap = [];
            $scope.TEWiseEventTEIs = [];


            if (args.show)
            {
                $scope.eventSelected = true;
                AjaxCalls.getEventbyId(args.event.event).then(function(event){
                    $scope.selectedEventAttendance = event;

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
                            $scope.updateMap($scope.TEtoEventTEIMap[key][j]);
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
                $scope.selectedEventAttendance = undefined;
            }

        });


        //get attributes for display in association widget
        AjaxCalls.getInvitationAndAttendedWidgetAttributes().then(function(attendanceAttributes){
            $scope.attendanceAttributes = attendanceAttributes;

        });

        // get all tracked entities

        AjaxCalls.getTrackedEntities().then(function(data){
            if (data.trackedEntities)
                $scope.trackedEntityMap = utilityService.prepareIdToObjectMap(data.trackedEntities,"id");
        });


        $scope.showAttendanceSelectionScreen = function () {
            //debugger
            var modalInstance = $modal.open({
                templateUrl: 'plan-customizations/components/contact_moment/attendance/addAttendance.html',
                controller: 'ADDAttendanceController',
                windowClass: 'modal-full-window',
                resolve: {

                }
            });
            modalInstance.selectedEventAttendance = $scope.selectedEventAttendance;
            modalInstance.result.then(function () {

            }, function () {
            });
        };


        $scope.updateMap = function(tei){

            for (var i=0;i<tei.attributes.length;i++){

                if (!$scope.teiAttributesMapAttendance[tei.trackedEntityInstance]){
                    $scope.teiAttributesMapAttendance[tei.trackedEntityInstance] = []
                }
                $scope.teiAttributesMapAttendance[tei.trackedEntityInstance][tei.attributes[i].attribute] = tei.attributes[i].value;
            }
        };

        $scope.deleteTrackedEntityInstanceFromEvent = function(trackedEntityInstance, attendanceEvent){

            var modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'delete',
                headerText: 'delete',
                bodyText: 'are_you_sure_to_delete'
            };

            ModalService.showModal({}, modalOptions).then(function(result){
                //alert( trackedEntityInstance  + "--" + attendanceEvent.eventMembers.length );
                if (attendanceEvent.eventMembers.length)
                {
                    for ( var i=0;i<attendanceEvent.eventMembers.length;i++ )
                    {
                        if (attendanceEvent.eventMembers[i].trackedEntityInstance == trackedEntityInstance)
                        {
                            attendanceEvent.eventMembers.splice(i,1);
                        }
                    }
                }

                if (attendanceEvent.eventMembers.length == 0)
                {
                    delete(attendanceEvent.eventMembers);
                }

                //update events list after delete tei

                DHIS2EventFactory.update(attendanceEvent).then(function(response)
                {
                    if (response.httpStatus == "OK")
                    {
                        $timeout(function () {
                            $rootScope.$broadcast('attendance-div', {event : attendanceEvent, show :true});
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

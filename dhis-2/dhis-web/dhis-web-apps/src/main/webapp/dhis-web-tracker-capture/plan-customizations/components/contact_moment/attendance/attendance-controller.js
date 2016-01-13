trackerCapture.controller('AttendanceController',
    function ($rootScope,
              $scope,
              $modal,
              $timeout,
              AjaxCalls,
              SessionStorageService,
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
        AjaxCalls.getAssociationWidgetAttributes().then(function(attendanceAttributes){
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
        }
    });
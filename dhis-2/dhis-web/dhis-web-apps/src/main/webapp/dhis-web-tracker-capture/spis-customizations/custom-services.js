/* global angular, moment, dhis2 */

'use strict';

/* Services */

angular.module('trackerCaptureServices')


    .service('CustomIDGenerationService',function($http,$q,ProgramFactory,RegistrationService){

        return {
            getOu: function (ou) {
                var def = $q.defer();
                $http.get('../api/organisationUnits/' + ou.id + ".json?fields=id,name,code,parent[id],attributeValues[attribute[id,name,code],value]").then(function (response) {

                    def.resolve(response.data);
                });
                return def.promise;
            },
            getCurrentToRootAttributeValue: function (ou, result, def) {
                var promise = this.getOu(ou);
                var thiz = this;
                promise.then(function (ou) {

                        for (var i=0;i<ou.attributeValues.length;i++){
                            if (ou.attributeValues[i].attribute.code == "facilityCode"){
                                result =   ou.attributeValues[i].value  + result;
                            }
                        }
                    result =  ":"+result;

                    if (ou.parent == undefined) {
                        def.resolve(result);
                        return;
                    } else {
                        return thiz.getCurrentToRootAttributeValue(ou.parent, result, def);
                    }
                });
                return def.promise();
            },
            createCustomId :  function(orgUnitUid){

                var thisDef = $.Deferred();
                var def = $.Deferred();
                var tempOu = {};
                tempOu.id = orgUnitUid;
                this.getCurrentToRootAttributeValue(tempOu,"",def);

                def.then(function(currentToRootOrgunitCodes){
                    var referenceLevel = 6;
                    var codes = currentToRootOrgunitCodes.split(":");
                    var level2Code = "00";
                    var level5code = "0000";
                    var level6code = "00";
                    var randomNo =  Math.floor(Math.random()*(99999-10000) + 10000);
                    if (codes[referenceLevel-4]){
                        level2Code = codes[referenceLevel-4].substr(0,2);
                    }
                    if (codes[referenceLevel-1]){
                        level5code = codes[referenceLevel-1].substr(0,4);
                    }
                    if (codes[referenceLevel]){
                        level6code = codes[referenceLevel].substr(0,2);
                    }

                    var Id = level2Code+ level5code+ level6code+ randomNo;
                    thisDef.resolve(Id);
                })
                return thisDef;

            },
            createCustomIdAndSave: function(tei,customIDAttribute,optionSets,attributesById){
                var def = $.Deferred();

                this.createCustomId(tei.orgUnit).then(function(customId){
                    var attributeExists = false;
                    angular.forEach(tei.attributes,function(attribute){
                        if (attribute.attribute == customIDAttribute.id){
                            attribute.value = customId;
                            attributeExists = true;
                        }
                    })

                    if (!attributeExists) {
                        customIDAttribute.value = customId;
                        tei.attributes.push(customIDAttribute);
                    }

                    var teI = {
                        "trackedEntity": tei.trackedEntityInstance,
                        "orgUnit": tei.orgUnit,
                        "attributes": tei.attributes
                    }
                    RegistrationService.registerOrUpdate(tei,optionSets,attributesById).then(function(response){
                        if (response.response.status == "SUCCESS"){

                            alert("Beneficiary Id : " + customId);
                        }
                        def.resolve(response.data);
                    })


                })

                return def;
            },
            validateAndCreateCustomId : function(tei,programUid,tEAttributes,destination,optionSets,attributesById) {
                var def = $.Deferred();
                var thiz = this;
                var customIDAttribute;
                var isValidProgram = false;
                var isValidAttribute = false;
                if (destination == 'PROFILE' || !destination || !programUid){
                    def.resolve("Not Needed");
                    return def;
                }

                ProgramFactory.get(programUid).then(function(program){
                    for (var i=0;i<program.attributeValues.length;i++)
                    {
                        if (program.attributeValues[i].attribute.code == 'allowRegistration' && program.attributeValues[i].value == "true"){
                                isValidProgram = true; break;
                        }
                    }

                    angular.forEach(tEAttributes, function (tEAttribute) {
                        for (var j=0;j<tEAttribute.attributeValues.length;j++)
                        {
                            if (tEAttribute.attributeValues[j].attribute.code == 'toBeUsedForCustomID' && tEAttribute.attributeValues[j].value == "true") {
                                isValidAttribute = true;
                                customIDAttribute = {
                                    attribute : tEAttribute.id,
                                    displayName : tEAttribute.name,
                                    type : tEAttribute.valueType,
                                    value : ""
                                }
                                break;
                            }
                        }
                    })

                    if (isValidAttribute && isValidProgram){
                        thiz.createCustomIdAndSave(tei,customIDAttribute,optionSets,attributesById).then(function(response){
                            def.resolve(response);
                        });
                    }else{
                        def.resolve("Validation Failed");
                    }
                })

                return def;
            }
        }
    })

    .service('ProgramStageSequencingService',function(CalendarService,$filter,orderByFilter) {
        return {
            updateCurrentEventAfterDataValueChange: function(currentEvent,dataValue){
                var isDePresent = false;

                if (!currentEvent.dataValues){
                    currentEvent.dataValues = [];
                }
                for (var i=0;i< currentEvent.dataValues.length;i++){
                    if (currentEvent.dataValues[i].dataElement == dataValue.dataElement){
                        currentEvent.dataValues[i].value = dataValue.value;
                        isDePresent = true;
                    }
                }

                if (!isDePresent){

                    currentEvent.dataValues.push(dataValue);
                }
            },
            getTargetStage : function(programStages,stagesById,currentStage){
                
                for (var i=0; i < programStages.length; i++){
                    if (programStages[i].sortOrder == (currentStage.sortOrder % programStages.length)+1){
                        return programStages[i];
                    }
                }
                return currentStage;
            },
            addOffsetAndFormatDate : function(referenceDate,offset){
                var calendarSetting = CalendarService.getSetting();

                var date = moment(referenceDate, calendarSetting.momentFormat).add('d', offset)._d;
                date = $filter('date')(date, calendarSetting.keyDateFormat);
                
                return date;
            },
            applySequencingOperationIfStageFlagged: function (dummyEvent,currentEvent,eventsByStage,programStages,stagesById,prStDes,currentStage) {

                // initial checking for null values - happens when no events exist
                if (!currentEvent || !currentStage)
                    return dummyEvent;

                var isStageSequencingEnabled = false;
                var isDeValid = false;
                var customTimeInterval = null;
                var isCustomTimeIntervalEnabled = false;
                var targetProgramStage = this.getTargetStage(programStages,stagesById,currentStage);


                //check if stage is valid
                for (var stageIndex=0;stageIndex<currentStage.attributeValues.length;stageIndex++){
                    if (currentStage.attributeValues[stageIndex].attribute.code == "isStageSequencingEnabled" && currentStage.attributeValues[stageIndex].value == "true"){
                        isStageSequencingEnabled = true;
                    }
                    if (currentStage.attributeValues[stageIndex].attribute.code == "isCustomTimeIntervalEnabled" && currentStage.attributeValues[stageIndex].value == "true"){
                        isCustomTimeIntervalEnabled  = true;
                    }
                    if (currentStage.attributeValues[stageIndex].attribute.code == "customTimeInterval" && currentStage.attributeValues[stageIndex].value ){
                        customTimeInterval  = currentStage.attributeValues[stageIndex].value;
                    }

                }
                
                if (isCustomTimeIntervalEnabled ){
                    if (currentStage.periodType || !customTimeInterval){
                        return dummyEvent;
                    }
                        var timeRange = customTimeInterval.split("-");
                        var evs = eventsByStage[currentStage.id];

                        evs = orderByFilter(evs, '-eventDate');
                        dummyEvent.dueDate = this.addOffsetAndFormatDate(evs[0].eventDate,timeRange[evs.length] ? timeRange[evs.length] : 0);
                    return dummyEvent;
                }
                
               
                if (!isStageSequencingEnabled || !currentEvent.dataValues){
                    return dummyEvent;
                }

                for (var dataValueIndex=0;dataValueIndex<currentEvent.dataValues.length;dataValueIndex++){
                    for (var dataElementAttributeValueIndex=0;dataElementAttributeValueIndex<prStDes[currentEvent.dataValues[dataValueIndex].dataElement].dataElement.attributeValues.length;dataElementAttributeValueIndex++){
                        if (prStDes[currentEvent.dataValues[dataValueIndex].dataElement].dataElement.attributeValues[dataElementAttributeValueIndex].attribute.code == "stageSequencingSkipLogicDataValue" &&
                            prStDes[currentEvent.dataValues[dataValueIndex].dataElement].dataElement.attributeValues[dataElementAttributeValueIndex].value == currentEvent.dataValues[dataValueIndex].value){
                            isDeValid = true;
                        }
                    }
                }

                if (isDeValid){
                    dummyEvent.programStage = targetProgramStage.id;
                    dummyEvent.name = targetProgramStage.name;
                    dummyEvent.reportDateDescription = targetProgramStage.reportDateDescription;

                    if (!targetProgramStage.periodType){
                        dummyEvent.dueDate = this.addOffsetAndFormatDate(currentEvent.eventDate,targetProgramStage.standardInterval ?  targetProgramStage.standardInterval : 0);
                    }
                }

                return dummyEvent;
            }


        }
    })

    .service('HideProgramFromDashboardService', function(){
        return {
            isProgramToBeUsedForRegistration : function(program){

                for(var i=0;i < program.attributeValues.length;i++){
                    if (program.attributeValues[i].attribute.code == "allowRegistration" && program.attributeValues[i].value == "true"){
                        return true;
                    }
                }
                return false;
            }
        }

        })



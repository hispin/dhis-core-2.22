/* global angular, moment, dhis2 */

'use strict';

/* Services */

angular.module('trackerCaptureServices')


    .service('CustomIDGenerationService',function($http,$q,ProgramFactory){

        return {
            getOu: function (ou) {
                var def = $q.defer();
                $http.get('../api/organisationUnits/' + ou.id + ".json?fields=id,name,code,parent[id],attributeValues[attribute[id,name],value]").then(function (response) {

                    def.resolve(response.data);
                });
                return def.promise;
            },
            getCurrentToRootAttributeValue: function (ou, result, def) {
                var promise = this.getOu(ou);
                var thiz = this;
                promise.then(function (ou) {

                        for (var i=0;i<ou.attributeValues.length;i++){
                            if (ou.attributeValues[i].attribute.name == "Facility code"){
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
            createCustomIdAndSave: function(tei,customIDAttribute){
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
                    var promise = $http.put( '../api/trackedEntityInstances/'+teI.trackedEntity,teI ).then(function(response){
                        if (response.data.response.status == "SUCCESS"){

                            alert("Beneficiary Id : " + customId);
                        }
                        def.resolve(response.data);

                    });


                })

                return def;
            },
            validateAndCreateCustomId : function(tei,programUid,tEAttributes,destination) {
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
                        if (program.attributeValues[i].attribute.name == 'Allow registration' && program.attributeValues[i].value == "true"){
                                isValidProgram = true; break;
                        }
                    }

                    angular.forEach(tEAttributes, function (tEAttribute) {
                        for (var j=0;j<tEAttribute.attributeValues.length;j++)
                        {
                            if (tEAttribute.attributeValues[j].attribute.name == 'Custom ID' && tEAttribute.attributeValues[j].value == "true") {
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
                        thiz.createCustomIdAndSave(tei,customIDAttribute).then(function(response){
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
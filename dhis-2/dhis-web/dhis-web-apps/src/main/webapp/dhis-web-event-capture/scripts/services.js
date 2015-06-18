/* global angular, dhis2 */

'use strict';

/* Services */

var eventCaptureServices = angular.module('eventCaptureServices', ['ngResource'])

.factory('ECStorageService', function(){
    var store = new dhis2.storage.Store({
        name: 'dhis2ec',
        adapters: [dhis2.storage.IndexedDBAdapter, dhis2.storage.DomSessionStorageAdapter, dhis2.storage.InMemoryAdapter],
        objectStores: ['programs', 'programStages', 'geoJsons', 'optionSets', 'events', 'programValidations', 'programRules', 'programRuleVariables', 'programIndicators', 'ouLevels', 'constants']
    });
    return{
        currentStore: store
    };
})

.factory('OfflineECStorageService', function($http, $q, $rootScope, ECStorageService){
    return {        
        hasLocalData: function() {
            var def = $q.defer();
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.getKeys('events').done(function(events){
                    $rootScope.$apply(function(){
                        def.resolve( events.length > 0 );
                    });                    
                });
            });            
            return def.promise;
        },
        getLocalData: function(){
            var def = $q.defer();            
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.getAll('events').done(function(events){
                    $rootScope.$apply(function(){
                        def.resolve({events: events});
                    });                    
                });
            });            
            return def.promise;
        },
        uploadLocalData: function(){            
            var def = $q.defer();
            this.getLocalData().then(function(localData){                
                var evs = {events: []};
                angular.forEach(localData.events, function(ev){
                    ev.event = ev.id;
                    delete ev.id;
                    evs.events.push(ev);
                });

                $http.post('../api/events', evs).then(function(evResponse){                            
                    def.resolve();
                });                      
            });
            return def.promise;
        }
    };
})

/* current selections */
.service('CurrentSelection', function(){

    this.ouLevels = null;     
    this.location = null;
    
    this.setOuLevels = function(ouLevels){
        this.ouLevels = ouLevels;
    };
    this.getOuLevels = function(){
        return this.ouLevels;
    };
    
    this.setLocation = function(location){
        this.location = location;
    };
    this.getLocation = function(){
        return this.location;
    };
})

/* Factory to fetch optionSets */
.factory('OptionSetService', function() { 
    return {
        getCode: function(options, key){
            if(options){
                for(var i=0; i<options.length; i++){
                    if( key === options[i].name){
                        return options[i].code;
                    }
                }
            }            
            return key;
        },        
        getName: function(options, key){
            if(options){
                for(var i=0; i<options.length; i++){                    
                    if( key === options[i].code){
                        return options[i].name;
                        //return options[i];
                    }
                }
            }            
            return key;
        }
    };
})

/* Factory to fetch programs */
.factory('ProgramFactory', function($q, $rootScope, SessionStorageService, ECStorageService) {  
    
    var userHasValidRole = function(program, userRoles){
        
        var hasRole = false;

        if($.isEmptyObject(program.userRoles)){
            return !hasRole;
        }

        for(var i=0; i < userRoles.length && !hasRole; i++){
            if( program.userRoles.hasOwnProperty( userRoles[i].id ) ){
                hasRole = true;
            }
        }        
        return hasRole;        
    };
    
    return {
        getProgramsByOu: function(ou, selectedProgram){
            var roles = SessionStorageService.get('USER_ROLES');
            var userRoles = roles && roles.userCredentials && roles.userCredentials.userRoles ? roles.userCredentials.userRoles : [];
            var def = $q.defer();
            
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.getAll('programs').done(function(prs){
                    var programs = [];
                    angular.forEach(prs, function(pr){                            
                        if(pr.organisationUnits.hasOwnProperty( ou.id ) && userHasValidRole(pr, userRoles)){
                            programs.push(pr);
                        }
                    });
                    
                    if(programs.length === 0){
                        selectedProgram = null;
                    }
                    else if(programs.length === 1){
                        selectedProgram = programs[0];
                    } 
                    else{
                        if(selectedProgram){
                            var continueLoop = true;
                            for(var i=0; i<programs.length && continueLoop; i++){
                                if(programs[i].id === selectedProgram.id){                                
                                    selectedProgram = programs[i];
                                    continueLoop = false;
                                }
                            }
                            if(continueLoop){
                                selectedProgram = null;
                            }
                        }
                    }
                    
                    $rootScope.$apply(function(){
                        def.resolve({programs: programs, selectedProgram: selectedProgram});
                    });                      
                });
            });
            
            return def.promise;
        }
    };
})

/* factory to fetch and process programValidations */
.factory('MetaDataFactory', function($q, $rootScope, ECStorageService) {  
    
    return {        
        get: function(store, uid){
            
            var def = $q.defer();
            
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.get(store, uid).done(function(pv){                    
                    $rootScope.$apply(function(){
                        def.resolve(pv);
                    });
                });
            });                        
            return def.promise;
        },
        getByProgram: function(store, program){
            var def = $q.defer();
            var obj = [];
            
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.getAll(store, program).done(function(pvs){   
                    angular.forEach(pvs, function(pv){
                        if(pv.program.id === program){                            
                            obj.push(pv);                               
                        }                        
                    });
                    $rootScope.$apply(function(){
                        def.resolve(obj);
                    });
                });                
            });            
            return def.promise;
        },
        getAll: function(store){
            var def = $q.defer();            
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.getAll(store).done(function(pvs){                       
                    $rootScope.$apply(function(){
                        def.resolve(pvs);
                    });
                });                
            });            
            return def.promise;
        }
    };        
})

/* factory for handling events */
.factory('DHIS2EventFactory', function($http, $q, ECStorageService, $rootScope) {   
    
    return {
        getByStage: function(orgUnit, programStage, pager, paging){
            
            var url = '../api/events.json?' + 'orgUnit=' + orgUnit + '&programStage=' + programStage;
            
            if(paging){
                var pgSize = pager ? pager.pageSize : 50;
                var pg = pager ? pager.page : 1;
                pgSize = pgSize > 1 ? pgSize  : 1;
                pg = pg > 1 ? pg : 1; 
                url = url  + '&pageSize=' + pgSize + '&page=' + pg + '&totalPages=true';
            }
            else{
                url = url  + '&skipPaging=true';
            }
            
            var promise = $http.get( url ).then(function(response){                    
                return response.data;        
            }, function(){     
                var def = $q.defer();
                ECStorageService.currentStore.open().done(function(){
                    ECStorageService.currentStore.getAll('events').done(function(evs){
                        var result = {events: [], pager: {pageSize: '', page: 1, toolBarDisplay: 5, pageCount: 1}};
                        angular.forEach(evs, function(ev){                            
                            if(ev.programStage === programStage && ev.orgUnit === orgUnit){
                                ev.event = ev.id;
                                result.events.push(ev);
                            }
                        }); 
                        $rootScope.$apply(function(){
                            def.resolve( result );
                        });                    
                    });
                });            
                return def.promise;
            });            
            
            return promise;
        },        
        get: function(eventUid){            
            var promise = $http.get('../api/events/' + eventUid + '.json').then(function(response){               
                return response.data;                
            }, function(){
                var p = dhis2.ec.store.get('events', eventUid).then(function(ev){
                    ev.event = eventUid;
                    return ev;
                });
                return p;
            });            
            return promise;
        },        
        create: function(dhis2Event){
            var promise = $http.post('../api/events.json', dhis2Event).then(function(response){
                return response.data;
            }, function(){            
                dhis2Event.id = dhis2.util.uid();  
                dhis2Event.event = dhis2Event.id;
                dhis2.ec.store.set( 'events', dhis2Event );                
                return {importSummaries: [{status: 'SUCCESS', reference: dhis2Event.id}]};
            });
            return promise;            
        },        
        delete: function(dhis2Event){
            var promise = $http.delete('../api/events/' + dhis2Event.event).then(function(response){
                return response.data;
            }, function(){
                dhis2.ec.store.remove( 'events', dhis2Event.event );
            });
            return promise;           
        },    
        update: function(dhis2Event){
            var promise = $http.put('../api/events/' + dhis2Event.event, dhis2Event).then(function(response){              
                return response.data;
            }, function(){
                dhis2.ec.store.remove('events', dhis2Event.event);
                dhis2Event.id = dhis2Event.event;
                dhis2.ec.store.set('events', dhis2Event);
            });
            return promise;
        },        
        updateForSingleValue: function(singleValue, fullValue){        
            var promise = $http.put('../api/events/' + singleValue.event + '/' + singleValue.dataValues[0].dataElement, singleValue ).then(function(response){
                 return response.data;
            }, function(){
                dhis2.ec.store.remove('events', fullValue.event);
                fullValue.id = fullValue.event;
                dhis2.ec.store.set('events', fullValue);
            });
            return promise;
        }
    };    
})

    /* Returns a function for getting rules for a specific program */
.factory('TrackerRulesFactory', function($q,$rootScope,ECStorageService){
    return{
        getOldProgramStageRules :function(programUid, programstageUid) {
            var rules = this.getProgramRules(programUid);
            
            //Only keep the rules actually matching the program stage we are in, or rules with no program stage defined.
            var programStageRules = [];
            angular.forEach(rules, function(rule) {
                if(rule.programstage_uid == null || rule.programstage_uid == "" || rule.programstage_uid == programstageUid) {
                   programStageRules.push(rule);
                }
            });
            
            return programStageRules;
        },
        
        getProgramStageRules : function(programUid, programStageUid){
            var def = $q.defer();
            
            ECStorageService.currentStore.open().done(function(){
                ECStorageService.currentStore.getAll('programRules').done(function(rules){                    
                    //The array will ultimately be returned to the caller.
                    var programRulesArray = [];
                    //Loop through and add the rules belonging to this program and program stage
                    angular.forEach(rules, function(rule){
                       if(rule.program.id == programUid) {
                           if(!rule.programStage || !rule.programStage.id || rule.programStage.id == programStageUid) {
                                programRulesArray.push(rule);
                            }
                       }
                    });

                    $rootScope.$apply(function(){
                        def.resolve(programRulesArray);
                    });
                });     
            });
                        
            return def.promise;
        }
    };  
})

/* Returns user defined variable names and their corresponding UIDs and types for a specific program */
.factory('TrackerRuleVariableFactory', function($rootScope, $q, ECStorageService){
    return{
        getProgramRuleVariables : function(programUid){
            var def = $q.defer();

            ECStorageService.currentStore.open().done(function(){
                
                ECStorageService.currentStore.getAll('programRuleVariables').done(function(variables){
                    
                    //The array will ultimately be returned to the caller.
                    var programRuleVariablesArray = [];
                    //Loop through and add the variables belonging to this program
                    angular.forEach(variables, function(variable){
                       if(variable.program.id == programUid) {
                            programRuleVariablesArray.push(variable);
                       }
                    });

                    $rootScope.$apply(function(){
                        def.resolve(programRuleVariablesArray);
                    });
                });
            });
                        
            return def.promise;
        }
    };
})

/* service for dealing with events */
.service('DHIS2EventService', function(){
    return {     
        //for simplicity of grid display, events were changed from
        //event.datavalues = [{dataElement: dataElement, value: value}] to
        //event[dataElement] = value
        //now they are changed back for the purpose of storage.   
        reconstructEvent: function(event, programStageDataElements){
            var e = {};
        
            e.event         = event.event;
            e.status        = event.status;
            e.program       = event.program;
            e.programStage  = event.programStage;
            e.orgUnit       = event.orgUnit;
            e.eventDate     = event.eventDate;

            var dvs = [];
            angular.forEach(programStageDataElements, function(prStDe){
                if(event.hasOwnProperty(prStDe.dataElement.id)){
                    dvs.push({dataElement: prStDe.dataElement.id, value: event[prStDe.dataElement.id]});
                }
            });

            e.dataValues = dvs;
            
            if(event.coordinate){
                e.coordinate = {latitude: event.coordinate.latitude ? event.coordinate.latitude : '',
                                     longitude: event.coordinate.longitude ? event.coordinate.longitude : ''};
            }

            return e;
        }        
    };
});
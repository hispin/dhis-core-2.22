
/* global dhis2, angular, i18n_ajax_login_failed, _, selection, selection */

dhis2.util.namespace('dhis2.ec');

// whether current user has any organisation units
dhis2.ec.emptyOrganisationUnits = false;

var i18n_no_orgunits = 'No organisation unit attached to current user, no data entry possible';
var i18n_offline_notification = 'You are offline, data will be stored locally';
var i18n_online_notification = 'You are online';
var i18n_need_to_sync_notification = 'There is data stored locally, please upload to server';
var i18n_sync_now = 'Upload';
var i18n_sync_success = 'Upload to server was successful';
var i18n_sync_failed = 'Upload to server failed, please try again later';
var i18n_uploading_data_notification = 'Uploading locally stored data to the server';

var PROGRAMS_METADATA = 'EVENT_PROGRAMS';

var EVENT_VALUES = 'EVENT_VALUES';
var optionSetsInPromise = [];

dhis2.ec.isOffline = false;
dhis2.ec.store = null;
dhis2.ec.memoryOnly = $('html').hasClass('ie7') || $('html').hasClass('ie8');
var adapters = [];    
if( dhis2.ec.memoryOnly ) {
    adapters = [ dhis2.storage.InMemoryAdapter ];
} else {
    adapters = [ dhis2.storage.IndexedDBAdapter, dhis2.storage.DomLocalStorageAdapter, dhis2.storage.InMemoryAdapter ];
}

dhis2.ec.store = new dhis2.storage.Store({
    name: 'dhis2ec',
    adapters: [dhis2.storage.IndexedDBAdapter, dhis2.storage.DomSessionStorageAdapter, dhis2.storage.InMemoryAdapter],
    objectStores: ['programs', 'geoJsons', 'optionSets', 'events', 'programValidations', 'programRules', 'programRuleVariables', 'programIndicators', 'ouLevels', 'constants']
});

(function($) {
    $.safeEach = function(arr, fn)
    {
        if (arr)
        {
            $.each(arr, fn);
        }
    };
})(jQuery);

/**
 * Page init. The order of events is:
 *
 * 1. Load ouwt 2. Load meta-data (and notify ouwt) 3. Check and potentially
 * download updated forms from server
 */
$(document).ready(function()
{
    $.ajaxSetup({
        type: 'POST',
        cache: false
    });

    $('#loaderSpan').show();
    
});


$(document).bind('dhis2.online', function(event, loggedIn)
{
    if (loggedIn)
    {   
        dhis2.ec.isOffline = false;
        
        var OfflineECStorageService = angular.element('body').injector().get('OfflineECStorageService');

        OfflineECStorageService.hasLocalData().then(function(localData){
            if(localData){
                var message = i18n_need_to_sync_notification
                    + ' <button id="sync_button" type="button">' + i18n_sync_now + '</button>';

                setHeaderMessage(message);

                $('#sync_button').bind('click', uploadLocalData);
            }
            else{
                if (dhis2.ec.emptyOrganisationUnits) {
                    setHeaderMessage(i18n_no_orgunits);
                }
                else {
                    setHeaderDelayMessage(i18n_online_notification);
                }
            }
        });
    }
    else
    {
        var form = [
            '<form style="display:inline;">',
            '<label for="username">Username</label>',
            '<input name="username" id="username" type="text" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
            '<label for="password">Password</label>',
            '<input name="password" id="password" type="password" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
            '<button id="login_button" type="button">Login</button>',
            '</form>'
        ].join('');

        setHeaderMessage(form);
        ajax_login();
    }
});

$(document).bind('dhis2.offline', function()
{
    if (dhis2.ec.emptyOrganisationUnits) {
        setHeaderMessage(i18n_no_orgunits);
    }
    else {
        dhis2.ec.isOffline = true;
        setHeaderMessage(i18n_offline_notification);
    }
});
    
function ajax_login()
{
    $('#login_button').bind('click', function()
    {
        var username = $('#username').val();
        var password = $('#password').val();

        $.post('../dhis-web-commons-security/login.action', {
            'j_username': username,
            'j_password': password
        }).success(function()
        {
            var ret = dhis2.availability.syncCheckAvailability();

            if (!ret)
            {
                alert(i18n_ajax_login_failed);
            }
        });
    });
}

function downloadMetaData(){    
    
    console.log('Loading required meta-data');
    var def = $.Deferred();
    var promise = def.promise();
    
    promise = promise.then( dhis2.ec.store.open );    
    promise = promise.then( getUserRoles );
    promise = promise.then( getCalendarSetting );
    promise = promise.then( getConstants );
    promise = promise.then( getOrgUnitLevels );    
    promise = promise.then( getMetaPrograms );     
    promise = promise.then( getPrograms );     
    promise = promise.then( getMetaProgramValidations );
    promise = promise.then( getProgramValidations );
    promise = promise.then( getMetaProgramIndicators );
    promise = promise.then( getProgramIndicators );
    promise = promise.then( getMetaProgramRules );
    promise = promise.then( getProgramRules );
    promise = promise.then( getMetaProgramRuleVariables );
    promise = promise.then( getProgramRuleVariables );
    promise = promise.then( getOptionSets );    
    promise.done( function() {    
        //Enable ou selection after meta-data has downloaded
        $( "#orgUnitTree" ).removeClass( "disable-clicks" );
        
        console.log( 'Finished loading meta-data' ); 
        dhis2.availability.startAvailabilityCheck();
        console.log( 'Started availability check' );
        selection.responseReceived();
    });         

    def.resolve();
}

function getUserRoles()
{
    var SessionStorageService = angular.element('body').injector().get('SessionStorageService');
    
    if( SessionStorageService.get('USER_ROLES') ){
       return; 
    }
    
    var def = $.Deferred();

    $.ajax({
        url: '../api/me.json?fields=id,name,userCredentials[userRoles[id,authorities]]',
        type: 'GET'
    }).done(function(response) {
        SessionStorageService.set('USER_ROLES', response);
        def.resolve();
    }).fail(function(){
        def.resolve();
    });

    return def.promise();
}

function getCalendarSetting()
{       
    var def = $.Deferred();

    $.ajax({
        url: '../api/systemSettings?key=keyCalendar&key=keyDateFormat',
        type: 'GET'
    }).done(function(response) {
        localStorage['CALENDAR_SETTING'] = JSON.stringify(response);
        def.resolve();
    }).fail(function(){
        def.resolve();
    });

    return def.promise();
}

function getConstants()
{
    dhis2.ec.store.getKeys( 'constants').done(function(res){        
        if(res.length > 0){
            return;
        }        
        return getD2Objects('constants', 'constants', '../api/constants.json', 'paging=false&fields=id,name,name,value');        
    });    
}

function getOrgUnitLevels()
{
    dhis2.ec.store.getKeys( 'ouLevels').done(function(res){        
        if(res.length > 0){
            return;
        }        
        return getD2Objects('ouLevels', 'organisationUnitLevels', '../api/organisationUnitLevels.json', 'filter=level:gt:1&fields=id,name,level&paging=false');
    });    
}

function getMetaPrograms()
{
    var def = $.Deferred();

    $.ajax({
        url: '../api/programs.json',
        type: 'GET',
        data:'filter=programType:eq:WITHOUT_REGISTRATION&paging=false&fields=id,version,categoryCombo[id,isDefault,categories[id]],programStages[id,version,programStageSections[id],programStageDataElements[dataElement[id,optionSet[id,version]]]]'
    }).done( function(response) {        
        def.resolve( response.programs ? response.programs: [] );
    }).fail(function(){
        def.resolve( null );
    });
    
    return def.promise(); 
}

function getPrograms( programs )
{
    if( !programs ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    var ids = [];
    _.each( _.values( programs ), function ( program ) {
        
        if(program.programStages && program.programStages[0].programStageDataElements){
            build = build.then(function() {
                var d = $.Deferred();
                var p = d.promise();
                dhis2.ec.store.get('programs', program.id).done(function(obj) {
                    if(!obj || obj.version !== program.version) {
                        ids.push( program.id );
                    }

                    d.resolve();
                });

                return p;
            });
        }        
    });

    build.done(function() {
        promise = promise.done( function () {
            var _ids = null;
            if( ids && ids.length > 0 ){
                _ids = ids.toString();
                _ids = '[' + _ids + ']';
                promise = promise.then( getAllPrograms( _ids ) );
            }            
            mainDef.resolve( programs, _ids );
        } );
        def.resolve();
        
    }).fail(function(){
        mainDef.resolve( null, null );
    });

    builder.resolve();

    return mainPromise;
}

function getAllPrograms( ids )
{    
    return function() {
        return $.ajax( {
            url: '../api/programs.json',
            type: 'GET',
            data: 'fields=id,name,programType,version,dataEntryMethod,enrollmentDateLabel,incidentDateLabel,displayIncidentDate,ignoreOverdueEvents,categoryCombo[id,name,isDefault,categories[id,name,categoryOptions[id,name]]],organisationUnits[id,name],programStages[id,name,version,description,excecutionDateLabel,captureCoordinates,dataEntryForm[id,name,style,htmlCode,format],minDaysFromStart,repeatable,preGenerateUID,programStageSections[id,name,programStageDataElements[dataElement[id]]],programStageDataElements[displayInReports,sortOrder,allowProvidedElsewhere,allowFutureDate,compulsory,dataElement[id,name,valueType,optionSetValue,formName,optionSet[id]]]],userRoles[id,name]&paging=false&filter=id:in:' + ids
        }).done( function( response ){
            
            if(response.programs){
                _.each(_.values( response.programs), function(program){
                    var ou = {};
                    _.each(_.values( program.organisationUnits), function(o){
                        ou[o.id] = o.name;
                    });
                    program.organisationUnits = ou;

                    var ur = {};
                    _.each(_.values( program.userRoles), function(u){
                        ur[u.id] = u.name;
                    });
                    program.userRoles = ur;

                    dhis2.ec.store.set( 'programs', program );
                });
            }
        });
    };
}

function getOptionSets( programs )
{    
    if( !programs ){
        return;
    }
    
    delete programs.programIds;
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();    

    _.each( _.values( programs ), function ( program ) {
        
        if(program.programStages && program.programStages[0].programStageDataElements){
            _.each(_.values( program.programStages[0].programStageDataElements), function(prStDe){
                if( prStDe.dataElement && prStDe.dataElement.optionSet && prStDe.dataElement.optionSet.id ){
                    build = build.then(function() {
                        var d = $.Deferred();
                        var p = d.promise();
                        dhis2.ec.store.get('optionSets', prStDe.dataElement.optionSet.id).done(function(obj) {
                            if( (!obj || obj.version !== prStDe.dataElement.optionSet.version) && optionSetsInPromise.indexOf(prStDe.dataElement.optionSet.id) === -1) {
                                optionSetsInPromise.push( prStDe.dataElement.optionSet.id );
                                promise = promise.then( getD2Object( prStDe.dataElement.optionSet.id, 'optionSets', '../api/optionSets', 'fields=id,name,version,options[id,name,code]', 'idb' ) );
                            }
                            d.resolve();
                        });

                        return p;
                    });
                }            
            }); 
        }                             
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( programs );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;    
}

function getMetaProgramValidations( programs, programIds )
{
    programs.programIds = programIds;
    return getD2MetaObject(programs, 'programValidations', '../api/programValidations.json', 'paging=false&fields=id&filter=program.id:in:');
}

function getProgramValidations( programValidations )
{  
    return checkAndGetD2Objects( programValidations, 'programValidations', '../api/programValidations', 'fields=id,name,name,operator,rightSide[expression,description],leftSide[expression,description],program[id]');
}

function getMetaProgramIndicators( programs )
{   
    return getD2MetaObject(programs, 'programIndicators', '../api/programIndicators.json', 'paging=false&fields=id&filter=program.id:in:');
}

function getProgramIndicators( programIndicators )
{
    return checkAndGetD2Objects( programIndicators, 'programIndicators', '../api/programIndicators', 'fields=id,name,code,shortName,displayInForm,expression,displayDescription,rootDate,description,valueType,name,filter,program[id]');
}

function getMetaProgramRules( programs )
{
    return getD2MetaObject(programs, 'programRules', '../api/programRules.json', 'paging=false&fields=id&filter=program.id:in:');
}

function getProgramRules( programRules )
{
    return checkAndGetD2Objects( programRules, 'programRules', '../api/programRules', 'fields=id,name,condition,description,program[id],programStage[id],priority,programRuleActions[id,content,location,data,programRuleActionType,programStageSection[id],dataElement[id],trackedEntityAttribute[id],programIndicator[id],programStage[id]]');
}

function getMetaProgramRuleVariables( programs )
{    
    return getD2MetaObject(programs, 'programRuleVariables', '../api/programRuleVariables.json', 'paging=false&fields=id&filter=program.id:in:');
}

function getProgramRuleVariables( programRuleVariables )
{
    return checkAndGetD2Objects( programRuleVariables, 'programRuleVariables', '../api/programRuleVariables', 'fields=id,name,name,programRuleVariableSourceType,program[id],programStage[id],dataElement[id]');
}

function getD2MetaObject( programs, objNames, url, filter )
{
    if( !programs || !programs.programIds){
        return;
    }
    
    //console.log('programs.programIds:  ', programs.programIds);
    filter = filter + programs.programIds;
    var def = $.Deferred();
    
    $.ajax({
        url: url,
        type: 'GET',
        data:filter
    }).done( function(response) {        
        def.resolve( {programs: programs, self: response[objNames], programIds: programs.programIds} );
        
    }).fail(function(){
        def.resolve( null );
    });
    
    return def.promise();    
}

function checkAndGetD2Objects( obj, store, url, filter )
{   
    if( !obj || !obj.programs || !obj.self || !obj.programIds){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    var ids = [];
    _.each( _.values( obj.self ), function ( obj) {
        build = build.then(function() {
            var d = $.Deferred();
            var p = d.promise();
            dhis2.ec.store.get(store, obj.id).done(function(o) {
                if(!o){                    
                    ids.push( obj.id );
                }
                d.resolve();
            });

            return p;
        });
    });

    build.done(function() {
        def.resolve();
        promise = promise.done( function () {
            
            if( ids && ids.length > 0 ){
                var _ids = ids.toString();
                _ids = '[' + _ids + ']';
                filter = filter + '&filter=id:in:' + _ids + '&paging=false';
                promise = promise.then( getAllD2Objects( store, url, filter ) );
            }
            
            mainDef.resolve( obj.programs, obj.programIds );
        } );
    }).fail(function(){
        mainDef.resolve( null );
    });

    builder.resolve();

    return mainPromise;
}


function getAllD2Objects( store, url, filter )
{
    return function() {        
        return $.ajax( {
            url: url,
            type: 'GET',            
            data: filter
        }).done( function( response ){
            if(response[store]){
                dhis2.ec.store.setAll( store, response[store] );
            }             
        });
    };
}

function getD2Objects(store, objs, url, filter)
{
    var def = $.Deferred();

    $.ajax({
        url: url,
        type: 'GET',
        data: filter
    }).done(function(response) {
        if(response[objs]){
            dhis2.ec.store.setAll( store, response[objs] );
        }            
        def.resolve();        
    }).fail(function(){
        def.resolve();
    });

    return def.promise();
}


function getD2Object( id, store, url, filter, storage )
{
    return function() {
        if(id){
            url = url + '/' + id + '.json';
        }
        return $.ajax( {
            url: url,
            type: 'GET',            
            data: filter
        }).done( function( response ){
            if(storage === 'idb'){
                if( response && response.id) {
                    dhis2.ec.store.set( store, response );
                }
            }
            if(storage === 'localStorage'){
                localStorage[store] = JSON.stringify(response);
            }            
            if(storage === 'sessionStorage'){
                var SessionStorageService = angular.element('body').injector().get('SessionStorageService');
                SessionStorageService.set(store, response);
            }            
        });
    };
}

function uploadLocalData()
{
    var OfflineECStorageService = angular.element('body').injector().get('OfflineECStorageService');
    setHeaderWaitMessage(i18n_uploading_data_notification);
     
    OfflineECStorageService.uploadLocalData().then(function(){
        dhis2.ec.store.removeAll( 'events' );
        log( 'Successfully uploaded local events' );      
        setHeaderDelayMessage( i18n_sync_success );
        selection.responseReceived(); //notify angular
    });
}

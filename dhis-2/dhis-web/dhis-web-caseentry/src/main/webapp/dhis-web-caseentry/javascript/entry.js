var prefixId = 'ps_';

var COLOR_RED = "#fb4754";
var COLOR_GREEN = "#8ffe8f";
var COLOR_YELLOW = "#f9f95a";
var COLOR_LIGHTRED = "#fb6bfb";
var COLOR_LIGHT_RED = "#ff7676";
var COLOR_LIGHT_YELLOW = "#ffff99";
var COLOR_LIGHT_GREEN = "#ccffcc";
var COLOR_LIGHT_LIGHTRED = "#ff99ff";

//--------------------------------------------------------------------------------------------
// Load program-stages by the selected program
//--------------------------------------------------------------------------------------------

function loadProgramStages()
{
	jQuery('#createNewEncounterDiv').dialog('close');
	hideById('dataEntryFormDiv');
	setFieldValue('executionDate','');
	setFieldValue('dueDate','');
	disable('completeBtn');
	disable('completeInBelowBtn');
	disable('validationBtn');
	disable('validationInBelowBtn');
	disable('newEncounterBtn');
	hideById('inputCriteriaDiv');
	$('#programStageIdTR').html('');
	hideById('programInstanceDiv');
	
	var programId = jQuery('#dataRecordingSelectDiv [name=programId]').val();
	if ( programId == 0 )
	{
		return;
	}
	
	jQuery.getJSON( "loadProgramStageInstances.action",
		{
			programId: programId
		},  
		function( json ) 
		{    
			showById('programInstanceDiv');
			hideById('executionDateTB');
				
			var type = jQuery('#dataRecordingSelectDiv [name=programId] option:selected').attr('type');
			if( type == 1 && json.programStageInstances.length > 1 )
			{
				for ( i in json.programStageInstances ) 
				{
					if( i!= 0 )
					{
						$('#programStageIdTR').append('<td><img src="images/rightarrow.png"></td>');
					}

					var status =json.programStageInstances[i].status;
					var programStageInstanceId = json.programStageInstances[i].id;
					var programStageId = json.programStageInstances[i].programStageId;
					var programStageName= json.programStageInstances[i].programStageName;
					var elementId = prefixId + programStageInstanceId;
					
					$('#programStageIdTR').append('<td><input name="programStageBtn" '
						+ 'id="' + elementId + '"' 
						+ 'type="button" class="stage-object" '
						+ 'psid="' + programStageId + '"' 
						+ 'psname="' + programStageName + '" '
						+ 'dueDate="' + json.programStageInstances[i].dueDate + '"'
						+ 'value="'+ programStageName + ' ' + json.programStageInstances[i].dueDate + '" '
						+ 'onclick="javascript:loadDataEntry(' + programStageInstanceId + ')"></td>');
					setEventColorStatus( elementId, status );
				}
				
				disable('completeBtn');
				disable('completeInBelowBtn');
				disable('validationBtn');
				disable('validationInBelowBtn');
				showById('programStageIdTR');
				showById('programInstanceFlowDiv');
			}
			// Load entry form for Single-event program or normal program with only one program-stage
			else
			{
				jQuery('#dueDateTR').attr('class','hidden');
				enable('completeBtn');
				enable('completeInBelowBtn');
				hideById('historyPlanLink');
				hideById('programStageIdTR');
				hideById('programInstanceFlowDiv');
				loadDataEntry( json.programStageInstances[0].id );
			}
	});
}

//--------------------------------------------------------------------------------------------
// Load data-entry-form
//--------------------------------------------------------------------------------------------

function loadDataEntry( programStageInstanceId )
{
	setInnerHTML('dataEntryFormDiv', '');
	showById('executionDateTB');
	showById('dataEntryFormDiv');
	setFieldValue( 'dueDate', '' );
	setFieldValue( 'executionDate', '' );
	disable('validationBtn');
	disable('validationInBelowBtn');
	disable('completeBtn');
	disable('completeInBelowBtn');
	disable('newEncounterBtn');
	
	jQuery(".stage-object-selected").removeClass('stage-object-selected');
	var selectedProgramStageInstance = jQuery( '#' + prefixId + programStageInstanceId );
	selectedProgramStageInstance.addClass('stage-object-selected');
	setFieldValue( 'programStageId', selectedProgramStageInstance.attr('psid') );
	
	showLoader();	
	$( '#dataEntryFormDiv' ).load( "dataentryform.action", 
		{ 
			programStageInstanceId: programStageInstanceId
		},function( )
		{
			var executionDate = jQuery('#dataRecordingSelectDiv input[id=executionDate]').val();
			var completed = jQuery('#entryFormContainer input[id=completed]').val();
			var irregular = jQuery('#entryFormContainer input[id=irregular]').val();
			showById('inputCriteriaDiv');
			enable('validationBtn');
			enable('validationInBelowBtn');
			if(  executionDate == '' )
			{
				disable('validationBtn');
				disable('validationInBelowBtn');
			}
			else if( executionDate != '' && completed == 'false' )
			{
				enable('completeBtn');
				enable('completeInBelowBtn');
			}
			else if( completed == 'true' )
			{
				disable('completeBtn');
				disable('completeInBelowBtn');
			}
			
			if( completed == 'true' && irregular == 'true' )
			{
				enable( 'newEncounterBtn' );
			}
			
			hideLoader();
			hideById('contentDiv'); 
		} );
}

//------------------------------------------------------------------------------
// Save value
//------------------------------------------------------------------------------

function saveVal( dataElementId )
{
	var programStageId = byId('programStageId').value;
	var fieldId = programStageId + '-' + dataElementId + '-val';
	
	var field = byId( fieldId ); 
	var fieldValue = jQuery.trim( field.value );

	var arrData = jQuery( "#" + fieldId ).attr('data').replace('{','').replace('}','').replace(/'/g,"").split(',');
	var data = new Array();
	for( var i in arrData )
	{	
		var values = arrData[i].split(':');
		var key = jQuery.trim( values[0] );
		var value = jQuery.trim( values[1] )
		data[key] = value;
	}
 
	var dataElementName = data['deName']; 
    var type = data['deType'];
 
	field.style.backgroundColor = '#ffffcc';
    
    if( fieldValue != '' )
    {
        if ( type == 'int' || type == 'number' || type == 'positiveNumber' || type == 'negativeNumber' )
        {
            if (  type == 'int' && !isInt( fieldValue ))
            {
                field.style.backgroundColor = '#ffcc00';

                window.alert( i18n_value_must_integer + '\n\n' + dataElementName );

                field.focus();

                return;
            }
			else if ( type == 'number' && !isRealNumber( fieldValue ) )
            {
                field.style.backgroundColor = '#ffcc00';
                window.alert( i18n_value_must_number + '\n\n' + dataElementName );
                field.focus();

                return;
            } 
			else if ( type == 'positiveNumber' && !isPositiveInt( fieldValue ) )
            {
                field.style.backgroundColor = '#ffcc00';
                window.alert( i18n_value_must_positive_integer + '\n\n' + dataElementName );
                field.focus();

                return;
            } 
			else if ( type == 'negativeNumber' && !isNegativeInt( fieldValue ) )
            {
                field.style.backgroundColor = '#ffcc00';
                window.alert( i18n_value_must_negative_integer + '\n\n' + dataElementName );
                field.focus();

                return;
            }
        }
    	
    }
    
	var valueSaver = new ValueSaver( dataElementId, fieldValue, type, '#ccffcc'  );
    valueSaver.save();
}

function saveOpt( dataElementId )
{
	var programStageId = byId('programStageId').value;
	var field = byId( programStageId + '-' + dataElementId + '-val' );	
	field.style.backgroundColor = '#ffffcc';
	
	var valueSaver = new ValueSaver( dataElementId, field.options[field.selectedIndex].value, 'bool', '#ccffcc' );
    valueSaver.save();
}

function updateProvidingFacility( dataElementId, checkField )
{
	var programStageId = byId( 'programStageId' ).value;
	var checked= checkField.checked;
    checkField.style.backgroundColor = '#ffffcc';
	
    var facilitySaver = new FacilitySaver( dataElementId, checked, '#ccffcc' );
    facilitySaver.save();
    
}

function saveExecutionDate( programStageId, executionDateValue )
{
    var field = document.getElementById( 'executionDate' );
	
    field.style.backgroundColor = '#ffffcc';
	
    var executionDateSaver = new ExecutionDateSaver( programStageId, executionDateValue, '#ccffcc' );
    executionDateSaver.save();
	
    if( !jQuery("#entryForm").is(":visible") )
    {
        toggleContentForReportDate(true);
    }
}

/**
* Display data element name in selection display when a value field recieves
* focus.
* XXX May want to move this to a separate function, called by valueFocus.
* @param e focus event
* @author Hans S. Tommerholt
*/
function valueFocus(e) 
{
    //Retrieve the data element id from the id of the field
    var str = e.target.id;
	
    var match = /.*\[(.*)\]/.exec( str ); //value[-dataElementId-]
	
    if ( ! match )
    {
        return;
    }

    var deId = match[1];
	
    //Get the data element name
    var nameContainer = document.getElementById('value[' + deId + '].name');
	
    if ( ! nameContainer )
    {
        return;
    }

    var name = '';
	
    var as = nameContainer.getElementsByTagName('a');

    if ( as.length > 0 )	//Admin rights: Name is in a link
    {
        name = as[0].firstChild.nodeValue;
    }
    else
    {
        name = nameContainer.firstChild.nodeValue;
    }
	
}

function keyPress( event, field )
{
    var key = 0;
    if ( event.charCode )
    {
        key = event.charCode; /* Safari2 (Mac) (and probably Konqueror on Linux, untested) */
    }
    else
    {
        if ( event.keyCode )
        {
            key = event.keyCode; /* Firefox1.5 (Mac/Win), Opera9 (Mac/Win), IE6, IE7Beta2, Netscape7.2 (Mac) */
        }
        else
        {
            if ( event.which )
            {
                key = event.which; /* Older Netscape? (No browsers triggered yet) */
            }
        }
    }
   
    if ( key == 13 ) /* CR */
    { 
        nextField = getNextEntryField( field );
        if ( nextField )
        {
            nextField.focus(); /* Does not seem to actually work in Safari, unless you also have an Alert in between */
        }
        return true;
    }
    
    /* Illegal characters can be removed with a new if-block and return false */
    return true;
}

function getNextEntryField( field )
{
    var inputs = document.getElementsByName( "entryfield" );
    
    // Simple bubble sort
    for ( var i = 0; i < inputs.length - 1; ++i )
    {
        for ( var j = i + 1; j < inputs.length; ++j )
        {
            if ( inputs[i].tabIndex > inputs[j].tabIndex )
            {
                tmp = inputs[i];
                inputs[i] = inputs[j];
                inputs[j] = tmp;
            }
        }
    }
    
    i = 0;
    for ( ; i < inputs.length; ++i )
    {
        if ( inputs[i] == field )
        {
            break;
        }
    }
    
    if ( i == inputs.length - 1 )
    {
        // No more fields after this:
        return false;
    }
    else
    {
        return inputs[i + 1];
    }
}

//-----------------------------------------------------------------
// Save value for dataElement of type text, number, boolean, combo
//-----------------------------------------------------------------

function ValueSaver( dataElementId_, value_, dataElementType_, resultColor_  )
{
    var SUCCESS = '#ccffcc';
    var ERROR = '#ccccff';
	
    var dataElementId = dataElementId_;
	var providedElsewhereId = getFieldValue('programStageId') + "_" + dataElementId_ + "_facility";
	var value = value_;
	var type = dataElementType_;
    var resultColor = resultColor_;
	
    this.save = function()
    {
		var params  = 'dataElementId=' + dataElementId;
			params += '&programStageInstanceId=' + getFieldValue('programStageInstanceId');
		
		params += '&providedElsewhere=';
		if( byId( providedElsewhereId ) != null )
			params += byId( providedElsewhereId ).checked;
		
		params += '&value=';
		if( value != '')
			params += htmlEncode(value);
		
		$.ajax({
			   type: "POST",
			   url: "saveValue.action",
			   data: params,
			   dataType: "xml",
			   success: function(result){
					handleResponse (result);
			   },
			   error: function(request,status,errorThrown) {
					handleHttpError (request);
			   }
			});
    };
 
    function handleResponse( rootElement )
    {
        var codeElement = rootElement.getElementsByTagName( 'code' )[0];
        var code = parseInt( codeElement.firstChild.nodeValue );
        if ( code == 0 )
        {
            markValue( resultColor );
        }
        else
        {
            if(value!="")
            {
                markValue( ERROR );
                window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
            }
            else
            {
                markValue( resultColor );
            }
        }
    }
 
    function handleHttpError( errorCode )
    {
        markValue( ERROR );
        window.alert( i18n_saving_value_failed_error_code + '\n\n' + errorCode );
    }
 
    function markValue( color )
    {
		var programStageId = getFieldValue('programStageId');
        var element = byId( programStageId + "-" + dataElementId + '-val' );
        element.style.backgroundColor = color;
    }
}

function FacilitySaver( dataElementId_, providedElsewhere_, resultColor_ )
{
    var SUCCESS = 'success';
    var ERROR = '#error';
	
    var dataElementId = dataElementId_;
	var providedElsewhere = providedElsewhere_;
    var resultColor = resultColor_;

    this.save = function()
    {
		var params  = 'dataElementId=' + dataElementId;
			params += '&providedElsewhere=' + providedElsewhere ;
		$.ajax({
			   type: "POST",
			   url: "saveProvidingFacility.action",
			   data: params,
			   dataType: "xml",
			   success: function(result){
					handleResponse (result);
			   },
			   error: function(request,status,errorThrown) {
					handleHttpError (request);
			   }
			});
    };

    function handleResponse( rootElement )
    {
        var codeElement = rootElement.getElementsByTagName( 'code' )[0];
        var code = parseInt( codeElement.firstChild.nodeValue );
        if ( code == 0 )
        {
            markValue( SUCCESS );
        }
        else
        {
            markValue( ERROR );
            window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
        }
    }

    function handleHttpError( errorCode )
    {
        markValue( ERROR );
        window.alert( i18n_saving_value_failed_error_code + '\n\n' + errorCode );
    }

    function markValue( result )
    {
		var programStageId = byId( 'programStageId' ).value;
        if( result == SUCCESS )
        {
            jQuery('label[for="' + programStageId + '_facility"]').toggleClass('checked');
        }
        else if( result == ERROR )
        {
            jQuery('label[for="' + programStageId + '_facility"]').removeClass('checked');
            jQuery('label[for="' + programStageId + '_facility"]').addClass('error');
        }
    }
}

function ExecutionDateSaver( programStageId_, executionDate_, resultColor_ )
{
    var SUCCESS = '#ccffcc';
    var ERROR = '#ffcc00';
	
    var programStageId = programStageId_;
    var executionDate = executionDate_;
    var resultColor = resultColor_;

    this.save = function()
    {
		var params  = "executionDate=" + executionDate;
			params += "&programStageId=" + programStageId;
			
		$.ajax({
			   type: "POST",
			   url: "saveExecutionDate.action",
			   data: params,
			   dataType: "xml",
			   success: function(result){
					var selectedProgramStageInstance = jQuery( '#' + prefixId + getFieldValue('programStageInstanceId') );
					jQuery(".stage-object-selected").css('border-color', COLOR_LIGHTRED);
					jQuery(".stage-object-selected").css('background-color', COLOR_LIGHT_LIGHTRED);
					enable('completeBtn');
					enable('completeInBelowBtn');
					enable('validationBtn');
					enable('validationInBelowBtn');
					disable('newEncounterBtn');
					setFieldValue( 'programStageId', selectedProgramStageInstance.attr('psid') );
					
					handleResponse (result);
			   },
			   error: function(request,status,errorThrown) {
					handleHttpError (request);
			   }
			});
    };

    function handleResponse( rootElement )
    {
		rootElement = rootElement.getElementsByTagName( 'message' )[0];
        var codeElement = rootElement.getAttribute( 'type' );
        if ( codeElement == 'success' )
        {
            markValue( resultColor );
			if( getFieldValue('programStageInstanceId' )=='' )
			{
				var programStageInstanceId = rootElement.firstChild.nodeValue;
				setFieldValue('programStageInstanceId', programStageInstanceId);
				loadDataEntry( getFieldValue('programStageId') );
			}
			else
			{
				showById('entryFormContainer');
				showById('dataEntryFormDiv');
				showById('entryForm');
			}
        }
        else
        {
            if( executionDate != "")
            {
                markValue( ERROR );
                showWarningMessage( i18n_invalid_date );
            }
            else
            {
                markValue( ERROR );
				showWarningMessage( i18n_please_enter_report_date );
            }
			hideById('dataEntryFormDiv');
			hideById('inputCriteriaDiv');
        }
    }

    function handleHttpError( errorCode )
    {
        markValue( ERROR );
        window.alert( i18n_saving_value_failed_error_code + '\n\n' + errorCode );
    }

    function markValue( color )
    {
        var element = document.getElementById( 'executionDate' );
           
        element.style.backgroundColor = color;
    }
}

//-----------------------------------------------------------------
//
//-----------------------------------------------------------------

function toggleContentForReportDate(show)
{
    if( show ){
        jQuery("#entryForm").show();
    }else {
        jQuery("#entryForm").hide();
    }
}

function doComplete()
{
    var flag = false;
    jQuery("#dataEntryFormDiv input[name='entryfield'],select[name='entryselect']").each(function(){
        jQuery(this).parent().removeClass("errorCell");
        if( jQuery(this).metadata({
            "type":"attr",
            "name":"data"
        }).compulsory ){
            if( !jQuery(this).val() || jQuery(this).val() == "undifined" ){
                flag = true;
                jQuery(this).parent().addClass("errorCell");
            }
        }
    });
    if( flag ){
        alert(i18n_error_required_field);
        return;
    }else {
        if( confirm(i18n_complete_confirm_message) )
		{
			$.postJSON( "completeDataEntry.action",
				{
					programStageInstanceId: getFieldValue('programStageInstanceId')
				},
				function (data)
				{
					jQuery(".stage-object-selected").css('border-color', COLOR_GREEN);
					jQuery(".stage-object-selected").css('background-color', COLOR_LIGHT_GREEN);

					disable('completeBtn');
					disable('completeInBelowBtn');
					enable('newEncounterBtn');
					var irregular = jQuery('#entryFormContainer [name=irregular]').val();
					if( irregular == 'true' )
					{
						jQuery('#createNewEncounterDiv').dialog({
								title: i18n_create_new_event,
								maximize: true, 
								closable: true,
								modal:false,
								overlay:{background:'#000000', opacity:0.1},
								width: 300,
								height: 100
							}).show('fast');
							
						var standardInterval =  jQuery('#dataRecordingSelectDiv [name=programStageId] option:selected').attr('standardInterval');
						var date = new Date();
						var d = date.getDate() + eval(standardInterval);
						var m = date.getMonth();
						var y = date.getFullYear();
						var edate= new Date(y, m, d);
												
						jQuery('#dueDateNewEncounter').datepicker( "setDate" , edate )
					}
					
					var selectedProgram = jQuery('#dataRecordingSelectForm [name=programId] option:selected');
					if( selectedProgram.attr('type')=='2' && irregular == 'false' )
					{
						selectedProgram.remove();
					}
					
					enable('createEventBtn');
					selection.enable();
					hideLoader();
					hideById('contentDiv');
				});
		}
    }
}

function closeDueDateDiv()
{
	jQuery('#createNewEncounterDiv').dialog('close');
}

TOGGLE = {
    init : function() {
        jQuery(".togglePanel").each(function(){
            jQuery(this).next("table:first").addClass("sectionClose");
            jQuery(this).addClass("close");
            jQuery(this).click(function(){
                var table = jQuery(this).next("table:first");
                if( table.hasClass("sectionClose")){
                    table.removeClass("sectionClose").addClass("sectionOpen");
                    jQuery(this).removeClass("close").addClass("open");
                    window.scroll(0,jQuery(this).position().top);
                }else if( table.hasClass("sectionOpen")){
                    table.removeClass("sectionOpen").addClass("sectionClose");
                    jQuery(this).removeClass("open").addClass("close");
                }
            });
        });
    }
};

function initCustomCheckboxes()
{
    jQuery('input[type=checkbox]').prettyCheckboxes();
}

function entryFormContainerOnReady()
{
	var currentFocus = undefined;

    if( jQuery("#entryFormContainer") ) {
		
        if( jQuery("#executionDate").val() != '' )
        {
            toggleContentForReportDate(true);
        }
		
        jQuery("input[name='entryfield'],select[name='entryselect']").each(function(){
            jQuery(this).focus(function(){
                currentFocus = this;
            });
            
            jQuery(this).addClass("inputText");
        });
		
        TOGGLE.init();
				
		jQuery("#entryForm :input").each(function()
		{ 
			if( jQuery(this).attr( 'options' )!= null )
			{
				autocompletedField(jQuery(this).attr('id'));
			}
		});
    }
}

//------------------------------------------------------
// Run validation
//------------------------------------------------------

function runValidation()
{
	$('#validateProgramDiv' ).load( 'validateProgram.action' ).dialog({
			title: i18n_violate_validation,
			maximize: true, 
			closable: true,
			modal:true,
			overlay:{background:'#000000', opacity:0.1},
			width: 800,
			height: 450
		});
}

//------------------------------------------------------
// Register Irregular-encounter
//------------------------------------------------------

function registerIrregularEncounter( dueDate )
{
	jQuery.postJSON( "registerIrregularEncounter.action",{ dueDate: dueDate }, 
		function( json ) 
		{   
			var programStageInstanceId = json.message;
			jQuery('#createNewEncounterDiv').dialog('close');
			enable('completeBtn');
			enable('completeInBelowBtn');
			disable('newEncounterBtn');
			
			var programStageName = jQuery(".stage-object-selected").attr('psname');
			var flag = false;
			jQuery("#programStageIdTR input[name='programStageBtn']").each(function(i,item){
				var element = jQuery(item);
				var dueDateInStage = element.attr('dueDate');
				
				if( dueDate < dueDateInStage && !flag)
				{	
					var elementId = prefixId + programStageInstanceId;
					jQuery('<td><input name="programStageBtn" '
						+ 'id="' + elementId + '" ' 
						+ 'psid="' + programStageInstanceId + '" '
						+ 'psname="' + programStageName + '" '
						+ 'dueDate="' + dueDate + '" '
						+ 'value="'+ programStageName + ' ' + dueDate + '" '
						+ 'onclick="javascript:loadDataEntry(' + programStageInstanceId + ')" '
						+ 'type="button" class="stage-object" '
						+ '></td>'
						+ '<td><img src="images/rightarrow.png"></td>')
					.insertBefore(element.parent());
					setEventColorStatus( elementId, 3 );
					flag = true;
				}
			});
		});
}

function autocompletedField( idField )
{
	var input = jQuery( "#" +  idField )
	var dataElementId = input.attr( 'dataElementId' );
	var options = new Array();
	options = input.attr('options').replace('[', '').replace(']', '').split(', ');
	options.push(" ");

	input.autocomplete({
			delay: 0,
			minLength: 0,
			source: options,
			select: function( event, ui ) {
				input.val(ui.item.value);
				saveVal( dataElementId );
				input.autocomplete( "close" );
			},
			change: function( event, ui ) {
				if ( !ui.item ) {
					var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( $(this).val() ) + "$", "i" ),
						valid = false;
					for (var i = 0; i < options.length; i++)
					{
						if (options[i].match( matcher ) ) {
							this.selected = valid = true;
							break;
						}
					}
					if ( !valid ) {
						// remove invalid value, as it didn't match anything
						$( this ).val( "" );
						input.data( "autocomplete" ).term = "";
						return false;
					}
				}
				saveVal( dataElementId );
			}
		})
		.addClass( "ui-widget" );

	this.button = $( "<button type='button'>&nbsp;</button>" )
		.attr( "tabIndex", -1 )
		.attr( "title", i18n_show_all_items )
		.insertAfter( input )
		.button({
			icons: {
				primary: "ui-icon-triangle-1-s"
			},
			text: false
		})
		.addClass( "optionset-small-button" )
		.click(function() {
			// close if already visible
			if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
				input.autocomplete( "close" );
				return;
			}

			// work around a bug (likely same cause as #5265)
			$( this ).blur();

			// pass empty string as value to search for, displaying all results
			input.autocomplete( "search", "" );
			input.focus();
		});
}

/* function hexToR(h) {
	return parseInt((cutHex(h)).substring(0,2),16) + 
	parseInt((cutHex(h)).substring(2,4),16);
	parseInt((cutHex(h)).substring(2,4),16);
} */

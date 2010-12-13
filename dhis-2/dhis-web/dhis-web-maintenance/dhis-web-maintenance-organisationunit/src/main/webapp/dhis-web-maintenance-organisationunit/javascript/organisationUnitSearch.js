
function organisationUnitSelected( orgUnitIds )
{
	if ( orgUnitIds[0] && orgUnitIds[0] > 0 ) {
	    $.getJSON( "../dhis-web-commons-ajax-json/getOrganisationUnit.action?id=" + orgUnitIds[0], function( data ) {        
	    	$( "#selectedOrganisationUnit" ).val( "[ " + data.organisationUnit.name + " ]" );
	    } );
	}
	else {
		$( "#selectedOrganisationUnit" ).val( "[ " + i18n_all + " ]" );
	}
}

selection.setListenerFunction( organisationUnitSelected );

function initOrgUnitDetails()
{
	$( "#organisationUnitDetails" ).dialog( {
		modal:true,
		autoOpen:false,
		width:600,
		height:600
	} );
}

function showOrgUnitDetails( id )
{
	$( "#organisationUnitDetails" ).load( "getOrganisationUnitDetails.action?id=" + id, function() {
		$( "#organisationUnitDetails" ).dialog( "open" );
	} );
}

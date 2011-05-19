// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showPatientAttributeDetails( patientAttributeId )
{
    var request = new Request();
    request.setResponseTypeXML( 'patientAttribute' );
    request.setCallbackSuccess( patientAttributeReceived );
    request.send( 'getPatientAttribute.action?id=' + patientAttributeId );
}

function patientAttributeReceived( patientAttributeElement )
{
	setInnerHTML( 'idField', getElementValue( patientAttributeElement, 'id' ) );
	setInnerHTML( 'nameField', getElementValue( patientAttributeElement, 'name' ) );	
    setInnerHTML( 'descriptionField', getElementValue( patientAttributeElement, 'description' ) );
    
    var valueTypeMap = { 'NUMBER':i18n_number, 'BOOL':i18n_yes_no, 'TEXT':i18n_text, 'DATE':i18n_date, 'COMBO':i18n_combo };
    var valueType = getElementValue( patientAttributeElement, 'valueType' );    
    
    setInnerHTML( 'valueTypeField', valueTypeMap[valueType] );    
   
    showDetails();
}

// -----------------------------------------------------------------------------
// Remove Patient Attribute
// -----------------------------------------------------------------------------

function removePatientAttribute( patientAttributeId, name )
{
	removeItem( patientAttributeId, name, i18n_confirm_delete, 'removePatientAttribute.action' );	
}

ATTRIBUTE_OPTION = 
{
	selectValueType : 	function (this_)
	{
		if ( jQuery(this_).val() == "COMBO" )
		{
			jQuery("#attributeComboRow").show();
			if( jQuery("#attrOptionContainer").find("input").length ==0 ) 
			{
				ATTRIBUTE_OPTION.addOption();
				ATTRIBUTE_OPTION.addOption();
			}
		}else {
			jQuery("#attributeComboRow").hide();
		}
	},
	checkOnSubmit : function ()
	{
		if( jQuery("#valueType").val() != "COMBO" ) 
		{
			jQuery("#attrOptionContainer").children().remove();
			return true;
		}else {
			$("input","#attrOptionContainer").each(function(){ 
				if( !jQuery(this).val() )
					jQuery(this).remove();
			});
			if( $("input","#attrOptionContainer").length < 2)
			{
				alert(i118_at_least_2_option);
				return false;
			}else return true;
		}
	},
	addOption : function ()
	{
		jQuery("#attrOptionContainer").append(ATTRIBUTE_OPTION.createInput());
	},
	remove : function (this_, optionId)
	{
		
		if( jQuery(this_).siblings("input").attr("name") != "attrOptions")
		{
			jQuery.get("removePatientAttributeOption.action?id="+optionId,function(data){
				if( data.response == "success")
				{
					jQuery(this_).parent().parent().remove();
					showSuccessMessage( data.message );
				}else 
				{
					showErrorMessage( data.message );
				}
			});
		}else
		{
			jQuery(this_).parent().parent().remove();
		}
	},
	removeInAddForm : function(this_)
	{
		jQuery(this_).parent().parent().remove();
	},
	createInput : function ()
	{
		return "<tr><td><input type='text' name='attrOptions' style='width:28em'/><a href='#' style='text-decoration: none; margin-left:0.5em;' title='"+i18n_remove_option+"'  onClick='ATTRIBUTE_OPTION.remove(this,null)'>[ - ]</a></td></tr>";
	}
}

// ------------------------------------------------------------------
// Add Patient-attribute
// ------------------------------------------------------------------

function showAddPatientAttributeForm()
{
	hideById('attributeList');
	jQuery('#loaderDiv').show();
	jQuery('#editPatientAttributeForm').load('showAddPatientAttributeForm.action',
	{
	}, function()
	{
		showById('editPatientAttributeForm');
		jQuery('#loaderDiv').hide();
	});
}

function addPatientAttribute()
{	
	$.ajax({
		type: "POST",
		url: 'addPatientAttribute.action',
		data: getParamsForDiv('editPatientAttributeForm'),
		success: function( json ) {
			if( json.response == 'success')
			{
				onClickBackBtn();
			}
		}
	});
	
    return false;
}

// ------------------------------------------------------------------
// Show Update Patient-attribute
// ------------------------------------------------------------------

function showUpdatePatientAttributeForm( attributeId )
{
	hideById('attributeList');
	jQuery('#loaderDiv').show();
	jQuery('#editPatientAttributeForm').load('showUpdatePatientAttributeForm.action',
	{
		id:attributeId
	}, function()
	{
		showById('editPatientAttributeForm');
		jQuery('#loaderDiv').hide();
	});
	
}

function updatePatientAttribute()
{
	$.ajax({
		type: "POST",
		url: 'updatePatientAttribute.action',
		data: getParamsForDiv('editPatientAttributeForm'),
		success: function( json ) {
			if( json.response == 'success')
			{
				onClickBackBtn();
			}
		}
	});
    return false;
}

// ------------------------------------------------------------------
// Click Back button
// ------------------------------------------------------------------

function onClickBackBtn()
{
	hideById('editPatientAttributeForm');	
	jQuery('#loaderDiv').show();
	jQuery('#attributeList').load('patientAttributeList.action',
	{
	}, function()
	{
		showById('attributeList');
		jQuery('#loaderDiv').hide();
	});
}	
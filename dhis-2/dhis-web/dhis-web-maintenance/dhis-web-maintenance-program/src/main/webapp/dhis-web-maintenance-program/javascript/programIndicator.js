$(function() {
  dhis2.contextmenu.makeContextMenu({
    menuId: 'contextMenu',
    menuItemActiveClass: 'contextMenuItemActive'
  });
});

// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showUpdateProgramIndicator( context ) {
  location.href = 'showUpdateProgramIndicator.action?id=' + context.id;
}

function removeIndicator( context ) {
  removeItem( context.id, context.name, i18n_confirm_delete , 'removeProgramIndicator.action' );
}

function showProgramIndicatorDetails( context ) {
  jQuery.getJSON('getProgramIndicator.action', { id: context.id }, function( json ) {
    setInnerHTML('nameField', json.programIndicator.name);
    setInnerHTML('codeField', json.programIndicator.code);
    setInnerHTML('descriptionField', json.programIndicator.description);
    setInnerHTML('valueTypeField', json.programIndicator.valueType);
    setInnerHTML('rootDateField', json.programIndicator.rootDate);
    setInnerHTML('expressionField', json.programIndicator.expression);
    setInnerHTML('idField', json.programIndicator.uid);

    showDetails();
  });
}

// -----------------------------------------------------------------------------
// Remove Program Indicator
// -----------------------------------------------------------------------------

function removeProgramIndicator( context ) {
  removeItem(context.id, context.name, i18n_confirm_delete, 'removeProgramIndicator.action');
}

function filterExpressionSelect( event, value, fieldName ) {
	var field = byId(fieldName);
	
	for ( var index = 0; index < field.options.length; index++ )
    {
		var option = field.options[index];
		
		if ( value.length == 0 || option.text.toLowerCase().indexOf( value.toLowerCase() ) != -1 )
		{
			option.style.display = "block";
		}
		else
		{
			option.style.display = "none";
		}
    }	    
}

function getTrackedEntityDataElements( type ) {
  var fieldId = type + '-data-elements';
  clearListById(fieldId);
  var programStageId = getFieldValue('programStageId');

  jQuery.getJSON('getTrackedEntityDataElements.action',
    {
      programId: getFieldValue('programId'),
      programStageId: programStageId
    }
    , function( json ) {
      var dataElements = jQuery('#' + fieldId);
      for( i in json.dataElements ) {
        if( json.dataElements[i].type == 'int' || json.dataElements[i].type == 'date' ) {
          dataElements.append("<option value='" + json.dataElements[i].id + "' title='" + json.dataElements[i].name + "' suggested='" + json.dataElements[i].optionset + "'>" + json.dataElements[i].name + "</option>");
        }
      }
    });
}

function insertDataElement( type ) {
  var psFieldId = type + '-program-stage',
      deFieldId = type + '-data-elements',
      areaId = type,
      programStageId = getFieldValue(psFieldId),
      dataElementId = getFieldValue(deFieldId);

  insertTextCommon(areaId, "#{" + programStageId + "." + dataElementId + "}");
  getExpressionDescription( type );
}

function insertAttribute( type ){
  var atFieldId = type + '-attributes',
      areaId = type,
      attributeId = getFieldValue(atFieldId);

  insertTextCommon(areaId, "A{" + attributeId + "}");
  getExpressionDescription( type );
}

function insertVariable( type ){
  var varFieldId = type + '-variables',
      areaId = type,
      variableId = getFieldValue(varFieldId);

  insertTextCommon(areaId, "V{" + variableId + "}");
  getExpressionDescription( type );
}

function insertConstant( type ){
  var coFieldId = type + '-constants',
      areaId = type,
      constantId = getFieldValue(coFieldId);

  insertTextCommon(areaId, "C{" + constantId + "}");
  getExpressionDescription( type );
}

function insertOperator( type, value ) {
  insertTextCommon(type, ' ' + value + ' ');
  getExpressionDescription( type );
}

function getExpressionDescription( type ) {
	var expression = getFieldValue( type );
	
	if( expression == '' )
	{
		setInnerHTML(type + '-description', '');
	}
	else
	{
		$.getJSON('../api/programIndicators/' + type + '/description', {
			expression: expression
		}, function( json ) {
			if( json.valid ){
				setFieldValue(type + '-check', json.message);
				setInnerHTML(type + '-description', json.description);
			}
			else {
				setFieldValue(type + '-check','');
				setInnerHTML(type + '-description', json.message);
			}
		});
	}
}

function programIndicatorOnChange() {
  var valueType = getFieldValue('valueType');
  if( valueType == 'int' ) {
    hideById('rootDateTR');
    disable('rootDate');
  }
  else {
    showById('rootDateTR');
    enable('rootDate');
  }
}

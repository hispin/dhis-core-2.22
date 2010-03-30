
// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showDataElementCategoryDetails( categoryId )
{	
    var request = new Request();
    request.setResponseTypeXML( 'dataElementCategory' );
    request.setCallbackSuccess( dataElementCategoryReceived );
    request.send( 'getDataElementCategory.action?id=' + categoryId );
}

function dataElementCategoryReceived( categoryElement )
{
    setFieldValue( 'nameField', getElementValue( categoryElement, 'name' ) );    
    setFieldValue( 'categoryOptionsCountField', getElementValue( categoryElement, 'categoryOptionCount' ) );
          
    showDetails();
}

// -----------------------------------------------------------------------------
// Delete Category
// -----------------------------------------------------------------------------

function removeDataElementCategory( categoryId, categoryName )
{
	removeItem( categoryId, categoryName, i18n_confirm_delete, 'removeDataElementCategory.action' );
}

function addCategoryOptionToCategory( categoryName )
{
	if ( listContainsById( 'categoryOptionNames', categoryName ) )
	{
		setMessage( i18n_category_option_name_already_exists );
	}
	else
	{
		hideById( "message" );
		addOption( 'categoryOptionNames', categoryName, categoryName );
		document.getElementById( 'categoryOptionName' ).value = "";
	}
}

// ----------------------------------------------------------------------
// Validation
// ----------------------------------------------------------------------

function validateAddDataElementCategory()
{
    var request = new Request();
    request.setResponseTypeXML( 'message' );
    request.setCallbackSuccess( addDataElementCategoryValidationCompleted );

    if ( document.getElementById( 'categoryOptionNames' ).options.length == 0 )
    {
        setMessage( i18n_must_include_category_option );
        return;
    }

    var requestString = 'validateDataElementCategory.action?name=' + getFieldValue( 'name' );

    requestString += "&conceptName=" + getFieldValue( 'conceptName' );

    requestString += "&" + getParamString( 'categoryOptionNames' );

    request.send( requestString );
  
    return false;
}

function addDataElementCategoryValidationCompleted( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;

    if ( type == 'success' )
    {
  	    selectAllById( 'categoryOptionNames' );
        document.getElementById( 'addDataElementCategoryForm' ).submit();
    }  
    else if ( type == 'input' )
    {
  	    setMessage( message );
    }
}

function validateEditDataElementCategory()
{
    var request = new Request();
    request.setResponseTypeXML( 'message' );
    request.setCallbackSuccess( editDataElementCategoryValidationCompleted );
  
    var requestString = 'validateDataElementCategory.action?id=' + getFieldValue( 'id' ) + 
        '&name=' + getFieldValue( 'name' );

    requestString += "&conceptName=" + htmlEncode( document.getElementById( 'conceptName' ).value );

    requestString += "&" + getParamString( 'categoryOptions' );
  
    request.send( requestString );
    
    return false;
}

function editDataElementCategoryValidationCompleted( messageElement )
{
    var type = messageElement.getAttribute( 'type' );
    var message = messageElement.firstChild.nodeValue;

    if ( type == 'success' )
    {
        selectAllById( 'categoryOptions' );
        document.getElementById( 'editDataElementCategoryForm' ).submit();
    }
    else if ( type == 'input' )
    {
        setMessage( message );
    }
}

function validateAddCategoryOption() {

	var categoryName = $( "#categoryOptionName" ).val();
	
	$.post( "validateDataElementCategoryOption.action", 
	{
		name:categoryName
	},
	function ( xmlObject ) 
	{
		xmlObject = xmlObject.getElementsByTagName( "message" )[0];
		var type = xmlObject.getAttribute( "type" );
		
		if ( type == "input" ) 
		{
			setMessage( xmlObject.firstChild.nodeValue );
		}
		else if(mode == "update"){
			updateCategoryOptionName();
		}
		else
		{
			addCategoryOptionToCategory( categoryName );
		}
	}, "xml");
}


// ----------------------------------------------------------------------
// Rename Category Option
// ----------------------------------------------------------------------

function updateCategoryOptionName(){
	validateAddCategoryOption();
	var request = new Request();
    request.setResponseTypeXML( 'xmlObject' );
    request.setCallbackSuccess( updateCategoryOptionNameReceived );
	var params = "id=" + byId('categoryOptions').value;
		params += '&name=' + byId('categoryOptionName').value;
	request.sendAsPost(params);
	request.send('updateDataElementCategoryOption.action');
}

function updateCategoryOptionNameReceived(xmlObject){
	var categoryOptions = byId('categoryOptions');
		categoryOptions[categoryOptions.selectedIndex].text = byId('categoryOptionName').value;
	setMessage( i18n_option_rename_successfully );
	
}


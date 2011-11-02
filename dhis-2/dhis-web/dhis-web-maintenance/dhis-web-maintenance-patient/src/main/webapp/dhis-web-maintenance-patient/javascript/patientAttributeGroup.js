// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showPatientAttributeGroupDetails( patientAttributeGroupId )
{
	jQuery.post( 'getPatientAttributeGroup.action', { id: patientAttributeGroupId },
		function ( json ) {
				
			setInnerHTML( 'idField', json.patientAttributeGroup.id );
			setInnerHTML( 'nameField', json.patientAttributeGroup.name );	
			setInnerHTML( 'descriptionField', json.patientAttributeGroup.description );
			setInnerHTML( 'noAttributeField', json.patientAttributeGroup.noAttribute );

			showDetails();
	});
}

// -----------------------------------------------------------------------------
// Remove Patient Attribute
// -----------------------------------------------------------------------------

function removePatientAttributeGroup( patientAttributeGroupId, name )
{
    removeItem( patientAttributeGroupId, name, i18n_confirm_delete, 'removePatientAttributeGroup.action' );
}
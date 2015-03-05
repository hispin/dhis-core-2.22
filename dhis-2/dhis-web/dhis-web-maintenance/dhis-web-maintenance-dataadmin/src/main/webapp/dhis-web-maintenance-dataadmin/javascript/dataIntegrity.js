$( document ).ready( function() {
    showLoader();

    $.ajax({
        url: '../api/dataIntegrity',
        method: 'POST',
        success: pollDataIntegrityCheckFinished,
        error: function( xhr, txtStatus, err ) {
            showErrorMessage( "Data integrity checks cannot be run. Request failed.", 3 );
            throw Error( xhr.responseText );
        }
    } );
} );

var checkFinishedTimeout = null;

function pollDataIntegrityCheckFinished() {
    pingNotifications( 'DATAINTEGRITY', 'notificationsTable', function() {
        $.getJSON( "getDataIntegrityReport.action", {}, function( json ) {
            hideLoader();
            $( "#di-title" ).hide();
            $( "#di-completed" ).show();
            populateIntegrityItems( json );
            clearTimeout( checkFinishedTimeout );
        } );
    } );
    checkFinishedTimeout = setTimeout( "pollDataIntegrityCheckFinished()", 1500 );
}

function populateIntegrityItems( json ) {
    displayViolationList( json.dataElementsWithoutDataSet, "dataElementsWithoutDataSet", false );
    displayViolationList( json.dataElementsWithoutGroups, "dataElementsWithoutGroups", false );
    displayViolationList( json.dataElementsViolatingExclusiveGroupSets, "dataElementsViolatingExclusiveGroupSets", true );
    displayViolationList( json.dataElementsInDataSetNotInForm, "dataElementsInDataSetNotInForm", true );
    displayViolationList( json.dataElementsAssignedToDataSetsWithDifferentPeriodTypes, "dataElementsAssignedToDataSetsWithDifferentPeriodTypes", true );
    displayViolationList( json.categoryOptionCombosNotInDataElementCategoryCombo, "categoryOptionCombosNotInDataElementCategoryCombo", true );
    displayViolationList( json.dataSetsNotAssignedToOrganisationUnits, "dataSetsNotAssignedToOrganisationUnits", false );
    displayViolationList( json.sectionsWithInvalidCategoryCombinations, "sectionsWithInvalidCategoryCombinations", false );
    displayViolationList( json.indicatorsWithIdenticalFormulas, "indicatorsWithIdenticalFormulas", false );
    displayViolationList( json.indicatorsWithoutGroups, "indicatorsWithoutGroups", false );
    displayViolationList( json.invalidIndicatorNumerators, "invalidIndicatorNumerators", true );
    displayViolationList( json.invalidIndicatorDenominators, "invalidIndicatorDenominators", true );
    displayViolationList( json.indicatorsViolatingExclusiveGroupSets, "indicatorsViolatingExclusiveGroupSets", true );
    displayViolationList( json.organisationUnitsWithCyclicReferences, "organisationUnitsWithCyclicReferences", false );
    displayViolationList( json.orphanedOrganisationUnits, "orphanedOrganisationUnits", false );
    displayViolationList( json.organisationUnitsWithoutGroups, "organisationUnitsWithoutGroups", false );
    displayViolationList( json.organisationUnitsViolatingExclusiveGroupSets, "organisationUnitsViolatingExclusiveGroupSets", true );
    displayViolationList( json.organisationUnitGroupsWithoutGroupSets, "organisationUnitGroupsWithoutGroupSets", false );
    displayViolationList( json.duplicatePeriods, "duplicatePeriods", false );
    displayViolationList( json.validationRulesWithoutGroups, "validationRulesWithoutGroups", false );
    displayViolationList( json.invalidValidationRuleLeftSideExpressions, "invalidValidationRuleLeftSideExpressions", true );
    displayViolationList( json.invalidValidationRuleRightSideExpressions, "invalidValidationRuleRightSideExpressions", true );
}

function displayViolationList( list, id, lineBreak ) {
    var $button = $( "#" + id + "Button" );
    var $container = $( "#" + id + "Div" );

    if ( list.length > 0 ) {
        // Display image "drop-down" button
        $button
           .attr( { src: "../images/down.png", title: "View violations" } )
           .css( { cursor: "pointer" } )
           .click( function() { $container.slideToggle( "fast" ); } );

        // Populate violation div
        var violations = "";
        
        for ( var i = 0; i < list.length; i++ ) {
            violations += list[i] + "<br>";
            violations += !!lineBreak ? "<br>" : "";
        }
        
        $container.html( violations );
    }
    else
    {
        // Display image "check" button
        $button.attr({ src: "../images/check.png", title: "No violations" });
    }
        
    $container.hide();
}

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.caseaggregation;

import java.util.Collection;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patientdatavalue.PatientDataValue;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStageInstance;

/**
 * @author Chau Thu Tran
 * 
 * @version CaseAggregationCondititionService.java Nov 17, 2010 10:56:29 AM
 */
public interface CaseAggregationConditionService
{
    int  addCaseAggregationCondition( CaseAggregationCondition caseAggregationCondition );

    void updateCaseAggregationCondition( CaseAggregationCondition caseAggregationCondition );

    void deleteCaseAggregationCondition( CaseAggregationCondition caseAggregationCondition );
    
    CaseAggregationCondition getCaseAggregationCondition( int id );
    
    Collection<CaseAggregationCondition> getAllCaseAggregationCondition( );
   
    CaseAggregationCondition getCaseAggregationCondition( DataElement dataElement, DataElementCategoryOptionCombo optionCombo);
    
    Collection<CaseAggregationCondition> getCaseAggregationCondition( DataElement dataElement );

    
    Double parseConditition( CaseAggregationCondition aggregationCondition, OrganisationUnit orgunit, Period period );
        
    Collection<PatientDataValue> getPatientDataValues( CaseAggregationCondition aggregationCondition, OrganisationUnit orgunit, Period period );
    
    Collection<Patient> getPatients( CaseAggregationCondition aggregationCondition, OrganisationUnit orgunit, Period period );
    
    Collection<ProgramStageInstance> getProgramStageInstances( CaseAggregationCondition aggregationCondition, OrganisationUnit orgunit, Period period );
    
    Collection<DataElement> getDataElementsInCondition( String aggregationExpression );
    
    Collection<DataElementCategoryOptionCombo> getOptionCombosInCondition( String aggregationExpression );
    
    Collection<Program> getProgramsInCondition( String aggregationExpression );
    
    Collection<PatientAttribute> getPatientAttributesInCondition( String aggregationExpression );
    
    String getConditionDescription( String condition );
}

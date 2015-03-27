package org.hisp.dhis.program;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import static org.hisp.dhis.program.ProgramIndicator.KEY_ATTRIBUTE;
import static org.hisp.dhis.program.ProgramIndicator.KEY_DATAELEMENT;
import static org.hisp.dhis.program.ProgramIndicator.KEY_PROGRAM_VARIABLE;
import static org.hisp.dhis.program.ProgramIndicator.VALUE_TYPE_DATE;
import static org.hisp.dhis.program.ProgramIndicator.VALUE_TYPE_INT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementDomain;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chau Thu Tran
 */
public class ProgramIndicatorServiceTest
    extends DhisSpringTest
{
    @Autowired
    private ProgramIndicatorService programIndicatorService;

    @Autowired
    private TrackedEntityAttributeService attributeService;
    
    @Autowired
    private TrackedEntityInstanceService entityInstanceService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramStageService programStageService;

    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private TrackedEntityDataValueService dataValueService;
    
    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private ProgramStageDataElementService programStageDataElementService;
  
    @Autowired
    private TrackedEntityAttributeValueService attributeValueService;

    @Autowired
    private ProgramStageInstanceService programStageInstanceService;
    

    @Autowired
    private ConstantService constantService;

    private Date incidenDate;

    private Date enrollmentDate;
    
    private ProgramStage psA;
    private ProgramStage psB;

    private Program programA;
    private Program programB;

    private ProgramInstance programInstance;
    
    private DataElement deA;    
    private DataElement deB;
    
    private TrackedEntityAttribute atA;
    private TrackedEntityAttribute atB;
    private TrackedEntityAttribute atC;
    private TrackedEntityAttribute atD;

    private ProgramIndicator indicatorDate;
    private ProgramIndicator indicatorInt;
    private ProgramIndicator indicatorC;
    private ProgramIndicator indicatorD;
    private ProgramIndicator indicatorE;
    private ProgramIndicator indicatorF;
    private ProgramIndicator indicatorG;
    private ProgramIndicator indicatorH;
    private ProgramIndicator indicatorI;
    private ProgramIndicator indicatorJ;
    
    @Override
    public void setUpTest()
    {
        OrganisationUnit organisationUnit = createOrganisationUnit( 'A' );
        organisationUnitService.addOrganisationUnit( organisationUnit );

        // ---------------------------------------------------------------------
        // Program 
        // ---------------------------------------------------------------------
        
        programA = createProgram( 'A', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programA );

        psA = new ProgramStage( "StageA", programA );
        psA.setSortOrder( 1 );
        programStageService.saveProgramStage( psA );

        psB = new ProgramStage( "StageB", programA );
        psB.setSortOrder( 2 );
        programStageService.saveProgramStage( psB );

        Set<ProgramStage> programStages = new HashSet<>();
        programStages.add( psA );
        programStages.add( psB );
        programA.setProgramStages( programStages );
        programService.updateProgram( programA );

        programB = createProgram( 'B', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programB );

        // ---------------------------------------------------------------------
        // Program Stage DE 
        // ---------------------------------------------------------------------
       
        deA = createDataElement( 'A' );
        deA.setDomainType( DataElementDomain.TRACKER );
	deA.setType( DataElement.VALUE_TYPE_NUMBER );
		
        deB = createDataElement( 'B' );
        deB.setDomainType( DataElementDomain.TRACKER );  
	deB.setType( DataElement.VALUE_TYPE_DATE );
        
        dataElementService.addDataElement( deA );
        dataElementService.addDataElement( deB );

        ProgramStageDataElement stageDataElementA = new ProgramStageDataElement( psA, deA, false, 1 );
        ProgramStageDataElement stageDataElementB = new ProgramStageDataElement( psA, deB, false, 2 );
        ProgramStageDataElement stageDataElementC = new ProgramStageDataElement( psB, deA, false, 1 );
        ProgramStageDataElement stageDataElementD = new ProgramStageDataElement( psB, deB, false, 2 );

        programStageDataElementService.addProgramStageDataElement( stageDataElementA );
        programStageDataElementService.addProgramStageDataElement( stageDataElementB );
        programStageDataElementService.addProgramStageDataElement( stageDataElementC );
        programStageDataElementService.addProgramStageDataElement( stageDataElementD );

        // ---------------------------------------------------------------------
        // TrackedEntityInstance & Enrollement
        // ---------------------------------------------------------------------       
        
        TrackedEntityInstance entityInstance = createTrackedEntityInstance( 'A', organisationUnit );
        entityInstanceService.addTrackedEntityInstance( entityInstance );

        incidenDate = DateUtils.getMediumDate( "2014-10-22" );
        enrollmentDate = DateUtils.getMediumDate( "2014-12-31" );

        programInstance = programInstanceService.enrollTrackedEntityInstance( entityInstance, programA, enrollmentDate, incidenDate,
            organisationUnit );

        incidenDate = DateUtils.getMediumDate( "2014-10-22" );
        enrollmentDate = DateUtils.getMediumDate( "2014-12-31" );

        programInstance = programInstanceService.enrollTrackedEntityInstance( entityInstance, programA, enrollmentDate, incidenDate,
            organisationUnit );
        
        // ---------------------------------------------------------------------
        // TrackedEntityAttribute
        // ---------------------------------------------------------------------       
        
        atA = createTrackedEntityAttribute( 'A', TrackedEntityAttribute.TYPE_NUMBER );
        atB = createTrackedEntityAttribute( 'B', TrackedEntityAttribute.TYPE_NUMBER );
        atC = createTrackedEntityAttribute( 'C', TrackedEntityAttribute.TYPE_DATE );
        atD = createTrackedEntityAttribute( 'D', TrackedEntityAttribute.TYPE_DATE );
        
        attributeService.addTrackedEntityAttribute( atA );
        attributeService.addTrackedEntityAttribute( atB );
        attributeService.addTrackedEntityAttribute( atC );
        attributeService.addTrackedEntityAttribute( atD );
        
      
        TrackedEntityAttributeValue attributeValueA = new TrackedEntityAttributeValue( atA, entityInstance, "1" );
        TrackedEntityAttributeValue attributeValueB = new TrackedEntityAttributeValue( atB, entityInstance, "2" );
        TrackedEntityAttributeValue attributeValueC = new TrackedEntityAttributeValue( atC, entityInstance, "2015-01-01" );
        TrackedEntityAttributeValue attributeValueD = new TrackedEntityAttributeValue( atD, entityInstance, "2015-01-03" );

        attributeValueService.addTrackedEntityAttributeValue( attributeValueA );
        attributeValueService.addTrackedEntityAttributeValue( attributeValueB );
        attributeValueService.addTrackedEntityAttributeValue( attributeValueC );
        attributeValueService.addTrackedEntityAttributeValue( attributeValueD );
        
        // ---------------------------------------------------------------------
        // TrackedEntityDataValue
        // ---------------------------------------------------------------------       
       
        ProgramStageInstance stageInstanceA = programStageInstanceService.getProgramStageInstance( programInstance, psA );
        ProgramStageInstance stageInstanceB = programStageInstanceService.getProgramStageInstance( programInstance, psB );

        Set<ProgramStageInstance> programStageInstances = new HashSet<>();
        programStageInstances.add( stageInstanceA );
        programStageInstances.add( stageInstanceB );
        programInstance.setProgramStageInstances( programStageInstances );
        
        
        TrackedEntityDataValue dataValueA = new TrackedEntityDataValue( stageInstanceA, deA, "3" );
        TrackedEntityDataValue dataValueB = new TrackedEntityDataValue( stageInstanceA, deB, "2015-03-01" );
        TrackedEntityDataValue dataValueC = new TrackedEntityDataValue( stageInstanceB, deA, "5" );
        TrackedEntityDataValue dataValueD = new TrackedEntityDataValue( stageInstanceB, deB, "2015-03-15" );
        
        
        dataValueService.saveTrackedEntityDataValue( dataValueA );
        dataValueService.saveTrackedEntityDataValue( dataValueB );
        dataValueService.saveTrackedEntityDataValue( dataValueC );
        dataValueService.saveTrackedEntityDataValue( dataValueD );

        // ---------------------------------------------------------------------
        // Constant
        // ---------------------------------------------------------------------       
                
        Constant constantA = createConstant( 'A', 7.0 );
        constantService.saveConstant( constantA );
        
        // ---------------------------------------------------------------------
        // ProgramIndicator
        // ---------------------------------------------------------------------       
       
        indicatorInt = new ProgramIndicator( "IndicatorA", "IndicatorDesA", VALUE_TYPE_INT, "( " + KEY_PROGRAM_VARIABLE + "{"
            + ProgramIndicator.ENROLLMENT_DATE + "} - " + KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.INCIDENT_DATE + "} )  / " 
            + ProgramIndicator.KEY_CONSTANT + "{" + constantA.getUid() + "}" );
        indicatorInt.setUid( "UID-DATE" );
        indicatorInt.setShortName( "DATE" );
        indicatorInt.setProgram( programA );

        indicatorDate = new ProgramIndicator( "IndicatorB", "IndicatorDesB", ProgramIndicator.VALUE_TYPE_DATE, "70" );
        indicatorDate.setRootDate( ProgramIndicator.INCIDENT_DATE );
        indicatorDate.setUid( "UID-INT" );
        indicatorDate.setShortName( "INT" );
        indicatorDate.setProgram( programA );

        indicatorC = new ProgramIndicator( "IndicatorC", "IndicatorDesB", ProgramIndicator.VALUE_TYPE_INT, "0" );
        indicatorC.setUid( "UID-C" );
        indicatorC.setShortName( "C" );
        indicatorC.setProgram( programB );
        
        indicatorD = new ProgramIndicator( "IndicatorD", "IndicatorDesD", ProgramIndicator.VALUE_TYPE_INT, "0 + A + 4 + " + ProgramIndicator.KEY_PROGRAM_VARIABLE + "{"
            + ProgramIndicator.INCIDENT_DATE + "}" );
        indicatorD.setUid( "UID-D" );
        indicatorD.setShortName( "D" );
        indicatorD.setProgram( programB );
        
        indicatorE = new ProgramIndicator( "IndicatorE", "IndicatorDesE", VALUE_TYPE_INT, 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "} + " + 
            KEY_DATAELEMENT + "{" + psB.getUid() + "." + deA.getUid() + "} - " + 
            KEY_ATTRIBUTE + "{" + atA.getUid() + "} + " + KEY_ATTRIBUTE + "{" + atB.getUid() + "}" );
        
        indicatorF = new ProgramIndicator( "IndicatorF", "IndicatorDesF", VALUE_TYPE_INT, "(" + 
            KEY_DATAELEMENT + "{" + psB.getUid() + "." + deB.getUid() + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " + 
            KEY_ATTRIBUTE + "{" + atA.getUid() + "} + " + KEY_ATTRIBUTE + "{" + atB.getUid() + "}" );
        

        indicatorG = new ProgramIndicator( "IndicatorG", "IndicatorDesG", VALUE_TYPE_INT, "(" + 
            KEY_DATAELEMENT + "{" + psB.getUid() + "." + deB.getUid() + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " +
            KEY_ATTRIBUTE + "{" + atA.getUid() + "} + " + 
            KEY_ATTRIBUTE + "{" + atB.getUid() + "} * " 
            + ProgramIndicator.KEY_CONSTANT + "{" + constantA.getUid() + "}"  );
        
        indicatorH = new ProgramIndicator( "IndicatorH", "IndicatorDesH", VALUE_TYPE_INT, "(" + 
            KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.CURRENT_DATE + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "}" );
        
        
        indicatorI = new ProgramIndicator( "IndicatorI", "IndicatorDesI", VALUE_TYPE_DATE, "(" + 
            KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.CURRENT_DATE + "} - " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deB.getUid() + "} ) + " + 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "}" ); 
        indicatorI.setRootDate( ProgramIndicator.INCIDENT_DATE );        

        indicatorJ = new ProgramIndicator( "IndicatorJ", "IndicatorDesJ", VALUE_TYPE_DATE, "(" +
            KEY_ATTRIBUTE + "{" + atC.getUid() + "}  - " + 
            KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.ENROLLMENT_DATE + "} ) + " +           
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "} * " + 
            ProgramIndicator.KEY_CONSTANT + "{" + constantA.getUid() + "}" ); 
        indicatorJ.setRootDate( ProgramIndicator.INCIDENT_DATE );
    }

    // -------------------------------------------------------------------------
    // CRUD tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );
        int idB = programIndicatorService.addProgramIndicator( indicatorInt );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testDeleteProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );
        int idB = programIndicatorService.addProgramIndicator( indicatorInt );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );

        programIndicatorService.deleteProgramIndicator( indicatorDate );

        assertNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );

        programIndicatorService.deleteProgramIndicator( indicatorInt );

        assertNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNull( programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testUpdateProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );

        indicatorDate.setName( "B" );
        programIndicatorService.updateProgramIndicator( indicatorDate );

        assertEquals( "B", programIndicatorService.getProgramIndicator( idA ).getName() );
    }

    @Test
    public void testGetProgramIndicatorById()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );
        int idB = programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( indicatorDate, programIndicatorService.getProgramIndicator( idA ) );
        assertEquals( indicatorInt, programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testGetProgramIndicatorByName()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( "IndicatorA", programIndicatorService.getProgramIndicator( "IndicatorA" ).getName() );
        assertEquals( "IndicatorB", programIndicatorService.getProgramIndicator( "IndicatorB" ).getName() );
    }

    @Test
    public void testGetAllProgramIndicators()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertTrue( equals( programIndicatorService.getAllProgramIndicators(), indicatorDate, indicatorInt ) );
    }

    @Test
    public void testGetProgramIndicatorByShortName()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( "INT", programIndicatorService.getProgramIndicatorByShortName( "INT" ).getShortName() );
        assertEquals( "DATE", programIndicatorService.getProgramIndicatorByShortName( "DATE" ).getShortName() );
    }

    @Test
    public void testGetProgramIndicatorByUid()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( "UID-INT", programIndicatorService.getProgramIndicatorByUid( "UID-INT" ).getUid() );
        assertEquals( "UID-DATE", programIndicatorService.getProgramIndicatorByUid( "UID-DATE" ).getUid() );
    }

    @Test
    public void testGetProgramIndicatorsByProgram()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );
        programIndicatorService.addProgramIndicator( indicatorC );

        Collection<ProgramIndicator> indicators = programIndicatorService.getProgramIndicators( programA );
        assertEquals( 2, indicators.size() );
        assertTrue( indicators.contains( indicatorDate ) );
        assertTrue( indicators.contains( indicatorInt ) );

        indicators = programIndicatorService.getProgramIndicators( programB );
        assertEquals( 1, indicators.size() );
        assertTrue( indicators.contains( indicatorC ) );

    }

    // -------------------------------------------------------------------------
    // Logic tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetProgramStageDataElementsInExpression()
    {
        Set<ProgramStageDataElement> elements = programIndicatorService.getProgramStageDataElementsInExpression( indicatorE );
        
        assertEquals( 2, elements.size() );
        
        assertTrue( elements.contains( new ProgramStageDataElement( psA, deA ) ) );
        assertTrue( elements.contains( new ProgramStageDataElement( psB, deA ) ) );
    }
    
    @Test
    public void testGetAttributesInExpression()
    {
        Set<TrackedEntityAttribute> attributes = programIndicatorService.getAttributesInExpression( indicatorE );
        
        assertEquals( 2, attributes.size() );
        assertTrue( attributes.contains( atA ) );
        assertTrue( attributes.contains( atB ) );
    }
    
    @Test
    public void testGetProgramIndicatorValue()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );
        programIndicatorService.addProgramIndicator( indicatorE );
        programIndicatorService.addProgramIndicator( indicatorF );
        programIndicatorService.addProgramIndicator( indicatorG );
        programIndicatorService.addProgramIndicator( indicatorH );
        programIndicatorService.addProgramIndicator( indicatorI );
        programIndicatorService.addProgramIndicator( indicatorJ );

        String valueINT = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorInt);
        assertEquals( "10.0", valueINT );

        String valueDATE = programIndicatorService.getProgramIndicatorValue( programInstance,  indicatorDate  );
        assertEquals( DateUtils.getMediumDateString( enrollmentDate ), valueDATE );
        
        String valueE = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorE );
        assertEquals( "9.0", valueE  );
        
        String valueF = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorF );
        assertEquals( "17.0", valueF );

        String valueG = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorG );
        assertEquals( "29.0", valueG );
    }

    @Test
    public void testGetProgramIndicatorValues()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        Map<String, String> indicatorMap = programIndicatorService.getProgramIndicatorValues( programInstance );
        assertEquals( 2, indicatorMap.keySet().size() );
        assertEquals( "10.0", indicatorMap.get( "IndicatorA" ) );
        assertEquals( DateUtils.getMediumDateString( enrollmentDate ), indicatorMap.get( "IndicatorB" ) );

    }
    
    @Test
    public void testGetExpressionDescription()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        String description = programIndicatorService.getExpressionDescription( indicatorDate.getExpression() );
        assertEquals( "70", description);
        
        description = programIndicatorService.getExpressionDescription( indicatorInt.getExpression() );
        assertEquals( "( Enrollment date - Incident date )  / ConstantA", description);
        
    }
    
    @Test
    public void testExpressionIsValid()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );
        programIndicatorService.addProgramIndicator( indicatorD );

        assertEquals( ProgramIndicator.VALID, programIndicatorService.expressionIsValid( indicatorDate.getExpression() ) );
        assertEquals( ProgramIndicator.VALID, programIndicatorService.expressionIsValid( indicatorInt.getExpression() ) );
        assertEquals( ProgramIndicator.EXPRESSION_NOT_WELL_FORMED, programIndicatorService.expressionIsValid( indicatorD.getExpression() ) );
    }    
}

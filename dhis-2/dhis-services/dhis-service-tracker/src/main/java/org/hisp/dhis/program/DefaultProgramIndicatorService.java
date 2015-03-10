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

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chau Thu Tran
 */
@Transactional
public class DefaultProgramIndicatorService
    implements ProgramIndicatorService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramIndicatorStore programIndicatorStore;

    public void setProgramIndicatorStore( ProgramIndicatorStore programIndicatorStore )
    {
        this.programIndicatorStore = programIndicatorStore;
    }

    private ProgramStageService programStageService;

    public void setProgramStageService( ProgramStageService programStageService )
    {
        this.programStageService = programStageService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private TrackedEntityDataValueService dataValueService;

    public void setDataValueService( TrackedEntityDataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private ProgramStageInstanceService programStageInstanceService;

    public void setProgramStageInstanceService( ProgramStageInstanceService programStageInstanceService )
    {
        this.programStageInstanceService = programStageInstanceService;
    }

    private TrackedEntityAttributeService attributeService;

    public void setAttributeService( TrackedEntityAttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    private TrackedEntityAttributeValueService attributeValueService;

    public void setAttributeValueService( TrackedEntityAttributeValueService attributeValueService )
    {
        this.attributeValueService = attributeValueService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    public int addProgramIndicator( ProgramIndicator programIndicator )
    {
        return programIndicatorStore.save( programIndicator );
    }

    @Override
    public void updateProgramIndicator( ProgramIndicator programIndicator )
    {
        programIndicatorStore.update( programIndicator );
    }

    @Override
    public void deleteProgramIndicator( ProgramIndicator programIndicator )
    {
        programIndicatorStore.delete( programIndicator );
    }

    @Override
    public ProgramIndicator getProgramIndicator( int id )
    {
        return i18n( i18nService, programIndicatorStore.get( id ) );
    }

    @Override
    public ProgramIndicator getProgramIndicator( String name )
    {
        return i18n( i18nService, programIndicatorStore.getByName( name ) );
    }

    @Override
    public ProgramIndicator getProgramIndicatorByUid( String uid )
    {
        return i18n( i18nService, programIndicatorStore.getByUid( uid ) );
    }

    @Override
    public ProgramIndicator getProgramIndicatorByShortName( String shortName )
    {
        return i18n( i18nService, programIndicatorStore.getByShortName( shortName ) );
    }

    @Override
    public Collection<ProgramIndicator> getAllProgramIndicators()
    {
        return i18n( i18nService, programIndicatorStore.getAll() );
    }

    @Override
    public Collection<ProgramIndicator> getProgramIndicators( Program program )
    {
        return i18n( i18nService, programIndicatorStore.getByProgram( program ) );
    }

    @Override
    public String getProgramIndicatorValue( ProgramInstance programInstance, ProgramIndicator programIndicator )
    {
        Double value = getValue( programInstance, programIndicator );

        if ( value != null )
        {
            if ( programIndicator.getValueType().equals( ProgramIndicator.VALUE_TYPE_DATE ) )
            {
                Date baseDate = new Date();

                if ( ProgramIndicator.INCIDENT_DATE.equals( programIndicator.getRootDate() ) )
                {
                    baseDate = programInstance.getDateOfIncident();
                }
                else if ( ProgramIndicator.ENROLLEMENT_DATE.equals( programIndicator.getRootDate() ) )
                {
                    baseDate = programInstance.getEnrollmentDate();
                }

                Date date = DateUtils.getDateAfterAddition( baseDate, value.intValue() );

                return DateUtils.getMediumDateString( date );
            }

            return String.valueOf( Math.floor( value ) );
        }

        return null;
    }

    @Override
    public Map<String, String> getProgramIndicatorValues( ProgramInstance programInstance )
    {
        Map<String, String> result = new HashMap<>();

        Collection<ProgramIndicator> programIndicators = programIndicatorStore.getByProgram( programInstance.getProgram() );

        for ( ProgramIndicator programIndicator : programIndicators )
        {
            String value = getProgramIndicatorValue( programInstance, programIndicator );
            
            if ( value != null )
            {
                result.put( programIndicator.getDisplayName(),
                    getProgramIndicatorValue( programInstance, programIndicator ) );
            }
        }

        return result;
    }

    @Override
    public String getExpressionDescription( String expression )
    {
        StringBuffer description = new StringBuffer();

        Pattern patternCondition = Pattern.compile( ProgramIndicator.regExp );

        Matcher matcher = patternCondition.matcher( expression );
        
        while ( matcher.find() )
        {
            String key = matcher.group( 1 );
            String uid1 = matcher.group( 2 );

            if ( ProgramIndicator.KEY_DATAELEMENT.equals( key ) )
            {
                String uid2 = matcher.group( 3 );

                ProgramStage programStage = programStageService.getProgramStage( uid1 );
                DataElement dataElement = dataElementService.getDataElement( uid2 );

                if ( programStage != null && dataElement != null )
                {
                    String programStageName = programStage.getDisplayName();

                    String dataelementName = dataElement.getDisplayName();

                    matcher.appendReplacement( description, ProgramIndicator.KEY_DATAELEMENT + "{" + programStageName
                        + ProgramIndicator.SEPARATOR_ID + dataelementName + "}" );
                }
            }

            else if ( ProgramIndicator.KEY_ATTRIBUTE.equals( key ) )
            {
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( uid1 );
                
                if ( attribute != null )
                {
                    matcher.appendReplacement( description,
                        ProgramIndicator.KEY_ATTRIBUTE + "{" + attribute.getDisplayName() + "}" );
                }
            }
            else if ( ProgramIndicator.KEY_CONSTANT.equals( key ) )
            {
                Constant constant = constantService.getConstant( uid1 );
                
                if ( constant != null )
                {
                    matcher.appendReplacement( description,
                        ProgramIndicator.KEY_CONSTANT + "{" + constant.getDisplayName() + "}" );
                }
            }
        }

        matcher.appendTail( description );

        return description.toString();

    }

    public String expressionIsValid( String expression )
    {
        StringBuffer description = new StringBuffer();

        Pattern patternCondition = Pattern.compile( ProgramIndicator.regExp );

        Matcher matcher = patternCondition.matcher( expression );
        
        while ( matcher.find() )
        {
            String key = matcher.group( 1 );
            String uid1 = matcher.group( 2 );

            if ( ProgramIndicator.KEY_DATAELEMENT.equals( key ) )
            {
                String uid2 = matcher.group( 3 );

                ProgramStage programStage = programStageService.getProgramStage( uid1 );
                DataElement dataElement = dataElementService.getDataElement( uid2 );

                if ( programStage != null && dataElement != null )
                {
                    matcher.appendReplacement( description, String.valueOf( 1 ) );
                }
                else
                {
                    return ProgramIndicator.EXPRESSION_NOT_WELL_FORMED;
                }
            }

            else if ( ProgramIndicator.KEY_ATTRIBUTE.equals( key ) )
            {
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( uid1 );
                
                if ( attribute != null )
                {
                    matcher.appendReplacement( description, String.valueOf( 1 ) );
                }
                else
                {
                    return ProgramIndicator.EXPRESSION_NOT_WELL_FORMED;
                }
            }
            else if ( ProgramIndicator.KEY_CONSTANT.equals( key ) )
            {
                Constant constant = constantService.getConstant( uid1 );
                
                if ( constant != null )
                {
                    matcher.appendReplacement( description, String.valueOf( constant.getValue() ) );
                }
                else
                {
                    return ProgramIndicator.EXPRESSION_NOT_WELL_FORMED;
                }
            }
            else if ( ProgramIndicator.KEY_PROGRAM_VARIABLE.equals( key ) )
            {
                matcher.appendReplacement( description, String.valueOf( 0 ) );
            }
        }

        matcher.appendTail( description );

        // ---------------------------------------------------------------------
        // Well-formed expression
        // ---------------------------------------------------------------------

        if ( MathUtils.expressionHasErrors( description.toString() ) )
        {
            return ProgramIndicator.EXPRESSION_NOT_WELL_FORMED;
        }

        return ProgramIndicator.VALID;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private Double getValue( ProgramInstance programInstance, ProgramIndicator indicator )
    {
        StringBuffer description = new StringBuffer();

        Pattern pattern = Pattern.compile( ProgramIndicator.regExp );
        Matcher matcher = pattern.matcher( indicator.getExpression() );

        while ( matcher.find() )
        {
            String key = matcher.group( 1 );
            String uid1 = matcher.group( 2 );

            if ( ProgramIndicator.KEY_DATAELEMENT.equals( key ) )
            {
                String uid2 = matcher.group( 3 );
                ProgramStage programStage = programStageService.getProgramStage( uid1 );
                DataElement dataElement = dataElementService.getDataElement( uid2 );

                if ( programStage != null && dataElement != null )
                {
                    ProgramStageInstance programStageInstance = programStageInstanceService.getProgramStageInstance(
                        programInstance, programStage );

                    TrackedEntityDataValue dataValue = dataValueService.getTrackedEntityDataValue(
                        programStageInstance, dataElement );

                    if ( dataValue == null )
                    {
                        return null;
                    }

                    String value = dataValue.getValue();

                    if ( indicator.getValueType().equals( ProgramIndicator.VALUE_TYPE_INT ) && 
                        ( dataElement == null || dataElement.getType().equals( DataElement.VALUE_TYPE_DATE ) ) )
                    {
                        value = DateUtils.daysBetween( new Date(), DateUtils.getDefaultDate( value ) ) + " ";
                    }

                    matcher.appendReplacement( description, value );
                }
                else
                {
                    return null;
                }
            }
            else if ( ProgramIndicator.KEY_ATTRIBUTE.equals( key ) )
            {
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( uid1 );
                
                if ( attribute != null )
                {
                    TrackedEntityAttributeValue attributeValue = attributeValueService.getTrackedEntityAttributeValue(
                        programInstance.getEntityInstance(), attribute );

                    if ( attributeValue != null )
                    {
                        matcher.appendReplacement( description, attributeValue.getValue() );
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
            else if ( ProgramIndicator.KEY_CONSTANT.equals( key ) )
            {
                Constant constant = constantService.getConstant( uid1 );
                
                if ( constant != null )
                {
                    matcher.appendReplacement( description, String.valueOf( constant.getValue() ) );
                }
                else
                {
                    return null;
                }
            }
            else if ( ProgramIndicator.KEY_PROGRAM_VARIABLE.equals( key ) )
            {
                Date currentDate = new Date();
                Date date = null;
                
                if ( uid1.equals( ProgramIndicator.ENROLLEMENT_DATE ) )
                {
                    date = programInstance.getEnrollmentDate();
                }
                else if ( uid1.equals( ProgramIndicator.INCIDENT_DATE ) )
                {
                    date = programInstance.getDateOfIncident();
                }
                else if ( uid1.equals( ProgramIndicator.CURRENT_DATE ) )
                {
                    date = programInstance.getDateOfIncident();
                }

                if ( date != null )
                {
                    matcher.appendReplacement( description, DateUtils.daysBetween( date, currentDate ) + "" );
                }
            }

        }

        matcher.appendTail( description );
        
        return MathUtils.calculateExpression( description.toString() );
    }
}

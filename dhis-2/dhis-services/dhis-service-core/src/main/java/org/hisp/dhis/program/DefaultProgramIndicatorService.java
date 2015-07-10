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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.hisp.dhis.commons.util.ExpressionUtils;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.expression.ExpressionService;
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
    public List<ProgramIndicator> getAllProgramIndicators()
    {
        return i18n( i18nService, programIndicatorStore.getAll() );
    }

    @Override
    public String getProgramIndicatorValue( ProgramIndicator programIndicator, ProgramInstance programInstance )
    {
        Double value = getValue( programIndicator, programInstance, null );

        if ( value != null )
        {
            if ( programIndicator.getValueType().equals( ProgramIndicator.VALUE_TYPE_DATE ) )
            {
                Date baseDate = new Date();

                if ( ProgramIndicator.INCIDENT_DATE.equals( programIndicator.getRootDate() ) )
                {
                    baseDate = programInstance.getDateOfIncident();
                }
                else if ( ProgramIndicator.ENROLLMENT_DATE.equals( programIndicator.getRootDate() ) )
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
    public Double getProgramIndicatorValue( ProgramIndicator indicator, Map<String, Double> valueMap )
    {
        StringBuffer buffer = new StringBuffer();

        String expression = indicator.getExpression();
        
        Matcher matcher = ProgramIndicator.EXPRESSION_PATTERN.matcher( expression );
        
        while ( matcher.find() )
        {
            String key = matcher.group( 1 );
            
            Double value = null;

            //TODO query by program stage
            
            if ( ProgramIndicator.KEY_DATAELEMENT.equals( key ) )
            {
                String de = matcher.group( 3 );
                
                value = valueMap.get( de );
            }
            else if ( ProgramIndicator.KEY_ATTRIBUTE.equals( key ) || ProgramIndicator.KEY_CONSTANT.equals( key ) )
            {
                String uid = matcher.group( 2 );
                
                value = valueMap.get( uid );
            }
            
            if ( value == null )
            {
                return null;
            }
            
            matcher.appendReplacement( buffer, Matcher.quoteReplacement( String.valueOf( value ) ) );
        }

        expression = TextUtils.appendTail( matcher, buffer );

        return MathUtils.calculateExpression( expression );
    }
    
    @Override
    public Map<String, String> getProgramIndicatorValues( ProgramInstance programInstance )
    {
        Map<String, String> result = new HashMap<>();

        Collection<ProgramIndicator> programIndicators = new HashSet<>( programInstance.getProgram().getProgramIndicators() );

        for ( ProgramIndicator programIndicator : programIndicators )
        {
            String value = getProgramIndicatorValue( programIndicator, programInstance );
            
            if ( value != null )
            {
                result.put( programIndicator.getDisplayName(), value );
            }
        }

        return result;
    }

    @Override
    public String getExpressionDescription( String expression )
    {
        if ( expression == null )
        {
            return null;
        }
        
        StringBuffer description = new StringBuffer();

        Matcher matcher = ProgramIndicator.EXPRESSION_PATTERN.matcher( expression );
        
        while ( matcher.find() )
        {
            String key = matcher.group( 1 );
            String uid = matcher.group( 2 );

            if ( ProgramIndicator.KEY_DATAELEMENT.equals( key ) )
            {
                String de = matcher.group( 3 );

                ProgramStage programStage = programStageService.getProgramStage( uid );
                DataElement dataElement = dataElementService.getDataElement( de );

                if ( programStage != null && dataElement != null )
                {
                    String programStageName = programStage.getDisplayName();

                    String dataelementName = dataElement.getDisplayName();

                    matcher.appendReplacement( description, programStageName + ProgramIndicator.SEPARATOR_ID + dataelementName );
                }
            }
            else if ( ProgramIndicator.KEY_ATTRIBUTE.equals( key ) )
            {
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( uid );
                
                if ( attribute != null )
                {
                    matcher.appendReplacement( description, attribute.getDisplayName() );
                }
            }
            else if ( ProgramIndicator.KEY_CONSTANT.equals( key ) )
            {
                Constant constant = constantService.getConstant( uid );
                
                if ( constant != null )
                {
                    matcher.appendReplacement( description, constant.getDisplayName() );
                }
            }
            else if ( ProgramIndicator.KEY_PROGRAM_VARIABLE.equals( key ) )
            {
                if ( ProgramIndicator.CURRENT_DATE.equals( uid ) )
                {
                    matcher.appendReplacement( description, "Current date" );
                }
                else if ( ProgramIndicator.ENROLLMENT_DATE.equals( uid ) )
                {
                    matcher.appendReplacement( description, "Enrollment date" );
                }
                else if ( ProgramIndicator.INCIDENT_DATE.equals( uid ) )
                {
                    matcher.appendReplacement( description, "Incident date" );
                }
                else if ( ProgramIndicator.VAR_VALUE_COUNT.equals( uid ) )
                {
                    matcher.appendReplacement( description, "Value count" );
                }
                else if ( ProgramIndicator.VAR_ZERO_POS_VALUE_COUNT.equals( uid ) )
                {
                    matcher.appendReplacement( description, "Zero or positive value count" );
                }
            }
        }

        matcher.appendTail( description );

        return description.toString();
    }

    @Override
    public String expressionIsValid( String expression )
    {
        String expr = getSubstitutedExpression( expression );
        
        if ( ProgramIndicator.INVALID_IDENTIFIERS_IN_EXPRESSION.equals( expr ) )
        {
            return expr;
        }
        
        if ( !ExpressionUtils.isValid( expr, null ) )
        {
            return ProgramIndicator.EXPRESSION_NOT_WELL_FORMED;
        }

        return ProgramIndicator.VALID;
    }
    
    @Override
    public String filterIsValid( String filter )
    {
        String expr = getSubstitutedExpression( filter );

        if ( ProgramIndicator.INVALID_IDENTIFIERS_IN_EXPRESSION.equals( expr ) )
        {
            return expr;
        }
        
        return ExpressionUtils.isBoolean( expr, null ) ? ProgramIndicator.VALID : ProgramIndicator.FILTER_NOT_EVALUATING_TO_TRUE_OR_FALSE;
    }    
    
    /**
     * Generates an expression where all items are substituted with a sample value
     * in order to maintain a valid expression syntax.
     * 
     * @param expression the expression.
     */
    private String getSubstitutedExpression( String expression )
    {
        StringBuffer expr = new StringBuffer();

        Matcher matcher = ProgramIndicator.EXPRESSION_PATTERN.matcher( expression );
        
        while ( matcher.find() )
        {
            String key = matcher.group( 1 );
            String uid = matcher.group( 2 );

            if ( ProgramIndicator.KEY_DATAELEMENT.equals( key ) )
            {
                String de = matcher.group( 3 );

                ProgramStage programStage = programStageService.getProgramStage( uid );
                DataElement dataElement = dataElementService.getDataElement( de );

                if ( programStage != null && dataElement != null )
                {
                    matcher.appendReplacement( expr, String.valueOf( 1 ) );
                }
                else
                {
                    return ProgramIndicator.INVALID_IDENTIFIERS_IN_EXPRESSION;
                }
            }
            else if ( ProgramIndicator.KEY_ATTRIBUTE.equals( key ) )
            {
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( uid );
                
                if ( attribute != null )
                {
                    matcher.appendReplacement( expr, String.valueOf( 1 ) );
                }
                else
                {
                    return ProgramIndicator.INVALID_IDENTIFIERS_IN_EXPRESSION;
                }
            }
            else if ( ProgramIndicator.KEY_CONSTANT.equals( key ) )
            {
                Constant constant = constantService.getConstant( uid );
                
                if ( constant != null )
                {
                    matcher.appendReplacement( expr, String.valueOf( constant.getValue() ) );
                }
                else
                {
                    return ProgramIndicator.INVALID_IDENTIFIERS_IN_EXPRESSION;
                }
            }
            else if ( ProgramIndicator.KEY_PROGRAM_VARIABLE.equals( key ) )
            {
                matcher.appendReplacement( expr, String.valueOf( 1 ) );
            }
        }
        
        matcher.appendTail( expr );

        return expr.toString();
    }

    @Override
    public Set<DataElement> getDataElementsInIndicators( Collection<ProgramIndicator> indicators )
    {
        Set<DataElement> dataElements = new HashSet<>();
        
        for ( ProgramIndicator indicator : indicators )
        {
            Set<ProgramStageDataElement> psds = getProgramStageDataElementsInExpression( indicator.getExpression() );
            
            for ( ProgramStageDataElement psd : psds )
            {
                dataElements.add( psd.getDataElement() );
            }
        }
        
        return dataElements;
    }
    
    @Override
    public Set<ProgramStageDataElement> getProgramStageDataElementsInExpression( String expression )
    {
        Set<ProgramStageDataElement> elements = new HashSet<>();
        
        Matcher matcher = ProgramIndicator.DATAELEMENT_PATTERN.matcher( expression );
        
        while ( matcher.find() )
        {
            String ps = matcher.group( 1 );
            String de = matcher.group( 2 );
            
            ProgramStage programStage = programStageService.getProgramStage( ps );
            DataElement dataElement = dataElementService.getDataElement( de );
            
            if ( programStage != null && dataElement != null )
            {
                elements.add( new ProgramStageDataElement( programStage, dataElement ) );
            }
        }
        
        return elements;
    }

    @Override
    public Set<TrackedEntityAttribute> getAttributesInIndicators( Collection<ProgramIndicator> indicators )
    {
        Set<TrackedEntityAttribute> attributes = new HashSet<>();
        
        for ( ProgramIndicator indicator : indicators )
        {
            attributes.addAll( getAttributesInExpression( indicator.getExpression() ) );
        }
        
        return attributes;
    }
    
    @Override
    public Set<TrackedEntityAttribute> getAttributesInExpression( String expression )
    {
        Set<TrackedEntityAttribute> attributes = new HashSet<>();

        Matcher matcher = ProgramIndicator.ATTRIBUTE_PATTERN.matcher( expression );
        
        while ( matcher.find() )
        {
            String at = matcher.group( 1 );
            
            TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( at );
            
            if ( attribute != null )
            {
                attributes.add( attribute );
            }
        }
        
        return attributes;        
    }

    @Override
    public Set<Constant> getConstantsInIndicators( Collection<ProgramIndicator> indicators )
    {
        Set<Constant> constants = new HashSet<>();
        
        for ( ProgramIndicator indicator : indicators )
        {
            constants.addAll( getConstantsInExpression( indicator.getExpression() ) );
        }
        
        return constants;
    }
    
    @Override
    public Set<Constant> getConstantsInExpression( String expression )
    {
        Set<Constant> constants = new HashSet<>();

        Matcher matcher = ExpressionService.CONSTANT_PATTERN.matcher( expression );
        
        while ( matcher.find() )
        {
            String co = matcher.group( 1 );
            
            Constant constant = constantService.getConstant( co );
            
            if ( constant != null )
            {
                constants.add( constant );
            }
        }
        
        return constants;        
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Get indicator value for the given arguments. If programStageInstance 
     * argument is null, the program stage instance will be retrieved based on 
     * the given program instance in combination with the program stage from the indicator expression.
     * 
     * @param indicator the indicator, must be not null.
     * @param programInstance the program instance, can be null.
     * @param programStageInstance the program stage instance, can be null.
     */
    private Double getValue( ProgramIndicator indicator, ProgramInstance programInstance, ProgramStageInstance programStageInstance )
    {
        StringBuffer buffer = new StringBuffer();

        String expression = indicator.getExpression();
        
        Matcher matcher = ProgramIndicator.EXPRESSION_PATTERN.matcher( expression );

        int valueCount = 0;
        int zeroPosValueCount = 0;
        
        while ( matcher.find() )
        {
            String key = matcher.group( 1 );
            String uid = matcher.group( 2 );

            if ( ProgramIndicator.KEY_DATAELEMENT.equals( key ) )
            {
                String de = matcher.group( 3 );
                ProgramStage programStage = programStageService.getProgramStage( uid );
                DataElement dataElement = dataElementService.getDataElement( de );

                if ( programStage != null && dataElement != null )
                {
                    ProgramStageInstance psi = programStageInstance != null ?
                        programStageInstance :
                        programStageInstanceService.getProgramStageInstance( programInstance, programStage );

                    TrackedEntityDataValue dataValue = dataValueService.getTrackedEntityDataValue( psi, dataElement );

                    if ( dataValue == null )
                    {
                        return null;
                    }

                    String value = dataValue.getValue();

                    if ( dataElement.getType().equals( DataElement.VALUE_TYPE_DATE ) )
                    {
                        value = DateUtils.daysBetween( new Date(), DateUtils.getDefaultDate( value ) ) + " ";
                    }

                    matcher.appendReplacement( buffer, value );
                    
                    valueCount++;
                    zeroPosValueCount = isZeroOrPositive( value ) ? ( zeroPosValueCount + 1 ) : zeroPosValueCount;
                }
                else
                {
                    return null;
                }
            }
            else if ( ProgramIndicator.KEY_ATTRIBUTE.equals( key ) )
            {
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( uid );
                
                if ( attribute != null )
                {
                    TrackedEntityAttributeValue attributeValue = attributeValueService.getTrackedEntityAttributeValue(
                        programInstance.getEntityInstance(), attribute );

                    if ( attributeValue != null )
                    {
                        String value = attributeValue.getValue();
                        
                        if ( attribute.getValueType().equals( TrackedEntityAttribute.TYPE_DATE ) )
                        {
                            value = DateUtils.daysBetween( new Date(), DateUtils.getDefaultDate( value ) ) + " ";
                        }
                        
                        matcher.appendReplacement( buffer, value );
                        
                        valueCount++;
                        zeroPosValueCount = isZeroOrPositive( value ) ? ( zeroPosValueCount + 1 ) : zeroPosValueCount;
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
                Constant constant = constantService.getConstant( uid );
                
                if ( constant != null )
                {
                    matcher.appendReplacement( buffer, String.valueOf( constant.getValue() ) );
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
                
                if ( ProgramIndicator.ENROLLMENT_DATE.equals( uid ) )
                {
                    date = programInstance.getEnrollmentDate();
                }
                else if ( ProgramIndicator.INCIDENT_DATE.equals( uid ) )
                {
                    date = programInstance.getDateOfIncident();
                }
                else if ( ProgramIndicator.CURRENT_DATE.equals( uid ) )
                {
                    date = currentDate;
                }
                
                if ( date != null )
                {
                    matcher.appendReplacement( buffer, DateUtils.daysBetween( currentDate, date ) + "" );
                }
            }
        }

        expression = TextUtils.appendTail( matcher, buffer );

        // ---------------------------------------------------------------------
        // Value count variable
        // ---------------------------------------------------------------------

        buffer = new StringBuffer();
        matcher = ProgramIndicator.VALUECOUNT_PATTERN.matcher( expression );
        
        while ( matcher.find() )
        {
            String var = matcher.group( 1 );
            
            if ( ProgramIndicator.VAR_VALUE_COUNT.equals( var ) )
            {
                matcher.appendReplacement( buffer, String.valueOf( valueCount ) );
            }
            else if ( ProgramIndicator.VAR_ZERO_POS_VALUE_COUNT.equals( var ) )
            {
                matcher.appendReplacement( buffer, String.valueOf( zeroPosValueCount ) );
            }            
        }
        
        expression = TextUtils.appendTail( matcher, buffer );
        
        return MathUtils.calculateExpression( expression );
    }

    private boolean isZeroOrPositive( String value )
    {
        return MathUtils.isNumeric( value ) && Double.valueOf( value ) >= 0d;
    }
}

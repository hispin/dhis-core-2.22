package org.hisp.dhis.importexport.dhis14.xml.converter;

/*
 * Copyright (c) 2004-2007, University of Oslo
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

import static org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler.convertAggregationOperatorFromDhis14;
import static org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler.convertAggregationOperatorToDhis14;
import static org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler.convertBooleanFromDhis14;
import static org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler.convertBooleanToDhis14;
import static org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler.convertTypeToDhis14;
import static org.hisp.dhis.system.util.ConversionUtils.parseInt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.dataelement.CalculatedDataElement;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.expression.Expression;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.GroupMemberType;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;
import org.hisp.dhis.importexport.analysis.ImportAnalyser;
import org.hisp.dhis.importexport.converter.AbstractDataElementConverter;
import org.hisp.dhis.importexport.dhis14.util.Dhis14DateUtil;
import org.hisp.dhis.importexport.dhis14.util.Dhis14ObjectMappingUtil;
import org.hisp.dhis.importexport.dhis14.util.Dhis14ParsingUtils;
import org.hisp.dhis.importexport.mapping.NameMappingUtil;

/**
 * @author Lars Helge Overland
 * @version $Id: DataElementConverter.java 6455 2008-11-24 08:59:37Z larshelg $
 */
public class DataElementConverter
    extends AbstractDataElementConverter implements XMLConverter
{
    public static final String ELEMENT_NAME = "DataElement";
    
    private static final String FIELD_ID = "DataElementID";
    private static final String FIELD_SORT_ORDER = "SortOrder";
    private static final String FIELD_CODE = "DataElementCode";
    private static final String FIELD_NAME = "DataElementName";
    private static final String FIELD_SHORT_NAME = "DataElementShort";
    private static final String FIELD_DOS = "DataElementDOS";
    private static final String FIELD_PROMPT = "DataElementPrompt";
    private static final String FIELD_META = "MetaDataElement";
    private static final String FIELD_DATA_TYPE = "DataTypeID";
    private static final String FIELD_PERIOD_TYPE = "DataPeriodTypeID";
    private static final String FIELD_VALID_FROM = "ValidFrom";
    private static final String FIELD_VALID_TO = "ValidTo";
    private static final String FIELD_DESCRIPTION = "DataElementDescription";
    private static final String FIELD_COMMENT = "Comment";
    private static final String FIELD_CALCULATED = "Calculated";
    private static final String FIELD_SAVE_CALCULATED = "SaveCalculated";
    private static final String FIELD_AGGREGATION_START_LEVEL = "AggregateStartLevel";
    private static final String FIELD_AGGREGATION_OPERATOR = "AggregateOperator";
    private static final String FIELD_SELECTED = "Selected";
    private static final String FIELD_LAST_USER = "LastUserID";
    private static final String FIELD_LAST_UPDATED = "LastUpdated";

    private static final int VALID_FROM = 34335;
    private static final int VALID_TO = 2958465;
    private static final int AGG_START_LEVEL = 5;

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private Map<Integer, String> expressionMap;
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     */
    public DataElementConverter( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    /**
     * Constructor for read operations.
     * 
     * @param importObjectService the importObjectService to use.
     * @param dataElementService the dataElementService to use.
     */
    public DataElementConverter( ImportObjectService importObjectService,
        DataElementService dataElementService,
        Map<Integer, String> expressionMap,
        ImportAnalyser importAnalyser )
    {
        this.importObjectService = importObjectService;
        this.dataElementService = dataElementService;
        this.expressionMap = expressionMap;
        this.importAnalyser = importAnalyser;
    }

    // -------------------------------------------------------------------------
    // AbstractDataElementConverter implementation
    // -------------------------------------------------------------------------
        
    @Override
    protected DataElement getMatching( DataElement object )
    {
        DataElement element = super.getMatching( object );
        
        if ( element instanceof CalculatedDataElement )
        {
            ((CalculatedDataElement)element).isSaved(); // Loading saved property into memory
            ((CalculatedDataElement)element).getExpression().getExpression(); // Loading expression into memory
        }
        
        return element;
    }
    
    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------
    
    public void write( XMLWriter writer, ExportParams params )
    {
        Collection<DataElement> elements = dataElementService.getDataElements( params.getAllDataElements() ); // Calculated elements handled together
        
        if ( elements != null && elements.size() > 0 )
        {
            for ( DataElement object : elements )
            {
                final boolean calculated = object instanceof CalculatedDataElement;
                final boolean saveCalculated = calculated ? ((CalculatedDataElement)object).isSaved() : false;
                
                writer.openElement( ELEMENT_NAME );
                
                writer.writeElement( FIELD_ID, String.valueOf( object.getId() ) );
                writer.writeElement( FIELD_SORT_ORDER, object.getSortOrder() != null ? String.valueOf( object.getSortOrder() ) : EMPTY );
                writer.writeElement( FIELD_CODE, object.getCode() );
                writer.writeElement( FIELD_NAME, object.getName() );
                writer.writeElement( FIELD_SHORT_NAME, object.getShortName() );
                writer.writeElement( FIELD_DOS, object.getShortName().replaceAll( "[^a-zA-Z0-9]", "" ) );
                writer.writeElement( FIELD_PROMPT, object.getName() );
                writer.writeElement( FIELD_META, String.valueOf( 0 ) );
                writer.writeElement( FIELD_DATA_TYPE, convertTypeToDhis14( object.getType() ) );
                writer.writeElement( FIELD_PERIOD_TYPE, String.valueOf( 1 ) );
                writer.writeElement( FIELD_VALID_FROM, String.valueOf( VALID_FROM ) );
                writer.writeElement( FIELD_VALID_TO, String.valueOf( VALID_TO ) );
                writer.writeElement( FIELD_DESCRIPTION, object.getDescription() );
                writer.writeElement( FIELD_COMMENT, "" );
                writer.writeElement( FIELD_CALCULATED, convertBooleanToDhis14( calculated ) );
                writer.writeElement( FIELD_SAVE_CALCULATED, convertBooleanToDhis14( saveCalculated ) );
                writer.writeElement( FIELD_AGGREGATION_START_LEVEL, String.valueOf( AGG_START_LEVEL ) );
                writer.writeElement( FIELD_AGGREGATION_OPERATOR, convertAggregationOperatorToDhis14( object.getAggregationOperator() ) );
                writer.writeElement( FIELD_SELECTED, String.valueOf( 0 ) );
                writer.writeElement( FIELD_LAST_USER, String.valueOf( 1 ) );
                writer.writeElement( FIELD_LAST_UPDATED, Dhis14DateUtil.getDateString( object.getLastUpdated() ) );
                
                writer.closeElement();
                
                NameMappingUtil.addDataElementAggregationOperatorMapping( object.getId(), object.getAggregationOperator() );
            }
        }
    }
    
    public void read( XMLReader reader, ImportParams params )
    {
        final Map<String, String> values = reader.readElements( ELEMENT_NAME );
        
        final boolean calculated = convertBooleanFromDhis14( values.get( FIELD_CALCULATED ) );
        
        final DataElementCategoryCombo categoryCombo = new DataElementCategoryCombo();
        
        if ( calculated )
        {
            final CalculatedDataElement element = new CalculatedDataElement();

            element.setCategoryCombo( categoryCombo );
            
            element.setId( Integer.valueOf( values.get( FIELD_ID ) ) );
            element.setName( values.get( FIELD_NAME ) );
            element.setShortName( values.get( FIELD_SHORT_NAME ) );
            element.setDescription( Dhis14ParsingUtils.removeNewLine( values.get( FIELD_DESCRIPTION ) ) );
            element.setActive( true );        
            element.setValueType( Dhis14ObjectMappingUtil.getDataElementValueTypeMap().get( Integer.parseInt( values.get( FIELD_DATA_TYPE ) ) ) );
            element.setType( DataElement.TYPE_AGGREGATE );
            element.setAggregationOperator( convertAggregationOperatorFromDhis14( values.get( FIELD_AGGREGATION_OPERATOR ) ) );
            element.setSortOrder( parseInt( values.get( FIELD_SORT_ORDER ) ) );
            element.setLastUpdated( Dhis14DateUtil.getDate( values.get( FIELD_LAST_UPDATED ) ) );
            element.getCategoryCombo().setId( 1 ); //TODO hack
            element.setSaved( convertBooleanFromDhis14( values.get( FIELD_SAVE_CALCULATED ) ) );
            element.setExpression( new Expression( expressionMap.get( element.getId() ), null, new HashSet<DataElement>() ) );
            
            NameMappingUtil.addDataElementAggregationOperatorMapping( element.getId(), element.getAggregationOperator() );
            
            read( element, GroupMemberType.NONE, params );
        }
        else
        {
            final DataElement element = new DataElement();

            element.setCategoryCombo( categoryCombo );
            
            element.setId( Integer.valueOf( values.get( FIELD_ID ) ) );
            element.setName( values.get( FIELD_NAME ) );
            element.setShortName( values.get( FIELD_SHORT_NAME ) );
            element.setDescription( Dhis14ParsingUtils.removeNewLine( values.get( FIELD_DESCRIPTION ) ) );
            element.setActive( true );        
            element.setValueType( Dhis14ObjectMappingUtil.getDataElementValueTypeMap().get( Integer.parseInt( values.get( FIELD_DATA_TYPE ) ) ) );
            element.setType( DataElement.TYPE_AGGREGATE );
            element.setAggregationOperator( convertAggregationOperatorFromDhis14( values.get( FIELD_AGGREGATION_OPERATOR ) ) );
            element.setSortOrder( parseInt( values.get( FIELD_SORT_ORDER ) ) );
            element.setLastUpdated( Dhis14DateUtil.getDate( values.get( FIELD_LAST_UPDATED ) ) );
            element.getCategoryCombo().setId( 1 ); //TODO hack
            
            NameMappingUtil.addDataElementAggregationOperatorMapping( element.getId(), element.getAggregationOperator() );
            
            read( element, GroupMemberType.NONE, params );
        }        
    }    
}

package org.hisp.dhis.resourcetable.jdbc;

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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.dbms.DbmsManager;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.resourcetable.ResourceTable;
import org.hisp.dhis.resourcetable.ResourceTableStore;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Lars Helge Overland
 */
public class JdbcResourceTableStore
    implements ResourceTableStore
{
    private static final Log log = LogFactory.getLog( JdbcResourceTableStore.class );
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private DbmsManager dbmsManager;

    public void setDbmsManager( DbmsManager dbmsManager )
    {
        this.dbmsManager = dbmsManager;
    }

    // -------------------------------------------------------------------------
    // ResourceTableStore implementation
    // -------------------------------------------------------------------------

    public void generateResourceTable( ResourceTable<?> resourceTable )
    {
        final String createTableSql = resourceTable.getCreateTempTableStatement();
        final Optional<String> populateTableSql = resourceTable.getPopulateTempTableStatement();
        final Optional<List<Object[]>> populateTableContent = resourceTable.getPopulateTempTableContent();
        final Optional<String> createIndexSql = resourceTable.getCreateIndexStatement();

        // ---------------------------------------------------------------------
        // Create table
        // ---------------------------------------------------------------------

        log.info( "Create table SQL: " + createTableSql );
        
        jdbcTemplate.execute( createTableSql );

        // ---------------------------------------------------------------------
        // Populate table
        // ---------------------------------------------------------------------

        if ( populateTableSql.isPresent() )
        {
            log.info( "Populate table SQL: " + populateTableSql.get() );
            
            jdbcTemplate.execute( populateTableSql.get() );
        }
        else if ( populateTableContent.isPresent() )
        {
            List<Object[]> content = populateTableContent.get();
            
            log.info( "Populate table content rows: " + content.size() );
            
            if ( content.size() > 0 )
            {
                int columns = content.get( 0 ).length;
                
                batchUpdate( columns, resourceTable.getTempTableName(), content );
            }
        }

        // ---------------------------------------------------------------------
        // Create index
        // ---------------------------------------------------------------------

        if ( createIndexSql.isPresent() )
        {
            log.info( "Create index SQL: " + createIndexSql.get() );
            
            jdbcTemplate.execute( createIndexSql.get() );
        }
        
        // ---------------------------------------------------------------------
        // Swap tables
        // ---------------------------------------------------------------------

        if ( dbmsManager.tableExists( resourceTable.getTableName() ) )
        {
            jdbcTemplate.execute( resourceTable.getDropTableStatement() );
        }
        
        jdbcTemplate.execute( resourceTable.getRenameTempTableStatement() );
        
        log.info( "Swapped resource table, done: " + resourceTable.getTableName() );
    }
    
    @Override
    public void batchUpdate( int columns, String tableName, List<Object[]> batchArgs )
    {
        if ( columns == 0 || tableName == null )
        {
            return;
        }
        
        StringBuilder builder = new StringBuilder( "insert into " + tableName + " values (" );
        
        for ( int i = 0; i < columns; i++ )
        {
            builder.append( "?," );
        }
        
        builder.deleteCharAt( builder.length() - 1 ).append( ")" );
        
        jdbcTemplate.batchUpdate( builder.toString(), batchArgs );
    }
    
    // -------------------------------------------------------------------------
    // DataElementCategoryOptionComboTable
    // -------------------------------------------------------------------------

    @Override
    public void createAndPopulateDataElementCategoryOptionCombo()
    {
        try
        {
            jdbcTemplate.execute( "DROP TABLE IF EXISTS " + TABLE_NAME_DATA_ELEMENT_CATEGORY_OPTION_COMBO );            
        }
        catch ( BadSqlGrammarException ex )
        {
            // Do nothing, table does not exist
        }
        
        final String create = "CREATE TABLE " + TABLE_NAME_DATA_ELEMENT_CATEGORY_OPTION_COMBO + " (" +
            "dataelementid INTEGER NOT NULL, " +
            "dataelementuid VARCHAR(11) NOT NULL, " +
            "categoryoptioncomboid INTEGER NOT NULL, " +
            "categoryoptioncombouid VARCHAR(11) NOT NULL)";

        log.info( "Create data element category option combo SQL: " + create );
        
        jdbcTemplate.execute( create );
        
        final String sql = 
            "insert into " + TABLE_NAME_DATA_ELEMENT_CATEGORY_OPTION_COMBO + 
            " (dataelementid, dataelementuid, categoryoptioncomboid, categoryoptioncombouid) " +
            "select de.dataelementid as dataelementid, de.uid as dataelementuid, " +
            "coc.categoryoptioncomboid as categoryoptioncomboid, coc.uid as categoryoptioncombouid " +
            "from dataelement de " +
            "join categorycombos_optioncombos cc on de.categorycomboid = cc.categorycomboid " +
            "join categoryoptioncombo coc on cc.categoryoptioncomboid = coc.categoryoptioncomboid";
        
        log.info( "Insert data element category option combo SQL: " + sql );
        
        jdbcTemplate.execute( sql );
        
        final String index = "CREATE INDEX dataelement_categoryoptioncombo ON " + 
            TABLE_NAME_DATA_ELEMENT_CATEGORY_OPTION_COMBO + " (dataelementuid, categoryoptioncombouid)";
        
        log.info( "Create data element category option combo index SQL: " + index );

        jdbcTemplate.execute( index );        
    }

    // -------------------------------------------------------------------------
    // DataApprovalMinLevelTable
    // -------------------------------------------------------------------------

    @Override
    public void createAndPopulateDataApprovalMinLevel( Set<OrganisationUnitLevel> levels )
    {
        try
        {
            jdbcTemplate.execute( "drop table if exists " + TABLE_NAME_DATA_APPROVAL_MIN_LEVEL );            
        }
        catch ( BadSqlGrammarException ex )
        {
            // Do nothing, table does not exist
        }
        
        final String create = "create table " + TABLE_NAME_DATA_APPROVAL_MIN_LEVEL + "(" +
            "datasetid integer not null, " +
            "periodid integer not null, " +
            "organisationunitid integer not null, " +
            "attributeoptioncomboid integer not null, " +
            "minlevel integer not null);";

        log.info( "Create data approval min level SQL: " + create );
        
        jdbcTemplate.execute( create );
        
        String sql = 
            "insert into " + TABLE_NAME_DATA_APPROVAL_MIN_LEVEL + 
            " (datasetid,periodid,organisationunitid,attributeoptioncomboid,minlevel) " +
            "select da.datasetid, da.periodid, da.organisationunitid, da.attributeoptioncomboid, dal.level as minlevel " +
            "from dataapproval da " +
            "inner join dataapprovallevel dal on da.dataapprovallevelid=dal.dataapprovallevelid " +
            "where not exists ( " +
                "select 1 from dataapproval da2 " +
                "inner join dataapprovallevel dal2 on da2.dataapprovallevelid=dal2.dataapprovallevelid " +
                "inner join _orgunitstructure ous2 on da2.organisationunitid=ous2.organisationunitid " +
                "where da.datasetid=da2.datasetid and da.periodid=da2.periodid and da.attributeoptioncomboid=da2.attributeoptioncomboid " +
                "and dal2.level < dal.level " +
                "and ( ";
        
        for ( OrganisationUnitLevel level : levels )
        {
            sql += "da.organisationunitid = ous2.idlevel" + level.getLevel() + " or ";
        }
        
        sql = TextUtils.removeLastOr( sql ) + ") )";
        
        log.info( "Insert data approval min level SQL: " + sql );

        jdbcTemplate.execute( sql );
        
        final String index = 
            "create index in_dataapprovalminlevel_datasetid on _dataapprovalminlevel(datasetid);" +
            "create index in_dataapprovalminlevel_periodid on _dataapprovalminlevel(periodid);" +
            "create index in_dataapprovalminlevel_organisationunitid on _dataapprovalminlevel(organisationunitid);" +
            "create index in_dataapprovalminlevel_attributeoptioncomboid on _dataapprovalminlevel(attributeoptioncomboid);";
        
        log.info( "Create data approval min level index SQL: " + index );
        
        jdbcTemplate.execute( index );
    }
}

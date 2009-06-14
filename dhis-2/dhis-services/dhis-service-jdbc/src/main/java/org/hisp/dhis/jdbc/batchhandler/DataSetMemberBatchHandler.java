package org.hisp.dhis.jdbc.batchhandler;

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

import org.amplecode.quick.JdbcConfiguration;
import org.amplecode.quick.batchhandler.AbstractBatchHandler;
import org.hisp.dhis.importexport.GroupMemberAssociation;

/**
 * @author Lars Helge Overland
 * @version $Id: DataSetMemberBatchHandler.java 5062 2008-05-01 18:10:35Z larshelg $
 */
public class DataSetMemberBatchHandler
    extends AbstractBatchHandler
{
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
 
    public DataSetMemberBatchHandler( JdbcConfiguration configuration )
    {
        super( configuration );
        
        hasSinglePrimaryKey = false;
    } 

    // -------------------------------------------------------------------------
    // AbstractBatchHandler implementation
    // -------------------------------------------------------------------------

    protected void setTableName()
    {
        this.tableName = "datasetmembers";
    }
    
    protected void openSqlStatement()
    {
        addColumns();
        
        sqlBuffer.append( statementBuilder.getInsertStatementOpening( tableName ) );
    }
    
    protected String getUpdateSqlStatement( Object object )
    {
        addColumns();
        
        addValues( object );
        
        return statementBuilder.getUpdateStatement( tableName );
    }
    
    protected String getIdentifierStatement( Object objectName )
    {
        GroupMemberAssociation association = (GroupMemberAssociation) objectName;
        
        String sql = statementBuilder.getValueStatement( tableName, "datasetid", "dataelementid", 
            "datasetid", association.getGroupId(), "dataelementid", association.getMemberId() );
        
        return sql;
    }

    protected String getUniquenessStatement( Object object )
    {
        GroupMemberAssociation association = (GroupMemberAssociation) object;

        String sql = statementBuilder.getValueStatement( tableName, "datasetid", "dataelementid", 
            "datasetid", association.getGroupId(), "dataelementid", association.getMemberId() );
        
        return sql;
    }
    
    protected void addColumns()
    {
        statementBuilder.setColumn( "datasetid" );
        statementBuilder.setColumn( "dataelementid" );
    }
    
    protected void addValues( Object object )
    {
        GroupMemberAssociation association = (GroupMemberAssociation) object;
        
        statementBuilder.setInt( association.getGroupId() );
        statementBuilder.setInt( association.getMemberId() );
    }   
}

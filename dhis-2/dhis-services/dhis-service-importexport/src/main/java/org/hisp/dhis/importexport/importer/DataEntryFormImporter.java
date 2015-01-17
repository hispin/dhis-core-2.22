package org.hisp.dhis.importexport.importer;

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

import org.amplecode.quick.BatchHandler;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataentryform.DataEntryFormService;
import org.hisp.dhis.importexport.GroupMemberType;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.Importer;
import org.hisp.dhis.importexport.mapping.NameMappingUtil;

/**
 * @author Chau Thu Tran
 * 
 * @version $ID: DataEntryFormImporter.java Dec 16, 2010 2:36:05 PM $
 */
public class DataEntryFormImporter
    extends AbstractImporter<DataEntryForm>
    implements Importer<DataEntryForm>
{
    protected DataEntryFormService dataEntryFormService;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    
    public DataEntryFormImporter(  )
    {
    }

    public DataEntryFormImporter( BatchHandler<DataEntryForm> batchHandler, DataEntryFormService dataEntryFormService )
    {
        this.batchHandler = batchHandler;
        this.dataEntryFormService = dataEntryFormService;
    }

    // -------------------------------------------------------------------------
    // Override methods
    // -------------------------------------------------------------------------

    @Override
    public void importObject( DataEntryForm object, ImportParams params )
    {
        NameMappingUtil.addDataEntryFormMapping( object.getId(), object.getName() );
        
        read( object, GroupMemberType.NONE, params );
    }

    @Override
    protected void importUnique( DataEntryForm object )
    {
        batchHandler.addObject( object );
    }

    @Override
    protected void importMatching( DataEntryForm object, DataEntryForm match )
    {
        match.setName( object.getName() );
        match.setHtmlCode( object.getHtmlCode() );

        dataEntryFormService.updateDataEntryForm( match );
    }

    @Override
    protected DataEntryForm getMatching( DataEntryForm object )
    {
        return dataEntryFormService.getDataEntryFormByName( object.getName() );
    }

    @Override
    protected boolean isIdentical( DataEntryForm object, DataEntryForm existing )
    {
        if ( object.getName().equals( existing.getName() ) )
        {
            return true;
        }

        return false;
    }
}

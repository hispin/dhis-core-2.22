package org.hisp.dhis.dxf2.metadata;

/*
 * Copyright (c) 2012, University of Oslo
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

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.hisp.dhis.dxf2.importsummary.ImportCount;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Transactional
@Service
public class DefaultImportService
    implements ImportService
{
    //-------------------------------------------------------------------------------------------------------
    // Dependencies
    //-------------------------------------------------------------------------------------------------------

    @Autowired
    private Set<Importer> importerClasses = new HashSet<Importer>();

    private Importer findImporterClass( List<? extends IdentifiableObject> clazzes )
    {
        if ( !clazzes.isEmpty() )
        {
            return findImporterClass( clazzes.get( 0 ).getClass() );
        }

        return null;
    }

    private Importer findImporterClass( Class<?> clazz )
    {
        for ( Importer i : importerClasses )
        {
            if ( i.canHandle( clazz ) )
            {
                return i;
            }
        }

        return null;
    }

    private void doImport( List<? extends IdentifiableObject> objects, ImportOptions importOptions, ImportSummary importSummary )
    {
        if ( !objects.isEmpty() )
        {
            Importer importer = findImporterClass( objects );

            if ( importer != null )
            {
                List<ImportConflict> conflicts = importer.importCollection( objects, importOptions );
                ImportCount count = importer.getCurrentImportCount();

                importSummary.getConflicts().addAll( conflicts );
                importSummary.getCounts().add( count );
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------
    // ImportService Implementation
    //-------------------------------------------------------------------------------------------------------

    @Override
    public ImportSummary importDxf2( DXF2 dxf2 )
    {
        return importDxf2WithImportOptions( dxf2, ImportOptions.getDefaultImportOptions() );
    }

    @Override
    public ImportSummary importDxf2WithImportOptions( DXF2 dxf2, ImportOptions importOptions )
    {
        ImportSummary importSummary = new ImportSummary();

        // Imports.. this could be made even more generic, just need to make sure that everything is imported in
        // the correct order
        doImport( dxf2.getConstants(), importOptions, importSummary );

        return importSummary;
    }
}

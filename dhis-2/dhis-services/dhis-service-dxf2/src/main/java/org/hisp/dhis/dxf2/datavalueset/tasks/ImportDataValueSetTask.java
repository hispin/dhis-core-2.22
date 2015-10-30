package org.hisp.dhis.dxf2.datavalueset.tasks;

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

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.commons.util.DebugUtils;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.scheduling.TaskId;
import org.hisp.dhis.security.SecurityContextRunnable;

import com.google.common.net.MediaType;

/**
 * @author Lars Helge Overland
 */
public class ImportDataValueSetTask
    extends SecurityContextRunnable
{
    private static final Log log = LogFactory.getLog( ImportDataValueSetTask.class );
    
    private InputStream in;
     
    private MediaType mediaType;
    
    private final TaskId taskId;
    
    private DataValueSetService dataValueSetService;

    private ImportOptions importOptions;
    
    public ImportDataValueSetTask( InputStream in, MediaType mediaType, DataValueSetService dataValueSetService, TaskId taskId )
    {
        super();
        this.in = in;
        this.mediaType = mediaType;
        this.dataValueSetService = dataValueSetService;
        this.taskId = taskId;
    }
    
    @Override
    public void call()
    {
        try
        {
            if ( MediaType.XML_UTF_8.equals( mediaType ) )
            {
                 dataValueSetService.saveDataValueSet( in, importOptions, taskId );
            }
            else if ( MediaType.JSON_UTF_8.equals( mediaType ) )
            {
                dataValueSetService.saveDataValueSetJson( in, importOptions, taskId );
            }
            else if ( MediaType.CSV_UTF_8.equals( mediaType ) )
            {
                dataValueSetService.saveDataValueSetCsv( in, importOptions, taskId );
            }
            else if ( MediaType.PDF.equals( mediaType ) )
            {
                dataValueSetService.saveDataValueSetPdf( in, importOptions, taskId );
            }
        }
        catch ( Exception ex )
        {
            log.error( DebugUtils.getStackTrace( ex ) );
            throw new RuntimeException( ex );
        }
    }
}

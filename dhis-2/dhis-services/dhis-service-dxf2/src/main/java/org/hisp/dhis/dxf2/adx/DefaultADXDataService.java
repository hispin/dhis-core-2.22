package org.hisp.dhis.dxf2.adx;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import org.amplecode.staxwax.factory.XMLFactory;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.datavalueset.DataExportParams;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.dxf2.importsummary.ImportSummaries;
import org.amplecode.staxwax.reader.XMLReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.hisp.dhis.dxf2.datavalueset.PipedImporter;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.period.Period;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author bobj
 */
public class DefaultADXDataService
    implements ADXDataService
{
    @Autowired
    protected DataValueSetService dataValueSetService;

    protected ExecutorService executor;

    public static final int PIPE_BUFFER_SIZE = 4096;

    public static final int TOTAL_MINUTES_TO_WAIT = 5;

    public void setDataValueSetService( DataValueSetService dataValueSetService )
    {
        this.dataValueSetService = dataValueSetService;
    }

    @Override
    public void getData( DataExportParams params, OutputStream out )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ImportSummaries postData( InputStream in, ImportOptions importOptions )
        throws IOException
    {

        XMLReader adxReader = XMLFactory.getXMLReader( in );

        ImportSummaries importSummaries = new ImportSummaries();

        adxReader.moveToStartElement( ADXConstants.ROOT, ADXConstants.NAMESPACE );

        // TODO: inject this?
        executor = Executors.newSingleThreadExecutor();

        while ( adxReader.moveToStartElement( ADXConstants.GROUP, ADXConstants.NAMESPACE ) )
        {
            try (PipedOutputStream pipeOut = new PipedOutputStream())
            {
                Future<ImportSummary> futureImportSummary;
                futureImportSummary = executor.submit(new PipedImporter( dataValueSetService, importOptions, pipeOut ) );
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                XMLStreamWriter dxfWriter = factory.createXMLStreamWriter( pipeOut );
                parseADXGroupToDxf( adxReader, dxfWriter );
                pipeOut.flush();

                importSummaries.addImportSummary( futureImportSummary.get( TOTAL_MINUTES_TO_WAIT, TimeUnit.SECONDS ) );
            } 
            catch ( IOException | XMLStreamException | InterruptedException | ExecutionException | TimeoutException ex )
            {
                ImportSummary importSummary = new ImportSummary();
                importSummary.setStatus( ImportStatus.ERROR );
                importSummary.setDescription( "Exception: " + ex.getMessage() );
                importSummaries.addImportSummary( importSummary );
                Logger.getLogger( DefaultADXDataService.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }

        return importSummaries;
    }

    protected void parseADXGroupToDxf( XMLReader adxReader, XMLStreamWriter dxfWriter ) throws XMLStreamException
    {
        dxfWriter.writeStartDocument( "1.0" );
        dxfWriter.writeStartElement( "dataValueSet" );
        dxfWriter.writeDefaultNamespace( "http://dhis2.org/schema/dxf/2.0" );

        Map<String, String> groupAttributes = readAttributes( adxReader );

        String periodStr = groupAttributes.get( ADXConstants.PERIOD );
        groupAttributes.remove( ADXConstants.PERIOD );

        Period period = ADXPeriod.parse( periodStr );
        dxfWriter.writeAttribute( "period", period.getIsoDate() );
        
        // pass through the remaining attributes to dxf
        for ( String attribute : groupAttributes.keySet() )
        {
            dxfWriter.writeAttribute( attribute, groupAttributes.get( attribute ) );
        }

        while ( adxReader.moveToStartElement( ADXConstants.DATAVALUE, ADXConstants.GROUP ) )
        {
            parseADXDataValueToDxf( adxReader, dxfWriter );
        }
        dxfWriter.writeEndElement();
        dxfWriter.writeEndDocument();
    }

    protected void parseADXDataValueToDxf( XMLReader adxReader, XMLStreamWriter dxfWriter ) throws XMLStreamException
    {
        dxfWriter.writeStartElement( "dataValue" );

        Map<String, String> groupAttributes = readAttributes( adxReader );

        // pass through the remaining attributes to dxf
        for ( String attribute : groupAttributes.keySet() )
        {
            dxfWriter.writeAttribute( attribute, groupAttributes.get( attribute ) );
        }
        dxfWriter.writeEndElement();
    }

    // TODO  this should really be part of staxwax library
    protected Map<String, String> readAttributes( XMLReader staxWaxReader ) throws XMLStreamException
    {
        Map<String, String> attributes = new HashMap<>();

        XMLStreamReader reader = staxWaxReader.getXmlStreamReader();

        if ( reader.getEventType() != START_ELEMENT )
        {
            throw new IllegalArgumentException( "Trying to retrieve attributes from non START_ELEMENT node" );
        }

        // Read attributes
        for ( int i = 0; i < reader.getAttributeCount(); i++ )
        {
            attributes.put( reader.getAttributeLocalName( i ), reader.getAttributeValue( i ) );
        }

        return attributes;
    }
}

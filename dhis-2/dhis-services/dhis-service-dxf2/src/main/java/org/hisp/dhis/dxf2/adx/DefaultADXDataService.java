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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import org.amplecode.staxwax.factory.XMLFactory;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.datavalueset.DataExportParams;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.dxf2.importsummary.ImportSummaries;
import org.amplecode.staxwax.reader.XMLReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.period.Period;
import org.w3c.dom.Element;

/**
 *
 * @author bobj
 */
public class DefaultADXDataService
    implements ADXDataService
{
    protected DataValueSetService dataValueSetService;

    @Override
    public void getData( DataExportParams params, OutputStream out )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ImportSummaries postData( InputStream in, ImportOptions importOptions )
        throws IOException
    {
        XMLReader reader = XMLFactory.getXMLReader( in );

        ImportSummaries importSummaries = new ImportSummaries();

        reader.moveToStartElement( ADXConstants.ROOT, ADXConstants.NAMESPACE );

        while ( reader.moveToStartElement( ADXConstants.GROUP, ADXConstants.NAMESPACE ) )
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            try
            {
                docBuilder = docFactory.newDocumentBuilder();
                Document dxf = docBuilder.newDocument();

                // buld a dxf2 datavalueset document from each adx group
                parseADXGroupToDxf( reader, dxf );
                
                // write the document to String
                DOMSource source = new DOMSource( dxf );
                StringWriter writer = new StringWriter();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                
                StreamResult result = new StreamResult(writer);
                transformer.transform( source, result );
                // create an inputstream for the String
                InputStream dxfIn = IOUtils.toInputStream(result.toString(), "UTF-8" );
                
                // pass off to the dxf2 datavalueset service
                importSummaries.addImportSummary( dataValueSetService.saveDataValueSet( dxfIn, importOptions ) );
            } 
            catch ( Exception ex )
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

    protected void parseADXGroupToDxf( XMLReader reader, Document dxf ) throws XMLStreamException
    {
        Element root = dxf.createElementNS( "http://dhis2.org/schema/dxf/2.0", "dataValueSet" );
        
        Map<String, String> groupAttributes = readAttributes( reader );

        String periodStr = groupAttributes.get( ADXConstants.PERIOD );
        groupAttributes.remove( ADXConstants.PERIOD );

        Period period = ADXPeriod.parse( periodStr );
        root.setAttribute( "period", period.getIsoDate() );
        // pass through the remaining attributes to dxf
        for ( String attribute : groupAttributes.keySet() )
        {
            root.setAttribute( attribute, groupAttributes.get( attribute ) );
        }

        dxf.appendChild( root );
        
        while ( reader.moveToStartElement( ADXConstants.DATAVALUE, ADXConstants.GROUP ) )
        {
            parseADXDataValueToDxf( reader, dxf );
        }
    }

    protected void parseADXDataValueToDxf( XMLReader reader, Document dxf ) throws XMLStreamException
    {
        Element dv = dxf.createElementNS( "http://dhis2.org/schema/dxf/2.0","dataValue");

        Map<String, String> groupAttributes = readAttributes( reader );

        // pass through the remaining attributes to dxf
        for ( String attribute : groupAttributes.keySet() )
        {
            dv.setAttribute( attribute, groupAttributes.get( attribute ) );
        }
        dxf.getFirstChild().appendChild( dv );
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

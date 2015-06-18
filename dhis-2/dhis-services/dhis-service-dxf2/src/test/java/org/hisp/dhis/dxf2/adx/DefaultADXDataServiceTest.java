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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.amplecode.staxwax.factory.XMLFactory;
import org.amplecode.staxwax.reader.XMLReader;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

/**
 *
 * @author bobj
 */
public class DefaultADXDataServiceTest
{

    protected static final String SIMPLE_ADX_SAMPLE = "adx/adx_data_sample1.xml";

    public DefaultADXDataServiceTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    {
    }

    @AfterClass
    public static void tearDownClass()
    {
    }

    @Before
    public void setUp() throws IOException
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of parseADXGroup method, of class DefaultADXDataService.
     */
    @Test
    public void testParseADXGroup() throws Exception
    {
        try
        {
            System.out.println( "parseADXGroup" );
            XMLReader reader = XMLFactory.getXMLReader( new ClassPathResource( SIMPLE_ADX_SAMPLE ).getInputStream() );
            reader.moveToStartElement( ADXConstants.ROOT, ADXConstants.NAMESPACE );
            System.out.println( "Parsing adx, exported: " + reader.getAttributeValue( "exported" ) );

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            docBuilder = docFactory.newDocumentBuilder();

            DefaultADXDataService instance = new DefaultADXDataService();

            while ( reader.moveToStartElement( ADXConstants.GROUP, ADXConstants.NAMESPACE ) )
            {
                System.out.println( "Parsing new group" );
                Document dxf = docBuilder.newDocument();
                instance.parseADXGroupToDxf( reader, dxf );

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource( dxf );
                StreamResult result = new StreamResult( System.out );

                transformer.transform( source, result );
                System.out.println();
            }
        } 
        catch ( Exception ex )
        {
            fail( ex.toString() );
        }
    }

}

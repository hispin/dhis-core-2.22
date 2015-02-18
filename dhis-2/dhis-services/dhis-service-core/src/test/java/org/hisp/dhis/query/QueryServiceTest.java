package org.hisp.dhis.query;

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

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.schema.SchemaService;
import org.jfree.data.time.Year;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class QueryServiceTest
    extends DhisSpringTest
{
    @Autowired
    private SchemaService schemaService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private IdentifiableObjectManager _identifiableObjectManager;

    @Override
    protected void setUpTest() throws Exception
    {
        this.identifiableObjectManager = _identifiableObjectManager;
    }

    private void createDataElements()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );
        DataElement dataElementD = createDataElement( 'D' );
        DataElement dataElementE = createDataElement( 'E' );
        DataElement dataElementF = createDataElement( 'F' );

        dataElementA.setCreated( Year.parseYear( "2001" ).getStart() );
        dataElementB.setCreated( Year.parseYear( "2002" ).getStart() );
        dataElementC.setCreated( Year.parseYear( "2003" ).getStart() );
        dataElementD.setCreated( Year.parseYear( "2004" ).getStart() );
        dataElementE.setCreated( Year.parseYear( "2005" ).getStart() );
        dataElementF.setCreated( Year.parseYear( "2006" ).getStart() );

        identifiableObjectManager.save( dataElementA );
        identifiableObjectManager.save( dataElementB );
        identifiableObjectManager.save( dataElementC );
        identifiableObjectManager.save( dataElementD );
        identifiableObjectManager.save( dataElementE );
        identifiableObjectManager.save( dataElementF );
    }

    private boolean collectionContainsUid( Collection<? extends IdentifiableObject> collection, String uid )
    {
        for ( IdentifiableObject identifiableObject : collection )
        {
            if ( identifiableObject.getUid().equals( uid ) )
            {
                return true;
            }
        }

        return false;
    }

    @Test
    public void getAllQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        Result result = queryService.query( query );

        assertEquals( 6, result.size() );
    }

    @Test
    public void getMinMaxQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.setFirstResult( 2 );
        query.setMaxResults( 10 );

        assertEquals( 4, queryService.query( query ).size() );

        query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.setFirstResult( 2 );
        query.setMaxResults( 2 );

        assertEquals( 2, queryService.query( query ).size() );
    }

    @Test
    public void getEqQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.eq( "id", "deabcdefghA" ) );
        Result result = queryService.query( query );

        assertEquals( 1, result.size() );
        assertEquals( "deabcdefghA", result.getItems().get( 0 ).getUid() );
    }

    @Test
    public void getNeQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.ne( "id", "deabcdefghA" ) );
        Result result = queryService.query( query );

        assertEquals( 5, result.size() );
    }

    @Test
    public void getLikeQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.like( "name", "%F" ) );
        Result result = queryService.query( query );

        assertEquals( 1, result.size() );
        assertEquals( "deabcdefghF", result.getItems().get( 0 ).getUid() );
    }

    @Test
    public void getGtQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.gt( "created", Year.parseYear( "2003" ).getStart() ) );
        Result result = queryService.query( query );

        assertEquals( 3, result.size() );

        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghD" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghE" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghF" ) );
    }

    @Test
    public void getLtQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.lt( "created", Year.parseYear( "2003" ).getStart() ) );
        Result result = queryService.query( query );

        assertEquals( 2, result.size() );

        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghA" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghB" ) );
    }

    @Test
    public void getGeQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.ge( "created", Year.parseYear( "2003" ).getStart() ) );
        Result result = queryService.query( query );

        assertEquals( 4, result.size() );

        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghC" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghD" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghE" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghF" ) );
    }

    @Test
    public void getLeQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.le( "created", Year.parseYear( "2003" ).getStart() ) );
        Result result = queryService.query( query );

        assertEquals( 3, result.size() );

        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghA" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghB" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghC" ) );
    }

    @Test
    public void getBetweenQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.between( "created", Year.parseYear( "2003" ).getStart(), Year.parseYear( "2005" ).getStart() ) );
        Result result = queryService.query( query );

        assertEquals( 3, result.size() );

        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghC" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghD" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghE" ) );
    }

    @Test
    public void getInQuery()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );
        query.add( Restrictions.in( "id", "deabcdefghD", "deabcdefghF" ) );
        Result result = queryService.query( query );

        assertEquals( 2, result.size() );

        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghD" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghF" ) );
    }

    @Test
    public void resultTransformerTest()
    {
        createDataElements();
        Query query = Query.from( schemaService.getDynamicSchema( DataElement.class ) );

        Result result = queryService.query( query, new ResultTransformer()
        {
            @Override
            public Result transform( MutableResult result )
            {
                return new Result();
            }
        } );

        assertEquals( 0, result.size() );

        result = queryService.query( query, new ResultTransformer()
        {
            @Override
            public Result transform( MutableResult result )
            {
                return new Result( result.getItems() );
            }
        } );

        assertEquals( 6, result.size() );

        result = queryService.query( query, new ResultTransformer()
        {
            @Override
            public Result transform( MutableResult result )
            {
                Iterator<? extends IdentifiableObject> iterator = result.getItems().iterator();

                while ( iterator.hasNext() )
                {
                    IdentifiableObject identifiableObject = iterator.next();

                    if ( identifiableObject.getUid().equals( "deabcdefghD" ) )
                    {
                        iterator.remove();
                    }
                }

                return result;
            }
        } );

        assertEquals( 5, result.size() );

        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghA" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghB" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghC" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghE" ) );
        assertTrue( collectionContainsUid( result.getItems(), "deabcdefghF" ) );
    }
}

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

import org.hisp.dhis.query.operators.Between;
import org.hisp.dhis.query.operators.Equal;
import org.hisp.dhis.query.operators.GreaterEqual;
import org.hisp.dhis.query.operators.GreaterThan;
import org.hisp.dhis.query.operators.ILike;
import org.hisp.dhis.query.operators.LessEqual;
import org.hisp.dhis.query.operators.LessThan;
import org.hisp.dhis.query.operators.Like;
import org.hisp.dhis.query.operators.NotEqual;
import org.hisp.dhis.query.operators.NotNull;
import org.hisp.dhis.query.operators.Null;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class OperatorTest
{
    @Test
    public void testBetweenValidTypes()
    {
        Between operator = new Between( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testEqualValidTypes()
    {
        Equal operator = new Equal( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testEqual()
    {
        Equal operator = new Equal( "operator" );

        assertTrue( operator.test( "operator" ) );
        assertFalse( operator.test( Boolean.TRUE ) );
        assertFalse( operator.test( new Float( 0 ) ) );
        assertFalse( operator.test( Collections.emptyList() ) );
    }

    @Test
    public void testNotEqualValidTypes()
    {
        NotEqual operator = new NotEqual( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testGreaterEqualValidTypes()
    {
        GreaterEqual operator = new GreaterEqual( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testGreaterThanValidTypes()
    {
        GreaterThan operator = new GreaterThan( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testILikeValidTypes()
    {
        ILike operator = new ILike( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertFalse( operator.isValid( Number.class ) );
        assertFalse( operator.isValid( Date.class ) );
        assertFalse( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testLikeValidTypes()
    {
        Like operator = new Like( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertFalse( operator.isValid( Number.class ) );
        assertFalse( operator.isValid( Date.class ) );
        assertFalse( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testLessEqualValidTypes()
    {
        LessEqual operator = new LessEqual( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testLessThanValidTypes()
    {
        LessThan operator = new LessThan( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testNullValidTypes()
    {
        Null operator = new Null( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testNotNullValidTypes()
    {
        NotNull operator = new NotNull( "operator" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }
}

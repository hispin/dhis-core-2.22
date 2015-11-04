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

import java.util.Arrays;
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
        Between operator = new Between( "10", "20" );

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testBetweenInt()
    {
        Between operator = new Between( "10", "20" );

        assertTrue( operator.test( 10 ) );
        assertTrue( operator.test( 15 ) );
        assertTrue( operator.test( 20 ) );
        assertFalse( operator.test( 9 ) );
        assertFalse( operator.test( 21 ) );
    }

    @Test
    public void testBetweenCollection()
    {
        Between operator = new Between( "2", "4" );

        assertFalse( operator.test( Collections.singletonList( 1 ) ) );
        assertTrue( operator.test( Arrays.asList( 1, 2 ) ) );
        assertTrue( operator.test( Arrays.asList( 1, 2, 3 ) ) );
        assertTrue( operator.test( Arrays.asList( 1, 2, 3, 4 ) ) );
        assertFalse( operator.test( Arrays.asList( 1, 2, 3, 4, 5 ) ) );
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
    public void testNotEqual()
    {
        NotEqual operator = new NotEqual( "operator" );

        assertFalse( operator.test( "operator" ) );
        assertTrue( operator.test( Boolean.TRUE ) );
        assertTrue( operator.test( new Float( 0 ) ) );
        assertTrue( operator.test( Collections.emptyList() ) );
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
    public void testGreaterEqual()
    {
        GreaterEqual operator = new GreaterEqual( "10" );

        assertFalse( operator.test( 6 ) );
        assertFalse( operator.test( 7 ) );
        assertFalse( operator.test( 8 ) );
        assertFalse( operator.test( 9 ) );
        assertTrue( operator.test( 10 ) );
        assertTrue( operator.test( 11 ) );
        assertTrue( operator.test( 12 ) );
        assertTrue( operator.test( 13 ) );
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
    public void testGreaterThan()
    {
        GreaterThan operator = new GreaterThan( "10" );

        assertFalse( operator.test( 6 ) );
        assertFalse( operator.test( 7 ) );
        assertFalse( operator.test( 8 ) );
        assertFalse( operator.test( 9 ) );
        assertFalse( operator.test( 10 ) );
        assertTrue( operator.test( 11 ) );
        assertTrue( operator.test( 12 ) );
        assertTrue( operator.test( 13 ) );
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
    public void testILike()
    {
        ILike operator = new ILike( "operator" );

        assertTrue( operator.test( "operator" ) );
        assertTrue( operator.test( "OPERATOR" ) );
        assertFalse( operator.test( "abc" ) );
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
    public void testLike()
    {
        Like operator = new Like( "operator" );

        assertTrue( operator.test( "operator" ) );
        assertFalse( operator.test( "OPERATOR" ) );
        assertFalse( operator.test( "abc" ) );
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
    public void testLessEqual()
    {
        LessEqual operator = new LessEqual( "10" );

        assertTrue( operator.test( 6 ) );
        assertTrue( operator.test( 7 ) );
        assertTrue( operator.test( 8 ) );
        assertTrue( operator.test( 9 ) );
        assertTrue( operator.test( 10 ) );
        assertFalse( operator.test( 11 ) );
        assertFalse( operator.test( 12 ) );
        assertFalse( operator.test( 13 ) );
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
    public void testLessThan()
    {
        LessThan operator = new LessThan( "10" );

        assertTrue( operator.test( 6 ) );
        assertTrue( operator.test( 7 ) );
        assertTrue( operator.test( 8 ) );
        assertTrue( operator.test( 9 ) );
        assertFalse( operator.test( 10 ) );
        assertFalse( operator.test( 11 ) );
        assertFalse( operator.test( 12 ) );
        assertFalse( operator.test( 13 ) );
    }

    @Test
    public void testNullValidTypes()
    {
        Null operator = new Null();

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testNull()
    {
        Null operator = new Null();

        assertTrue( operator.test( null ) );
        assertFalse( operator.test( "test" ) );
    }

    @Test
    public void testNotNullValidTypes()
    {
        NotNull operator = new NotNull();

        assertTrue( operator.isValid( String.class ) );
        assertTrue( operator.isValid( Number.class ) );
        assertTrue( operator.isValid( Date.class ) );
        assertTrue( operator.isValid( Boolean.class ) );
        assertFalse( operator.isValid( Collection.class ) );
    }

    @Test
    public void testNotNull()
    {
        NotNull operator = new NotNull();

        assertFalse( operator.test( null ) );
        assertTrue( operator.test( "test" ) );
    }
}

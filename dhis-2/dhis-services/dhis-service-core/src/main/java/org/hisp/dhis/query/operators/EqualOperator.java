package org.hisp.dhis.query.operators;

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

import org.hisp.dhis.query.Typed;

import java.util.Collection;
import java.util.Date;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class EqualOperator extends Operator
{
    public EqualOperator( String arg )
    {
        super( Typed.from( String.class, Boolean.class, Number.class, Date.class ), arg );
    }

    @Override
    public boolean test( Object value )
    {
        if ( args.isEmpty() || value == null )
        {
            return false;
        }

        if ( String.class.isInstance( value ) )
        {
            String s1 = getValue( String.class );
            String s2 = (String) value;

            return s1 != null && s2.equals( s1 );
        }
        else if ( Boolean.class.isInstance( value ) )
        {
            Boolean s1 = getValue( Boolean.class );
            Boolean s2 = (Boolean) value;

            return s1 != null && s2.equals( s1 );
        }
        else if ( Integer.class.isInstance( value ) )
        {
            Integer s1 = getValue( Integer.class );
            Integer s2 = (Integer) value;

            return s1 != null && s2.equals( s1 );
        }
        else if ( Float.class.isInstance( value ) )
        {
            Float s1 = getValue( Float.class );
            Float s2 = (Float) value;

            return s1 != null && s2.equals( s1 );
        }
        else if ( Collection.class.isInstance( value ) )
        {
            Collection<?> collection = (Collection<?>) value;
            Integer size = getValue( Integer.class );

            return size != null && collection.size() == size;
        }
        else if ( Date.class.isInstance( value ) )
        {
            Date s1 = getValue( Date.class );
            Date s2 = (Date) value;

            return s1 != null && s2.equals( s1 );
        }
        else if ( Enum.class.isInstance( value ) )
        {
            String s1 = args.get( 0 );
            String s2 = String.valueOf( value );

            return s2.equals( s1 );
        }

        return false;
    }
}

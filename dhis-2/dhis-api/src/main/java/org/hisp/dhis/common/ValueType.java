package org.hisp.dhis.common;

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

import com.google.common.collect.Lists;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;

import java.util.Date;
import java.util.List;

/**
 * @author Lars Helge Overland
 */
public enum ValueType
{
    TEXT( String.class ),
    LONG_TEXT( String.class ),
    LETTER( String.class ),
    PHONE_NUMBER( String.class ),
    EMAIL( String.class ),
    BOOLEAN( Boolean.class ),
    TRUE_ONLY( Boolean.class ),
    DATE( Date.class ),
    DATETIME( Date.class ),
    NUMBER( Double.class ),
    UNIT_INTERVAL( Double.class ),
    PERCENTAGE( Double.class ),
    INTEGER( Integer.class ),
    INTEGER_POSITIVE( Integer.class ),
    INTEGER_NEGATIVE( Integer.class ),
    INTEGER_ZERO_OR_POSITIVE( Integer.class ),
    TRACKER_ASSOCIATE( TrackedEntityInstance.class ),
    OPTION_SET( String.class ),
    USERNAME( String.class );

    public static List<ValueType> INTEGER_TYPES = Lists.newArrayList(
        INTEGER, INTEGER_POSITIVE, INTEGER_NEGATIVE, INTEGER_ZERO_OR_POSITIVE
    );

    public static List<ValueType> NUMERIC_TYPES = Lists.newArrayList(
        INTEGER, INTEGER_POSITIVE, INTEGER_NEGATIVE, INTEGER_ZERO_OR_POSITIVE, NUMBER, UNIT_INTERVAL, PERCENTAGE
    );

    public static List<ValueType> TEXT_TYPES = Lists.newArrayList( TEXT, LONG_TEXT, LETTER );

    public static List<String> INTEGER_TYPE_STRINGS = Lists.newArrayList(
        INTEGER.toString(), INTEGER_POSITIVE.toString(), INTEGER_NEGATIVE.toString(), INTEGER_ZERO_OR_POSITIVE.toString()
    );

    public static List<String> NUMERIC_TYPE_STRINGS = Lists.newArrayList(
        INTEGER.toString(), INTEGER_POSITIVE.toString(), INTEGER_NEGATIVE.toString(), INTEGER_ZERO_OR_POSITIVE.toString(),
        NUMBER.toString(), UNIT_INTERVAL.toString(), PERCENTAGE.toString()
    );

    public static List<String> TEXT_TYPE_STRINGS = Lists.newArrayList( TEXT.toString(), LONG_TEXT.toString(), LETTER.toString() );

    private final Class<?> javaClass;

    ValueType()
    {
        this.javaClass = null;
    }

    ValueType( Class<?> javaClass )
    {
        this.javaClass = javaClass;
    }

    public Class<?> getJavaClass()
    {
        return javaClass;
    }

    public boolean isInteger()
    {
        return this == INTEGER || this == INTEGER_POSITIVE || this == INTEGER_NEGATIVE || this == INTEGER_ZERO_OR_POSITIVE;
    }

    public boolean isNumeric()
    {
        return this.isInteger() || this == NUMBER || this == UNIT_INTERVAL || this == PERCENTAGE;
    }

    public boolean isText()
    {
        return this == TEXT || this == LONG_TEXT;
    }

    public boolean isDate()
    {
        return this == DATE || this == DATETIME;
    }

    /**
     * TODO replace string value type on data element with ValueType and remove
     * this method.
     */
    public static ValueType getFromDataElement( DataElement dataElement )
    {
        return getFromDataElementTypes( dataElement.getType(), dataElement.getNumberType(), dataElement.getTextType() );
    }

    public static ValueType getFromDataElementTypes( String type, String numberType, String textType )
    {
        if ( DataElement.VALUE_TYPE_STRING.equals( type ) )
        {
            if ( DataElement.VALUE_TYPE_LONG_TEXT.equals( textType ) )
            {
                return ValueType.LONG_TEXT;
            }
            else
            {
                return ValueType.TEXT;
            }
        }
        else if ( DataElement.VALUE_TYPE_INT.equals( type ) )
        {
            if ( DataElement.VALUE_TYPE_UNIT_INTERVAL.equals( numberType ) )
            {
                return ValueType.UNIT_INTERVAL;
            }
            else if ( DataElement.VALUE_TYPE_PERCENTAGE.equals( numberType ) )
            {
                return ValueType.PERCENTAGE;
            }
            else if ( DataElement.VALUE_TYPE_INT.equals( numberType ) )
            {
                return ValueType.INTEGER;
            }
            else if ( DataElement.VALUE_TYPE_POSITIVE_INT.equals( numberType ) )
            {
                return ValueType.INTEGER_POSITIVE;
            }
            else if ( DataElement.VALUE_TYPE_ZERO_OR_POSITIVE_INT.equals( numberType ) )
            {
                return ValueType.INTEGER_ZERO_OR_POSITIVE;
            }
            else if ( DataElement.VALUE_TYPE_NEGATIVE_INT.equals( numberType ) )
            {
                return ValueType.INTEGER_NEGATIVE;
            }
            else
            {
                return ValueType.NUMBER;
            }
        }
        else if ( DataElement.VALUE_TYPE_BOOL.equals( type ) )
        {
            return ValueType.BOOLEAN;
        }
        else if ( DataElement.VALUE_TYPE_TRUE_ONLY.equals( type ) )
        {
            return ValueType.TRUE_ONLY;
        }
        else if ( DataElement.VALUE_TYPE_DATE.equals( type ) )
        {
            return ValueType.DATE;
        }
        else if ( DataElement.VALUE_TYPE_DATETIME.equals( type ) )
        {
            return ValueType.DATETIME;
        }
        else if ( DataElement.VALUE_TYPE_USER_NAME.equals( type ) )
        {
            return ValueType.USERNAME;
        }

        return ValueType.TEXT; // Fall back
    }

    /**
     * TODO replace string value type on attribute with ValueType and remove
     * this method.
     */
    public static ValueType getFromAttribute( TrackedEntityAttribute attribute )
    {
        if ( TrackedEntityAttribute.TYPE_NUMBER.equals( attribute.getValueType() ) || DataElement.VALUE_TYPE_INT.equals( attribute.getValueType() ) )
        {
            return ValueType.NUMBER;
        }
        else if ( TrackedEntityAttribute.TYPE_BOOL.equals( attribute.getValueType() ) || TrackedEntityAttribute.TYPE_TRUE_ONLY.equals( attribute.getValueType() ) )
        {
            return ValueType.BOOLEAN;
        }
        else if ( TrackedEntityAttribute.TYPE_DATE.equals( attribute.getValueType() ) )
        {
            return ValueType.DATE;
        }
        else if ( TrackedEntityAttribute.TYPE_TRACKER_ASSOCIATE.equals( attribute.getValueType() ) )
        {
            return ValueType.TRACKER_ASSOCIATE;
        }
        else if ( TrackedEntityAttribute.TYPE_USERS.equals( attribute.getValueType() ) )
        {
            return ValueType.USERNAME;
        }

        return ValueType.TEXT; // Fall back
    }

    //TODO remove and replace with ValueType.valueOf
    public static ValueType fromValue( String value )
    {
        for ( ValueType valueType : ValueType.values() )
        {
            if ( valueType.toString().equalsIgnoreCase( value ) )
            {
                return valueType;
            }
        }

        return null;
    }
}

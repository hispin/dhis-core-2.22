package org.hisp.dhis.dxf2.common;

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

import com.google.common.base.MoreObjects;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableProperty;
import org.springframework.util.StringUtils;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class IdSchemes
{
    private IdentifiableProperty idScheme = IdentifiableProperty.UID;

    private String idSchemeAttribute;

    private IdentifiableProperty dataElementIdScheme = IdentifiableProperty.UID;

    private String dataElementIdSchemeAttribute;

    private IdentifiableProperty categoryOptionComboIdScheme = IdentifiableProperty.UID;

    private String categoryOptionComboIdSchemeAttribute;

    private IdentifiableProperty orgUnitIdScheme = IdentifiableProperty.UID;

    private String orgUnitIdSchemeAttribute;

    private IdentifiableProperty programIdScheme = IdentifiableProperty.UID;

    private String programIdSchemeAttribute;

    private IdentifiableProperty programStageIdScheme = IdentifiableProperty.UID;

    private String programStageIdSchemeAttribute;

    public IdSchemes()
    {
    }

    public IdentifiableProperty getScheme( IdentifiableProperty identifiableProperty )
    {
        return idScheme != null ? idScheme : identifiableProperty;
    }

    public IdentifiableProperty getIdScheme()
    {
        return idScheme;
    }

    public String getIdSchemeAttribute()
    {
        return idSchemeAttribute;
    }

    public IdSchemes setIdScheme( String idScheme )
    {
        if ( isAttribute( idScheme ) )
        {
            this.idScheme = IdentifiableProperty.ATTRIBUTE;
            this.idSchemeAttribute = idScheme.substring( 10 );
            return this;
        }

        this.idScheme = IdentifiableProperty.valueOf( idScheme.toUpperCase() );
        return this;
    }

    public IdentifiableProperty getDataElementIdScheme()
    {
        return getScheme( dataElementIdScheme );
    }

    public String getDataElementIdSchemeAttribute()
    {
        return dataElementIdSchemeAttribute;
    }

    public IdSchemes setDataElementIdScheme( String idScheme )
    {
        if ( isAttribute( idScheme ) )
        {
            this.dataElementIdScheme = IdentifiableProperty.ATTRIBUTE;
            this.dataElementIdSchemeAttribute = idScheme.substring( 10 );
            return this;
        }

        this.dataElementIdScheme = IdentifiableProperty.valueOf( idScheme.toUpperCase() );
        return this;
    }

    public IdentifiableProperty getCategoryOptionComboIdScheme()
    {
        return getScheme( categoryOptionComboIdScheme );
    }

    public String getCategoryOptionComboIdSchemeAttribute()
    {
        return categoryOptionComboIdSchemeAttribute;
    }

    public IdSchemes setCategoryOptionComboIdScheme( String idScheme )
    {
        if ( isAttribute( idScheme ) )
        {
            this.categoryOptionComboIdScheme = IdentifiableProperty.ATTRIBUTE;
            this.categoryOptionComboIdSchemeAttribute = idScheme.substring( 10 );
            return this;
        }

        this.categoryOptionComboIdScheme = IdentifiableProperty.valueOf( idScheme.toUpperCase() );
        return this;
    }

    public IdentifiableProperty getOrgUnitIdScheme()
    {
        return getScheme( orgUnitIdScheme );
    }

    public String getOrgUnitIdSchemeAttribute()
    {
        return orgUnitIdSchemeAttribute;
    }

    public IdSchemes setOrgUnitIdScheme( String idScheme )
    {
        if ( isAttribute( idScheme ) )
        {
            this.orgUnitIdScheme = IdentifiableProperty.ATTRIBUTE;
            this.orgUnitIdSchemeAttribute = idScheme.substring( 10 );
            return this;
        }

        this.orgUnitIdScheme = IdentifiableProperty.valueOf( idScheme.toUpperCase() );
        return this;
    }

    public IdentifiableProperty getProgramIdScheme()
    {
        return getScheme( programIdScheme );
    }

    public String getProgramIdSchemeAttribute()
    {
        return programIdSchemeAttribute;
    }

    public IdSchemes setProgramIdScheme( String idScheme )
    {
        if ( isAttribute( idScheme ) )
        {
            this.programIdScheme = IdentifiableProperty.ATTRIBUTE;
            this.programIdSchemeAttribute = idScheme.substring( 10 );
            return this;
        }

        this.programIdScheme = IdentifiableProperty.valueOf( idScheme.toUpperCase() );
        return this;
    }

    public IdentifiableProperty getProgramStageIdScheme()
    {
        return getScheme( programStageIdScheme );
    }

    public String getProgramStageIdSchemeAttribute()
    {
        return programStageIdSchemeAttribute;
    }

    public IdSchemes setProgramStageIdScheme( String idScheme )
    {
        if ( isAttribute( idScheme ) )
        {
            this.programStageIdScheme = IdentifiableProperty.ATTRIBUTE;
            this.programStageIdSchemeAttribute = idScheme.substring( 10 );
            return this;
        }

        this.programStageIdScheme = IdentifiableProperty.valueOf( idScheme.toUpperCase() );
        return this;
    }

    public static String getValue( String uid, String code, IdentifiableProperty identifiableProperty )
    {
        boolean idScheme = IdentifiableProperty.ID.equals( identifiableProperty ) || IdentifiableProperty.UID.equals( identifiableProperty );
        return idScheme ? uid : code;
    }


    public static String getValue( IdentifiableObject identifiableObject, IdentifiableProperty identifiableProperty )
    {
        boolean idScheme = IdentifiableProperty.ID.equals( identifiableProperty ) || IdentifiableProperty.UID.equals( identifiableProperty );

        if ( idScheme )
        {
            return identifiableObject.getUid();
        }
        else if ( IdentifiableProperty.CODE.equals( identifiableProperty ) )
        {
            return identifiableObject.getCode();
        }
        else if ( IdentifiableProperty.NAME.equals( identifiableProperty ) )
        {
            return identifiableObject.getName();
        }

        return null;
    }

    public static boolean isAttribute( String str )
    {
        return !StringUtils.isEmpty( str ) && str.toUpperCase().startsWith( "ATTRIBUTE:" ) && str.length() == 21;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
            .add( "idScheme", idScheme )
            .add( "idSchemeAttribute", idSchemeAttribute )
            .add( "dataElementIdScheme", dataElementIdScheme )
            .add( "dataElementIdSchemeAttribute", dataElementIdSchemeAttribute )
            .add( "categoryOptionComboIdScheme", categoryOptionComboIdScheme )
            .add( "categoryOptionComboIdSchemeAttribute", categoryOptionComboIdSchemeAttribute )
            .add( "orgUnitIdScheme", orgUnitIdScheme )
            .add( "orgUnitIdSchemeAttribute", orgUnitIdSchemeAttribute )
            .add( "programIdScheme", programIdScheme )
            .add( "programIdSchemeAttribute", programIdSchemeAttribute )
            .add( "programStageIdScheme", programStageIdScheme )
            .add( "programStageIdSchemeAttribute", programStageIdSchemeAttribute )
            .toString();
    }
}

package org.hisp.dhis.common;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import java.util.List;

import org.hisp.dhis.dataelement.CategoryOptionGroup;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.program.ProgramIndicator;

/**
 * @author Lars Helge Overland
 */
public abstract class GenericAnalyticalObjectService<T extends BaseAnalyticalObject>
    implements AnalyticalObjectService<T>
{
    protected abstract AnalyticalObjectStore<T> getAnalyticalObjectStore();

    public List<T> getAnalyticalObjects( Indicator indicator )
    {
        return getAnalyticalObjectStore().getAnalyticalObjects( indicator );
    }
    
    public List<T> getAnalyticalObjects( DataElement dataElement )
    {
        return getAnalyticalObjectStore().getAnalyticalObjects( dataElement );
    }
    
    public List<T> getAnalyticalObjects( DataSet dataSet )
    {
        return getAnalyticalObjectStore().getAnalyticalObjects( dataSet );
    }

    public List<T> getAnalyticalObjects( ProgramIndicator programIndicator )
    {
        return getAnalyticalObjectStore().getAnalyticalObjects( programIndicator );
    }

    public List<T> getAnalyticalObjects( Period period )
    {
        return getAnalyticalObjectStore().getAnalyticalObjects( period );
    }

    public List<T> getAnalyticalObjects( OrganisationUnit organisationUnit )
    {
        return getAnalyticalObjectStore().getAnalyticalObjects( organisationUnit );
    }

    public List<T> getAnalyticalObjects( CategoryOptionGroup categoryOptionGroup )
    {
        return getAnalyticalObjectStore().getAnalyticalObjects( categoryOptionGroup );
    }
    
    public int countAnalyticalObjects( Indicator indicator )
    {
        return getAnalyticalObjectStore().countAnalyticalObjects( indicator );
    }

    public int countAnalyticalObjects( DataElement dataElement )
    {
        return getAnalyticalObjectStore().countAnalyticalObjects( dataElement );
    }

    public int countAnalyticalObjects( DataSet dataSet )
    {
        return getAnalyticalObjectStore().countAnalyticalObjects( dataSet );
    }

    public int countAnalyticalObjects( ProgramIndicator programIndicator )
    {
        return getAnalyticalObjectStore().countAnalyticalObjects( programIndicator );
    }
    
    public int countAnalyticalObjects( Period period )
    {
        return getAnalyticalObjectStore().countAnalyticalObjects( period );
    }
    
    public int countAnalyticalObjects( OrganisationUnit organisationUnit )
    {
        return getAnalyticalObjectStore().countAnalyticalObjects( organisationUnit );
    }
    
    public int countAnalyticalObjects( CategoryOptionGroup categoryOptionGroup )
    {
        return getAnalyticalObjectStore().countAnalyticalObjects( categoryOptionGroup );
    }
}

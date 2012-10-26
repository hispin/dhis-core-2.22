package org.hisp.dhis.api.controller;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.Pager;

/**
 * @author Lars Helge Overland
 */
public class AbstractAccessControlController<T extends IdentifiableObject>
    extends AbstractCrudController<T>
{
    @Override
    protected List<T> getEntityList( WebMetaData metaData, WebOptions options )
    {
        List<T> entityList;

        Date lastUpdated = options.getLastUpdated();

        if ( lastUpdated != null )
        {
            entityList = new ArrayList<T>( manager.getAccessibleByLastUpdated( getEntityClass(), lastUpdated ) );
        }
        else if ( options.hasPaging() )
        {
            int count = manager.getCount( getEntityClass() );

            Pager pager = new Pager( options.getPage(), count );
            metaData.setPager( pager );

            entityList = new ArrayList<T>( manager.getAccessibleBetween( getEntityClass(), pager.getOffset(), pager.getPageSize() ) );
        }
        else
        {
            entityList = new ArrayList<T>( manager.getAllAccessible( getEntityClass() ) );
        }

        return entityList;
    }

    @Override
    protected List<T> queryForEntityList( WebMetaData metaData, WebOptions options, String query )
    {
        List<T> entityList;

        if ( options.hasPaging() )
        {
            int count = manager.getCount( getEntityClass() );

            Pager pager = new Pager( options.getPage(), count );
            metaData.setPager( pager );

            entityList = new ArrayList<T>( manager.getAccessibleBetweenLikeName( getEntityClass(), query, pager.getOffset(), pager.getPageSize() ) );
        }
        else
        {
            entityList = new ArrayList<T>( manager.getAccessibleLikeName( getEntityClass(), query ) );
        }

        return entityList;
    }
}

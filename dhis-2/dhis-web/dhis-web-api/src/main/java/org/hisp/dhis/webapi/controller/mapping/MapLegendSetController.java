package org.hisp.dhis.webapi.controller.mapping;

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

import org.hisp.dhis.common.MergeStrategy;
import org.hisp.dhis.dxf2.common.JacksonUtils;
import org.hisp.dhis.mapping.MapLegend;
import org.hisp.dhis.mapping.MapLegendSet;
import org.hisp.dhis.mapping.MappingService;
import org.hisp.dhis.schema.descriptors.MapLegendSetSchemaDescriptor;
import org.hisp.dhis.webapi.controller.AbstractCrudController;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping( value = MapLegendSetSchemaDescriptor.API_ENDPOINT )
public class MapLegendSetController
    extends AbstractCrudController<MapLegendSet>
{
    @Autowired
    private MappingService mappingService;

    @Override
    @RequestMapping( method = RequestMethod.POST, consumes = "application/json" )
    @PreAuthorize( "hasRole('F_GIS_ADMIN') or hasRole('ALL')" )
    public void postJsonObject( HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        MapLegendSet legendSet = JacksonUtils.fromJson( request.getInputStream(), MapLegendSet.class );

        for ( MapLegend legend : legendSet.getMapLegends() )
        {
            mappingService.addMapLegend( legend );
        }

        mappingService.addMapLegendSet( legendSet );

        ContextUtils.createdResponse( response, "Map legend set created", MapLegendSetSchemaDescriptor.API_ENDPOINT + "/" + legendSet.getUid() );
    }

    @Override
    @RequestMapping( value = "/{uid}", method = RequestMethod.PUT, consumes = "application/json" )
    @PreAuthorize( "hasRole('F_GIS_ADMIN') or hasRole('ALL')" )
    public void putJsonObject( @PathVariable String uid, HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        MapLegendSet legendSet = mappingService.getMapLegendSet( uid );

        if ( legendSet == null )
        {
            ContextUtils.notFoundResponse( response, "Map legend set does not exist: " + uid );
            return;
        }

        Iterator<MapLegend> legends = legendSet.getMapLegends().iterator();

        while ( legends.hasNext() )
        {
            MapLegend legend = legends.next();
            legends.remove();
            mappingService.deleteMapLegend( legend );
        }

        MapLegendSet newLegendSet = JacksonUtils.fromJson( request.getInputStream(), MapLegendSet.class );

        for ( MapLegend legend : newLegendSet.getMapLegends() )
        {
            mappingService.addMapLegend( legend );
        }

        legendSet.mergeWith( newLegendSet, MergeStrategy.MERGE_IF_NOT_NULL );

        mappingService.updateMapLegendSet( legendSet );
    }

    @Override
    @RequestMapping( value = "/{uid}", method = RequestMethod.DELETE )
    @PreAuthorize( "hasRole('F_GIS_ADMIN') or hasRole('ALL')" )
    public void deleteObject( @PathVariable String uid, HttpServletRequest request, HttpServletResponse response ) throws Exception
    {
        MapLegendSet legendSet = mappingService.getMapLegendSet( uid );

        if ( legendSet == null )
        {
            ContextUtils.notFoundResponse( response, "Map legend set does not exist: " + uid );
            return;
        }

        Iterator<MapLegend> legends = legendSet.getMapLegends().iterator();

        while ( legends.hasNext() )
        {
            MapLegend legend = legends.next();
            legends.remove();
            mappingService.deleteMapLegend( legend );
        }

        mappingService.deleteMapLegendSet( legendSet );
    }
}

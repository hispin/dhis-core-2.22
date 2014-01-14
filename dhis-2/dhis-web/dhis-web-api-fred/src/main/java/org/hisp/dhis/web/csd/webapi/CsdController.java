package org.hisp.dhis.web.csd.webapi;

/*
 * Copyright (c) 2004-2013, University of Oslo
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

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.web.csd.domain.Envelope;
import org.hisp.dhis.web.csd.domain.csd.Csd;
import org.hisp.dhis.web.csd.domain.csd.Facility;
import org.hisp.dhis.web.csd.domain.csd.OtherID;
import org.hisp.dhis.web.csd.domain.csd.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@Controller
@RequestMapping( value = "/csd" )
public class CsdController
{
    @Autowired
    private OrganisationUnitService organisationUnitService;

    @RequestMapping( value = "", method = RequestMethod.POST )
    public @ResponseBody Csd csdRequest( @RequestBody Envelope envelope, HttpServletResponse response ) throws IOException
    {
        Date lastModified = null;

        try
        {
            lastModified = envelope.getBody().getGetModificationsRequest().getLastModified();
        }
        catch ( NullPointerException ex )
        {
            throw new HttpClientErrorException( HttpStatus.BAD_REQUEST );
        }

        List<OrganisationUnit> byLastUpdated = new ArrayList<OrganisationUnit>(
            organisationUnitService.getAllOrganisationUnitsByLastUpdated( lastModified ) );

        Csd csd = convertToCsd( byLastUpdated );

        return csd;
    }

    private Csd convertToCsd( Iterable<OrganisationUnit> organisationUnits )
    {
        Csd csd = new Csd();
        csd.getFacilityDirectory().setFacilities( new ArrayList<Facility>() );

        for ( OrganisationUnit organisationUnit : organisationUnits )
        {
            Facility facility = new Facility();
            facility.setOid( organisationUnit.getUid() ); // TODO use code for OID?

            facility.getOtherID().add( new OtherID( organisationUnit.getUid(), "dhis2-uid" ) );

            if ( organisationUnit.getCode() != null )
            {
                facility.getOtherID().add( new OtherID( organisationUnit.getCode(), "dhis2-code" ) );
            }

            facility.setPrimaryName( organisationUnit.getDisplayName() );

            Record record = new Record();
            record.setCreated( organisationUnit.getCreated() );
            record.setUpdated( organisationUnit.getLastUpdated() );

            if ( organisationUnit.isActive() )
            {
                record.setStatus( "Active" );
            }

            facility.setRecord( record );

            csd.getFacilityDirectory().getFacilities().add( facility );
        }

        return csd;
    }
}

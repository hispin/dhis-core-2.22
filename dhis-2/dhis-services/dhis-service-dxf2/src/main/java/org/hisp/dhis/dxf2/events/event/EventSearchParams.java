package org.hisp.dhis.dxf2.events.event;

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

import java.util.Date;

import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.dxf2.common.IdSchemes;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStatus;

/**
 * @author Lars Helge Overland
 */
public class EventSearchParams
{
    private Program program;
    
    private ProgramStage programStage;
    
    private ProgramStatus programStatus;
    
    private Boolean followUp;
    
    private OrganisationUnit orgUnit;
    
    private OrganisationUnitSelectionMode orgUnitSelectionMode;
    
    private TrackedEntityInstance trackedEntityInstance;
    
    private Date startDate;
    
    private Date endDate;
    
    private EventStatus eventStatus;
    
    private Date lastUpdated;
    
    private IdSchemes idSchemes;

    private Integer page;
    
    private Integer pageSize;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EventSearchParams()
    {
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public boolean isPaging()
    {
        return page != null || pageSize != null;
    }

    public int getPageWithDefault()
    {
        return page != null && page > 0 ? page : 1;
    }
    
    public int getPageSizeWithDefault()
    {
        return pageSize != null && pageSize >= 0 ? pageSize : 100;
    }

    public int getOffset()
    {
        return ( getPageWithDefault() - 1 ) * getPageSizeWithDefault();
    }
    
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Program getProgram()
    {
        return program;
    }

    public void setProgram( Program program )
    {
        this.program = program;
    }

    public ProgramStage getProgramStage()
    {
        return programStage;
    }

    public void setProgramStage( ProgramStage programStage )
    {
        this.programStage = programStage;
    }

    public ProgramStatus getProgramStatus()
    {
        return programStatus;
    }

    public void setProgramStatus( ProgramStatus programStatus )
    {
        this.programStatus = programStatus;
    }

    public Boolean getFollowUp()
    {
        return followUp;
    }

    public void setFollowUp( Boolean followUp )
    {
        this.followUp = followUp;
    }

    public OrganisationUnit getOrgUnit()
    {
        return orgUnit;
    }

    public void setOrgUnit( OrganisationUnit orgUnit )
    {
        this.orgUnit = orgUnit;
    }

    public OrganisationUnitSelectionMode getOrgUnitSelectionMode()
    {
        return orgUnitSelectionMode;
    }

    public void setOrgUnitSelectionMode( OrganisationUnitSelectionMode orgUnitSelectionMode )
    {
        this.orgUnitSelectionMode = orgUnitSelectionMode;
    }

    public TrackedEntityInstance getTrackedEntityInstance()
    {
        return trackedEntityInstance;
    }

    public void setTrackedEntityInstance( TrackedEntityInstance trackedEntityInstance )
    {
        this.trackedEntityInstance = trackedEntityInstance;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate( Date startDate )
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate( Date endDate )
    {
        this.endDate = endDate;
    }

    public EventStatus getEventStatus()
    {
        return eventStatus;
    }

    public void setEventStatus( EventStatus eventStatus )
    {
        this.eventStatus = eventStatus;
    }

    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated( Date lastUpdated )
    {
        this.lastUpdated = lastUpdated;
    }

    public IdSchemes getIdSchemes()
    {
        return idSchemes;
    }

    public void setIdSchemes( IdSchemes idSchemes )
    {
        this.idSchemes = idSchemes;
    }

    public Integer getPage()
    {
        return page;
    }

    public void setPage( Integer page )
    {
        this.page = page;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public void setPageSize( Integer pageSize )
    {
        this.pageSize = pageSize;
    }
}

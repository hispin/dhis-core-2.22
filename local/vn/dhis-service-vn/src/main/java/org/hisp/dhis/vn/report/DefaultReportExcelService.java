package org.hisp.dhis.vn.report;
/*
 * Copyright (c) 2004-2007, University of Oslo
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
import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserStore;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
@Transactional
public class DefaultReportExcelService
    implements ReportExcelService
{
    // -------------------------------------------------
    // Dependency
    // -------------------------------------------------

    private ReportExcelStore reportStore;

    public void setReportStore( ReportExcelStore reportStore )
    {
        this.reportStore = reportStore;
    }

    private UserStore userStore;

    // -------------------------------------------------
    // Service of Report
    // -------------------------------------------------

    public void setUserStore( UserStore userStore )
    {
        this.userStore = userStore;
    }

    public int addReport( ReportExcelInterface report )
    {
        return reportStore.addReport( report );
    }

    public void updateReport( ReportExcelInterface report )
    {
        reportStore.updateReport( report );
    }

    public void deleteReport( int id )
    {
        reportStore.deleteReport( id );
    }

    public ReportExcelInterface getReport( int id )
    {
        return reportStore.getReport( id );
    }

    public ReportExcelInterface getReport( String name )
    {
        return reportStore.getReport( name );
    }

    public Collection<ReportExcelInterface> getReportsByOrganisationUnit( OrganisationUnit organisationUnit )
    {
        return reportStore.getReportsByOrganisationUnit( organisationUnit );
    }

    public Collection<ReportExcelInterface> getALLReport()
    {
        return reportStore.getALLReport();
    }

    public Collection<ReportExcelInterface> getReports( User user, boolean superUser, String group )
    {
        if ( user == null || (user != null && superUser) )
        {
            return this.getReportsByGroup( group );
        }
        else
        {
            Set<UserAuthorityGroup> userRoles = userStore.getUserCredentials( user ).getUserAuthorityGroups();

            Collection<ReportExcelInterface> reports = new ArrayList<ReportExcelInterface>();

            for ( ReportExcelInterface report : this.getReportsByGroup( group ) )
            {
                if ( CollectionUtils.intersection( report.getUserRoles(), userRoles ).size() > 0 )
                {
                    reports.add( report );
                }
            }

            return reports;
        }
    }

    public Collection<String> getReportGroups()
    {
        return reportStore.getReportGroups();
    }

    public Collection<ReportExcelInterface> getReportsByGroup( String group )
    {
        return reportStore.getReportsByGroup( group );

    }

    // -------------------------------------------------
    // Service of Report Item
    // -------------------------------------------------

    public void addReportItem( ReportItem reportItem )
    {
        reportStore.addReportItem( reportItem );
    }

    public void updateReportItem( ReportItem reportItem )
    {
        reportStore.updateReportItem( reportItem );
    }

    public void deleteReportItem( int id )
    {
        reportStore.deleteReportItem( id );
    }

    public ReportItem getReportItem( int id )
    {
        return reportStore.getReportItem( id );
    }

    public ReportItem getReportItem( String name )
    {
        return reportStore.getReportItem( name );
    }

    public Collection<ReportItem> getALLReportItem()
    {
        return reportStore.getALLReportItem();
    }

    public Collection<ReportItem> getReportItem( String itemType, ReportExcelNormal reportExcelNormal )
    {
        return reportStore.getReportItem( itemType, reportExcelNormal );
    }

    public Collection<ReportItem> getReportItem( int sheetNo, Integer reportId )
    {
        return reportStore.getReportItem( sheetNo, reportId );
    }

    public Collection<Integer> getSheets( Integer reportId )
    {
        return reportStore.getSheets( reportId );
    }
 

    // --------------------------------------
    // Report DataElement Order
    // --------------------------------------
 
    public DataElementOrderInGroup getDataElementOrderInGroup( Integer arg0 )
    {       
        return reportStore.getDataElementOrderInGroup( arg0 );
    }

    public void updateDataElementOrderInGroup( DataElementOrderInGroup arg0 )
    {
        reportStore.updateDataElementOrderInGroup( arg0 );
    }    
        
    
}

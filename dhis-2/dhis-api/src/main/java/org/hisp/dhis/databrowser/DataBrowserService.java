package org.hisp.dhis.databrowser;

/*
 * Copyright (c) 2004-${year}, University of Oslo
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

import org.hisp.dhis.period.PeriodType;

/**
 * @author jonasaar, briane, eivinhb
 */
public interface DataBrowserService
{
    String ID = DataBrowserService.class.getName();

    // -------------------------------------------------------------------------
    // DataBrowser
    // -------------------------------------------------------------------------

    /**
     * Method that retrieves - all DataSets with DataElement quantity - in a
     * given period and type (DataSet | Count)
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */
    DataBrowserTable getDataSetsInPeriod( String startDate, String endDate, PeriodType periodType );

    /**
     * Method that retrieves - all DataElementGroups with DataElement quantity -
     * in a given period and type (DataElementGroup | Count)
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */
    DataBrowserTable getDataElementGroupsInPeriod( String startDate, String endDate, PeriodType periodType );
  
    /**
     * Method that retrieves - all OrganisationUnitGroups with DataElement quantity - in
     * a given period and type (OrgUnitGroup | Count)
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */
    DataBrowserTable getOrgUnitGroupsInPeriod( String startDate, String endDate, PeriodType periodType );

    /**
     * Method that retrieves - all OrganisationUnits with DataElement quantity - in a
     * given period - that is child of a given OrganisationUnit parent.
     * 
     * @param orgUnitParent the OrganisationUnit parent
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */
    DataBrowserTable getOrgUnitsInPeriod( Integer orgUnitParent, String startDate, String endDate, PeriodType periodType );

    /**
     * Method that retrieves - all the DataElements count - in a given period -
     * for a given DataSet and returns a DataBrowserTable with the data.
     * 
     * @param dataSetId the DataSet id
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */
    DataBrowserTable getCountDataElementsForDataSetInPeriod( Integer dataSetId, String startDate, String endDate,
        PeriodType periodType );

    /**
     * Method that retrieves - all the DataElements count - in a given period -
     * for a given DataElementGroup and returns a DataBrowserTable with the data.
     * 
     * @param dataElementGroupId the DataElementGroup id
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */
    DataBrowserTable getCountDataElementsForDataElementGroupInPeriod( Integer dataElementGroupId, String startDate,
        String endDate, PeriodType periodType );

    /**
     * Method retrieves - all the DataElementGroups count - in a given period -
     * for a given OrganisationUnitGroup and returns a DataBrowserTable with the data.
     * 
     * @param orgUnitGroupId the OrganisationUnitGroup id
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */  
    DataBrowserTable getCountDataElementGroupsForOrgUnitGroupInPeriod( Integer orgUnitGroupId, String startDate,
        String endDate, PeriodType periodType );

    /**
     * Method that retrieves - all the DataElements count - in a given period - 
     * for a given OrganisationUnit and returns a DataBrowserTable with the data.
     * 
     * @param orgUnitId the OrganisationUnit id
     * @param startDate the start date
     * @param endDate the end date
     * @param periodType the period type
     * @return DataBrowserTable the DataBrowserTable with structure for presentation
     */
    DataBrowserTable getCountDataElementsForOrgUnitInPeriod( Integer orgUnitId, String startDate,
        String endDate, PeriodType periodType );    

}

package org.hisp.dhis.dataapproval.hibernate;

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

import static org.hisp.dhis.dataapproval.DataApprovalState.ACCEPTED_HERE;
import static org.hisp.dhis.dataapproval.DataApprovalState.APPROVED_ABOVE;
import static org.hisp.dhis.dataapproval.DataApprovalState.APPROVED_HERE;
import static org.hisp.dhis.dataapproval.DataApprovalState.UNAPPROVABLE;
import static org.hisp.dhis.dataapproval.DataApprovalState.UNAPPROVED_ABOVE;
import static org.hisp.dhis.dataapproval.DataApprovalState.UNAPPROVED_READY;
import static org.hisp.dhis.dataapproval.DataApprovalState.UNAPPROVED_WAITING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.common.IdentifiableObjectUtils;
import org.hisp.dhis.commons.collection.CachingMap;
import org.hisp.dhis.dataapproval.DataApproval;
import org.hisp.dhis.dataapproval.DataApprovalLevel;
import org.hisp.dhis.dataapproval.DataApprovalLevelService;
import org.hisp.dhis.dataapproval.DataApprovalState;
import org.hisp.dhis.dataapproval.DataApprovalStatus;
import org.hisp.dhis.dataapproval.DataApprovalStore;
import org.hisp.dhis.dataapproval.DataApprovalWorkflow;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Jim Grace
 */
public class HibernateDataApprovalStore
    extends HibernateGenericStore<DataApproval>
    implements DataApprovalStore
{
    private static final Log log = LogFactory.getLog( HibernateDataApprovalStore.class );

    private static final int MAX_APPROVAL_LEVEL = 99999999;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private DataApprovalLevelService dataApprovalLevelService;

    public void setDataApprovalLevelService( DataApprovalLevelService dataApprovalLevelService )
    {
        this.dataApprovalLevelService = dataApprovalLevelService;
    }

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    // -------------------------------------------------------------------------
    // DataApproval
    // -------------------------------------------------------------------------

    @Override
    public void addDataApproval( DataApproval dataApproval )
    {
        dataApproval.setPeriod( periodService.reloadPeriod( dataApproval.getPeriod() ) );

        save( dataApproval );
    }

    @Override
    public void updateDataApproval( DataApproval dataApproval )
    {
        dataApproval.setPeriod( periodService.reloadPeriod( dataApproval.getPeriod() ) );

        update( dataApproval );
    }

    @Override
    public void deleteDataApproval( DataApproval dataApproval )
    {
        dataApproval.setPeriod( periodService.reloadPeriod( dataApproval.getPeriod() ) );

        delete( dataApproval );
    }    

    @Override
    public void deleteDataApprovals( OrganisationUnit organisationUnit )
    {
        String hql = "delete from DataApproval d where d.organisationUnit = :unit";
        
        sessionFactory.getCurrentSession().createQuery( hql ).
            setEntity( "unit", organisationUnit ).executeUpdate();
    }

    @Override
    public DataApproval getDataApproval( DataApproval dataApproval )
    {
        return getDataApproval( dataApproval.getDataApprovalLevel(), dataApproval.getWorkflow(),
            dataApproval.getPeriod(), dataApproval.getOrganisationUnit(), dataApproval.getAttributeOptionCombo() );
    }

    @Override
    public DataApproval getDataApproval( DataApprovalLevel dataApprovalLevel, DataApprovalWorkflow workflow, Period period,
        OrganisationUnit organisationUnit, DataElementCategoryOptionCombo attributeOptionCombo )
    {
        Period storedPeriod = periodService.reloadPeriod( period );

        Criteria criteria = getCriteria();
        criteria.add( Restrictions.eq( "dataApprovalLevel", dataApprovalLevel ) );
        criteria.add( Restrictions.eq( "workflow", workflow ) );
        criteria.add( Restrictions.eq( "period", storedPeriod ) );
        criteria.add( Restrictions.eq( "organisationUnit", organisationUnit ) );
        criteria.add( Restrictions.eq( "attributeOptionCombo", attributeOptionCombo ) );

        return (DataApproval) criteria.uniqueResult();
    }

    @Override
    public List<DataApprovalStatus> getDataApprovals( DataApprovalWorkflow workflow,
        Period period, OrganisationUnit orgUnit, DataElementCategoryCombo attributeCombo,
        Set<DataElementCategoryOptionCombo> attributeOptionCombos )
    {
        final CachingMap<Integer, DataElementCategoryOptionCombo> optionComboCache = new CachingMap<>();
        final CachingMap<Integer, OrganisationUnit> orgUnitCache = new CachingMap<>();
        
        final User user = currentUserService.getCurrentUser();

        final String startDate = DateUtils.getMediumDateString( period.getStartDate() );
        final String endDate = DateUtils.getMediumDateString( period.getEndDate() );

        boolean maySeeDefaultCategoryCombo = user == null || user.getUserCredentials() == null ||
            ( CollectionUtils.isEmpty( user.getUserCredentials().getCogsDimensionConstraints() )
            && CollectionUtils.isEmpty( user.getUserCredentials().getCatDimensionConstraints() ) );

        boolean isDefaultCombo = attributeOptionCombos != null && attributeOptionCombos.size() == 1
            && categoryService.getDefaultDataElementCategoryOptionCombo().equals( attributeOptionCombos.toArray()[0] );

        if ( isDefaultCombo && !maySeeDefaultCategoryCombo )
        {
            log.warn( "DefaultCategoryCombo selected but user " + user.getUsername() + " lacks permission to see it." );

            return new ArrayList<>(); // Unapprovable.
        }

        List<DataApprovalLevel> approvalLevels = workflow.getSortedLevels();

        if ( CollectionUtils.isEmpty( approvalLevels ) )
        {
            log.info( "No approval levels configured for workflow " + workflow.getName() );

            return new ArrayList<>(); // Unapprovable.
        }

        int orgUnitLevel = 0;
        String orgUnitJoinOn = null;
        String highestApprovedOrgUnitCompare = null;

        if ( orgUnit != null )
        {
            orgUnitLevel = orgUnit.getLevel();
            orgUnitJoinOn = "o.organisationunitid = " + orgUnit.getId();
            highestApprovedOrgUnitCompare = "da.organisationunitid = o.organisationunitid ";
        }
        else
        {
            highestApprovedOrgUnitCompare = "";
            orgUnitLevel = 0;

            for ( DataApprovalLevel dal : approvalLevels )
            {
                if ( dal.getOrgUnitLevel() != orgUnitLevel )
                {
                    orgUnitLevel = dal.getOrgUnitLevel(); // Remember lowest (last level -> greatest number) level.

                    highestApprovedOrgUnitCompare += ( highestApprovedOrgUnitCompare.length() == 0 ? "(" : " or" )
                        + " da.organisationunitid = o.idlevel" + orgUnitLevel;
                }
            }

            highestApprovedOrgUnitCompare += ") ";

            orgUnitJoinOn = "o.level = " + orgUnitLevel;
        }

        boolean isSuperUser = currentUserService.currentUserIsSuper();

        DataApprovalLevel lowestApprovalLevelForOrgUnit = null;

        String joinAncestors = StringUtils.EMPTY;
        String testAncestors = StringUtils.EMPTY;

        for ( int i = 1; i < orgUnitLevel; i++ )
        {
            joinAncestors += "left join _orgunitstructure o" + i + " on o" + i + ".organisationunitid = o.organisationunitid and o" + i + ".idlevel" + i + " = coo.organisationunitid ";
            testAncestors += "or o" + i + ".organisationunitid is not null ";
        }

        String readyBelowSubquery = "true"; // Ready below if this is the lowest (highest number) approval orgUnit level.

        int orgUnitLevelAbove = 0;

        int highestApprovalOrgUnitLevel = MAX_APPROVAL_LEVEL;

        for ( DataApprovalLevel dal : approvalLevels )
        {
            if ( dal.getOrgUnitLevel() < highestApprovalOrgUnitLevel )
            {
                highestApprovalOrgUnitLevel = dal.getOrgUnitLevel();
            }

            if ( dal.getOrgUnitLevel() < orgUnitLevel )
            {
                orgUnitLevelAbove = dal.getOrgUnitLevel(); // Keep getting the lowest org unit level above ours.
            }

            if ( dal.getOrgUnitLevel() == orgUnitLevel )
            {
                lowestApprovalLevelForOrgUnit = dal;
            }

            if ( dal.getOrgUnitLevel() > orgUnitLevel ) // If there is a lower (higher number) approval orgUnit level.
            {
                String coOrgUnitConstraint = ""; // Constrain lower level orgUnit by categoryOption orgUnit(s) (if any).

                if ( !isDefaultCombo )
                {
                    for ( int i = 1; i <= dal.getOrgUnitLevel(); i++ )
                    {
                        coOrgUnitConstraint += ( i == 1 ? "" : "or " ) + "( o2.level = " + i + " and ous.idlevel" + i + " = c_o.organisationunitid ) ";
                    }
                }

                boolean acceptanceRequiredForApproval = (Boolean) systemSettingManager.getSystemSetting( SettingKey.ACCEPTANCE_REQUIRED_FOR_APPROVAL );

                readyBelowSubquery = "not exists (select 1 from _orgunitstructure ous " +
                    "where not exists (select 1 from dataapproval da " +
                        "join period p on p.periodid = da.periodid " +
                        "where da.organisationunitid = ous.organisationunitid " +
                        "and da.dataapprovallevelid = " + dal.getId() +
                        "and '" + endDate + "' >= p.startdate and '" + endDate + "' <= p.enddate " +
                        "and da.workflowid = " + workflow.getId() +
                        "and da.attributeoptioncomboid = cocco.categoryoptioncomboid " +
                        ( acceptanceRequiredForApproval ? "and da.accepted " : "" ) +
                    ") " +
                    "and ous.idlevel" + orgUnitLevel + " = o.organisationunitid " +
                    "and ous.level = " + dal.getOrgUnitLevel() + " " +
                    ( isDefaultCombo ? "" :
                        "and ( not exists ( select 1 from categoryoption_organisationunits c_o where c_o.categoryoptionid = cocco.categoryoptionid ) " +
                            "or exists ( select 1 from categoryoption_organisationunits c_o " +
                            "join _orgunitstructure o2 on o2.organisationunitid = c_o.organisationunitid " +
                            "where c_o.categoryoptionid = cocco.categoryoptionid and (" + coOrgUnitConstraint + ") ) ) " ) +
                    ")";
                break;
            }
        }

        String approvedAboveSubquery = "false"; // Not approved above if this is the highest (lowest number) approval orgUnit level.

        if ( orgUnitLevelAbove > 0 && orgUnit != null )
        {
            approvedAboveSubquery = "exists(select 1 from dataapproval da " +
                "join period p on p.periodid = da.periodid " +
                "join dataapprovallevel dal on dal.dataapprovallevelid = da.dataapprovallevelid " +
                "join _orgunitstructure ou on ou.organisationunitid = o.organisationunitid and ou.idlevel" + orgUnitLevelAbove + " = da.organisationunitid " +
                "where '" + endDate + "' >= p.startdate and '" + endDate + "' <= p.enddate " +
                "and da.workflowid = " + workflow.getId() + " and da.attributeoptioncomboid = cocco.categoryoptioncomboid)";
        }

        final String sql =
            "select cocco.categoryoptioncomboid, o.organisationunitid, " +
            "(select min(coalesce(dal.level + case when da.accepted then .0 else .1 end, 0)) from period p " +
                "left join dataapproval da on da.workflowid = " + workflow.getId() + " and da.periodid = p.periodid " +
                "left join dataapprovallevel dal on dal.dataapprovallevelid = da.dataapprovallevelid " +
                "where '" + endDate + "' >= p.startdate and '" + endDate + "' <= p.enddate " +
                "and da.attributeoptioncomboid = cocco.categoryoptioncomboid and " + highestApprovedOrgUnitCompare +
            ") as highest_approved, " +
            readyBelowSubquery + " as ready_below, " +
            approvedAboveSubquery + " as approved_above " +
            "from categoryoptioncombos_categoryoptions cocco " +
                ( attributeCombo == null ? "" : "join categorycombos_optioncombos ccoc on ccoc.categoryoptioncomboid = cocco.categoryoptioncomboid " +
                    " and ccoc.categorycomboid = " + attributeCombo.getId() + " " ) +
                "join dataelementcategoryoption co on co.categoryoptionid = cocco.categoryoptionid " +
                    "and (co.startdate is null or co.startdate <= '" + endDate + "') and (co.enddate is null or co.enddate >= '" + startDate + "') " +
                "join _orgunitstructure o on " + orgUnitJoinOn + " " +
                "left join categoryoption_organisationunits coo on coo.categoryoptionid = co.categoryoptionid " +
                "left join _orgunitstructure ous on ous.idlevel" + orgUnitLevel + " = o.organisationunitid and ous.organisationunitid = coo.organisationunitid " +
                joinAncestors +
                "where ( coo.categoryoptionid is null or ous.organisationunitid is not null " + testAncestors + ")" +
                ( attributeOptionCombos == null || attributeOptionCombos.isEmpty() ? "" : " and cocco.categoryoptioncomboid in (" +
                    StringUtils.join( IdentifiableObjectUtils.getIdentifiers( attributeOptionCombos ), "," ) + ") " ) +
                ( isSuperUser || user == null ? "" :
                    " and ( co.publicaccess is null or left(co.publicaccess, 1) = 'r' or co.userid is null or co.userid = " + user.getId() + " or exists ( " +
                    "select 1 from dataelementcategoryoptionusergroupaccesses couga " +
                    "left join usergroupaccess uga on uga.usergroupaccessid = couga.usergroupaccessid " +
                    "left join usergroupmembers ugm on ugm.usergroupid = uga.usergroupid " +
                    "where couga.categoryoptionid = cocco.categoryoptionid and ugm.userid = " + user.getId() + ") )" );

        log.debug( "Get approval SQL: " + sql );

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( sql );

        Map<Integer, DataApprovalLevel> levelMap = dataApprovalLevelService.getDataApprovalLevelMap();
        
        List<DataApprovalStatus> statusList = new ArrayList<>();

        while ( rowSet.next() )
        {
            final Integer aoc = rowSet.getInt( 1 );
            final Integer ouId = rowSet.getInt( 2 );
            final Double highestApproved = rowSet.getDouble( 3 );
            final boolean readyBelow = rowSet.getBoolean( 4 );
            final boolean approvedAbove = rowSet.getBoolean( 5 );

            final int level = highestApproved == null ? 0 : highestApproved.intValue();
            final boolean accepted = ( highestApproved == level );

            DataApprovalLevel statusLevel = ( level == 0 ? null : levelMap.get( level ) ); // null if not approved
            DataApprovalLevel daLevel = ( statusLevel == null ? lowestApprovalLevelForOrgUnit : statusLevel );

            DataElementCategoryOptionCombo optionCombo = aoc == null || aoc == 0 ? null : optionComboCache.get( aoc, () -> categoryService.getDataElementCategoryOptionCombo( aoc ) );

            OrganisationUnit ou = orgUnit != null ? orgUnit : orgUnitCache.get( ouId, () -> organisationUnitService.getOrganisationUnit( ouId ) );

            if ( ou != null )
            {
                DataApproval da = new DataApproval( daLevel, workflow, period, ou, optionCombo, accepted, null, null );

                DataApprovalState state = (
                    approvedAbove ?
                        APPROVED_ABOVE :
                        statusLevel == null ?
                            lowestApprovalLevelForOrgUnit == null ?
                                orgUnitLevelAbove == 0 ?
                                    UNAPPROVABLE :
                                    UNAPPROVED_ABOVE :
                                readyBelow ?
                                    UNAPPROVED_READY :
                                    UNAPPROVED_WAITING :
                            accepted ?
                                ACCEPTED_HERE :
                                APPROVED_HERE );
    
                statusList.add( new DataApprovalStatus( state, da, statusLevel, null ) );
    
                log.debug( "Get approval result: level " + level + " dataApprovalLevel " + ( daLevel != null ? daLevel.getLevel() : "[none]" )
                    + " approved " + ( statusLevel != null )
                    + " readyBelow " + readyBelow + " approvedAbove " + approvedAbove
                    + " accepted " + accepted + " state " + ( state != null ? state.name() : "[none]" ) + " " + da );
            }
        }

        return statusList;
    }
}

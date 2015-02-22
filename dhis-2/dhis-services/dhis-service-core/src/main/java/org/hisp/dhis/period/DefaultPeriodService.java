package org.hisp.dhis.period;

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

import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.Filter;
import org.hisp.dhis.system.util.FilterUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Kristian Nordal
 * @version $Id: DefaultPeriodService.java 5983 2008-10-17 17:42:44Z larshelg $
 */
@Transactional
public class DefaultPeriodService
    implements PeriodService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodStore periodStore;

    public void setPeriodStore( PeriodStore periodStore )
    {
        this.periodStore = periodStore;
    }

    // -------------------------------------------------------------------------
    // Period
    // -------------------------------------------------------------------------

    @Override
    public int addPeriod( Period period )
    {
        return periodStore.addPeriod( period );
    }

    @Override
    public void deletePeriod( Period period )
    {
        periodStore.delete( period );
    }

    @Override
    public Period getPeriod( int id )
    {
        return periodStore.get( id );
    }
    
    @Override
    public Period getPeriod( String isoPeriod )
    {
        Period period = PeriodType.getPeriodFromIsoString( isoPeriod );
        
        if ( period != null )
        {        
            period = periodStore.getPeriod( period.getStartDate(), period.getEndDate(), period.getPeriodType() );
        }
        
        return period;
    }

    @Override
    public Period getPeriod( Date startDate, Date endDate, PeriodType periodType )
    {
        return periodStore.getPeriod( startDate, endDate, periodType );
    }

    @Override
    public Collection<Period> getAllPeriods()
    {
        return periodStore.getAll();
    }

    @Override
    public Collection<Period> getPeriods( final Collection<Integer> identifiers )
    {
        Collection<Period> periods = getAllPeriods();

        return identifiers == null ? periods : FilterUtils.filter( periods, new Filter<Period>()
        {
            @Override
            public boolean retain( Period object )
            {
                return identifiers.contains( object.getId() );
            }
        } );
    }

    @Override
    public Collection<Period> getPeriodsByPeriodType( PeriodType periodType )
    {
        return periodStore.getPeriodsByPeriodType( periodType );
    }

    @Override
    public Collection<Period> getPeriodsBetweenDates( Date startDate, Date endDate )
    {
        return periodStore.getPeriodsBetweenDates( startDate, endDate );
    }

    @Override
    public Collection<Period> getPeriodsBetweenDates( PeriodType periodType, Date startDate, Date endDate )
    {
        return periodStore.getPeriodsBetweenDates( periodType, startDate, endDate );
    }

    @Override
    public Collection<Period> getPeriodsBetweenOrSpanningDates( Date startDate, Date endDate )
    {
        return periodStore.getPeriodsBetweenOrSpanningDates( startDate, endDate );
    }

    @Override
    public Collection<Period> getIntersectingPeriodsByPeriodType( PeriodType periodType, Date startDate, Date endDate )
    {
        return periodStore.getIntersectingPeriodsByPeriodType( periodType, startDate, endDate );
    }

    @Override
    public Collection<Period> getIntersectingPeriods( Date startDate, Date endDate )
    {
        return periodStore.getIntersectingPeriods( startDate, endDate );
    }

    @Override
    public Collection<Period> getIntersectionPeriods( Collection<Period> periods )
    {
        Set<Period> intersecting = new HashSet<>();

        for ( Period period : periods )
        {
            intersecting.addAll( getIntersectingPeriods( period.getStartDate(), period.getEndDate() ) );
        }

        return intersecting;
    }

    @Override
    public Collection<Period> getBoundaryPeriods( Period period, Collection<Period> periods )
    {
        Collection<Period> immutablePeriods = new ArrayList<>( periods );

        Iterator<Period> iterator = immutablePeriods.iterator();

        while ( iterator.hasNext() )
        {
            Period iterated = iterator.next();

            if ( !DateUtils.strictlyBetween( period.getStartDate(), iterated.getStartDate(), iterated.getEndDate() )
                && !DateUtils.strictlyBetween( period.getEndDate(), iterated.getStartDate(), iterated.getEndDate() ) )
            {
                iterator.remove();
            }
        }

        return immutablePeriods;
    }

    @Override
    public Collection<Period> getInclusivePeriods( Period period, Collection<Period> periods )
    {
        Collection<Period> immutablePeriods = new ArrayList<>( periods );

        Iterator<Period> iterator = immutablePeriods.iterator();

        while ( iterator.hasNext() )
        {
            Period iterated = iterator.next();

            if ( !DateUtils.between( iterated.getStartDate(), period.getStartDate(), period.getEndDate() )
                || !DateUtils.between( iterated.getEndDate(), period.getStartDate(), period.getEndDate() ) )
            {
                iterator.remove();
            }
        }

        return immutablePeriods;
    }

    @Override
    public List<Period> reloadPeriods( List<Period> periods )
    {
        List<Period> reloaded = new ArrayList<>();

        for ( Period period : periods )
        {
            reloaded.add( periodStore.reloadForceAddPeriod( period ) );
        }

        return reloaded;
    }

    @Override
    public List<Period> getPeriods( Period lastPeriod, int historyLength )
    {
        List<Period> periods = new ArrayList<>( historyLength );

        lastPeriod = periodStore.reloadForceAddPeriod( lastPeriod );

        CalendarPeriodType periodType = (CalendarPeriodType) lastPeriod.getPeriodType();

        for ( int i = 0; i < historyLength; ++i )
        {
            Period p = getPeriodFromDates( lastPeriod.getStartDate(), lastPeriod.getEndDate(), periodType );

            periods.add( p != null ? p : lastPeriod );

            lastPeriod = periodType.getPreviousPeriod( lastPeriod );
        }

        Collections.reverse( periods );

        return periods;
    }

    @Override
    public Collection<Period> namePeriods( Collection<Period> periods, I18nFormat format )
    {
        for ( Period period : periods )
        {
            period.setName( format.formatPeriod( period ) );
        }

        return periods;
    }

    @Override
    public Period getPeriodFromDates( Date startDate, Date endDate, PeriodType periodType )
    {
        return periodStore.getPeriodFromDates( startDate, endDate, periodType );
    }

    @Override
    public Period reloadPeriod( Period period )
    {
        return periodStore.reloadForceAddPeriod( period );
    }
    
    @Override
    public Period reloadIsoPeriod( String isoPeriod )
    {
        Period period = PeriodType.getPeriodFromIsoString( isoPeriod );
        
        return period != null ? reloadPeriod( period ) : null;
    }
    
    @Override
    public List<Period> reloadIsoPeriods( List<String> isoPeriods )
    {
        List<Period> periods = new ArrayList<>();
        
        for ( String iso : isoPeriods )
        {
            Period period = reloadIsoPeriod( iso );
            
            if ( period != null )
            {
                periods.add( period );
            }
        }
        
        return periods;
    }
    
    @Override
    public PeriodHierarchy getPeriodHierarchy( Collection<Period> periods )
    {
        PeriodHierarchy hierarchy = new PeriodHierarchy();
        
        for ( Period period : periods )
        {
            hierarchy.getIntersectingPeriods().put( period.getId(), new HashSet<>( getIdentifiers( Period.class, getIntersectingPeriods( period.getStartDate(), period.getEndDate() ) ) ) );
            hierarchy.getPeriodsBetween().put( period.getId(), new HashSet<>( getIdentifiers( Period.class, getPeriodsBetweenDates( period.getStartDate(), period.getEndDate() ) ) ) );
        }
        
        return hierarchy;
    }

    // -------------------------------------------------------------------------
    // PeriodType
    // -------------------------------------------------------------------------

    @Override
    public PeriodType getPeriodType( int id )
    {
        return periodStore.getPeriodType( id );
    }

    @Override
    public List<PeriodType> getAllPeriodTypes()
    {
        return PeriodType.getAvailablePeriodTypes();
    }

    @Override
    public PeriodType getPeriodTypeByName( String name )
    {
        return PeriodType.getPeriodTypeByName( name );
    }

    @Override
    public PeriodType getPeriodTypeByClass( Class<? extends PeriodType> periodType )
    {
        return periodStore.getPeriodType( periodType );
    }

    @Override
    public PeriodType reloadPeriodType( PeriodType periodType )
    {
        return periodStore.reloadPeriodType( periodType );
    }

    // -------------------------------------------------------------------------
    // PeriodType
    // -------------------------------------------------------------------------

    @Override
    public void deleteRelativePeriods( RelativePeriods relativePeriods )
    {
        periodStore.deleteRelativePeriods( relativePeriods );
    }
}

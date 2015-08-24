package org.hisp.dhis.calendar.impl;

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

import org.hisp.dhis.calendar.Calendar;
import org.hisp.dhis.calendar.DateTimeUnit;
import org.hisp.dhis.period.Cal;
import org.hisp.dhis.period.DailyPeriodType;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class EthiopianCalendarTest
{
    private Calendar calendar;

    @Before
    public void init()
    {
        calendar = EthiopianCalendar.getInstance();
    }

    @Test
    public void testIsoStartOfYear()
    {
        DateTimeUnit startOfYear = calendar.isoStartOfYear( 2007 );

        assertEquals( 2014, startOfYear.getYear() );
        assertEquals( 9, startOfYear.getMonth() );
        assertEquals( 11, startOfYear.getDay() );
    }

    @Test
    public void testDaysInMonth()
    {
        int month12 = calendar.daysInMonth( 2007, 12 );
        int month13 = calendar.daysInMonth( 2007, 13 );

        assertEquals( 36, month12 );
        assertEquals( 36, month13 );

        month12 = calendar.daysInMonth( 2004, 12 );
        month13 = calendar.daysInMonth( 2004, 13 );

        assertEquals( 35, month12 );
        assertEquals( 35, month13 );
    }

    @Test
    public void testGenerateDailyPeriods()
    {
        Date startDate = new Cal( 1975, 1, 1, true ).time();
        Date endDate = new Cal( 2025, 1, 2, true ).time();

        List<Period> days = new DailyPeriodType().generatePeriods( calendar, startDate, endDate );
        assertEquals( 18264, days.size() );
    }

    @Test
    public void testGenerateQuarterlyPeriods()
    {
        Date startDate = new Cal( 1975, 1, 1, true ).time();
        Date endDate = new Cal( 2025, 1, 2, true ).time();

        List<Period> quarters = new QuarterlyPeriodType().generatePeriods( calendar, startDate, endDate );
        assertEquals( 201, quarters.size() );
    }

    @Test
    @Ignore
    public void testGenerateMonthlyPeriods()
    {
        Date startDate = new Cal( 1975, 1, 1, true ).time();
        Date endDate = new Cal( 2025, 1, 2, true ).time();

        List<Period> monthly = new MonthlyPeriodType().generatePeriods( calendar, startDate, endDate );
        assertEquals( 601, monthly.size() );
    }
}

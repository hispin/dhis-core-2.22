package org.hisp.dhis.period;

/*
 * Copyright (c) 2004-2014, University of Oslo
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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hisp.dhis.common.BaseNameableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.Weighted;
import org.hisp.dhis.common.adapter.JacksonPeriodTypeDeserializer;
import org.hisp.dhis.common.adapter.JacksonPeriodTypeSerializer;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author Kristian Nordal
 */
@JacksonXmlRootElement( localName = "period", namespace = DxfNamespaces.DXF_2_0)
public class Period
    extends BaseNameableObject
    implements Weighted
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -4445992494203466044L;

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Required.
     */
    private PeriodType periodType;

    /**
     * Required. Must be unique together with endDate.
     */
    private Date startDate;

    /**
     * Required. Must be unique together with startDate.
     */
    private Date endDate;
    
    /**
     * Transient string holding the ISO representation of the period.
     */
    private transient String isoPeriod;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Period()
    {
    }

    public Period( Period period )
    {
        this.id = period.getId();
        this.periodType = period.getPeriodType();
        this.startDate = period.getStartDate();
        this.endDate = period.getEndDate();
        this.name = period.getName();
        this.isoPeriod = period.getIsoDate();
    }

    protected Period( PeriodType periodType, Date startDate, Date endDate )
    {
        this.periodType = periodType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    protected Period( PeriodType periodType, Date startDate, Date endDate, String isoPeriod )
    {
        this.periodType = periodType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isoPeriod = isoPeriod;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    @Override
    public String getUid()
    {
        return getIsoDate();
    }

    @Override
    public String getCode()
    {
        return getIsoDate();
    }
    
    @Override
    public String getName()
    {
        return name != null ? name : getIsoDate();
    }

    @Override
    public String getShortName()
    {
        return shortName != null ? shortName : getIsoDate();
    }

    /**
     * Returns an ISO8601 formatted string version of the period.
     *
     * @return the period string
     */
    public String getIsoDate()
    {
        return isoPeriod != null ? isoPeriod : periodType.getIsoDate( this );
    }

    /**
     * Copies the transient properties (name) from the argument Period
     * to this Period.
     *
     * @param other Period to copy from.
     * @return this Period.
     */
    public Period copyTransientProperties( Period other )
    {
        this.name = other.getName();
        this.shortName = other.getShortName();
        this.code = other.getCode();

        return this;
    }

    /**
     * Returns the frequency order of the period type of the period.
     *
     * @return the frequency order.
     */
    public int frequencyOrder()
    {
        return periodType != null ? periodType.getFrequencyOrder() : YearlyPeriodType.FREQUENCY_ORDER;
    }

    @Override
    public int getWeight()
    {
        return frequencyOrder() + 15;
    }

    /**
     * Returns start date formatted as string.
     *
     * @return start date formatted as string.
     */
    public String getStartDateString()
    {
        return getMediumDateString( startDate );
    }
    
    /**
     * Returns end date formatted as string.
     *
     * @return end date formatted as string.
     */
    public String getEndDateString()
    {
        return getMediumDateString( endDate );
    }

    /**
     * Formats a Date to the format YYYY-MM-DD.
     *
     * @param date the Date to parse.
     * @return A formatted date string. Null if argument is null.
     */
    private String getMediumDateString( Date date )
    {
        final SimpleDateFormat format = new SimpleDateFormat();

        format.applyPattern( DEFAULT_DATE_FORMAT );

        return date != null ? format.format( date ) : null;
    }

    /**
     * Returns the potential number of periods of the given period type which is
     * spanned by this period.
     *
     * @param type the period type.
     * @return the potential number of periods of the given period type spanned
     *         by this period.
     */
    public int getPeriodSpan( PeriodType type )
    {
        double no = (double) this.periodType.getFrequencyOrder() / type.getFrequencyOrder();

        return (int) Math.floor( no );
    }

    /**
     * Returns the number of days in the period, i.e. the days between the start
     * and end date.
     * 
     * @return number of days in period.
     */
    public int getDaysInPeriod()
    {
        Days days = Days.daysBetween( new DateTime( startDate ), new DateTime( endDate ) );
        return days.getDays() + 1;
    }
    
    // -------------------------------------------------------------------------
    // hashCode, equals and toString
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        int prime = 31;
        int result = 1;

        result = result * prime + (startDate != null ? startDate.hashCode() : 0);
        result = result * prime + (endDate != null ? endDate.hashCode() : 0);
        result = result * prime + (periodType != null ? periodType.hashCode() : 0);

        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null )
        {
            return false;
        }

        if ( !(o instanceof Period) )
        {
            return false;
        }

        final Period other = (Period) o;

        return startDate.equals( other.getStartDate() ) &&
            endDate.equals( other.getEndDate() ) &&
            periodType.equals( other.getPeriodType() );
    }

    @Override
    public String toString()
    {
        return "[" + (periodType == null ? "" : periodType.getName() + ": ") + startDate + " - " + endDate + "]";
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    @JsonProperty
    @JsonView( {DetailedView.class, ExportView.class} )
    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate( Date endDate )
    {
        this.endDate = endDate;
    }

    @JsonProperty
    @JsonSerialize( using = JacksonPeriodTypeSerializer.class )
    @JsonDeserialize( using = JacksonPeriodTypeDeserializer.class )
    @JsonView( {DetailedView.class, ExportView.class} )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0)
    public PeriodType getPeriodType()
    {
        return periodType;
    }

    public void setPeriodType( PeriodType periodType )
    {
        this.periodType = periodType;
    }

    @JsonProperty
    @JsonView( {DetailedView.class, ExportView.class} )
    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate( Date startDate )
    {
        this.startDate = startDate;
    }
}

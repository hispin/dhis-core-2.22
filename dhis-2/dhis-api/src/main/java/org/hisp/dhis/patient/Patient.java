/*
 * Copyright (c) 2004-2009, University of Oslo
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

package org.hisp.dhis.patient;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.program.Program;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class Patient
    implements Serializable
{
    public static final String MALE = "M";

    public static final String FEMALE = "F";

    private Integer id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String gender;

    private Date birthDate;

    private boolean birthDateEstimated = false;

    private Date deathDate;

    private Date registrationDate;

    private boolean isDead = false;

    private Set<PatientIdentifier> identifiers = new HashSet<PatientIdentifier>();

    private Set<Program> programs = new HashSet<Program>();

    private Set<PatientAttribute> attributes = new HashSet<PatientAttribute>();

    private Patient representative;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Patient()
    {
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public PatientIdentifier getPreferredPatientIdentifier()
    {
        if ( getIdentifiers() != null && getIdentifiers().size() > 0 )
        {
            for ( PatientIdentifier patientIdentifier : getIdentifiers() )
            {
                if ( patientIdentifier.getPreferred() )
                {
                    return patientIdentifier;
                }
            }
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // hashCode and equals
    // -------------------------------------------------------------------------

    @Override
    public int hashCode()
    {
        return id.hashCode();
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

        if ( !(o instanceof Patient) )
        {
            return false;
        }

        final Patient other = (Patient) o;

        return id.equals( other.getId() );
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Integer getId()
    {
        return id;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName( String firstName )
    {
        this.firstName = firstName;
    }

    public String getMiddleName()
    {
        return middleName;
    }

    public void setMiddleName( String middleName )
    {
        this.middleName = middleName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName( String lastName )
    {
        this.lastName = lastName;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender( String gender )
    {
        this.gender = gender;
    }

    public Date getBirthDate()
    {
        return birthDate;
    }

    public void setBirthDate( Date birthDate )
    {
        this.birthDate = birthDate;
    }

    public void setBirthDateEstimated( Boolean birthDateEstimated )
    {
        this.birthDateEstimated = birthDateEstimated;
    }

    public Boolean getBirthDateEstimated()
    {
        return birthDateEstimated;
    }

    public Date getDeathDate()
    {
        return deathDate;
    }

    public void setDeathDate( Date deathDate )
    {
        this.deathDate = deathDate;
    }

    public Boolean getIsDead()
    {
        return isDead;
    }

    public void setIsDead( Boolean isDead )
    {
        this.isDead = isDead;
    }

    public Set<PatientIdentifier> getIdentifiers()
    {
        return identifiers;
    }

    public void setIdentifiers( Set<PatientIdentifier> identifiers )
    {
        this.identifiers = identifiers;
    }

    public Set<Program> getPrograms()
    {
        return programs;
    }

    public void setPrograms( Set<Program> programs )
    {
        this.programs = programs;
    }

    public void setRegistrationDate( Date registrationDate )
    {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public Set<PatientAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes( Set<PatientAttribute> attributes )
    {
        this.attributes = attributes;
    }

    public void setRepresentative( Patient representative )
    {
        this.representative = representative;
    }

    public Patient getRepresentative()
    {
        return representative;
    }

    // -------------------------------------------------------------------------
    // Convenience method
    // -------------------------------------------------------------------------

    public String getAge()
    {
        if ( birthDate == null )
        {
            return "0";
        }

        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.setTime( birthDate );

        Calendar todayCalendar = Calendar.getInstance();

        int age = todayCalendar.get( Calendar.YEAR ) - birthCalendar.get( Calendar.YEAR );

        if ( todayCalendar.get( Calendar.MONTH ) < birthCalendar.get( Calendar.MONTH ) )
        {
            age--;
        }
        else if ( todayCalendar.get( Calendar.MONTH ) == birthCalendar.get( Calendar.MONTH )
            && todayCalendar.get( Calendar.DAY_OF_MONTH ) < birthCalendar.get( Calendar.DAY_OF_MONTH ) )
        {
            age--;
        }

        if ( age <= 1 )
        {
            return "( < 1 yr )";

        }

        else
        {
            return "( " + age + " yr )";
        }
    }

    public void setBirthDateFromAge( int age )
    {
        Calendar todayCalendar = Calendar.getInstance();

        // Assumed relative to the 1st of January
        todayCalendar.set( Calendar.DATE, 1 );
        todayCalendar.set( Calendar.MONTH, Calendar.JANUARY );
        todayCalendar.add( Calendar.YEAR, -1 * age );

        setBirthDate( todayCalendar.getTime() );
        setBirthDateEstimated( true );
    }

    public String getFullName()
    {
        return firstName + " " + middleName + " " + lastName;
    }

}

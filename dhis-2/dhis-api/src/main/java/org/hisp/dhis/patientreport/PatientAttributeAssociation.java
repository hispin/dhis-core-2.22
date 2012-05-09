/*
 * Copyright (c) 2004-2012, University of Oslo
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

package org.hisp.dhis.patientreport;

import org.hisp.dhis.patient.PatientAttribute;

/**
 * @author Chau Thu Tran
 * 
 * @version $PatientAttribueAssociation.java May 7, 2012 1:47:15 PM$
 */
public class PatientAttributeAssociation
{
    private Integer id;

    private PatientTabularReport patientTabularReport;

    private PatientAttribute patientAttribute;

    private String key;

    private boolean hidden;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public PatientAttributeAssociation()
    {
    }

    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public Integer getId()
    {
        return id;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }

    public PatientTabularReport getPatientTabularReport()
    {
        return patientTabularReport;
    }

    public void setPatientTabularReport( PatientTabularReport patientTabularReport )
    {
        this.patientTabularReport = patientTabularReport;
    }

    public PatientAttribute getPatientAttribute()
    {
        return patientAttribute;
    }

    public void setPatientAttribute( PatientAttribute patientAttribute )
    {
        this.patientAttribute = patientAttribute;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden( boolean hidden )
    {
        this.hidden = hidden;
    }

}

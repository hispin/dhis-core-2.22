package org.hisp.dhis.caseentry.action.patient;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeOption;
import org.hisp.dhis.patient.PatientAttributeOptionService;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipService;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class AddPatientAction
    implements Action
{
    public static final String PREFIX_ATTRIBUTE = "attr";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PatientService patientService;

    private PatientAttributeService patientAttributeService;

    private PatientAttributeOptionService patientAttributeOptionService;

    private RelationshipTypeService relationshipTypeService;

    private RelationshipService relationshipService;

    private OrganisationUnitSelectionManager selectionManager;

    private I18nFormat format;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer representativeId;

    private Integer relationshipTypeId;

    private Integer relationshipId;

    private boolean relationshipFromA;

    private String message;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        OrganisationUnit organisationUnit = selectionManager.getSelectedOrganisationUnit();

        Patient patient = new Patient();

        // ---------------------------------------------------------------------
        // Set location
        // ---------------------------------------------------------------------

        patient.setOrganisationUnit( organisationUnit );

        // ---------------------------------------------------------------------
        // Patient Attributes
        // ---------------------------------------------------------------------

        HttpServletRequest request = ServletActionContext.getRequest();

        Collection<PatientAttribute> attributes = patientAttributeService.getAllPatientAttributes();

        Set<PatientAttributeValue> patientAttributeValues = new HashSet<PatientAttributeValue>();

        PatientAttributeValue attributeValue = null;

        if ( attributes != null && attributes.size() > 0 )
        {
            for ( PatientAttribute attribute : attributes )
            {
                String value = request.getParameter( PREFIX_ATTRIBUTE + attribute.getId() );
                if ( StringUtils.isNotBlank( value ) )
                {
                    attributeValue = new PatientAttributeValue();
                    attributeValue.setPatient( patient );
                    attributeValue.setPatientAttribute( attribute );
                    attributeValue.setValue( value.trim() );

                    if ( attribute.getValueType().equals( PatientAttribute.TYPE_AGE ) )
                    {
                        value = format.formatDate( PatientAttribute.getDateFromAge( Integer.parseInt( value ) ) );
                    }
                    else if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( attribute.getValueType() ) )
                    {
                        PatientAttributeOption option = patientAttributeOptionService.get( Integer.parseInt( value ) );
                        if ( option != null )
                        {
                            attributeValue.setPatientAttributeOption( option );
                            attributeValue.setValue( option.getName() );
                        }
                    }
                    patientAttributeValues.add( attributeValue );
                }
            }
        }

        int patientId = patientService.createPatient( patient, representativeId, relationshipTypeId,
            patientAttributeValues );

        // -------------------------------------------------------------------------
        // Create relationship
        // -------------------------------------------------------------------------

        if ( relationshipId != null && relationshipTypeId != null )
        {
            Patient relationship = patientService.getPatient( relationshipId );
            if ( relationship != null )
            {
                Relationship rel = new Relationship();
                if ( relationshipFromA )
                {
                    rel.setPatientA( relationship );
                    rel.setPatientB( patient );
                }
                else
                {
                    rel.setPatientA( patient );
                    rel.setPatientB( relationship );
                }
                if ( relationshipTypeId != null )
                {
                    RelationshipType relType = relationshipTypeService.getRelationshipType( relationshipTypeId );
                    if ( relType != null )
                    {
                        rel.setRelationshipType( relType );
                        relationshipService.saveRelationship( rel );
                    }
                }
            }
        }

        message = patient.getUid() + "_" + patientId;

        return SUCCESS;
    }

    // -----------------------------------------------------------------------------
    // Getter/Setter
    // -----------------------------------------------------------------------------

    public void setRelationshipTypeService( RelationshipTypeService relationshipTypeService )
    {
        this.relationshipTypeService = relationshipTypeService;
    }

    public void setRelationshipId( Integer relationshipId )
    {
        this.relationshipId = relationshipId;
    }

    public void setRelationshipFromA( boolean relationshipFromA )
    {
        this.relationshipFromA = relationshipFromA;
    }

    public void setRelationshipService( RelationshipService relationshipService )
    {
        this.relationshipService = relationshipService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public String getMessage()
    {
        return message;
    }

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    public void setPatientAttributeOptionService( PatientAttributeOptionService patientAttributeOptionService )
    {
        this.patientAttributeOptionService = patientAttributeOptionService;
    }

    public void setRepresentativeId( Integer representativeId )
    {
        this.representativeId = representativeId;
    }

    public void setRelationshipTypeId( Integer relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

}

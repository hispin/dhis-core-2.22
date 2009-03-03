package org.hisp.dhis.web.reports.action;

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

import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;

import com.opensymphony.xwork.Action;

/**
 * @author Kristian Nordal
 * @version $Id: SelectOrganisationUnitAction.java 2878 2007-02-21 08:36:56Z andegje $
 */
public class SelectOrganisationUnitAction
    implements Action
{
    private String source;
    
    private String orgUnitName;
    
    private String levelNumber;

    // ----------------------------------------------------------------------
    // Dependencies
    // ----------------------------------------------------------------------

    private OrganisationUnitSelectionManager selectionManager;

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }


    // ----------------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------------

    public String getSource()
    {
        return source;
    }

    
    public String getOrgUnitName() {
		return orgUnitName;
	}
		
    
    public String getLevelNumber() {
		return levelNumber;
	}

    
    // ----------------------------------------------------------------------
    // Action
    // ----------------------------------------------------------------------



	public String execute()
        throws Exception
    {
        OrganisationUnit selectedOrganisationUnit = selectionManager.getSelectedOrganisationUnit();
       
               
        if ( selectedOrganisationUnit == null )
        {
            System.out.println( "INPUT" );

            return INPUT;
        }
        else
        {
            System.out.println( "SUCCESS" );

            source = selectedOrganisationUnit.getId() + "";

            orgUnitName = selectedOrganisationUnit.getName();
            
            OrganisationUnit parentOrgUnit = selectedOrganisationUnit.getParent();
            if(parentOrgUnit == null)
            {
            	levelNumber = "1";
            }
            else
            {
            	parentOrgUnit = parentOrgUnit.getParent();
            	if(parentOrgUnit != null)
            	{
            		parentOrgUnit = parentOrgUnit.getParent();
            		if(parentOrgUnit != null)
            		{
            			parentOrgUnit = parentOrgUnit.getParent();
            			if(parentOrgUnit != null)
                		{
            				parentOrgUnit = parentOrgUnit.getParent();
                			if(parentOrgUnit != null)                  		
                				{ 
                					parentOrgUnit = parentOrgUnit.getParent();
                					if(parentOrgUnit != null)	
                						{
                							parentOrgUnit = parentOrgUnit.getParent();
                							if(parentOrgUnit != null)	
                							{
                								levelNumber = "8";
                							}
                							else 
                								{levelNumber = "7";} 
                						}
                					else 
                						{levelNumber = "6";}
                				}                			
                			else 
                				{ levelNumber = "5"; }	
                		}
            			else { levelNumber = "4"; }            			
            		}
            		else { levelNumber = "3";}
            	}
            	else { levelNumber = "2"; }
            }
            
            return SUCCESS;
        }
    }
}

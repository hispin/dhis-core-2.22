package org.hisp.dhis.reportexcel.importing.action;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.reportexcel.ReportLocationManager;

/**
 * @author Tran Thanh Tri
 * @version $Id
 */

public class UploadExcelFileAction
    extends org.hisp.dhis.reportexcel.action.ActionSupport
{
    // -------------------------------------------
    // Dependency
    // -------------------------------------------

    private ReportLocationManager reportLocationManager;

    public void setReportLocationManager( ReportLocationManager reportLocationManager )
    {
        this.reportLocationManager = reportLocationManager;
    }

//    private OrganisationUnitSelectionManager organisationUnitSelectionManager;
//
//    public void setOrganisationUnitSelectionManager( OrganisationUnitSelectionManager organisationUnitSelectionManager )
//    {
//        this.organisationUnitSelectionManager = organisationUnitSelectionManager;
//    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private String fileName;

    public void setUploadFileName( String fileName )
    {
        this.fileName = fileName;
    }

    private File upload;

    public void setUpload( File upload )
    {
        this.upload = upload;
    }

    private File fileExcel;

    public File getFileExcel()
    {
        return fileExcel;
    }

    public void setFileExcel( File fileExcel )
    {
        this.fileExcel = fileExcel;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
//        OrganisationUnit organisationUnit = organisationUnitSelectionManager.getSelectedOrganisationUnit();

//        if ( organisationUnit == null )
//            return SUCCESS;

        File directory = reportLocationManager.getReportExcelTempDirectory(  );
        
        if ( upload != null )
        {

            try
            {
                FileInputStream fin = new FileInputStream( upload );
                
                byte[] data = new byte[8192];
                int byteReads = fin.read( data );
                
                fileExcel = new File( directory, fileName );

                FileOutputStream fout = new FileOutputStream( fileExcel );
                
                while ( byteReads != -1 )
                {
                    fout.write( data, 0, byteReads );
                    fout.flush();
                    byteReads = fin.read( data );
                }
                fin.close();
                
                fout.close();

                return SUCCESS;
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                return ERROR;
            }

        }

        return SUCCESS;
    }
}

/*
 * Copyright (c) 2004-2010, University of Oslo
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
package org.hisp.dhis.reportexcel.exportitem.action;

import java.util.ArrayList;
import java.util.List;

import org.amplecode.quick.StatementManager;
import org.hisp.dhis.reportexcel.ExportReportService;
import org.hisp.dhis.reportexcel.ReportExcel;
import org.hisp.dhis.reportexcel.ReportExcelItem;

import com.opensymphony.xwork2.Action;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */

public class CopyExportItemToExportReportAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    private ExportReportService exportReportService;

    private StatementManager statementManager;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Integer exportReportId;

    private Integer sheetNo;

    private List<String> exportItems = new ArrayList<String>();

    // -------------------------------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------------------------------

    public void setExportReportService( ExportReportService exportReportService )
    {
        this.exportReportService = exportReportService;
    }

    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }

    public Integer getExportReportId()
    {
        return exportReportId;
    }

    public Integer getSheetNo()
    {
        return sheetNo;
    }

    public void setExportReportId( Integer exportReportId )
    {
        this.exportReportId = exportReportId;
    }

    public void setExportItems( List<String> exportItems )
    {
        this.exportItems = exportItems;
    }

    public void setSheetNo( Integer sheetNo )
    {
        this.sheetNo = sheetNo;
    }

    public String execute()
        throws Exception
    {
        statementManager.initialise();

        ReportExcel exportReport = exportReportService.getExportReport( exportReportId );
        
        for ( String itemId : this.exportItems )
        {
            ReportExcelItem reportItem = exportReportService.getExportItem( Integer.parseInt( itemId ) );

            ReportExcelItem newExportItem = new ReportExcelItem();
            
            newExportItem.setName( reportItem.getName() );
            newExportItem.setItemType( reportItem.getItemType() );
            newExportItem.setPeriodType( reportItem.getPeriodType() );
            newExportItem.setExpression( reportItem.getExpression() );
            newExportItem.setRow( reportItem.getRow() );
            newExportItem.setColumn( reportItem.getColumn() );
            newExportItem.setSheetNo( sheetNo );
            newExportItem.setReportExcel( exportReport );
            
            exportReportService.addExportItem( newExportItem );
        }

        statementManager.destroy();

        return SUCCESS;
    }
}

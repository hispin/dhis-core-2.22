package org.hisp.dhis.importexport.xls.converter;

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

import java.util.Collection;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.XLSConverter;
import org.hisp.dhis.system.util.ExcelUtils;

/**
 * @author Dang Duy Hieu
 * @version $$
 */
public class ExtendedDataElementConverter
    extends ExcelUtils
    implements XLSConverter
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementService dataElementService;

    public ExtendedDataElementConverter( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    // -------------------------------------------------------------------------
    // PDFConverter implementation
    // -------------------------------------------------------------------------

    public void write( WritableWorkbook workbook, ExportParams params, int sheetIndex )
    {
        I18n i18n = params.getI18n();

        int rowNumber = 0;
        int columnIndex = 0;

        WritableSheet sheet = workbook.createSheet( i18n.getString( "data_elements" ), sheetIndex );
        
        WritableCellFormat FORMAT_LABEL_MERGED = new WritableCellFormat( new WritableFont( WritableFont.ARIAL, 14,
            WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE ) );

        WritableCellFormat FORMAT_LABEL = new WritableCellFormat( new WritableFont( WritableFont.ARIAL, 13,
            WritableFont.NO_BOLD, true, UnderlineStyle.NO_UNDERLINE, Colour.WHITE ) );

        WritableCellFormat FORMAT_TEXT = new WritableCellFormat( new WritableFont( WritableFont.ARIAL, 11,
            WritableFont.NO_BOLD, false ) );

        Collection<DataElement> elements = dataElementService.getDataElements( params.getDataElements() );

        try
        {            
            setUpFormat( FORMAT_LABEL_MERGED, Alignment.JUSTIFY, Border.ALL, BorderLineStyle.THIN, Colour.BROWN );
            setUpFormat( FORMAT_LABEL, Alignment.CENTRE, Border.ALL, BorderLineStyle.THIN, Colour.TAN );
            setUpFormat( FORMAT_TEXT, Alignment.GENERAL, Border.ALL, BorderLineStyle.DOTTED, Colour.BLACK );
            
            printExtendedDataElementHeaders( sheet, FORMAT_LABEL_MERGED, FORMAT_LABEL, i18n, rowNumber++, columnIndex );

            rowNumber++;

            for ( DataElement element : elements )
            {
                addExtendedDataElementCellToSheet( sheet, FORMAT_TEXT, element, i18n, rowNumber++, columnIndex );
            }
        }
        catch ( RowsExceededException e )
        {
            e.printStackTrace();
        }
        catch ( WriteException e )
        {
            e.printStackTrace();
        }
    }
}

package org.hisp.dhis.dxf2.gml;

import org.hisp.dhis.dxf2.metadata.MetaData;

import java.io.InputStream;

/**
 * Wraps the result of {@link GmlImportService#preProcessGml(InputStream)}.
 * This is necessary when performing GML import on a context where exceptions
 * due to malformed input cannot be caught, thus leaving the user uninformed of
 * the error. This class will wrap the failure and provide the Throwable to the
 * consuming class on error or the resulting MetaData object on success.
 *
 * @author Halvdan Hoem grelland <halvdanhg@gmail.com>
 */
public final class GmlPreProcessingResult
{
    private boolean isSuccess;
    private MetaData resultMetaData;
    private Throwable throwable;

    private GmlPreProcessingResult(){}

    public static GmlPreProcessingResult success( MetaData resultMetaData ) {
        GmlPreProcessingResult result = new GmlPreProcessingResult();
        result.setResultMetaData( resultMetaData );
        result.setSuccess( true );

        return result;
    }

    public static GmlPreProcessingResult failure( Throwable throwable )
    {
        GmlPreProcessingResult result = new GmlPreProcessingResult();
        result.setThrowable( throwable );
        result.setSuccess( false );

        return result;
    }

    private void setSuccess( boolean isSuccess )
    {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess()
    {
        return isSuccess;
    }

    private void setResultMetaData( MetaData resultMetaData )
    {
        this.resultMetaData = resultMetaData;
    }

    public MetaData getResultMetaData()
    {
        return resultMetaData;
    }

    private void setThrowable( Throwable throwable )
    {
        this.throwable = throwable;
    }

    public Throwable getThrowable()
    {
        return throwable;
    }
}

package org.hisp.dhis.webapi.utils;

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

import org.hisp.dhis.dxf2.webmessage.WebMessage;
import org.hisp.dhis.dxf2.webmessage.WebMessageResponse;
import org.hisp.dhis.dxf2.webmessage.WebMessageStatus;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public final class WebMessageUtils
{
    public static WebMessage createWebMessage( String message, WebMessageStatus status, int httpStatus )
    {
        WebMessage webMessage = new WebMessage( status, httpStatus );
        webMessage.setMessage( message );

        return webMessage;
    }

    public static WebMessage createWebMessage( String message, String devMessage, WebMessageStatus status, int httpStatus )
    {
        WebMessage webMessage = new WebMessage( status, httpStatus );
        webMessage.setMessage( message );
        webMessage.setDevMessage( devMessage );

        return webMessage;
    }

    public static WebMessage createWebMessage( String message, String devMessage, WebMessageStatus status, int httpStatus, WebMessageResponse webMessageResponse )
    {
        WebMessage webMessage = new WebMessage( status, httpStatus );
        webMessage.setMessage( message );
        webMessage.setDevMessage( devMessage );
        webMessage.setResponse( webMessageResponse );

        return webMessage;
    }

    public static WebMessage ok( String message )
    {
        return createWebMessage( message, WebMessageStatus.OK, HttpServletResponse.SC_OK );
    }

    public static WebMessage ok( String message, String devMessage )
    {
        return createWebMessage( message, devMessage, WebMessageStatus.OK, HttpServletResponse.SC_OK );
    }

    public static WebMessage created( String message )
    {
        return createWebMessage( message, WebMessageStatus.OK, HttpServletResponse.SC_CREATED );
    }

    public static WebMessage created( String message, String devMessage )
    {
        return createWebMessage( message, devMessage, WebMessageStatus.OK, HttpServletResponse.SC_CREATED );
    }

    public static WebMessage notFound( String message )
    {
        return createWebMessage( message, WebMessageStatus.ERROR, HttpServletResponse.SC_NOT_FOUND );
    }

    public static WebMessage notFound( Class<?> klass, String id )
    {
        String message = klass.getSimpleName() + " with id " + id + " could not be found.";
        return createWebMessage( message, WebMessageStatus.ERROR, HttpServletResponse.SC_NOT_FOUND );
    }

    public static WebMessage notFound( String message, String devMessage )
    {
        return createWebMessage( message, devMessage, WebMessageStatus.ERROR, HttpServletResponse.SC_NOT_FOUND );
    }

    public static WebMessage conflict( String message )
    {
        return createWebMessage( message, WebMessageStatus.ERROR, HttpServletResponse.SC_CONFLICT );
    }

    public static WebMessage conflict( String message, String devMessage )
    {
        return createWebMessage( message, devMessage, WebMessageStatus.ERROR, HttpServletResponse.SC_CONFLICT );
    }

    public static WebMessage error( String message )
    {
        return createWebMessage( message, WebMessageStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
    }

    public static WebMessage error( String message, String devMessage )
    {
        return createWebMessage( message, devMessage, WebMessageStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
    }

    public static WebMessage badRequest( String message )
    {
        return createWebMessage( message, WebMessageStatus.ERROR, HttpServletResponse.SC_BAD_REQUEST );
    }

    public static WebMessage badRequest( String message, String devMessage )
    {
        return createWebMessage( message, devMessage, WebMessageStatus.ERROR, HttpServletResponse.SC_BAD_REQUEST );
    }

    public static WebMessage forbidden( String message )
    {
        return createWebMessage( message, WebMessageStatus.ERROR, HttpServletResponse.SC_FORBIDDEN );
    }

    public static WebMessage forbidden( String message, String devMessage )
    {
        return createWebMessage( message, WebMessageStatus.ERROR, HttpServletResponse.SC_FORBIDDEN );
    }

    private WebMessageUtils()
    {
    }
}

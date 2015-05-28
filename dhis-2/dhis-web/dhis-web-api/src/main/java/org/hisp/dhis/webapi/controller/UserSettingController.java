package org.hisp.dhis.webapi.controller;

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

import org.hisp.dhis.system.util.LocaleUtils;
import org.hisp.dhis.user.UserSettingService;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;

/**
 * @author Lars Helge Overland
 */
@Controller
@RequestMapping( "/userSettings" )
public class UserSettingController
{
    @Autowired
    private UserSettingService userSettingService;

    @RequestMapping( value = "/{key}", method = RequestMethod.POST )
    public void setUserSetting(
        @PathVariable String key,
        @RequestParam( value = "user", required = false ) String username,
        @RequestParam( value = "value", required = false ) String value,
        @RequestBody( required = false ) String valuePayload, HttpServletResponse response )
    {
        if ( key == null )
        {
            ContextUtils.conflictResponse( response, "Key must be specified" );
            return;
        }

        if ( value == null && valuePayload == null )
        {
            ContextUtils.conflictResponse( response, "Value must be specified as query param or as payload" );
            return;
        }

        value = value != null ? value : valuePayload;

        if ( username == null )
        {
            userSettingService.saveUserSetting( key, valueToSet( key, value ) );

        }
        else
        {
            userSettingService.saveUserSetting( key, valueToSet( key, value ), username );
        }

        ContextUtils.okResponse( response, "User setting saved" );
    }

    @RequestMapping( value = "/{key}", method = RequestMethod.GET )
    public void getSystemSetting( @PathVariable( "key" ) String key,
        @RequestParam( value = "user", required = false ) String username, HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        String value;

        if ( username == null )
        {
            value = getStringValue( key, userSettingService.getUserSetting( key ) );
        }
        else
        {
            value = getStringValue( key, userSettingService.getUserSetting( key, username ) );
        }

        if ( value == null )
        {
            ContextUtils.notFoundResponse( response, "User setting not found." );
            return;
        }

        String contentType;

        if ( request.getHeader( "Accept" ) == null || "*/*".equals( request.getHeader( "Accept" ) ) )
        {
            contentType = MediaType.TEXT_PLAIN_VALUE;
        }
        else
        {
            contentType = request.getHeader( "Accept" );
        }

        response.setContentType( contentType );
        response.getWriter().println( value );
    }

    @RequestMapping( value = "/{key}", method = RequestMethod.DELETE )
    public void removeSystemSetting( @PathVariable( "key" ) String key )
    {
        userSettingService.deleteUserSetting( key );
    }

    private Serializable valueToSet( String key, String value )
    {
        if ( key.equals( UserSettingService.KEY_UI_LOCALE ) || key.equals( UserSettingService.KEY_DB_LOCALE ) )
        {
            return LocaleUtils.getLocale( value );
        }
        else
        {
            return value;
        }
    }

    private String getStringValue( String key, Serializable value )
    {
        if ( key.equals( UserSettingService.KEY_UI_LOCALE ) || key.equals( UserSettingService.KEY_DB_LOCALE ) )
            return ((Locale) value).getLanguage();
        else
            return (String) value;
    }
}

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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hisp.dhis.dxf2.render.RenderService;
import org.hisp.dhis.dxf2.webmessage.WebMessage;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.keyjsonvalue.KeyJsonValue;
import org.hisp.dhis.keyjsonvalue.KeyJsonValueService;
import org.hisp.dhis.webapi.utils.WebMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Stian Sandvold
 */
@Controller
@RequestMapping( "/dataStore" )
public class KeyJsonValueController
{
    @Autowired
    private KeyJsonValueService keyJsonValueService;

    @Autowired
    private RenderService renderService;

    @RequestMapping( value = "", method = RequestMethod.GET, produces = "application/json" )
    public
    @ResponseBody
    List<String> getNamespaces( HttpServletResponse response )
        throws IOException
    {
        return keyJsonValueService.getNamespaces();
    }

    @RequestMapping( value = "/{namespace}", method = RequestMethod.GET, produces = "application/json" )
    public
    @ResponseBody
    List<String> getKeysInNamespace(
        @PathVariable String namespace,
        HttpServletResponse response )
        throws IOException, WebMessageException
    {
        if ( !keyJsonValueService.getNamespaces().contains( namespace ) )
        {
            throw new WebMessageException(
                WebMessageUtils.notFound( "The namespace '" + namespace + "' was not found." ) );
        }

        return keyJsonValueService.getKeysInNamespace( namespace );
    }

    @RequestMapping( value = "/{namespace}", method = RequestMethod.DELETE )
    public
    @ResponseBody
    WebMessage deleteNamespace(
        @PathVariable String namespace,
        HttpServletResponse response )
        throws WebMessageException
    {

        if ( !keyJsonValueService.getNamespaces().contains( namespace ) )
        {
            throw new WebMessageException(
                WebMessageUtils.notFound( "The namespace '" + namespace + "' was not found." ) );
        }

        keyJsonValueService.deleteNamespace( namespace );

        return WebMessageUtils.ok( "Namespace '" + namespace + "' deleted." );
    }

    @RequestMapping( value = "/{namespace}/{key}", method = RequestMethod.GET, produces = "application/json" )
    public
    @ResponseBody
    KeyJsonValue getKeyJsonValue(
        @PathVariable String namespace,
        @PathVariable String key,
        HttpServletResponse response )
        throws IOException, WebMessageException
    {
        KeyJsonValue keyJsonValue = keyJsonValueService.getKeyJsonValue( namespace, key );

        if ( keyJsonValue == null )
        {
            throw new WebMessageException( WebMessageUtils
                .notFound( "The key '" + key + "' was not found in the namespace '" + namespace + "'." ) );
        }

        return keyJsonValue;
    }

    @RequestMapping( value = "/{namespace}/{key}", method = RequestMethod.POST, produces = "application/json", consumes = "application/json" )
    public
    @ResponseBody
    KeyJsonValue addKey(
        @PathVariable String namespace,
        @PathVariable String key,
        @RequestBody String body,
        HttpServletResponse response )
        throws IOException, WebMessageException
    {
        if ( keyJsonValueService.getKeyJsonValue( namespace, key ) != null )
        {
            throw new WebMessageException( WebMessageUtils
                .conflict( "The key '" + key + "' already exists on the namespace '" + namespace + "'." ) );
        }

        if ( !renderService.isValidJson( body ) )
        {
            throw new WebMessageException( WebMessageUtils.badRequest( "The data is not valid JSON." ) );
        }

        KeyJsonValue keyJsonValue = new KeyJsonValue();

        keyJsonValue.setKey( key );
        keyJsonValue.setNamespace( namespace );
        keyJsonValue.setValue( body );

        keyJsonValueService.addKeyJsonValue( keyJsonValue );

        response.setStatus( HttpServletResponse.SC_CREATED );
        return keyJsonValue;
    }

    @RequestMapping( value = "/{namespace}/{key}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json" )
    public
    @ResponseBody
    KeyJsonValue updateKeyJsonValue(
        @PathVariable String namespace,
        @PathVariable String key,
        @RequestBody String body,
        HttpServletRequest request,
        HttpServletResponse response )
        throws WebMessageException, IOException
    {
        KeyJsonValue keyJsonValue = keyJsonValueService.getKeyJsonValue( namespace, key );

        if ( keyJsonValue == null )
        {
            throw new WebMessageException( WebMessageUtils
                .notFound( "The key '" + key + "' was not found in the namespace '" + namespace + "'." ) );
        }

        if ( !renderService.isValidJson( body ) )
        {
            throw new WebMessageException( WebMessageUtils.badRequest( "The data is not valid JSON." ) );
        }

        keyJsonValue.setValue( body );

        keyJsonValueService.updateKeyJsonValue( keyJsonValue );

        return keyJsonValue;
    }

    @RequestMapping( value = "/{namespace}/{key}", method = RequestMethod.DELETE, produces = "application/json" )
    public
    @ResponseBody
    WebMessage deleteKeyJsonValue(
        @PathVariable String namespace,
        @PathVariable String key,
        HttpServletResponse response )
        throws WebMessageException
    {
        KeyJsonValue keyJsonValue = keyJsonValueService.getKeyJsonValue( namespace, key );

        if ( keyJsonValue == null )
        {
            throw new WebMessageException( WebMessageUtils
                .notFound( "The key '" + key + "' was not found in the namespace '" + namespace + "'." ) );
        }

        keyJsonValueService.deleteKeyJsonValue( keyJsonValue );

        return WebMessageUtils.ok( "Key '" + key + "' deleted from namespace '" + namespace + "'." );
    }
}

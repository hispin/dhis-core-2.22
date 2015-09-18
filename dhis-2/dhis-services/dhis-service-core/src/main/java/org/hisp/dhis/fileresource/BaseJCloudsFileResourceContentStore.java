package org.hisp.dhis.fileresource;

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
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.input.NullInputStream;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;

import com.google.common.hash.HashCode;
import com.google.common.io.ByteSource;

/**
 * @author Halvdan Hoem Grelland
 */
public abstract class BaseJCloudsFileResourceContentStore
    implements FileResourceContentStore
{
    private BlobStore blobStore;
    private BlobStoreContext blobStoreContext;

    // -------------------------------------------------------------------------
    // Default config implementations
    // -------------------------------------------------------------------------

    protected Credentials getCredentials()
    {
        return new Credentials( "Unused", "Unused" );
    }

    protected Properties getOverrides()
    {
        return new Properties();
    }

    // -------------------------------------------------------------------------
    // Abstract methods
    // -------------------------------------------------------------------------

    protected abstract String getContainer();

    protected abstract String getJCloudsProviderKey();

    protected abstract String getLocation();

    // -------------------------------------------------------------------------
    // Life cycle management
    // -------------------------------------------------------------------------

    public void init()
    {
        Credentials credentials = getCredentials();
        Properties overrides = getOverrides();
        String providerKey = getJCloudsProviderKey();
        
        blobStoreContext = ContextBuilder.newBuilder( providerKey )
            .credentials( credentials.identity, credentials.credential )
            .overrides( overrides ).build( BlobStoreContext.class );

        blobStore = blobStoreContext.getBlobStore();

        Optional<? extends Location> location = blobStore.listAssignableLocations()
            .stream().filter( l -> l.getId().equals( getLocation() ) ).findFirst();

        blobStore.createContainerInLocation( location.isPresent() ? location.get() : null, getContainer() );
    }

    public void cleanUp()
    {
        blobStoreContext.close();
    }

    // -------------------------------------------------------------------------
    // FileResourceContentStore implementation
    // -------------------------------------------------------------------------

    public ByteSource getFileResourceContent( String key )
    {
        final Blob blob = getBlob( key );

        if ( blob == null )
        {
            return null;
        }

        return new ByteSource()
        {
            @Override
            public InputStream openStream()
            {
                try
                {
                    return blob.getPayload().openStream();
                }
                catch ( IOException e )
                {
                    return new NullInputStream( 0 );
                }
            }
        };
    }

    public String saveFileResourceContent( String key, ByteSource content, long size, String contentMd5 )
    {
        Blob blob = createBlob( key, content, size, contentMd5 );

        if ( blob == null )
        {
            return null;
        }

        putBlob( blob );

        return key;
    }

    public void deleteFileResourceContent( String key )
    {
        deleteBlob( key );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private Blob getBlob( String key )
    {
        return blobStore.getBlob( getContainer(), key );
    }

    private void deleteBlob( String key )
    {
        blobStore.removeBlob( getContainer(), key );
    }

    private String putBlob( Blob blob )
    {
        return blobStore.putBlob( getContainer(), blob );
    }

    private Blob createBlob( String key, ByteSource content, long size, String contentMd5 )
    {
        return blobStore.blobBuilder( key )
            .payload( content )
            .contentLength( size )
            .contentMD5( HashCode.fromString( contentMd5 ) )
            .build();
    }
}

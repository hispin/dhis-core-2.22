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

import com.google.common.hash.HashCode;
import com.google.common.io.ByteSource;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.external.location.LocationManager;
import org.hisp.dhis.hibernate.HibernateConfigurationProvider;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.filesystem.reference.FilesystemConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Halvdan Hoem Grelland
 */
public class DefaultFileResourceContentStore
    implements FileResourceContentStore
{
    private static final Log log = LogFactory.getLog( DefaultFileResourceContentStore.class );

    private BlobStore blobStore;
    private BlobStoreContext blobStoreContext;
    private String container;

    // -------------------------------------------------------------------------
    // Providers
    // -------------------------------------------------------------------------

    private static final String JCLOUDS_PROVIDER_KEY_FILESYSTEM = "filesystem";
    private static final String JCLOUDS_PROVIDER_KEY_AWS_S3 = "aws-s3";
    private static final String JCLOUDS_PROVIDER_KEY_TRANSIENT = "transient";

    private static final List<String> SUPPORTED_PROVIDERS = new ArrayList<String>() {{
        addAll( Arrays.asList(
            JCLOUDS_PROVIDER_KEY_FILESYSTEM,
            JCLOUDS_PROVIDER_KEY_AWS_S3
        ) );
    }};

    // -------------------------------------------------------------------------
    // Property keys
    // -------------------------------------------------------------------------

    private static final String FILE_STORE_CONFIG_NAMESPACE = "filestore";

    private static final String KEY_FILE_STORE_PROVIDER  = FILE_STORE_CONFIG_NAMESPACE + ".provider";
    private static final String KEY_FILE_STORE_CONTAINER = FILE_STORE_CONFIG_NAMESPACE + ".container";
    private static final String KEY_FILE_STORE_LOCATION  = FILE_STORE_CONFIG_NAMESPACE + ".location";
    private static final String KEY_FILE_STORE_IDENTITY  = FILE_STORE_CONFIG_NAMESPACE + ".identity";
    private static final String KEY_FILE_STORE_SECRET    = FILE_STORE_CONFIG_NAMESPACE + ".secret";

    // -------------------------------------------------------------------------
    // Defaults
    // -------------------------------------------------------------------------

    private static final String DEFAULT_CONTAINER = "dhis2-file-store";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private LocationManager locationManager;

    public void setLocationManager( LocationManager locationManager )
    {
        this.locationManager = locationManager;
    }

    private HibernateConfigurationProvider configurationProvider;

    public void setConfigurationProvider( HibernateConfigurationProvider configurationProvider )
    {
        this.configurationProvider = configurationProvider;
    }

    // -------------------------------------------------------------------------
    // Life cycle management
    // -------------------------------------------------------------------------

    // TODO Untangle and split up
    public void init()
    {
        // -------------------------------------------------------------------------
        // Parse properties
        // -------------------------------------------------------------------------

        Properties properties = configurationProvider.getConfiguration().getProperties();

        Map<String, String> fileStoreConfiguration = properties
            .entrySet().stream().filter( p -> ((String) p.getKey()).startsWith( FILE_STORE_CONFIG_NAMESPACE ) )
            .collect( Collectors.toMap(
                p -> StringUtils.strip( (String) p.getKey() ),
                p -> StringUtils.strip( (String) p.getValue() )
            ) );

        String provider = fileStoreConfiguration.getOrDefault( KEY_FILE_STORE_PROVIDER, JCLOUDS_PROVIDER_KEY_FILESYSTEM );

        if ( !SUPPORTED_PROVIDERS.contains( provider ) )
        {
            log.warn( "Ignored unsupported file store provider '" + provider + "', falling back to file system." );
            provider = JCLOUDS_PROVIDER_KEY_FILESYSTEM;
        }

        if ( provider.equals( JCLOUDS_PROVIDER_KEY_FILESYSTEM ) && !locationManager.externalDirectorySet() )
        {
            log.warn( "File system store provider configured but external directory is not set. Falling back to transient file store." );
            provider = JCLOUDS_PROVIDER_KEY_TRANSIENT;
        }

        container = fileStoreConfiguration.getOrDefault( KEY_FILE_STORE_CONTAINER, DEFAULT_CONTAINER );

        String location = fileStoreConfiguration.getOrDefault( KEY_FILE_STORE_LOCATION, null );
        Properties overrides = new Properties();
        Credentials credentials = new Credentials( "Unused", "Unused" );

        // -------------------------------------------------------------------------
        // Provider specific configuration
        // -------------------------------------------------------------------------

        if ( provider.equals( JCLOUDS_PROVIDER_KEY_FILESYSTEM ) && locationManager.externalDirectorySet() )
        {
            overrides.setProperty( FilesystemConstants.PROPERTY_BASEDIR, locationManager.getExternalDirectoryPath() );

            log.info( "File system store provider configured" );
        }
        else if ( provider.equals( JCLOUDS_PROVIDER_KEY_AWS_S3 ) )
        {
            credentials = new Credentials( fileStoreConfiguration.getOrDefault(
                KEY_FILE_STORE_IDENTITY, "" ), fileStoreConfiguration.getOrDefault( KEY_FILE_STORE_SECRET, "" ) );

            log.info( "AWS S3 filestore provider configured." );

            if ( credentials.identity.isEmpty() || credentials.credential.isEmpty() )
            {
                log.info( "AWS S3 store configured with empty credentials, authentication not possible" );
            }
        }

        // -------------------------------------------------------------------------
        // Set up BlobStore
        // -------------------------------------------------------------------------

        blobStoreContext = ContextBuilder.newBuilder( provider )
            .credentials( credentials.identity, credentials.credential )
            .overrides( overrides ).build( BlobStoreContext.class );

        blobStore = blobStoreContext.getBlobStore();

        Optional<? extends Location> configuredLocation = blobStore.listAssignableLocations()
            .stream().filter( l -> l.getId().equals( location ) ).findFirst();

        blobStore.createContainerInLocation( configuredLocation.isPresent() ? configuredLocation.get() : null, container );
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
        return blobStore.getBlob( container, key );
    }

    private void deleteBlob( String key )
    {
        blobStore.removeBlob( container, key );
    }

    private String putBlob( Blob blob )
    {
        return blobStore.putBlob( container, blob );
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

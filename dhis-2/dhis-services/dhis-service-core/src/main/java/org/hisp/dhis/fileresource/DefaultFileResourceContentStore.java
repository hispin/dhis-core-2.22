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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.external.location.LocationManager;
import org.hisp.dhis.hibernate.HibernateConfigurationProvider;
import org.jclouds.domain.Credentials;
import org.jclouds.filesystem.reference.FilesystemConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * TODO Merge with BaseJCloudsFileResourceContentStore ?
 * @author Halvdan Hoem Grelland
 */
public class DefaultFileResourceContentStore
    extends BaseJCloudsFileResourceContentStore
{
    private static final Log log = LogFactory.getLog( DefaultFileResourceContentStore.class );

    // -------------------------------------------------------------------------
    // Provider constants
    // -------------------------------------------------------------------------

    private static final String JCLOUDS_PROVIDER_KEY_FILESYSTEM = "filesystem";
    private static final String JCLOUDS_PROVIDER_KEY_AWS_S3 = "aws-s3";

    private static final List<String> AVAILABLE_PROVIDERS = new ArrayList<String>() {{
        addAll( Arrays.asList( JCLOUDS_PROVIDER_KEY_FILESYSTEM, JCLOUDS_PROVIDER_KEY_AWS_S3 ) );
    }};

    // -------------------------------------------------------------------------
    // Property keys
    // -------------------------------------------------------------------------

    private static final String FILESTORE_CONFIG_NAMESPACE = "filestore";

    private static final String KEY_FILESTORE_PROVIDER = FILESTORE_CONFIG_NAMESPACE + ".provider";
    private static final String KEY_FILESTORE_CONTAINER = FILESTORE_CONFIG_NAMESPACE + ".container";
    private static final String KEY_FILESTORE_LOCATION = FILESTORE_CONFIG_NAMESPACE + ".location";
    private static final String KEY_FILESTORE_IDENTITY = FILESTORE_CONFIG_NAMESPACE + ".identity";
    private static final String KEY_FILESTORE_SECRET = FILESTORE_CONFIG_NAMESPACE + ".secret";

    // -------------------------------------------------------------------------
    // Defaults
    // -------------------------------------------------------------------------

    private static final String DEFAULT_PROVIDER = "filesystem";
    private static final String DEFAULT_CONTAINER = "dhis2_filestore";

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    private Map<String, String> filestoreConfiguration;

    private String provider;
    private String container;
    private Credentials credentials;
    private String location;
    private Properties overrides = new Properties();

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
    // Lifecycle
    // -------------------------------------------------------------------------

    public void init()
    {
        filestoreConfiguration = configurationProvider.getConfiguration().getProperties()
            .entrySet().stream().filter(
                p -> ( (String) p.getKey() ).startsWith( FILESTORE_CONFIG_NAMESPACE ) )
            .collect( Collectors.toMap(
                p -> StringUtils.strip( (String) p.getKey() ),
                p -> StringUtils.strip( (String) p.getValue() )
            ) );

        provider = filestoreConfiguration.getOrDefault( KEY_FILESTORE_PROVIDER, DEFAULT_PROVIDER );

        if ( !AVAILABLE_PROVIDERS.contains( provider ) )
        {
            log.info( "Ignored unsupported file store provider '" + provider + "', falling back to file system." );
            provider = DEFAULT_PROVIDER;
        }

        container = filestoreConfiguration.getOrDefault( KEY_FILESTORE_CONTAINER, DEFAULT_CONTAINER );

        location = filestoreConfiguration.getOrDefault( KEY_FILESTORE_LOCATION, null );

        switch ( provider )
        {
            case JCLOUDS_PROVIDER_KEY_FILESYSTEM:
                configureFilesystemProvider();
                break;
            case JCLOUDS_PROVIDER_KEY_AWS_S3:
                configureAWSS3Provider();
                break;
            default:
                throw new IllegalArgumentException( "The filestore provider " + provider + " is not supported." );
        }

        super.init();
    }

    public void cleanUp()
    {
        super.cleanUp();
    }

    // -------------------------------------------------------------------------
    // Configuration implementation
    // -------------------------------------------------------------------------

    @Override
    protected Properties getOverrides()
    {
        return overrides;
    }

    @Override
    protected Credentials getCredentials()
    {
        return credentials;
    }

    @Override
    protected String getContainer()
    {
        return container;
    }

    @Override
    protected String getLocation()
    {
        return location;
    }

    @Override
    protected String getJCloudsProviderKey()
    {
        return provider;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void configureFilesystemProvider()
    {
        overrides.setProperty( FilesystemConstants.PROPERTY_BASEDIR, locationManager.getExternalDirectoryPath() );
        credentials = super.getCredentials();
        log.info( "File system filestore provider configured." );
    }

    private void configureAWSS3Provider()
    {
        credentials = new Credentials( filestoreConfiguration.getOrDefault(
            KEY_FILESTORE_IDENTITY, "" ), filestoreConfiguration.getOrDefault( KEY_FILESTORE_SECRET, "" ) );
        log.info( "AWS S3 filestore provider configured." );

        if ( credentials.identity.isEmpty() || credentials.credential.isEmpty() )
        {
            log.info( "AWS S3 configured with empty credentials. Authentication will fail" );
        }
    }
}

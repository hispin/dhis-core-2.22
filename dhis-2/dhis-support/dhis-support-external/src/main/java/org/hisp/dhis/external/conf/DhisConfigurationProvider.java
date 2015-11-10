package org.hisp.dhis.external.conf;

import java.util.Map;

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

import java.util.Properties;

/**
 * Interface which provides access to the DHIS 2 configuration specified through
 * the dhis.config or hibernate.properties file.
 * 
 * @author Lars Helge Overland
 */
public interface DhisConfigurationProvider
{
    /**
     * Get configuration as a set of properties.
     * 
     * @return a Properties instance.
     */
    Properties getProperties();
    
    /**
     * Get configuration as an immutable map. The maps contains only properties 
     * which keys are starting with the given key base.
     * 
     * @param keyBase the base for properties to include.
     * @return an immutable map of the properties.
     */
    Map<String, String> getProperties( String keyBase );
    
    /**
     * Get the property value for the given key, or the default value for the
     * configuration key if not exists.
     * 
     * @param key the configuration key.
     * @return the property value.
     */
    String getProperty( ConfigurationKey key );

    /**
     * Get the property value for the given key, or the default value if not
     * exists.
     * 
     * @param key the configuration key.
     * @param defaultValue the default value.
     * @return the property value.
     */
    String getPropertyOrDefault( ConfigurationKey key, String defaultValue );
    
    /**
     * Indicates whether LDAP authentication is configured.
     * 
     * @return true if LDAP authentication is configured.
     */
    boolean isLdapConfigured();
}

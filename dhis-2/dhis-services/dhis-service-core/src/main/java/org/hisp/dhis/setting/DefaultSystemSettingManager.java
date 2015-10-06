package org.hisp.dhis.setting;

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

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.system.util.ValidationUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stian Strandli
 * @author Lars Helge Overland
 */
@Transactional
public class DefaultSystemSettingManager
    implements SystemSettingManager
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SystemSettingStore systemSettingStore;

    public void setSystemSettingStore( SystemSettingStore systemSettingStore )
    {
        this.systemSettingStore = systemSettingStore;
    }

    private List<String> flags;

    public void setFlags( List<String> flags )
    {
        this.flags = flags;
    }

    // -------------------------------------------------------------------------
    // SystemSettingManager implementation
    // -------------------------------------------------------------------------

    @Override
    public void saveSystemSetting( String name, Serializable value )
    {
        SystemSetting setting = systemSettingStore.getByName( name );

        if ( setting == null )
        {
            setting = new SystemSetting();

            setting.setName( name );
            setting.setValue( value );

            systemSettingStore.save( setting );
        }
        else
        {
            setting.setValue( value );

            systemSettingStore.update( setting );
        }
    }

    @Override
    public Serializable getSystemSetting( String name )
    {
        SystemSetting setting = systemSettingStore.getByName( name );

        return setting != null && setting.hasValue() ? setting.getValue() : null;
    }

    @Override
    public Serializable getSystemSetting( String name, Serializable defaultValue )
    {
        SystemSetting setting = systemSettingStore.getByName( name );

        return setting != null && setting.hasValue() ? setting.getValue() : defaultValue;
    }

    @Override
    public Serializable getSystemSetting( Setting setting )
    {
        return getSystemSetting( setting.getName(), setting.getDefaultValue() );
    }

    @Override
    public List<SystemSetting> getAllSystemSettings()
    {
        return systemSettingStore.getAll();
    }

    @Override
    public void deleteSystemSetting( String name )
    {
        SystemSetting setting = systemSettingStore.getByName( name );

        if ( setting != null )
        {
            systemSettingStore.delete( setting );
        }
    }

    // -------------------------------------------------------------------------
    // Specific methods
    // -------------------------------------------------------------------------

    @Override
    public List<String> getFlags()
    {
        Collections.sort( flags );
        return flags;
    }

    @Override
    public String getFlagImage()
    {
        String flag = (String) getSystemSetting( Setting.FLAG );

        return flag != null ? flag + ".png" : null;
    }

    @Override
    public String getEmailHostName()
    {
        return StringUtils.trimToNull( (String) getSystemSetting( Setting.EMAIL_HOST_NAME ) );
    }

    @Override
    public int getEmailPort()
    {
        return (Integer) getSystemSetting( Setting.EMAIL_PORT );
    }

    @Override
    public String getEmailUsername()
    {
        return StringUtils.trimToNull( (String) getSystemSetting( Setting.EMAIL_USERNAME ) );
    }

    @Override
    public boolean getEmailTls()
    {
        return (Boolean) getSystemSetting( Setting.EMAIL_TLS );
    }

    @Override
    public String getEmailSender()
    {
        return StringUtils.trimToNull( (String) getSystemSetting( Setting.EMAIL_SENDER ) );
    }

    @Override
    public String getInstanceBaseUrl()
    {
        return StringUtils.trimToNull( (String) getSystemSetting( Setting.INSTANCE_BASE_URL ) );
    }

    @Override
    public boolean accountRecoveryEnabled()
    {
        return (Boolean) getSystemSetting( Setting.ACCOUNT_RECOVERY );
    }

    @Override
    public boolean selfRegistrationNoRecaptcha()
    {
        return (Boolean) getSystemSetting( Setting.SELF_REGISTRATION_NO_RECAPTCHA );
    }

    @Override
    public boolean emailEnabled()
    {
        return getEmailHostName() != null;
    }

    @Override
    public boolean systemNotificationEmailValid()
    {
        String address = (String) getSystemSetting( Setting.SYSTEM_NOTIFICATIONS_EMAIL );

        return address != null && ValidationUtils.emailIsValid( address );
    }

    @Override
    public boolean hideUnapprovedDataInAnalytics()
    {
        return (Boolean) getSystemSetting( Setting.HIDE_UNAPPROVED_DATA_IN_ANALYTICS );
    }

    @Override
    public String googleAnalyticsUA()
    {
        return StringUtils.trimToNull( (String) getSystemSetting( Setting.GOOGLE_ANALYTICS_UA ) );
    }

    @Override
    public Integer credentialsExpires()
    {
        return (Integer) getSystemSetting( Setting.CREDENTIALS_EXPIRES );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public List<String> getCorsWhitelist()
    {
        Serializable value = getSystemSetting( Setting.CORS_WHITELIST );
        
        return value != null ? (List<String>) value : Collections.emptyList();
    }

    @Override
    public Map<String, Serializable> getSystemSettingsAsMap()
    {
        Map<String, Serializable> settingsMap = new HashMap<>();
        Collection<SystemSetting> systemSettings = getAllSystemSettings();

        for ( SystemSetting systemSetting : systemSettings )
        {
            Serializable settingValue = systemSetting.getValue();
            if ( settingValue == null )
            {
                settingValue = DEFAULT_SETTINGS_VALUES.get( systemSetting.getName() );
            }

            settingsMap.put( systemSetting.getName(), settingValue );
        }

        return settingsMap;
    }

    @Override
    public Map<String, Serializable> getSystemSettings( Set<String> names )
    {
        Map<String, Serializable> map = new HashMap<>();

        for ( String name : names )
        {
            Serializable setting = getSystemSetting( name, DEFAULT_SETTINGS_VALUES.get( name ) );

            if ( setting != null )
            {
                map.put( name, setting );
            }
        }

        return map;
    }
}

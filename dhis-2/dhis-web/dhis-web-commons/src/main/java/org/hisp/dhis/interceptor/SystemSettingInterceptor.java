package org.hisp.dhis.interceptor;

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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ACCEPTANCE_REQUIRED_FOR_APPROVAL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ACCOUNT_RECOVERY;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ALLOW_OBJECT_ASSIGNMENT;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ANALYSIS_RELATIVE_PERIOD;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ANALYTICS_MAINTENANCE_MODE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_ANALYTICS_MAX_LIMIT;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_APPLICATION_FOOTER;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_APPLICATION_INTRO;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_APPLICATION_NOTIFICATION;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_APPLICATION_RIGHT_FOOTER;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_APPLICATION_TITLE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CACHE_STRATEGY;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CAN_GRANT_OWN_USER_AUTHORITY_GROUPS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CONFIGURATION;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CREDENTIALS_EXPIRES;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CUSTOM_LOGIN_PAGE_LOGO;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_CUSTOM_TOP_MENU_LOGO;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_DATABASE_SERVER_CPUS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_FACTOR_OF_DEVIATION;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_FLAG;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_FLAG_IMAGE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_GOOGLE_ANALYTICS_UA;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_HELP_PAGE_LINK;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_HIDE_UNAPPROVED_DATA_IN_ANALYTICS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_INSTANCE_BASE_URL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_MULTI_ORGANISATION_UNIT_FORMS;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_OPENID_PROVIDER;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_OPENID_PROVIDER_LABEL;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_PHONE_NUMBER_AREA_CODE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_REQUIRE_ADD_TO_VIEW;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_SELF_REGISTRATION_NO_RECAPTCHA;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_START_MODULE;
import static org.hisp.dhis.setting.SystemSettingManager.KEY_SYSTEM_NOTIFICATIONS_EMAIL;
import static org.hisp.dhis.setting.SystemSettingManager.SYSPROP_PORTAL;

import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.calendar.CalendarService;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.setting.Setting;
import org.hisp.dhis.setting.SystemSettingManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * @author Lars Helge Overland
 */
public class SystemSettingInterceptor
    implements Interceptor
{
    private static final String DATE_FORMAT = "dateFormat";
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SystemSettingManager systemSettingManager;

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    @Autowired
    private CalendarService calendarService;

    // -------------------------------------------------------------------------
    // AroundInterceptor implementation
    // -------------------------------------------------------------------------

    @Override
    public void destroy()
    {
    }

    @Override
    public void init()
    {
    }

    @Override
    public String intercept( ActionInvocation invocation )
        throws Exception
    {
        Map<String, Object> map = new HashMap<>();

        map.put( Setting.CALENDAR.getName(), calendarService.getSystemCalendarKey() );
        map.put( Setting.DATE_FORMAT.getName(), calendarService.getSystemDateFormatKey() );
        
        map.put( DATE_FORMAT, calendarService.getSystemDateFormat() );
        map.put( KEY_CACHE_STRATEGY, systemSettingManager.getSystemSetting( Setting.CACHE_STRATEGY ) );
        map.put( KEY_ANALYTICS_MAX_LIMIT, systemSettingManager.getSystemSetting( Setting.ANALYTICS_MAX_LIMIT ) );
        map.put( KEY_ANALYSIS_RELATIVE_PERIOD, systemSettingManager.getSystemSetting( Setting.ANALYSIS_RELATIVE_PERIOD ) );
        map.put( KEY_APPLICATION_TITLE, systemSettingManager.getSystemSetting( Setting.APPLICATION_TITLE ) );
        map.put( KEY_APPLICATION_INTRO, systemSettingManager.getSystemSetting( KEY_APPLICATION_INTRO ) );
        map.put( KEY_APPLICATION_NOTIFICATION, systemSettingManager.getSystemSetting( KEY_APPLICATION_NOTIFICATION ) );
        map.put( KEY_APPLICATION_FOOTER, systemSettingManager.getSystemSetting( KEY_APPLICATION_FOOTER ) );
        map.put( KEY_APPLICATION_RIGHT_FOOTER, systemSettingManager.getSystemSetting( KEY_APPLICATION_RIGHT_FOOTER ) );
        map.put( KEY_FLAG, systemSettingManager.getSystemSetting( Setting.FLAG ) );
        map.put( KEY_FLAG_IMAGE, systemSettingManager.getFlagImage() );
        map.put( KEY_START_MODULE, systemSettingManager.getSystemSetting( Setting.START_MODULE ) );
        map.put( KEY_FACTOR_OF_DEVIATION, systemSettingManager.getSystemSetting( Setting.FACTOR_OF_DEVIATION ) );
        map.put( KEY_PHONE_NUMBER_AREA_CODE, systemSettingManager.getSystemSetting( Setting.PHONE_NUMBER_AREA_CODE ) );
        map.put( KEY_MULTI_ORGANISATION_UNIT_FORMS, systemSettingManager.getSystemSetting( Setting.MULTI_ORGANISATION_UNIT_FORMS ) );
        map.put( KEY_ACCOUNT_RECOVERY, systemSettingManager.getSystemSetting( Setting.ACCOUNT_RECOVERY ) );
        map.put( KEY_CONFIGURATION, configurationService.getConfiguration() );
        map.put( Setting.APP_BASE_URL.getName(), systemSettingManager.getSystemSetting( Setting.APP_BASE_URL ) );
        map.put( KEY_INSTANCE_BASE_URL, systemSettingManager.getSystemSetting( KEY_INSTANCE_BASE_URL ) );
        map.put( KEY_GOOGLE_ANALYTICS_UA, systemSettingManager.getSystemSetting( Setting.GOOGLE_ANALYTICS_UA ) );
        map.put( KEY_CREDENTIALS_EXPIRES, systemSettingManager.credentialsExpires() );
        map.put( KEY_SELF_REGISTRATION_NO_RECAPTCHA, systemSettingManager.selfRegistrationNoRecaptcha() );
        map.put( KEY_OPENID_PROVIDER, systemSettingManager.getSystemSetting( KEY_OPENID_PROVIDER ) );
        map.put( KEY_OPENID_PROVIDER_LABEL, systemSettingManager.getSystemSetting( KEY_OPENID_PROVIDER_LABEL ) );
        map.put( KEY_CAN_GRANT_OWN_USER_AUTHORITY_GROUPS, systemSettingManager.getSystemSetting( Setting.CAN_GRANT_OWN_USER_AUTHORITY_GROUPS ) );
        map.put( KEY_CUSTOM_LOGIN_PAGE_LOGO, systemSettingManager.getSystemSetting( Setting.CUSTOM_LOGIN_PAGE_LOGO ) );
        map.put( KEY_CUSTOM_TOP_MENU_LOGO, systemSettingManager.getSystemSetting( Setting.CUSTOM_TOP_MENU_LOGO ) );
        map.put( KEY_ANALYTICS_MAINTENANCE_MODE, systemSettingManager.getSystemSetting( Setting.ANALYTICS_MAINTENANCE_MODE ) );
        map.put( KEY_DATABASE_SERVER_CPUS, systemSettingManager.getSystemSetting( Setting.DATABASE_SERVER_CPUS ) );
        map.put( KEY_HELP_PAGE_LINK, systemSettingManager.getSystemSetting( Setting.HELP_PAGE_LINK ) );
        map.put( KEY_HIDE_UNAPPROVED_DATA_IN_ANALYTICS, systemSettingManager.getSystemSetting( Setting.HIDE_UNAPPROVED_DATA_IN_ANALYTICS, false ) );
        map.put( KEY_ACCEPTANCE_REQUIRED_FOR_APPROVAL, systemSettingManager.getSystemSetting( Setting.ACCEPTANCE_REQUIRED_FOR_APPROVAL ) );
        map.put( KEY_SYSTEM_NOTIFICATIONS_EMAIL, systemSettingManager.getSystemSetting( KEY_SYSTEM_NOTIFICATIONS_EMAIL ) );
        map.put( KEY_REQUIRE_ADD_TO_VIEW, systemSettingManager.getSystemSetting( Setting.REQUIRE_ADD_TO_VIEW ) );
        map.put( KEY_ALLOW_OBJECT_ASSIGNMENT, systemSettingManager.getSystemSetting( Setting.ALLOW_OBJECT_ASSIGNMENT ) );
        map.put( SYSPROP_PORTAL, defaultIfEmpty( System.getProperty( SYSPROP_PORTAL ), String.valueOf( false ) ) );

        invocation.getStack().push( map );

        return invocation.invoke();
    }
}

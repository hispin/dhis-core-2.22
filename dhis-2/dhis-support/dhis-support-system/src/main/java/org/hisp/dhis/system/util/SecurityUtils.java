package org.hisp.dhis.system.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityUtils
{
    public static Collection<GrantedAuthority> getGrantedAuthorities( UserCredentials credentials )
    {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for ( UserAuthorityGroup group : credentials.getUserAuthorityGroups() )
        {
            for ( String authority : group.getAuthorities() )
            {
                authorities.add( new SimpleGrantedAuthority( authority ) );
            }
        }

        return authorities;
    }
}

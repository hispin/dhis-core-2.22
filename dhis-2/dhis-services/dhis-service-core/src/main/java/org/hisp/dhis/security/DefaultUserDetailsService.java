package org.hisp.dhis.security;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.system.util.SecurityUtils;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Torgeir Lorange Ostby
 */
public class DefaultUserDetailsService
    implements UserDetailsService
{
    public static final String ID = UserDetailsService.class.getName();

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // UserDetailsService implementation
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public final UserDetails loadUserByUsername( String username )
        throws UsernameNotFoundException, DataAccessException
    {
        UserCredentials credentials = userService.getUserCredentialsByUsername( username );

        // ---------------------------------------------------------------------
        // OpenId
        // ---------------------------------------------------------------------

        if ( credentials == null )
        {
            credentials = userService.getUserCredentialsByOpenId( username );

            if ( credentials == null )
            {
                throw new UsernameNotFoundException( "Username does not exist" );
            }
        }

        // ---------------------------------------------------------------------
        // If password is null, assume external authentication (OpenID, LDAP)
        // and set not encoded, random password to satisfy Spring Security
        // ---------------------------------------------------------------------

        String password = credentials.getPassword();
        
        if ( !credentials.hasPassword() )
        {
            password = CodeGenerator.generateCode( 60 );
        }
        
        boolean credentialsExpired = userService.credentialsNonExpired( credentials );

        return new User( credentials.getUsername(), password,
            !credentials.isDisabled(), true, credentialsExpired, true, SecurityUtils.getGrantedAuthorities( credentials ) );
    }
}

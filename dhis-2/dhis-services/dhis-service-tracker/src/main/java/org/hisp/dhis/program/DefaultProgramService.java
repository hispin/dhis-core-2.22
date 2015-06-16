package org.hisp.dhis.program;

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

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.validation.ValidationCriteria;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

/**
 * @author Abyot Asalefew
 */
@Transactional
public class DefaultProgramService
    implements ProgramService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramStore programStore;

    public void setProgramStore( ProgramStore programStore )
    {
        this.programStore = programStore;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    public int addProgram( Program program )
    {
        return programStore.save( program );
    }

    @Override
    public void updateProgram( Program program )
    {
        programStore.update( program );
    }

    @Override
    public void deleteProgram( Program program )
    {
        programStore.delete( program );
    }

    @Override
    public List<Program> getAllPrograms()
    {
        return i18n( i18nService, programStore.getAll() );
    }

    @Override
    public Program getProgram( int id )
    {
        return i18n( i18nService, programStore.get( id ) );
    }

    @Override
    public Program getProgramByName( String name )
    {
        return i18n( i18nService, programStore.getByName( name ) );
    }

    @Override
    public List<Program> getPrograms( OrganisationUnit organisationUnit )
    {
        return i18n( i18nService, programStore.get( organisationUnit ) );
    }

    @Override
    public List<Program> getPrograms( ValidationCriteria validationCriteria )
    {
        List<Program> programs = new ArrayList<>();

        for ( Program program : getAllPrograms() )
        {
            if ( program.getValidationCriteria().contains( validationCriteria ) )
            {
                programs.add( program );
            }
        }

        return i18n( i18nService, programs );
    }

    @Override
    public List<Program> getPrograms( int type )
    {
        return i18n( i18nService, programStore.getByType( type ) );
    }

    @Override
    public List<Program> getPrograms( int type, OrganisationUnit orgunit )
    {
        return i18n( i18nService, programStore.get( type, orgunit ) );
    }

    @Override
    public List<Program> getProgramsByCurrentUser()
    {
        return i18n( i18nService, getByCurrentUser() );
    }

    @Override
    public List<Program> getProgramsByUser( User user )
    {
        return i18n( i18nService, getByUser( user ) );
    }

    @Override
    public List<Program> getProgramsByCurrentUser( int type )
    {
        return i18n( i18nService, getByCurrentUser( type ) );
    }

    @Override
    public Program getProgram( String uid )
    {
        return i18n( i18nService, programStore.getByUid( uid ) );
    }

    @Override
    public List<Program> getProgramsByCurrentUser( OrganisationUnit organisationUnit )
    {
        List<Program> programs = new ArrayList<>( getPrograms( organisationUnit ) );
        programs.retainAll( getProgramsByCurrentUser() );

        return programs;
    }

    @Override
    public List<Program> getProgramsByTrackedEntity( TrackedEntity trackedEntity )
    {
        return i18n( i18nService, programStore.getByTrackedEntity( trackedEntity ) );
    }

    @Override
    public Integer getProgramCountByName( String name )
    {
        return i18n( i18nService, programStore.getCountLikeName( name ) );
    }

    @Override
    public List<Program> getProgramBetweenByName( String name, int min, int max )
    {
        return i18n( i18nService, programStore.getAllLikeName( name, min, max ) );
    }

    @Override
    public Integer getProgramCount()
    {
        return programStore.getCount();
    }

    @Override
    public List<Program> getProgramsBetween( int min, int max )
    {
        return i18n( i18nService, programStore.getAllOrderedName( min, max ) );
    }

    @Override
    public List<Program> getByCurrentUser()
    {
        return getByUser( currentUserService.getCurrentUser() );
    }

    public List<Program> getByUser( User user )
    {
        List<Program> programs = new ArrayList<>();

        if ( user != null && !user.isSuper() )
        {
            Set<UserAuthorityGroup> userRoles = userService.getUserCredentials( currentUserService.getCurrentUser() )
                .getUserAuthorityGroups();

            for ( Program program : programStore.getAll() )
            {
                if ( Sets.intersection( program.getUserRoles(), userRoles ).size() > 0 )
                {
                    programs.add( program );
                }
            }
        }
        else
        {
            programs = programStore.getAll();
        }

        return programs;
    }

    @Override
    public List<Program> getByCurrentUser( int type )
    {
        List<Program> programs = new ArrayList<>();

        if ( currentUserService.getCurrentUser() != null && !currentUserService.currentUserIsSuper() )
        {
            Set<UserAuthorityGroup> userRoles = userService.getUserCredentials( currentUserService.getCurrentUser() )
                .getUserAuthorityGroups();

            for ( Program program : programStore.getByType( type ) )
            {
                if ( Sets.intersection( program.getUserRoles(), userRoles ).size() > 0 )
                {
                    programs.add( program );
                }
            }
        }
        else
        {
            programs = programStore.getByType( type );
        }

        return programs;
    }
}

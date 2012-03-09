/*
 * Copyright (c) 2004-2009, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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
package org.hisp.dhis.program;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patientdatavalue.PatientDataValue;
import org.hisp.dhis.patientdatavalue.PatientDataValueService;
import org.hisp.dhis.system.grid.ListGrid;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Abyot Asalefew
 * @version $Id$
 */
@Transactional
public class DefaultProgramStageInstanceService
    implements ProgramStageInstanceService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramStageInstanceStore programStageInstanceStore;

    public void setProgramStageInstanceStore( ProgramStageInstanceStore programStageInstanceStore )
    {
        this.programStageInstanceStore = programStageInstanceStore;
    }

    private PatientDataValueService patientDataValueService;

    public void setPatientDataValueService( PatientDataValueService patientDataValueService )
    {
        this.patientDataValueService = patientDataValueService;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    public int addProgramStageInstance( ProgramStageInstance programStageInstance )
    {
        return programStageInstanceStore.save( programStageInstance );
    }

    public void deleteProgramStageInstance( ProgramStageInstance programStageInstance )
    {
        programStageInstanceStore.delete( programStageInstance );
    }

    public Collection<ProgramStageInstance> getAllProgramStageInstances()
    {
        return programStageInstanceStore.getAll();
    }

    public ProgramStageInstance getProgramStageInstance( int id )
    {
        return programStageInstanceStore.get( id );
    }

    public ProgramStageInstance getProgramStageInstance( ProgramInstance programInstance, ProgramStage programStage )
    {
        return programStageInstanceStore.get( programInstance, programStage );
    }

    public Collection<ProgramStageInstance> getProgramStageInstances( ProgramStage programStage )
    {
        return programStageInstanceStore.get( programStage );
    }

    public void updateProgramStageInstance( ProgramStageInstance programStageInstance )
    {
        programStageInstanceStore.update( programStageInstance );
    }

    public Map<Integer, String> colorProgramStageInstances( Collection<ProgramStageInstance> programStageInstances )
    {
        Map<Integer, String> colorMap = new HashMap<Integer, String>();

        for ( ProgramStageInstance programStageInstance : programStageInstances )
        {
            if ( programStageInstance.isCompleted() )
            {
                colorMap.put( programStageInstance.getId(), ProgramStageInstance.COLOR_GREEN );
            }
            else if ( programStageInstance.getExecutionDate() != null )
            {
                colorMap.put( programStageInstance.getId(), ProgramStageInstance.COLOR_LIGHTRED );
            }
            else
            {
                // -------------------------------------------------------------
                // If a program stage is not provided even a day after its due
                // date, then that service is alerted red - because we are
                // getting late
                // -------------------------------------------------------------

                Calendar dueDateCalendar = Calendar.getInstance();
                dueDateCalendar.setTime( programStageInstance.getDueDate() );
                dueDateCalendar.add( Calendar.DATE, 1 );

                if ( dueDateCalendar.getTime().before( new Date() ) )
                {
                    colorMap.put( programStageInstance.getId(), ProgramStageInstance.COLOR_RED );
                }
                else
                {
                    colorMap.put( programStageInstance.getId(), ProgramStageInstance.COLOR_YELLOW );
                }
            }
        }

        return colorMap;
    }

    public Collection<ProgramStageInstance> getProgramStageInstances( Collection<ProgramInstance> programInstances )
    {
        return programStageInstanceStore.get( programInstances );
    }

    public Collection<ProgramStageInstance> getProgramStageInstances( Date dueDate )
    {
        return programStageInstanceStore.get( dueDate );
    }

    public Collection<ProgramStageInstance> getProgramStageInstances( Date dueDate, Boolean completed )
    {
        return programStageInstanceStore.get( dueDate, completed );
    }

    public Collection<ProgramStageInstance> getProgramStageInstances( Date startDate, Date endDate )
    {
        return programStageInstanceStore.get( startDate, endDate );
    }

    public Collection<ProgramStageInstance> getProgramStageInstances( Date startDate, Date endDate, Boolean completed )
    {
        return programStageInstanceStore.get( startDate, endDate, completed );
    }

    public List<ProgramStageInstance> get( OrganisationUnit unit, Date after, Date before, Boolean completed )
    {
        return programStageInstanceStore.get( unit, after, before, completed );
    }

    public List<ProgramStageInstance> getProgramStageInstances( Patient patient, Boolean completed )
    {
        return programStageInstanceStore.get( patient, completed );
    }

    public List<ProgramStageInstance> searchProgramStageInstances( ProgramStage programStage,
        Map<Integer, String> searchingKeys, Collection<Integer> orgunitIds, Date startDate, Date endDate, int min,
        int max )
    {
        return programStageInstanceStore.get( programStage, searchingKeys, orgunitIds, startDate, endDate, min, max );
    }

    public List<ProgramStageInstance> searchProgramStageInstances( ProgramStage programStage,
        Map<Integer, String> searchingKeys, Collection<Integer> orgunitIds, Date startDate, Date endDate )
    {
        return programStageInstanceStore.get( programStage, searchingKeys, orgunitIds, startDate, endDate );
    }

    public Grid getTabularReport( ProgramStage programStage, List<DataElement> dataElements,
        Map<Integer, String> searchingKeys, Collection<Integer> orgunitIds, Date startDate, Date endDate, int min,
        int max, I18nFormat format, I18n i18n )
    {
        List<ProgramStageInstance> programStageInstances = searchProgramStageInstances( programStage, searchingKeys,
            orgunitIds, startDate, endDate, min, max );

        return createTabularGrid( programStage, programStageInstances, dataElements, startDate, endDate, format, i18n );
    }

    public Grid getTabularReport( ProgramStage programStage, List<DataElement> dataElements,
        Map<Integer, String> searchingKeys, Collection<Integer> orgunitIds, Date startDate, Date endDate,
        I18nFormat format, I18n i18n )
    {
        List<ProgramStageInstance> programStageInstances = searchProgramStageInstances( programStage, searchingKeys,
            orgunitIds, startDate, endDate );

        return createTabularGrid( programStage, programStageInstances, dataElements, startDate, endDate, format, i18n );
    }

    @Override
    public int countProgramStageInstances( ProgramStage programStage, Map<Integer, String> searchingKeys,
        Collection<Integer> orgunitIds, Date startDate, Date endDate )
    {
        return programStageInstanceStore.count( programStage, searchingKeys, orgunitIds, startDate, endDate );
    }

    public List<Grid> getProgramStageInstancesReport( ProgramInstance programInstance, I18nFormat format, I18n i18n )
    {
        List<Grid> grids = new ArrayList<Grid>();

        Collection<ProgramStageInstance> programStageInstances = programInstance.getProgramStageInstances();

        for ( ProgramStageInstance programStageInstance : programStageInstances )
        {
            Grid grid = new ListGrid();

            // ---------------------------------------------------------------------
            // Title
            // ---------------------------------------------------------------------
            Date executionDate = programStageInstance.getExecutionDate();
            String executionDateValue = (executionDate != null) ? format.formatDate( programStageInstance
                .getExecutionDate() ) : "[" + i18n.getString( "none" ) + "]";

            grid.setTitle( programStageInstance.getProgramStage().getName() );
            grid.setSubtitle( i18n.getString( "due_date" ) + ": "
                + format.formatDate( programStageInstance.getDueDate() ) + " - " + i18n.getString( "report_date" )
                + ": " + executionDateValue );

            // ---------------------------------------------------------------------
            // Headers
            // ---------------------------------------------------------------------

            grid.addHeader( new GridHeader( i18n.getString( "name" ), false, false ) );
            grid.addHeader( new GridHeader( i18n.getString( "value" ), false, false ) );

            // ---------------------------------------------------------------------
            // Values
            // ---------------------------------------------------------------------
            Collection<PatientDataValue> patientDataValues = patientDataValueService
                .getPatientDataValues( programStageInstance );

            if ( executionDate == null || patientDataValues == null || patientDataValues.size() == 0 )
            {
                grid.addRow();
                grid.addValue( "[" + i18n.getString( "none" ) + "]" );
                grid.addValue( "" );
            }
            else
            {
                for ( PatientDataValue patientDataValue : patientDataValues )
                {
                    DataElement dataElement = patientDataValue.getDataElement();

                    grid.addRow();
                    grid.addValue( dataElement.getName() );

                    if ( dataElement.getType().equals( DataElement.VALUE_TYPE_BOOL ) )
                    {
                        grid.addValue( i18n.getString( patientDataValue.getValue() ) );
                    }
                    else
                    {
                        grid.addValue( patientDataValue.getValue() );
                    }
                }
            }

            grids.add( grid );
        }

        return grids;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private Grid createTabularGrid( ProgramStage programStage, List<ProgramStageInstance> programStageInstances,
        List<DataElement> dataElements, Date startDate, Date endDate, I18nFormat format, I18n i18n )
    {
        Grid grid = new ListGrid();

        if ( dataElements != null && dataElements.size() > 0 && programStageInstances.size() > 0 )
        {
            Program program = programStage.getProgram();

            Boolean anonymous = program.getAnonymous();

            // ---------------------------------------------------------------------
            // Create a grid
            // ---------------------------------------------------------------------

            grid.setTitle( program.getName() + " - " + programStage.getName() );
            grid.setSubtitle( i18n.getString( "from" ) + " " + format.formatDate( startDate ) + " "
                + i18n.getString( "to" ) + " " + format.formatDate( endDate ) );

            // ---------------------------------------------------------------------
            // Headers
            // ---------------------------------------------------------------------

            for ( DataElement dataElement : dataElements )
            {
                grid.addHeader( new GridHeader( dataElement.getName(), false, false ) );
            }

            if ( !anonymous )
            {
                grid.addHeader( new GridHeader( "", true, false ) );
            }

            // ---------------------------------------------------------------------
            // Values
            // ---------------------------------------------------------------------

            for ( ProgramStageInstance programStageInstance : programStageInstances )
            {
                grid.addRow();

                for ( DataElement dataElement : dataElements )
                {
                    PatientDataValue patientDataValue = patientDataValueService.getPatientDataValue(
                        programStageInstance, dataElement );

                    if ( patientDataValue != null )
                    {
                        if ( dataElement.getType().equals( DataElement.VALUE_TYPE_BOOL ) )
                        {
                            grid.addValue( i18n.getString( patientDataValue.getValue() ) );
                        }
                        else
                        {
                            grid.addValue( patientDataValue.getValue() );
                        }
                    }
                    else
                    {
                        grid.addValue( "" );
                    }
                }
                
                if ( programStageInstance.getProgramInstance().getPatient() != null )
                {
                    grid.addValue( programStageInstance.getProgramInstance().getPatient()
                        .getId() );
                }
                else
                {
                    grid.addValue( "" );
                }
            }
        }

        return grid;
    }

}

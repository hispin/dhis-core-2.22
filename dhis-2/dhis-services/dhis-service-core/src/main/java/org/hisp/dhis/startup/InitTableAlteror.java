package org.hisp.dhis.startup;

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

import org.amplecode.quick.StatementManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.jdbc.StatementBuilder;
import org.hisp.dhis.system.startup.AbstractStartupRoutine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 */
public class InitTableAlteror
    extends AbstractStartupRoutine
{
    private static final Log log = LogFactory.getLog( InitTableAlteror.class );

    @Autowired
    private StatementManager statementManager;

    @Autowired
    private StatementBuilder statementBuilder;

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public void execute()
    {
        // domain type

        executeSql( "update dataelement set domaintype='AGGREGATE' where domaintype='aggregate' or domaintype is null;" );
        executeSql( "update dataelement set domaintype='TRACKER' where domaintype='patient';" );
        executeSql( "update users set invitation = false where invitation is null" );
        executeSql( "alter table dataelement alter column domaintype set not null;" );
        executeSql( "alter table programstageinstance alter column  status  type varchar(25);" );
        executeSql( "UPDATE programstageinstance SET status='ACTIVE' WHERE status='0';" );
        executeSql( "UPDATE programstageinstance SET status='COMPLETED' WHERE status='1';" );
        executeSql( "UPDATE programstageinstance SET status='SKIPPED' WHERE status='5';" );
        executeSql( "ALTER TABLE program DROP COLUMN displayonallorgunit" );

        upgradeProgramStageDataElements();
        updateValueTypes();

        executeSql( "ALTER TABLE program ALTER COLUMN \"type\" TYPE varchar(255);" );
        executeSql( "update program set \"type\"='WITH_REGISTRATION' where type='1' or type='2'" );
        executeSql( "update program set \"type\"='WITHOUT_REGISTRATION' where type='3'" );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void updateValueTypes()
    {
        executeSql( "alter table dataelement alter column valuetype type varchar(50)" );

        executeSql( "update dataelement set valuetype='NUMBER' where valuetype='int' and numbertype='number'" );
        executeSql( "update dataelement set valuetype='INTEGER' where valuetype='int' and numbertype='int'" );
        executeSql( "update dataelement set valuetype='INTEGER_POSITIVE' where valuetype='int' and numbertype='posInt'" );
        executeSql( "update dataelement set valuetype='INTEGER_NEGATIVE' where valuetype='int' and numbertype='negInt'" );
        executeSql( "update dataelement set valuetype='INTEGER_ZERO_OR_POSITIVE' where valuetype='int' and numbertype='zeroPositiveInt'" );
        executeSql( "update dataelement set valuetype='PERCENTAGE' where valuetype='int' and numbertype='percentage'" );
        executeSql( "update dataelement set valuetype='UNIT_INTERVAL' where valuetype='int' and numbertype='unitInterval'" );
        executeSql( "update dataelement set valuetype='NUMBER' where valuetype='int'" );

        executeSql( "update dataelement set valuetype='TEXT' where valuetype='string' and texttype='text'" );
        executeSql( "update dataelement set valuetype='LONG_TEXT' where valuetype='string' and texttype='longText'" );
        executeSql( "update dataelement set valuetype='TEXT' where valuetype='string'" );

        executeSql( "update dataelement set valuetype='DATE' where valuetype='date'" );
        executeSql( "update dataelement set valuetype='DATETIME' where valuetype='datetime'" );
        executeSql( "update dataelement set valuetype='BOOLEAN' where valuetype='bool'" );
        executeSql( "update dataelement set valuetype='TRUE_ONLY' where valuetype='trueOnly'" );
        executeSql( "update dataelement set valuetype='USERNAME' where valuetype='username'" );

        executeSql( "alter table dataelement drop column numbertype" );
        executeSql( "alter table dataelement drop column texttype" );

        executeSql( "update trackedentityattribute set valuetype='TEXT' where valuetype='string'" );
        executeSql( "update trackedentityattribute set valuetype='PHONE_NUMBER' where valuetype='phoneNumber'" );
        executeSql( "update trackedentityattribute set valuetype='EMAIL' where valuetype='email'" );
        executeSql( "update trackedentityattribute set valuetype='NUMBER' where valuetype='number'" );
        executeSql( "update trackedentityattribute set valuetype='NUMBER' where valuetype='int'" );
        executeSql( "update trackedentityattribute set valuetype='LETTER' where valuetype='letter'" );
        executeSql( "update trackedentityattribute set valuetype='BOOLEAN' where valuetype='bool'" );
        executeSql( "update trackedentityattribute set valuetype='TRUE_ONLY' where valuetype='trueOnly'" );
        executeSql( "update trackedentityattribute set valuetype='DATE' where valuetype='date'" );
        executeSql( "update trackedentityattribute set valuetype='OPTION_SET' where valuetype='optionSet'" );
        executeSql( "update trackedentityattribute set valuetype='TRACKER_ASSOCIATE' where valuetype='trackerAssociate'" );
        executeSql( "update trackedentityattribute set valuetype='USERNAME' where valuetype='users'" );
    }

    private void upgradeProgramStageDataElements()
    {
        if ( tableExists( "programstage_dataelements" ) )
        {
            String autoIncr = statementBuilder.getAutoIncrementValue();

            String insertSql =
                "insert into programstagedataelement(programstagedataelementid,programstageid,dataelementid,compulsory,allowprovidedelsewhere,sort_order,displayinreports,programstagesectionid,allowfuturedate,section_sort_order) " +
                    "select " + autoIncr + ",programstageid,dataelementid,compulsory,allowprovidedelsewhere,sort_order,displayinreports,programstagesectionid,allowfuturedate,section_sort_order " +
                    "from programstage_dataelements";

            executeSql( insertSql );

            String dropSql = "drop table programstage_dataelements";

            executeSql( dropSql );

            log.info( "Upgraded program stage data elements" );
        }
    }

    private int executeSql( String sql )
    {
        try
        {
            return statementManager.getHolder().executeUpdate( sql );
        }
        catch ( Exception ex )
        {
            log.debug( ex );

            return -1;
        }
    }

    private boolean tableExists( String table )
    {
        try
        {
            statementManager.getHolder().queryForInteger( "select 1 from " + table );
            return true;
        }
        catch ( Exception ex )
        {
            return false;
        }
    }
}

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

import com.google.common.io.ByteSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.system.scheduling.Scheduler;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Halvdan Hoem Grelland
 */
public class DefaultFileResourceService
    implements FileResourceService
{
    private static final Log log = LogFactory.getLog( DefaultFileResourceService.class );

    private static final String KEY_FILE_CLEANUP_TASK = "fileResourceCleanupTask";

    private static final Duration IS_ORPHAN_TIME_DELTA = Hours.TWO.toStandardDuration();
    private static final Duration LONG_STORAGE_DURATION_TIME_DELTA = Minutes.TWO.toStandardDuration();

    private static final Predicate<FileResource> IS_ORPHAN_PREDICATE =
        ( fr -> !fr.isAssigned() || fr.getStorageStatus() != FileResourceStorageStatus.STORED );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private GenericIdentifiableObjectStore<FileResource> fileResourceStore;

    public void setFileResourceStore( GenericIdentifiableObjectStore<FileResource> fileResourceStore )
    {
        this.fileResourceStore = fileResourceStore;
    }

    private FileResourceContentStore fileResourceContentStore;

    public void setFileResourceContentStore( FileResourceContentStore fileResourceContentStore )
    {
        this.fileResourceContentStore = fileResourceContentStore;
    }

    private Scheduler scheduler;

    public void setScheduler( Scheduler scheduler )
    {
        this.scheduler = scheduler;
    }

    private FileResourceUploadCallbackProvider uploadCallbackProvider;

    public void setUploadCallbackProvider( FileResourceUploadCallbackProvider uploadCallbackProvider )
    {
        this.uploadCallbackProvider = uploadCallbackProvider;
    }

    private FileResourceCleanUpTask fileResourceCleanUpTask;

    public void setFileResourceCleanUpTask( FileResourceCleanUpTask fileResourceCleanUpTask )
    {
        this.fileResourceCleanUpTask = fileResourceCleanUpTask;
    }

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    @PostConstruct
    public void init()
    {
        // Background task which queries for non-assigned or failed-upload
        // FileResources and deletes them from the database and/or file store.
        // Runs every day at 2 AM server time.

        scheduler.scheduleTask( KEY_FILE_CLEANUP_TASK, fileResourceCleanUpTask, Scheduler.CRON_DAILY_2AM );
    }

    // -------------------------------------------------------------------------
    // FileResourceService implementation
    // -------------------------------------------------------------------------

    @Transactional
    @Override
    public FileResource getFileResource( String uid )
    {
        // TODO Consider need for ensureStorageStatus
        return ensureStorageStatus( fileResourceStore.getByUid( uid ) );
    }

    @Override
    public List<FileResource> getFileResources( List<String> uids )
    {
        return fileResourceStore.getByUid( uids );
    }

    @Transactional
    @Override
    public List<FileResource> getOrphanedFileResources( )
    {
        return fileResourceStore.getAllLeCreated( new DateTime().minus( IS_ORPHAN_TIME_DELTA ).toDate() )
            .stream().filter( IS_ORPHAN_PREDICATE ).collect( Collectors.toList() );
    }

    @Transactional
    @Override
    public String saveFileResource( FileResource fileResource, File file )
    {
        fileResource.setStorageStatus( FileResourceStorageStatus.PENDING );
        fileResourceStore.save( fileResource );

        ListenableFuture<String> saveContentTask =
            scheduler.executeTask( () -> fileResourceContentStore.saveFileResourceContent( fileResource, file ) );

        String uid = fileResource.getUid();

        saveContentTask.addCallback( uploadCallbackProvider.getCallback( uid ) );

        return uid;
    }

    @Transactional
    @Override
    public void deleteFileResource( String uid )
    {
        if ( uid == null )
        {
            return;
        }

        FileResource fileResource = fileResourceStore.getByUid( uid );

        if ( fileResource == null )
        {
            return;
        }

        fileResourceContentStore.deleteFileResourceContent( fileResource.getStorageKey() );
        fileResourceStore.delete( fileResource );
    }

    @Override
    public ByteSource getFileResourceContent( FileResource fileResource )
    {
        return fileResourceContentStore.getFileResourceContent( fileResource.getStorageKey() );
    }

    @Override
    public boolean fileResourceExists( String uid )
    {
        return fileResourceStore.getByUid( uid ) != null;
    }

    @Override
    public void updateFileResource( FileResource fileResource )
    {
        fileResourceStore.update( fileResource );
    }

    @Override
    public URI getSignedGetFileResourceContentUri( String uid )
    {
        FileResource fileResource = getFileResource( uid );

        if ( fileResource == null )
        {
            return null;
        }

        return fileResourceContentStore.getSignedGetContentUri( fileResource.getStorageKey() );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Ensures that the storageStatus of the FileResource is correct.
     * If it has been pending for more than two minutes existance of the content is
     * 'double checked' in the file store.
     * If the content is actually present the storageStatus is updated to reflect this.
     *
     * TODO Should not be necessary but needs to be in place as a fail-safe due to mysterious issues with saving.
     */
    private FileResource ensureStorageStatus( FileResource fileResource )
    {
        if ( FileResourceStorageStatus.PENDING == fileResource.getStorageStatus() )
        {
            Duration pendingDuration = new Duration( new DateTime( fileResource.getLastUpdated() ), DateTime.now() );

            if ( pendingDuration.isLongerThan( LONG_STORAGE_DURATION_TIME_DELTA ) )
            {
                // Upload has been running for 2+ minutes and is still PENDING.
                // Check if content has actually been stored and correct to STORED if this is the case.

                boolean contentIsStored = fileResourceContentStore.fileResourceContentExists( fileResource.getStorageKey() );

                if ( contentIsStored )
                {
                    // Status is PENDING but content is actually stored. Fix it.
                    fileResource.setStorageStatus( FileResourceStorageStatus.STORED );
                    fileResourceStore.update( fileResource );
                    log.warn( "Corrected issue: File resource '" + fileResource.getUid() +
                        "' had storageStatus PENDING but content was fully stored." );
                }
            }
        }

        return fileResource;
    }
}

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
import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.system.scheduling.Scheduler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * @author Halvdan Hoem Grelland
 */
public class DefaultFileResourceService
    implements FileResourceService
{
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

    // -------------------------------------------------------------------------
    // FileResourceService implementation
    // -------------------------------------------------------------------------

    @Override
    public FileResource getFileResource( String uid )
    {
        return fileResourceStore.getByUid( uid );
    }

    @Override
    public List<FileResource> getFileResources( List<String> uids )
    {
        return fileResourceStore.getByUid( uids );
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
}

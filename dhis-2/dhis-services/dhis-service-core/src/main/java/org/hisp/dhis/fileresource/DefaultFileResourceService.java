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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.io.ByteSource;

/**
 * @author Halvdan Hoem Grelland
 */
public class DefaultFileResourceService
    implements FileResourceService
{
    private static final Log log = LogFactory.getLog( DefaultFileResourceService.class );

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

    // -------------------------------------------------------------------------
    // FileResourceService implementation
    // -------------------------------------------------------------------------

    @Override
    public FileResource getFileResource( String uid )
    {
        return fileResourceStore.getByUid( uid );
    }

    @Transactional
    @Override
    public String saveFileResource( FileResource fileResource, ByteSource content )
    {
        String storageKey = getRelativeStorageKey( fileResource );

        String key = fileResourceContentStore.saveFileResourceContent(
            storageKey, content, fileResource.getContentLength(), fileResource.getContentMD5() );

        if ( key == null )
        {
            log.debug( "Failed saving content for FileResource" );
            return null;
        }

        int id = fileResourceStore.save( fileResource );

        if ( id <= 0 )
        {
            log.debug( "Failed persisting the FileResource: " + fileResource.getName() );
            return null;
        }

        return fileResource.getUid();
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

        fileResourceContentStore.deleteFileResourceContent( getRelativeStorageKey( fileResource ) );
        fileResourceStore.delete( fileResource );
    }

    @Override
    public ByteSource getFileResourceContent( FileResource fileResource )
    {
        return fileResourceContentStore.getFileResourceContent( getRelativeStorageKey( fileResource ) );
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

    // ---------------------------------------------------------------------
    // Supportive methods
    // ---------------------------------------------------------------------

    private String getRelativeStorageKey( FileResource fileResource )
    {
        return StringUtils.prependIfMissing( fileResource.getStorageKey(), fileResource.getDomain().getContainerName() + "/" );
    }
}

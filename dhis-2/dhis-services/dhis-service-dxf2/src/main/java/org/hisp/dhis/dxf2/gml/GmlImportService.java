package org.hisp.dhis.dxf2.gml;

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

import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.metadata.MetaData;
import org.hisp.dhis.scheduling.TaskId;

import java.io.InputStream;

/**
 * Handles the transformation, sanitation and merging of geospatial
 * data for OrganisationUnits through processing and importing GML files.
 *
 * @author Halvdan Hoem Grelland
 */
public interface GmlImportService
{
    String ID = GmlImportService.class.getName();

    /**
     * Pre-process a GML document. The process, in short, entails the following:
     * <ol>
     *     <li>Parse the GML payload and transform it into DXF2 format</li>
     *     <li>Get the given identifiers (uid, code or name) from the parsed payload and fetch
     *     the corresponding entities from the DB</li>
     *     <li>Merge the geospatial data given in the input GML into DB entities and return</li>
     * </ol>
     *
     * The result of this process in returned in a {@link GmlPreProcessingResult} which
     * encapsulates the returned {@link MetaData} object or the exception in cause of parse
     * failure due to IO errors or malformed input.
     *
     * @param gmlInputStream the InputStream providing the GML input.
     * @return a GmlPreProcessingResult representing the end result of the process.
     */
    GmlPreProcessingResult preProcessGml( InputStream gmlInputStream );

    /**
     * Imports a MetaData object containing geospatial updates.
     * The MetaData should be retrieved using {@link #preProcessGml(InputStream)}.
     *
     * @param metaData the MetaData reflecting the geospatial updates.
     * @param userUid the UID of the user performing the import (task owner).
     * @param importOptions the ImportOptions for the MetaData importer.
     * @param taskId the TaskId of the process.
     */
    void importGml( MetaData metaData, String userUid, ImportOptions importOptions, TaskId taskId );
}

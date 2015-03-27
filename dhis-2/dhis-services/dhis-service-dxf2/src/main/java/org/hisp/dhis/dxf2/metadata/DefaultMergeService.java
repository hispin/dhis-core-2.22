package org.hisp.dhis.dxf2.metadata;

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

import java.util.Collection;

import org.hisp.dhis.common.MergeStrategy;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultMergeService implements MergeService
{
    @Autowired
    private SchemaService schemaService;

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> void merge( T source, T target, MergeStrategy mergeStrategy )
    {
        if ( source == null || target == null )
        {
            return;
        }

        Schema schema = schemaService.getDynamicSchema( source.getClass() );

        for ( Property property : schema.getProperties() )
        {
            if ( property.isCollection() )
            {
                Collection<?> sourceObject = ReflectionUtils.invokeMethod( source, property.getGetterMethod() );

                if ( sourceObject == null )
                {
                    continue;
                }

                sourceObject.clear();

                if ( property.isOwner() )
                {
                    if ( property.isManyToMany() )
                    {
                        Collection targetObject = ReflectionUtils.invokeMethod( target, property.getGetterMethod() );
                        sourceObject.addAll( targetObject );
                    }
                    else
                    {
                        // one-to-many
                    }
                }

                ReflectionUtils.invokeMethod( source, property.getSetterMethod(), sourceObject );
            }
            else
            {
                Object targetObject = ReflectionUtils.invokeMethod( target, property.getGetterMethod() );

                if ( mergeStrategy.isReplace() )
                {
                    ReflectionUtils.invokeMethod( source, property.getSetterMethod(), targetObject );
                }
                else if ( mergeStrategy.isMerge() && targetObject != null )
                {
                    ReflectionUtils.invokeMethod( source, property.getSetterMethod(), targetObject );
                }
            }
        }
    }
}

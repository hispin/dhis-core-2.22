package org.hisp.dhis.dxf2.objectfilter;

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

import com.google.common.collect.Lists;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.dxf2.objectfilter.ops.Op;
import org.hisp.dhis.dxf2.objectfilter.ops.OpStatus;
import org.hisp.dhis.dxf2.parser.ParserService;
import org.hisp.dhis.query.Order;
import org.hisp.dhis.query.Query;
import org.hisp.dhis.query.QueryService;
import org.hisp.dhis.query.Restriction;
import org.hisp.dhis.query.Restrictions;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultObjectFilterService implements ObjectFilterService
{
    @Autowired
    private ParserService parserService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private QueryService queryService;

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> query( Class<? extends IdentifiableObject> klass, List<String> filters, int first, int max )
    {
        Schema schema = schemaService.getDynamicSchema( klass );
        Query query = Query.from( schema );
        query.setFirstResult( first ).setMaxResults( max );
        query.add( getRestrictions( schema, filters ) );

        if ( schema.haveProperty( "name" ) && schema.getProperty( "name" ).isPersisted() )
        {
            query.addOrder( Order.asc( schema.getProperty( "name" ) ) );
        }
        else
        {
            query.addOrder( Order.desc( schema.getProperty( "created" ) ) );
        }

        return (List<T>) queryService.query( query ).getItems();
    }

    private List<Restriction> getRestrictions( Schema schema, List<String> filters )
    {
        List<Restriction> restrictions = new ArrayList<>();
        List<String> candidates = getRestrictionCandidates( schema, filters );

        if ( candidates.isEmpty() )
        {
            return restrictions;
        }

        for ( String candidate : candidates )
        {
            restrictions.add( getRestriction( schema, candidate ) );
        }

        return restrictions;
    }

    private List<String> getRestrictionCandidates( Schema schema, List<String> filters )
    {
        List<String> candidates = new ArrayList<>();

        Iterator<String> iterator = filters.iterator();

        while ( iterator.hasNext() )
        {
            String candidate = iterator.next();

            if ( !candidate.contains( "." ) && getRestriction( schema, candidate ) != null )
            {
                candidates.add( candidate );
                iterator.remove();
            }
        }

        return candidates;
    }

    private Restriction getRestriction( Schema schema, String filter )
    {
        if ( filter == null )
        {
            return null;
        }

        String[] split = filter.split( ":" );

        if ( split.length != 3 )
        {
            return null;
        }

        Property property = schema.getProperty( split[0] );

        if ( property == null || !property.isPersisted() || !property.isSimple() )
        {
            return null;
        }

        switch ( split[1] )
        {
            case "eq":
            {
                return Restrictions.eq( split[0], split[2] );
            }
            case "neq":
            {
                return Restrictions.ne( split[0], split[2] );
            }
            case "gt":
            {
                return Restrictions.gt( split[0], split[2] );
            }
            case "lt":
            {
                return Restrictions.lt( split[0], split[2] );
            }
            case "gte":
            {
                return Restrictions.ge( split[0], split[2] );
            }
            case "lte":
            {
                return Restrictions.le( split[0], split[2] );
            }
            case "like":
            {
                return Restrictions.like( split[0], "%" + split[2] + "%" );
            }
        }

        return null;
    }

    @Override
    public <T> List<T> filter( List<T> objects, List<String> filters )
    {
        if ( objects == null || objects.isEmpty() )
        {
            return Lists.newArrayList();
        }

        Filters parsed = parserService.parseObjectFilter( filters );

        List<T> list = Lists.newArrayList();

        for ( T object : objects )
        {
            if ( evaluateWithFilters( object, parsed ) )
            {
                list.add( object );
            }
        }

        return list;
    }

    @SuppressWarnings( "unchecked" )
    private <T> boolean evaluateWithFilters( T object, Filters filters )
    {
        if ( object == null )
        {
            return false;
        }

        Schema schema = schemaService.getDynamicSchema( object.getClass() );

        for ( String field : filters.getFilters().keySet() )
        {
            if ( !schema.getPropertyMap().containsKey( field ) )
            {
                continue;
            }

            Property descriptor = schema.getPropertyMap().get( field );

            if ( descriptor == null )
            {
                continue;
            }

            Object value = ReflectionUtils.invokeMethod( object, descriptor.getGetterMethod() );

            Object filter = filters.getFilters().get( field );

            if ( FilterOps.class.isInstance( filter ) )
            {
                if ( evaluateFilterOps( value, (FilterOps) filter ) )
                {
                    return false;
                }
            }
            else
            {
                Map<String, Object> map = (Map<String, Object>) filters.getFilters().get( field );
                Filters f = new Filters();
                f.setFilters( map );

                if ( map.containsKey( "__self__" ) )
                {
                    if ( evaluateFilterOps( value, (FilterOps) map.get( "__self__" ) ) )
                    {
                        return false;
                    }

                    map.remove( "__self__" );
                }

                if ( !descriptor.isCollection() )
                {
                    if ( !evaluateWithFilters( value, f ) )
                    {
                        return false;
                    }
                }
                else
                {
                    Collection<?> objectCollection = (Collection<?>) value;

                    if ( objectCollection.isEmpty() )
                    {
                        return false;
                    }

                    boolean include = false;

                    for ( Object idObject : objectCollection )
                    {
                        if ( evaluateWithFilters( idObject, f ) )
                        {
                            include = true;
                        }
                    }

                    if ( !include )
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Filters through every operator treating multiple of same operator as OR.
     */
    private boolean evaluateFilterOps( Object value, FilterOps filterOps )
    {
        for ( String operator : filterOps.getFilters().keySet() )
        {
            boolean include = false;

            List<Op> ops = filterOps.getFilters().get( operator );

            for ( Op op : ops )
            {
                OpStatus status = op.evaluate( value );

                if ( OpStatus.INCLUDE.equals( status ) )
                {
                    include = true;
                }
            }

            if ( !include )
            {
                return true;
            }
        }

        return false;
    }
}

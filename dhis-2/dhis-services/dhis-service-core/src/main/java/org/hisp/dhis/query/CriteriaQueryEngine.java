package org.hisp.dhis.query;

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

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class CriteriaQueryEngine implements QueryEngine
{
    @Autowired
    private final List<HibernateGenericStore> hibernateGenericStores = new ArrayList<>();

    private final Map<Class<?>, HibernateGenericStore> stores = new HashMap<>();

    @PostConstruct
    public void init()
    {
        for ( HibernateGenericStore store : hibernateGenericStores )
        {
            stores.put( store.getClazz(), store );
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public List<? extends IdentifiableObject> query( Query query )
    {
        Schema schema = query.getSchema();

        if ( schema == null )
        {
            return new ArrayList<>();
        }

        HibernateGenericStore store = stores.get( schema.getKlass() );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        Criteria criteria = buildCriteria( store.getSharingCriteria(), query );

        if ( criteria == null )
        {
            return new ArrayList<>();
        }

        return criteria.list();
    }

    private Criteria buildCriteria( Criteria criteria, Query query )
    {
        if ( query.getFirstResult() != null )
        {
            criteria.setFirstResult( query.getFirstResult() );
        }

        if ( query.getMaxResults() != null )
        {
            criteria.setMaxResults( query.getMaxResults() );
        }

        for ( Restriction restriction : query.getRestrictions() )
        {
            criteria.add( getHibernateCriterion( query.getSchema(), restriction ) );
        }

        for ( Order order : query.getOrders() )
        {
            criteria.addOrder( getHibernateOrder( order ) );
        }

        return criteria;
    }

    private Criterion getHibernateCriterion( Schema schema, Restriction restriction )
    {
        if ( restriction == null || restriction.getOperator() == null )
        {
            return null;
        }

        Property property = schema.getProperty( restriction.getPath() );

        switch ( restriction.getOperator() )
        {
            case EQ:
            {
                return Restrictions.eq( property.getFieldName(), restriction.getParameter( 0 ) );
            }
            case NE:
            {
                return Restrictions.ne( property.getFieldName(), restriction.getParameter( 0 ) );
            }
            case GT:
            {
                return Restrictions.gt( property.getFieldName(), restriction.getParameter( 0 ) );
            }
            case LT:
            {
                return Restrictions.lt( property.getFieldName(), restriction.getParameter( 0 ) );
            }
            case GE:
            {
                return Restrictions.ge( property.getFieldName(), restriction.getParameter( 0 ) );
            }
            case LE:
            {
                return Restrictions.le( property.getFieldName(), restriction.getParameter( 0 ) );
            }
            case BETWEEN:
            {
                return Restrictions.between( property.getFieldName(), restriction.getParameter( 0 ), restriction.getParameter( 1 ) );
            }
            case LIKE:
            {
                return Restrictions.like( property.getFieldName(), restriction.getParameter( 0 ) );
            }
            case IN:
            {
                return Restrictions.in( property.getFieldName(), restriction.getParameters() );
            }
        }

        return null;
    }

    public org.hibernate.criterion.Order getHibernateOrder( Order order )
    {
        if ( order == null || order.getProperty() == null || !order.getProperty().isPersisted() || !order.getProperty().isSimple() )
        {
            return null;
        }

        org.hibernate.criterion.Order criteriaOrder;

        if ( order.isAscending() )
        {
            criteriaOrder = org.hibernate.criterion.Order.asc( order.getProperty().getFieldName() );
        }
        else
        {
            criteriaOrder = org.hibernate.criterion.Order.desc( order.getProperty().getFieldName() );
        }

        if ( order.isIgnoreCase() )
        {
            criteriaOrder.ignoreCase();
        }

        return criteriaOrder;
    }
}

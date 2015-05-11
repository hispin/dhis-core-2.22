package org.hisp.dhis.security.bpp;

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

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.security.access.annotation.Jsr250MethodSecurityMetadataSource;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import javax.servlet.ServletException;

/**
 * From spring security 4.x, all roles are automatically pre-pended with ROLE_, we need to remove
 * this to probably support our authorities (ALL vs ROLE_ALL etc)
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultRolesPrefixPostProcessor implements BeanPostProcessor, PriorityOrdered
{
    @Override
    public Object postProcessAfterInitialization( Object bean, String beanName )
        throws BeansException
    {
        // remove this if you are not using JSR-250
        if ( bean instanceof Jsr250MethodSecurityMetadataSource )
        {
            ((Jsr250MethodSecurityMetadataSource) bean).setDefaultRolePrefix( null );
        }

        if ( bean instanceof DefaultMethodSecurityExpressionHandler )
        {
            ((DefaultMethodSecurityExpressionHandler) bean).setDefaultRolePrefix( null );
        }

        if ( bean instanceof DefaultWebSecurityExpressionHandler )
        {
            ((DefaultWebSecurityExpressionHandler) bean).setDefaultRolePrefix( null );
        }

        if ( bean instanceof SecurityContextHolderAwareRequestFilter )
        {
            SecurityContextHolderAwareRequestFilter filter = (SecurityContextHolderAwareRequestFilter) bean;
            filter.setRolePrefix( "" );

            try
            {
                filter.afterPropertiesSet();
            }
            catch ( ServletException e )
            {
                throw new FatalBeanException( e.getMessage(), e );
            }
        }

        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization( Object bean, String beanName )
        throws BeansException
    {
        return bean;
    }

    @Override
    public int getOrder()
    {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }
}

package org.hisp.dhis.common.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.MergeStrategy;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;

/**
 * @author Halvdan Hoem Grelland
 */
public class CacheableBaseIdentifiableObject
    extends BaseIdentifiableObject
    implements Cacheable
{
    public static final CacheStrategy DEFAULT_CACHE_STRATEGY = CacheStrategy.RESPECT_SYSTEM_SETTING;

    private CacheStrategy cacheStrategy = CacheStrategy.RESPECT_SYSTEM_SETTING;

    public void setCacheStrategy( CacheStrategy cacheStrategy )
    {
        this.cacheStrategy = cacheStrategy;
    }

    @Override
    public void mergeWith( IdentifiableObject other, MergeStrategy strategy )
    {
        super.mergeWith( other, strategy );

        if ( other.getClass().isInstance( this ) )
        {
            Cacheable cacheable = (Cacheable) other;

            if ( strategy.isReplace() )
            {
                cacheStrategy = cacheable.getCacheStrategy();
            }
            else if ( strategy.isMerge() )
            {
                cacheStrategy = cacheable.getCacheStrategy() == null ? cacheStrategy : cacheable.getCacheStrategy();
            }
        }
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    @Override
    public CacheStrategy getCacheStrategy()
    {
        return cacheStrategy;
    }
}

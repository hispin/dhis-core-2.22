package org.hisp.dhis.dataelement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hisp.dhis.common.BaseLinkableObject;
import org.hisp.dhis.common.Dxf2Namespace;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
@XmlRootElement( name = "categoryOptions", namespace = Dxf2Namespace.NAMESPACE )
@XmlAccessorType( value = XmlAccessType.NONE )
public class DataElementCategoryOptions extends BaseLinkableObject
{
    private List<DataElementCategoryOption> categoryOptions = new ArrayList<DataElementCategoryOption>();

    @XmlElement( name = "categoryOption" )
    @JsonProperty( value = "categoryOptions" )
    public List<DataElementCategoryOption> getCategoryOptions()
    {
        return categoryOptions;
    }

    public void setCategoryOptions( List<DataElementCategoryOption> categoryOptions )
    {
        this.categoryOptions = categoryOptions;
    }
}

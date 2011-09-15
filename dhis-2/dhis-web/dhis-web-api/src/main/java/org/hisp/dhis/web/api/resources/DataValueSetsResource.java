package org.hisp.dhis.web.api.resources;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.hisp.dhis.importexport.dxf2.model.DataValueSet;
import org.hisp.dhis.importexport.dxf2.service.DataValueSetService;
import org.springframework.beans.factory.annotation.Required;

@Path( "dataValueSets" )
public class DataValueSetsResource
{
    private DataValueSetService dataValueSetService;

    private VelocityManager velocityManager;

    @Context
    private UriInfo uriInfo;
    
    @GET
    @Produces( MediaType.TEXT_HTML )
    public String getDescription()
    {
        URI uri = uriInfo.getBaseUriBuilder().path( DataSetsResource.class ).build( );
        return velocityManager.render( uri, "dataValueSets" );
    }
     
    @POST
    @Consumes( MediaType.APPLICATION_XML )
    public void storeDataValueSet( DataValueSet dataValueSet )
    {
        dataValueSetService.saveDataValueSet( dataValueSet );
    }

    @Required
    public void setDataValueSetService( DataValueSetService dataValueSetService )
    {
        this.dataValueSetService = dataValueSetService;
    }

    @Required
    public void setVelocityManager( VelocityManager velocityManager )
    {
        this.velocityManager = velocityManager;
    }
}

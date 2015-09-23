package org.hisp.dhis.webapi.controller;

import org.apache.commons.io.IOUtils;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.external.location.LocationManager;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.webapi.utils.WebMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Serves and uploads custom images(PNG) for the logo on the frontpage (logo_front)
 * and for the logo on the top banner (logo_banner)
 * Created by Stian Sandvold on 10.09.2015.
 */
@Controller
@RequestMapping( "/staticContent" )
public class StaticContentController
{

    @Autowired
    private LocationManager locationManager;

    @Autowired
    private SystemSettingManager systemSettingManager;

    private HashMap<String, String> keyWhitelist = new HashMap<>();

    public StaticContentController()
    {
        // Add the allowed keys into the whitelist
        this.keyWhitelist.put( "logo_banner", SystemSettingManager.KEY_USE_CUSTOM_LOGO_BANNER );
        this.keyWhitelist.put( "logo_front", SystemSettingManager.KEY_USE_CUSTOM_LOGO_FRONT );
    }

    /**
     * Serves a png  associated with the key. if custom logo is not used, the request will redirect to the default
     * logos.
     *
     * @param key      key associated with the file\image
     * @param response the response associated with the request
     * @throws WebMessageException
     */
    @RequestMapping( value = "/{key}", method = RequestMethod.GET )
    public void getStaticContent(
        @PathVariable( "key" ) String key,
        HttpServletResponse response )
        throws WebMessageException
    {
        // Only keys in the whitelist is accepted at the current time.
        if ( !keyWhitelist.containsKey( key ) )
        {
            throw new WebMessageException( WebMessageUtils.badRequest( "This key is not yet supported" ) );
        }

        String useCustomFile = (String) systemSettingManager.getSystemSetting( keyWhitelist.get( key ) );

        if ( useCustomFile != null ) // Serve the default logos
        {
            try
            {
                response.sendRedirect( this.getDefaultLogoUrl( key ) );
            }
            catch ( IOException e )
            {
                throw new WebMessageException( WebMessageUtils.error( "Can't read the file." ) );
            }
        }
        else // Serve the custom logos
        {
            InputStream in = null;

            try
            {
                in = locationManager.getInputStream( key + ".png", "static" );
                response.setContentType( "image/png" );
                IOUtils.copy( in, response.getOutputStream() );
            }
            catch ( Exception e )
            {
                throw new WebMessageException(
                    WebMessageUtils.notFound( "The requested file could not be found" ) );
            }
            finally
            {
                IOUtils.closeQuietly( in );
            }
        }
    }

    /**
     * Uploads pngs based on a key. only accepts png and whitelisted keys
     *
     * @param key  to associate with the image
     * @param file associated with the key
     * @throws WebMessageException
     * @throws IOException
     */
    @RequestMapping( value = "/{key}", method = RequestMethod.POST )
    public void updateStaticContent(
        @PathVariable( "key" ) String key,
        @RequestParam( value = "file", required = false ) MultipartFile file
    )
        throws WebMessageException, IOException
    {
        if(file == null || file.isEmpty()) {
            throw new WebMessageException( WebMessageUtils.badRequest( "Missing parameter \"file\"" ) );
        }

        // Only PNG is accepted at the current time.
        if ( !file.getContentType().equalsIgnoreCase( "image/png" ) )
        {
            throw new WebMessageException(
                WebMessageUtils.badRequest( "This media format is not yet supported" ) );
        }

        // Only keys in the whitelist is accepted at the current time.
        if ( !keyWhitelist.containsKey( key ) )
        {
            throw new WebMessageException(
                WebMessageUtils.badRequest( "This key is not yet supported" ) );
        }

        File out = locationManager.getFileForWriting( key + ".png", "static" );

        try
        {
            file.transferTo( out );
        }
        catch ( IOException e )
        {
            throw new WebMessageException( (WebMessageUtils
                .error( "Error saving file. Make sure dhis_home envoirement variable is set." )) );
        }

    }

    /**
     * returns the relative url of the default logo for a given key.
     *
     * @param key the key associated with the logo
     * @return the relative url of the logo
     */
    private String getDefaultLogoUrl( String key )
    {
        String relativeUrlToImage = null;
        if ( key.equals( "logo_banner" ) )
        {
            relativeUrlToImage = "/dhis-web-commons/css/light_blue/logo_banner.png";
        }

        if ( key.equals( "logo_front" ) )
        {
            relativeUrlToImage = "/dhis-web-commons/flags/" + systemSettingManager.getFlagImage();
        }

        return relativeUrlToImage;
    }
}

package org.hisp.dhis.sms.config;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;

public class SmscountryGateWay
    extends AGateway
{
    private static final Log log = LogFactory.getLog( SmscountryGateWay.class );
    
    private static final String SENDER = "sender";

    private static final String RECIPIENT = "recipient";

    private static final String MESSAGE = "message";

    private Map<String, String> parameters;

    private String urlTemplate;

    public SmscountryGateWay( String id, String urlTemplate, Map<String, String> parameters )
    {
        super( id );
        this.urlTemplate = urlTemplate;
        this.parameters = parameters;
        
        setAttributes( AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM
                | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.FLASHSMS );
    }
    @Override
    public void startGateway()
        throws TimeoutException, GatewayException, IOException, InterruptedException
    {
        log.debug( "Starting gateway. " + getGatewayId() );
        super.startGateway();
    }

    @Override
    public void stopGateway()
        throws TimeoutException, GatewayException, IOException, InterruptedException
    {
        log.debug( "Stopping gateway. " + getGatewayId() );
        super.stopGateway();
    }

    @Override
    public boolean sendMessage( OutboundMessage msg )
        throws TimeoutException, GatewayException, IOException, InterruptedException
    {

        System.out.println("-------------------------------");

    	String postData=""; 
    	String retval = ""; 
    	//give all Parameters In String 
    	String User =parameters.get("username"); 
    	String passwd = parameters.get("password");
    	String mobilenumber = msg.getRecipient(); 
    	String message = msg.getText(); 
    	String sid = ""; 
    	String mtype = "N"; 
    	String DR = "Y"; 
    	postData += "User=" + URLEncoder.encode(User,"UTF-8") + "&passwd=" + passwd + "&mobilenumber=" + mobilenumber + "&message=" + URLEncoder.encode(message,"UTF-8") + "&sid=" + sid + "&mtype=" + mtype + "&DR=" + DR; 
    	URL url = new URL(urlTemplate);
        log.info( "RequestURL: " + postData + " " + getGatewayId() );

    	try{
    	HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
    	// If You Are Behind The Proxy Server Set IP And PORT else Comment Below 4 Lines
    	//Properties sysProps = System.getProperties();
    	//sysProps.put("proxySet", "true");
    	//sysProps.put("proxyHost", "Proxy Ip");
    	//sysProps.put("proxyPort", "PORT");
    	urlconnection.setRequestMethod("POST");
    	urlconnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
    	urlconnection.setDoOutput(true);
    	OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
    	out.write(postData);
    	out.close();
    	BufferedReader in = new BufferedReader( new InputStreamReader(urlconnection.getInputStream()));
    	String decodedString;
    	while ((decodedString = in.readLine()) != null) {
    	retval += decodedString;
    	}
    	in.close();
    	 if ( urlconnection.getResponseCode() != HttpURLConnection.HTTP_OK )
         {
             log.warn( "Couldn't send message, got response " + retval + " " + getGatewayId() );
             return false;
         }
    	}catch(IOException e){
    		 log.warn( "Couldn't send message " + getGatewayId() );
             return false;
    	}
    	
    	System.out.println(retval);
    	
    	return true;
      }
    

    @Override
    public int getQueueSchedulingInterval()
    {
        return 0;
    }
}

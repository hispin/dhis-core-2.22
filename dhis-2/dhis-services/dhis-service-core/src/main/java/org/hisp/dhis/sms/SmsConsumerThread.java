package org.hisp.dhis.sms;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.sms.incoming.IncomingSms;
import org.hisp.dhis.sms.incoming.IncomingSmsListener;
import org.hisp.dhis.sms.incoming.SmsMessageStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class SmsConsumerThread
{
    private static final Log log = LogFactory.getLog( SmsConsumerThread.class );

    private List<IncomingSmsListener> listeners;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private SmsSender smsSender;

    public SmsConsumerThread()
    {
    }

    public void spawnSmsConsumer()
    {
        IncomingSms message = messageQueue.get();

        while ( message != null )
        {
            log.info( "Received SMS: " + message.getText() );
            try
            {
                for ( IncomingSmsListener listener : listeners )
                {
                    if ( listener.accept( message ) )
                    {
                        listener.receive( message );
                        messageQueue.remove( message );
                        return;
                    }
                }

                smsSender.sendMessage( "No command found", message.getOriginator() );
                message.setStatus( SmsMessageStatus.UNHANDLED );
            }
            catch ( Exception e )
            {
                log.error( e );
                e.printStackTrace();
                smsSender.sendMessage( e.getMessage(), message.getOriginator() );
                message.setStatus( SmsMessageStatus.FAILED );
            }
            finally
            {
                messageQueue.remove( message );
                message = messageQueue.get();
            }
        }
    }

    @Autowired
    public void setListeners( List<IncomingSmsListener> listeners )
    {
        this.listeners = listeners;
        log.info( "Listeners registered are " + listeners );
    }

}

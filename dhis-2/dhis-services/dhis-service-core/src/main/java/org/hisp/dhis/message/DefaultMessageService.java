package org.hisp.dhis.message;

/*
 * Copyright (c) 2004-2010, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserGroup;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 */
@Transactional
public class DefaultMessageService
    implements MessageService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private MessageConversationStore messageConversationStore;

    public void setMessageConversationStore( MessageConversationStore messageConversationStore )
    {
        this.messageConversationStore = messageConversationStore;
    }

    private CurrentUserService currentUserService;
    
    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }
    
    private ConfigurationService configurationService;

    public void setConfigurationService( ConfigurationService configurationService )
    {
        this.configurationService = configurationService;
    }

    // -------------------------------------------------------------------------
    // MessageService implementation
    // -------------------------------------------------------------------------

    public int sendMessage( String subject, String text, Set<User> users )
    {
        // ---------------------------------------------------------------------
        // Add feedback recipients to users if they are not there
        // ---------------------------------------------------------------------

        UserGroup userGroup = configurationService.getConfiguration().getFeedbackRecipients();

        if ( userGroup != null && userGroup.getMembers().size() > 0 )
        {
            users.addAll( userGroup.getMembers() );
        }

        // ---------------------------------------------------------------------
        // Instantiate message, content and user messages
        // ---------------------------------------------------------------------

        User sender = currentUserService.getCurrentUser();
        
        MessageConversation conversation = new MessageConversation( subject, sender );
        
        conversation.addMessage( new Message( text, sender ) );
        
        for ( User user : users )
        {
            conversation.addUserMessage( new UserMessage( user ) );        
        }
        
        return saveMessageConversation( conversation );
    }

    public int sendFeedback( String subject, String text )
    {
        return sendMessage( subject, text, new HashSet<User>() );
    }
    
    public void sendReply( MessageConversation conversation, String text )
    {
        User sender = currentUserService.getCurrentUser();
        
        Message message = new Message( text, sender );
        
        conversation.markReplied( sender, message );
        
        updateMessageConversation( conversation );        
    }
        
    public int saveMessageConversation( MessageConversation conversation )
    {
        return messageConversationStore.save( conversation );
    }
    
    public void updateMessageConversation( MessageConversation conversation )
    {
        messageConversationStore.update( conversation );
    }
    
    public MessageConversation getMessageConversation( int id )
    {
        return messageConversationStore.get( id );
    }
        
    public long getUnreadMessageConversationCount()
    {
        return messageConversationStore.getUnreadUserMessageConversationCount( currentUserService.getCurrentUser() );
    }
    
    public long getUnreadMessageConversationCount( User user )
    {
        return messageConversationStore.getUnreadUserMessageConversationCount( user );
    }
    
    public List<MessageConversation> getMessageConversations( int first, int max )
    {
        return messageConversationStore.getMessageConversations( currentUserService.getCurrentUser(), first, max );
    }
}

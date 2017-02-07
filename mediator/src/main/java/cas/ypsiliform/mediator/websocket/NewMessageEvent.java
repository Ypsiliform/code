/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.mediator.websocket;

import javax.websocket.Session;

import cas.ypsiliform.messages.AbstractMessage;

public class NewMessageEvent
{

    private Session session;
    private AbstractMessage message;

    public NewMessageEvent(Session session, AbstractMessage message)
    {
        this.session = session;
        this.message = message;
    }

    public Session getSession()
    {
        return session;
    }

    public AbstractMessage getMessage()
    {
        return message;
    }

}

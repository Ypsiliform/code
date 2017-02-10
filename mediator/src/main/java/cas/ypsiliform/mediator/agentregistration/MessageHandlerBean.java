/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.mediator.agentregistration;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;

import cas.ypsiliform.mediator.AgentProxy;
import cas.ypsiliform.mediator.websocket.NewMessageEvent;
import cas.ypsiliform.mediator.websocket.SessionRepository;
import cas.ypsiliform.messages.AgentResponse;

@Stateless
@LocalBean
public class MessageHandlerBean
{

    @EJB(lookup = SessionRepository.LOOKUP)
    private SessionRepository repo;

    @Asynchronous
    public void onNewMessage(@Observes NewMessageEvent event)
    {
        if ( event.getMessage() instanceof AgentResponse )
        {
            AgentProxy agentProxy =
                repo.getAgentForSessionId(event.getSession().getId());
            if ( agentProxy != null )
            {
                agentProxy.onNewMessage(event);
            }
        }
    }

}

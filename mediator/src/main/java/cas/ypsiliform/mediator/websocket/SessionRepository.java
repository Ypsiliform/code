package cas.ypsiliform.mediator.websocket;

import cas.ypsiliform.mediator.AgentProxy;

public interface SessionRepository
{

    String LOOKUP =
        "java:global/mediator/AgentRegistrationBean!cas.ypsiliform.mediator.websocket.SessionRepository";

    void removeSession(String id);

    void onNewAgentRegistration(NewMessageEvent event);

    AgentProxy getAgentForSessionId(String id);

}

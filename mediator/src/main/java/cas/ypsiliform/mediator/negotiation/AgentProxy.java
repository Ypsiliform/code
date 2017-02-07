package cas.ypsiliform.mediator.negotiation;

import java.util.ArrayList;
import java.util.stream.Collectors;

import cas.ypsiliform.mediator.agentregistration.AgentData;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.mediator.websocket.NewMessageEvent;
import cas.ypsiliform.messages.AgentResponse;
import cas.ypsiliform.messages.MediatorRequest;

public class AgentProxy
{
    private ArrayList<AgentDeadListener> listeners =
        new ArrayList<AgentDeadListener>();

    private AgentData data;

    private Thenable<AgentResponse> responseAble;

    public AgentProxy(AgentData data)
    {
        this.data = data;
    }

    public AgentData getAgentData()
    {
        return data;
    }

    public boolean isMySessionId(String id)
    {
        return id.equals(data.getSession().getId());
    }

    public void onNewMessage(NewMessageEvent event)
    {
        if ( event.getMessage() instanceof AgentResponse )
        {
            if ( responseAble != null )
            {
                responseAble.resolve((AgentResponse) event.getMessage());
            }
        }
    }

    public void addAgentDeadListener(AgentDeadListener listener)
    {
        listeners.add(listener);
    }

    public void removeAgentDeadListener(AgentDeadListener listener)
    {
        listeners.remove(listener);
    }

    public int getId()
    {
        return data.getId();
    }

    public Integer getParentId()
    {
        return data.getParentAgent() != null ? data.getParentAgent().getId()
                : null;
    }

    public Iterable<Integer> getChildIds()
    {
        return data.getChildAgents()
            .stream()
            .map(AgentData::getId)
            .collect(Collectors.toList());
    }

    public void onAgentRemoved()
    {
        listeners.forEach(listener -> {
            listener.OnAgentDead(this);
        });
    }

    public Thenable<AgentResponse> sendSolutionProposals(MediatorRequest solution)
    {
        responseAble = new Thenable<>();
        data.sendMessage(solution);
        return responseAble;
    }

    public Thenable<Void> endNegotiation(SolutionProposal solution)
    {
        // TODO implementation missing
        throw new UnsupportedOperationException();
    }
}

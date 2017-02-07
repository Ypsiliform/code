package cas.ypsiliform.mediator.negotiation;

import java.util.ArrayList;
import java.util.stream.Collectors;

import cas.ypsiliform.mediator.agentregistration.AgentData;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.messages.AgentResponse;

public class AgentProxy
{
    private ArrayList<AgentDeadListener> listeners =
        new ArrayList<AgentDeadListener>();

    private AgentData data;

    public AgentProxy(AgentData data)
    {
        this.data = data;
    }

    public AgentData getAgentData()
    {
        return data;
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

    public Thenable<AgentResponse> sendSolutionProposal(SolutionProposal solution)
    {
        // TODO implementation missing
        throw new UnsupportedOperationException();
    }

    public Thenable<Void> endNegotiation(SolutionProposal solution)
    {
        // TODO implementation missing
        throw new UnsupportedOperationException();
    }
}

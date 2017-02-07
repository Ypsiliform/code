/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.mediator.agentregistration;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import cas.ypsiliform.messages.AbstractMessage;

public class AgentData
{

    private Session session;

    private int id;
    private String config;
    private List<AgentData> childAgents = new ArrayList<>();
    private AgentData parentAgent;
    private Integer[] initialDemand;

    public AgentData(int id,
                     String config,
                     Integer[] initialDemand,
                     Session session)
    {
        this.id = id;
        this.config = config;
        this.initialDemand = initialDemand;
        this.session = session;
    }

    public Session getSession()
    {
        return session;
    }

    public List<AgentData> getChildAgents()
    {
        return childAgents;
    }

    public void setChildAgents(List<AgentData> childAgents)
    {
        this.childAgents = childAgents;
    }

    public int getId()
    {
        return id;
    }

    public String getConfig()
    {
        return config;
    }

    public Integer[] getInitialDemand()
    {
        return initialDemand;
    }

    public void addChildren(AgentData agentData)
    {
        if ( !childAgents.contains(agentData) )
        {
            childAgents.add(agentData);
            agentData.setParentAgent(this);
        }
    }

    public AgentData getParentAgent()
    {
        return parentAgent;
    }

    public void setParentAgent(AgentData parentAgent)
    {
        this.parentAgent = parentAgent;
    }

    public void sendMessage(AbstractMessage message)
    {
        session.getAsyncRemote().sendObject(message);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((config == null) ? 0 : config.hashCode());
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        AgentData other = (AgentData) obj;
        if ( config == null )
        {
            if ( other.config != null )
                return false;
        }
        else if ( !config.equals(other.config) )
            return false;
        if ( id != other.id )
            return false;
        return true;
    }

}

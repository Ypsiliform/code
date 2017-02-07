/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.mediator.agentregistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;

import cas.ypsiliform.mediator.websocket.NewMessageEvent;
import cas.ypsiliform.mediator.websocket.SessionRepository;
import cas.ypsiliform.messages.AgentRegistration;

@Singleton
@Local(SessionRepository.class)
public class AgentRegistrationBean
    implements SessionRepository
{

    private Map<String, Map<Integer, List<Integer>>> requiresCache =
        new HashMap<>();
    private Map<String, Map<Integer, AgentData>> configAgentMap =
        new HashMap<>();

    @Override
    @Asynchronous
    public void onNewAgentRegistration(@Observes NewMessageEvent event)
    {
        if ( event.getMessage() instanceof AgentRegistration )
        {
            AgentRegistration regMsg = (AgentRegistration) event.getMessage();
            Map<Integer, AgentData> agentMap =
                configAgentMap.get(regMsg.getConfig());
            if ( agentMap == null )
            {
                agentMap = new HashMap<>();
            }
            AgentData agent = new AgentData(regMsg.getId(),
                                            regMsg.getConfig(),
                                            regMsg.getDemand(),
                                            event.getSession());
            agentMap.put(agent.getId(), agent);
            configAgentMap.put(agent.getConfig(), agentMap);

            buildTree(agentMap, agent, regMsg.getRequires());
            checkCache(regMsg.getConfig(), agentMap);

            if ( agentMap.size() == 5 )
            {
                //TODO: trigger Mediator
            }
        }
    }

    private void checkCache(String config, Map<Integer, AgentData> agentMap)
    {
        Map<Integer, List<Integer>> requires = requiresCache.get(config);
        if ( requires != null )
        {
            List<Integer> toRemove = new ArrayList<>();
            requires.entrySet().forEach(entry -> {
                AgentData agentData = agentMap.get(entry.getKey());
                if ( buildTree(agentMap, agentData, entry.getValue()) )
                {
                    toRemove.add(entry.getKey());
                }
            });
            toRemove.forEach(key -> {
                requires.remove(key);
            });
            if ( requires.isEmpty() )
            {
                requiresCache.remove(config);
            }
        }
    }

    private boolean buildTree(Map<Integer, AgentData> agentMap,
                              AgentData agent,
                              List<Integer> requires)
    {
        for ( int id : requires )
        {
            if ( agentMap.containsKey(id) )
            {
                agent.addChildren(agentMap.get(id));
            }
            else
            {
                Map<Integer, List<Integer>> map =
                    requiresCache.get(agent.getConfig());
                if ( map == null )
                {
                    map = new HashMap<>();
                }
                map.put(agent.getId(), requires);
                return false;
            }
        }
        return true;
    }

    @Override
    public void removeSession(String id)
    {
        List<AgentData> toRemove = new ArrayList<>();
        configAgentMap.entrySet().forEach(entry -> {
            List<AgentData> result = entry.getValue()
                .values()
                .stream()
                .filter(p -> p.getSession().getId().equals(id))
                .collect(Collectors.toList());
            toRemove.addAll(result);
        });
        toRemove.forEach(a -> {
            Map<Integer, AgentData> map = configAgentMap.get(a.getConfig());
            map.remove(a.getId());
            if ( map.isEmpty() )
            {
                configAgentMap.remove(a.getConfig());
            }
            //TODO: agent wrapper informieren das Agent gelöscht worden ist
        });
    }

}

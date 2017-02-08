/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.mediator.agentregistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Observes;

import cas.ypsiliform.mediator.Mediator;
import cas.ypsiliform.mediator.negotiation.AgentProxy;
import cas.ypsiliform.mediator.websocket.NewMessageEvent;
import cas.ypsiliform.mediator.websocket.SessionRepository;
import cas.ypsiliform.messages.AgentRegistration;

@Singleton
@Local(SessionRepository.class)
public class AgentRegistrationBean
    implements SessionRepository
{
    private static final Logger LOGGER =
        Logger.getLogger(AgentRegistrationBean.class.getName());

    private Map<String, Map<Integer, List<Integer>>> requiresCache =
        new HashMap<>();
    private Map<String, Map<Integer, AgentProxy>> configAgentMap =
        new HashMap<>();

    @Resource
    private ManagedExecutorService service;

    public AgentRegistrationBean()
    {

    }

    public AgentRegistrationBean(ManagedExecutorService service)
    {
        this.service = service;
    }

    @Override
    @Asynchronous
    public void onNewAgentRegistration(@Observes NewMessageEvent event)
    {
        if ( event.getMessage() instanceof AgentRegistration )
        {
            LOGGER.log(Level.FINE,
                       "new agentregistration msg received : "
                           + event.getMessage());
            AgentRegistration regMsg = (AgentRegistration) event.getMessage();
            Map<Integer, AgentProxy> agentMap =
                configAgentMap.get(regMsg.getConfig());
            if ( agentMap == null )
            {
                agentMap = new HashMap<>();
            }
            AgentData agent = new AgentData(regMsg.getId(),
                                            regMsg.getConfig(),
                                            regMsg.getDemand(),
                                            event.getSession());
            agentMap.put(agent.getId(), new AgentProxy(agent));
            configAgentMap.put(agent.getConfig(), agentMap);
            LOGGER.fine("before build tree and checkCache:" + configAgentMap
                + " cache = " + requiresCache);
            buildTree(agentMap, agent, regMsg.getRequires());
            checkCache(regMsg.getConfig(), agentMap);
            LOGGER.fine("after build tree and checkCache:" + configAgentMap
                + " cache = " + requiresCache);
            if ( agentMap.size() == 5 )
            {
                LOGGER.log(Level.FINE, "5 agent are registered start mediator");
                Integer lowestKey = Collections.min(agentMap.keySet());
                Mediator mediator = new Mediator(agentMap,
                                                 agentMap.get(lowestKey)
                                                     .getAgentData()
                                                     .getInitialDemand());
                service.execute(mediator);
            }
        }
    }

    private void checkCache(String config, Map<Integer, AgentProxy> agentMap)
    {
        Map<Integer, List<Integer>> requires = requiresCache.get(config);
        if ( requires != null )
        {
            List<Integer> toRemove = new ArrayList<>();
            requires.entrySet().forEach(entry -> {
                AgentData agentData =
                    agentMap.get(entry.getKey()).getAgentData();
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

    private boolean buildTree(Map<Integer, AgentProxy> agentMap,
                              AgentData agent,
                              List<Integer> requires)
    {
        for ( int id : requires )
        {
            if ( agentMap.containsKey(id) )
            {
                agent.addChildren(agentMap.get(id).getAgentData());
            }
            else
            {
                Map<Integer, List<Integer>> map =
                    requiresCache.get(agent.getConfig());
                if ( map == null )
                {
                    map = new HashMap<>();
                    requiresCache.put(agent.getConfig(), map);
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
                .map(AgentProxy::getAgentData)
                .filter(p -> p.getSession().getId().equals(id))
                .collect(Collectors.toList());
            toRemove.addAll(result);
        });
        toRemove.forEach(a -> {
            Map<Integer, AgentProxy> map = configAgentMap.get(a.getConfig());
            AgentProxy proxy = map.remove(a.getId());
            if ( map.isEmpty() )
            {
                configAgentMap.remove(a.getConfig());
                requiresCache.remove(a.getConfig());
            }
            if ( proxy != null )
            {
                proxy.onAgentRemoved();
            }
        });
    }

    @Override
    @Lock(LockType.READ)
    public AgentProxy getAgentForSessionId(String id)
    {
        return configAgentMap.values()
            .stream()
            .map(v -> v.values())
            .flatMap(x -> x.stream())
            .filter(agent -> agent.isMySessionId(id))
            .findFirst()
            .orElseGet(null);
    }

    //for testing
    public Map<String, Map<Integer, List<Integer>>> getRequiresCache()
    {
        return requiresCache;
    }

    //for testing
    public Map<String, Map<Integer, AgentProxy>> getConfigAgentMap()
    {
        return configAgentMap;
    }

}

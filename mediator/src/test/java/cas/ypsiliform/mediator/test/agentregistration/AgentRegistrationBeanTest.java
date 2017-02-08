/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.mediator.test.agentregistration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.websocket.Session;

import org.junit.Test;

import cas.ypsiliform.mediator.agentregistration.AgentRegistrationBean;
import cas.ypsiliform.mediator.negotiation.AgentProxy;
import cas.ypsiliform.mediator.websocket.NewMessageEvent;
import cas.ypsiliform.messages.AgentRegistration;

public class AgentRegistrationBeanTest
{

    private AgentRegistrationBean bean =
        new AgentRegistrationBean(new DummyManagedExecutorService());
    private Session session = null;

    @Test
    public void onNewMessage_5AgentREgisterd_BuildCorrectTree()
    {
        NewMessageEvent reg1 = buildAgentRegistration(1, 2);
        NewMessageEvent reg2 = buildAgentRegistration(2, 3);
        NewMessageEvent reg3 = buildAgentRegistration(3, 4);
        NewMessageEvent reg4 = buildAgentRegistration(4, 5);
        NewMessageEvent reg5 = buildAgentRegistration(5);

        bean.onNewAgentRegistration(reg1);
        bean.onNewAgentRegistration(reg3);
        bean.onNewAgentRegistration(reg4);
        bean.onNewAgentRegistration(reg2);
        bean.onNewAgentRegistration(reg5);

        Map<String, Map<Integer, AgentProxy>> configAgentMap =
            bean.getConfigAgentMap();
        Map<Integer, AgentProxy> agentMap = configAgentMap.get("config");

        for ( int i = 1; i <= 5; i++ )
        {
            AgentProxy proxy = agentMap.get(i);
            if ( i < 5 )
            {
                Integer next = proxy.getChildIds().iterator().next();
                org.junit.Assert.assertEquals(i + 1, (int) next);
            }
        }
    }

    private NewMessageEvent buildAgentRegistration(int id, Integer... requires)
    {
        AgentRegistration reg = new AgentRegistration();
        reg.setConfig("config");
        reg.setId(id);
        if ( id == 1 )
        {
            reg.setDemand(new Integer[12]);
        }
        else
        {
            reg.setDemand(new Integer[0]);
        }
        reg.setRequires(Arrays.asList(requires));
        return new NewMessageEvent(session, reg);
    }

    private class DummyManagedExecutorService
        implements ManagedExecutorService
    {

        @Override
        public void shutdown()
        {
            // TODO Auto-generated method stub

        }

        @Override
        public List<Runnable> shutdownNow()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isShutdown()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isTerminated()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Future<?> submit(Runnable task)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                             long timeout,
                                             TimeUnit unit)
            throws InterruptedException
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException,
                ExecutionException
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                               long timeout,
                               TimeUnit unit)
            throws InterruptedException,
                ExecutionException,
                TimeoutException
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void execute(Runnable command)
        {
            // TODO Auto-generated method stub

        }

    }

}

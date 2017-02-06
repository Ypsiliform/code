package cas.ypsiliform.agent.test;

import org.junit.Test;
import cas.ypsiliform.agent.Agent;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by paul on 06.02.17.
 */
public class AgentTest {

    private class AgentTestHelper extends Agent{

        public AgentTestHelper(double setupCost, double storageCost, int productionLimit, ArrayList<Integer> children) {
            super(setupCost,storageCost, productionLimit, children);
        }

        public int[] getProductionArray(int[] demand, boolean[] production_days) {
            return super.getProductionArray(demand, production_days);
        }
    }

    @Test
    public void getProductionArray() throws Exception {
        int expectedProduction[] = {40, 70, 70, 0, 70, 0};
        int demand[] = {0, 0, 100, 100, 50};
        boolean productionDays[] = {true, true, false, true, false};
        AgentTestHelper agent = new AgentTestHelper(10.0,0.5,70,new ArrayList<Integer>(2));
        assertArrayEquals(expectedProduction, agent.getProductionArray(demand, productionDays));
    }

}
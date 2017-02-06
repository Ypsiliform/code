package cas.ypsiliform.agent.test;

import org.junit.Test;
import cas.ypsiliform.agent.Agent;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by paul on 06.02.17.
 */
public class AgentTest {

    //innerclass that makes use of the protected methods to make them public, since only public methods can be tested
    private class AgentTestHelper extends Agent{

        public AgentTestHelper(double setupCost, double storageCost, int productionLimit, ArrayList<Integer> children) {
            super(setupCost,storageCost, productionLimit, children);
        }

        public int[] getProductionArray(int[] demands, boolean[] productionDays) {
            return super.getProductionArray(demands, productionDays);
        }

        public double getInitCosts(int items) {
            return super.getInitCosts(items);
        }

        protected double getProductionCosts(int[] production, int[] demands) {
            return super.getProductionCosts(production, demands);
        }
    }

    @Test
    public void getProductionArray() throws Exception {
        AgentTestHelper agent = new AgentTestHelper(10.0,0.5,70,new ArrayList<Integer>(2));

        //set the init values for testing
        int demands_1[]             = {0, 0, 100, 100, 50};

        boolean productionDays_1[]  = {true, true, false, true, false};
        int expectedProduction_1[]  = {40, 70, 70, 0, 70, 0};

        boolean productionDays_2[]  = {true, true, true, true, true};
        int expectedProduction_2[]  = {0, 0, 60, 70, 70, 50};

        boolean productionDays_3[]  = {true, false, false, false, false};
        int expectedProduction_3[]  = {180, 70, 0, 0, 0, 0};

        boolean productionDays_4[]  = {true, false, false, true, false};
        int expectedProduction_4[]  = {110, 70, 0, 0, 70, 0};

        assertArrayEquals(expectedProduction_1, agent.getProductionArray(demands_1, productionDays_1));
        assertArrayEquals(expectedProduction_2, agent.getProductionArray(demands_1, productionDays_2));
        assertArrayEquals(expectedProduction_3, agent.getProductionArray(demands_1, productionDays_3));
        assertArrayEquals(expectedProduction_4, agent.getProductionArray(demands_1, productionDays_4));
    }

    /*
    @Test
    public void getInitCosts() throws Exception {
        int expectedProduction[] = {40, 70, 70, 0, 70, 0};
        int demand[] = {0, 0, 100, 100, 50};
        boolean productionDays[] = {true, true, false, true, false};
        AgentTestHelper agent = new AgentTestHelper(10.0,0.5,70,new ArrayList<Integer>(2));
        assertArrayEquals(expectedProduction, agent.getProductionArray(demand, productionDays));
    }

    @Test
    public void getProductionCosts() throws Exception {
        int expectedProduction[] = {40, 70, 70, 0, 70, 0};
        int demand[] = {0, 0, 100, 100, 50};
        boolean productionDays[] = {true, true, false, true, false};
        AgentTestHelper agent = new AgentTestHelper(10.0,0.5,70,new ArrayList<Integer>(2));
        assertArrayEquals(expectedProduction, agent.getProductionArray(demand, productionDays));
    }
    */
}
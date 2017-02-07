package cas.ypsiliform.agent.test;

import cas.ypsiliform.messages.AgentResponse;
import cas.ypsiliform.messages.MediatorRequest;
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

        public Integer[] getProductionArray(Integer[] demands, boolean[] productionDays) {
            return super.getProductionArray(demands, productionDays);
        }

        public double getInitCosts(int items) {
            return super.getInitCosts(items);
        }

        public double getProductionCosts(Integer[] production, Integer[] demands) {
            return super.getProductionCosts(production, demands);
        }

        public AgentResponse handleMediatorRequest(MediatorRequest req) {
            return super.handleMediatorRequest(req);
        }
    }

    @Test
    public void getProductionArray() throws Exception {
        AgentTestHelper agent = new AgentTestHelper(10.0,0.5,70,new ArrayList<Integer>(2));

        //set the init values for testing
        Integer demands_1[]             = {0, 0, 100, 100, 50};

        boolean productionDays_1[]  = {true, true, false, true, false};
        Integer expectedProduction_1[]  = {40, 70, 70, 0, 70, 0};

        boolean productionDays_2[]  = {true, true, true, true, true};
        Integer expectedProduction_2[]  = {0, 0, 60, 70, 70, 50};

        boolean productionDays_3[]  = {true, false, false, false, false};
        Integer expectedProduction_3[]  = {180, 70, 0, 0, 0, 0};

        boolean productionDays_4[]  = {true, false, false, true, false};
        Integer expectedProduction_4[]  = {110, 70, 0, 0, 70, 0};

        assertArrayEquals(expectedProduction_1, agent.getProductionArray(demands_1, productionDays_1));
        assertArrayEquals(expectedProduction_2, agent.getProductionArray(demands_1, productionDays_2));
        assertArrayEquals(expectedProduction_3, agent.getProductionArray(demands_1, productionDays_3));
        assertArrayEquals(expectedProduction_4, agent.getProductionArray(demands_1, productionDays_4));
    }


    @Test
    public void getInitCosts() throws Exception {
        double setupCosts = 10;
        double storageCosts = 0.5;
        double expectedCosts = 0;
        AgentTestHelper agent = new AgentTestHelper(setupCosts,storageCosts,70,new ArrayList<Integer>(2));

        //check that  0 is caught
        assertEquals(expectedCosts, agent.getInitCosts(0), 0);

        //easy test, just one day
        expectedCosts = setupCosts + 10*storageCosts;
        assertEquals(expectedCosts, agent.getInitCosts(10), 0);

        //just one day production, result not an int
        expectedCosts = setupCosts + 15 * storageCosts;
        assertEquals(expectedCosts, agent.getInitCosts(15), 0);

        //complex test, 3 days of producing and storing goods
        expectedCosts = 3*setupCosts + 10 * storageCosts + 80 * storageCosts + 150 * storageCosts;
        assertEquals(expectedCosts,agent.getInitCosts(150), 0);
    }


    @Test
    public void getProductionCosts() throws Exception {
        double expectedCosts;
        double setupCosts = 10;
        double storageCosts = 0.5;
        Integer demands[] = {0, 0, 100, 100, 50};
        Integer expectedProduction_1[]  = {40, 70, 70, 0, 70, 0};
        Integer expectedProduction_2[]  = {0, 0, 60, 70, 70, 50};
        Integer expectedProduction_3[]  = {180, 70, 0, 0, 0, 0};
        Integer expectedProduction_4[]  = {110, 70, 0, 0, 70, 0};
        AgentTestHelper agent = new AgentTestHelper(setupCosts,storageCosts,70,new ArrayList<Integer>(2));

        expectedCosts = agent.getInitCosts(40)
                + 3 * setupCosts
                + 110 * storageCosts
                + 180 * storageCosts
                + 80 * storageCosts
                + 50 * storageCosts;
        assertEquals(expectedCosts, agent.getProductionCosts(expectedProduction_1, demands), 0);

        expectedCosts = 4 * setupCosts
                + 60 * storageCosts
                + 30 * storageCosts;
        assertEquals(expectedCosts, agent.getProductionCosts(expectedProduction_2, demands), 0);

        expectedCosts = agent.getInitCosts(180)
                + setupCosts
                + 250 * storageCosts
                + 250 * storageCosts
                + 150 * storageCosts
                + 50 * storageCosts;
        assertEquals(expectedCosts, agent.getProductionCosts(expectedProduction_3, demands), 0);

        expectedCosts = agent.getInitCosts(110)
                + 2 * setupCosts
                + 180 * storageCosts
                + 180 * storageCosts
                + 80 * storageCosts
                + 50 * storageCosts;
        assertEquals(expectedCosts, agent.getProductionCosts(expectedProduction_4, demands), 0);

    }

    /*
    @Test
    public void handleMediatorRequest() {

        //build the Mediatorrequest object
        MediatorRequest req = new MediatorRequest();
        req.setSolutions();

        //build the expected answer
        AgentResponse res = new AgentResponse();
        res
    }
    */
}
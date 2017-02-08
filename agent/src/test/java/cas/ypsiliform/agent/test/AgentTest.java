package cas.ypsiliform.agent.test;

import cas.ypsiliform.messages.AgentResponse;
import cas.ypsiliform.messages.EndNegotiation;
import cas.ypsiliform.messages.MediatorRequest;
import cas.ypsiliform.messages.Solution;
import org.junit.Test;
import cas.ypsiliform.agent.Agent;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by paul on 06.02.17.
 */
public class AgentTest {

    //innerclass that makes use of the protected methods to make them public, since only public methods can be tested
    private class AgentTestHelper extends Agent{

        public AgentTestHelper(int id, double setupCost, double storageCost, int productionLimit) {
            super(id, setupCost,storageCost, productionLimit);
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

        public int handleEndNegotiation(EndNegotiation msg) {
            return super.handleEndNegotiation(msg);
        }
    }

    @Test
    public void getProductionArray() throws Exception {
        AgentTestHelper agent = new AgentTestHelper(1, 10.0,0.5,70);

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
        AgentTestHelper agent = new AgentTestHelper(1, setupCosts,storageCosts,70);

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
        AgentTestHelper agent = new AgentTestHelper(1, setupCosts,storageCosts,70);

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


    @Test
    public void handleMediatorRequest() {
        //create the AgentTestHelper
        double setupCosts = 10;
        double storageCosts = 0.5;
        AgentTestHelper agent = new AgentTestHelper(1, setupCosts,storageCosts,70);

        //define the demands, production restrictions and expected results
        Integer demands[]               = {0, 0, 100, 100, 50};
        boolean productionDays_1[]      = {true, true, false, true, false};
        boolean productionDays_2[]      = {true, true, true, true, true};
        boolean productionDays_3[]      = {true, false, false, false, false};
        boolean productionDays_4[]      = {true, false, false, true, false};
        Integer expectedProduction_1[]  = {40, 70, 70, 0, 70, 0};
        Integer expectedProduction_2[]  = {0, 0, 60, 70, 70, 50};
        Integer expectedProduction_3[]  = {180, 70, 0, 0, 0, 0};
        Integer expectedProduction_4[]  = {110, 70, 0, 0, 70, 0};

        //Create 4 solutions that should be put into the MediatorRequest
        Solution solution_1 = new Solution();
        Solution solution_2 = new Solution();
        Solution solution_3 = new Solution();
        Solution solution_4 = new Solution();

        //fill the solutions and add them
        solution_1.setDemands(demands);
        solution_1.setSolution(productionDays_1);
        solution_2.setDemands(demands);
        solution_2.setSolution(productionDays_2);
        solution_3.setDemands(demands);
        solution_3.setSolution(productionDays_3);
        solution_4.setDemands(demands);
        solution_4.setSolution(productionDays_4);

        Map<Integer, Solution> allSolutions = new HashMap<>();
        allSolutions.put(0, solution_1);
        allSolutions.put(1, solution_2);
        allSolutions.put(2, solution_3);
        allSolutions.put(3, solution_4);

        //build the Mediatorrequest object
        MediatorRequest req = new MediatorRequest();
        req.setSolutions(allSolutions);

        //build the expected answers
        Map<Integer, Integer[]> allNewDemands = new HashMap<>();
        allNewDemands.put(0, expectedProduction_1);
        allNewDemands.put(1, expectedProduction_2);
        allNewDemands.put(2, expectedProduction_3);
        allNewDemands.put(3, expectedProduction_4);

        //calculate the expected costs
        Map<Integer, Double> allCosts = new HashMap<>();
        allCosts.put(0, agent.getProductionCosts(expectedProduction_1, demands));
        allCosts.put(1, agent.getProductionCosts(expectedProduction_2, demands));
        allCosts.put(2, agent.getProductionCosts(expectedProduction_3, demands));
        allCosts.put(3, agent.getProductionCosts(expectedProduction_4, demands));

        //build the response agent
        AgentResponse res = new AgentResponse();
        res.setDemands(allNewDemands);
        res.setCosts(allCosts);
        res.setSelection(1);

        //get the response from the function
        AgentResponse gen_res = agent.handleMediatorRequest(req);

        assertEquals(res, gen_res);
    }


    @Test
    public void handleEndNegotiation(){
        AgentTestHelper agent = new AgentTestHelper(1, 10,0.5,70);
        //create the EndNegotiation message
        EndNegotiation msg = new EndNegotiation();
        Solution solution = new Solution();
        Integer demands[]               = {0, 0, 100, 100, 50};
        boolean productionDays[]        = {true, true, false, true, false};
        solution.setDemands(demands);
        solution.setSolution(productionDays);
        msg.setSolution(solution);

        agent.handleEndNegotiation(msg);

    }

}
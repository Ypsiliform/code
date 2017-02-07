package cas.ypsiliform.agent;

import cas.ypsiliform.agent.websocket.MessageHandler;
import cas.ypsiliform.agent.websocket.WebsocketClient;
import cas.ypsiliform.messages.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by paul on 06.02.17.
 */

public class Agent implements MessageHandler{

    private double setupCost;
    private double storageCost;
    private int productionLimit;
    private ArrayList<Integer> children;
    private WebsocketClient client;

    /**
     * Agent constructor to initialize an agent
     * */
    public Agent(double setupCost, double storageCost, int productionLimit, ArrayList<Integer> children) {
        this.setupCost = setupCost;
        this.storageCost = storageCost;
        this.productionLimit = productionLimit;
        this.children = children;
    }

    public double getSetupCost() {
        return setupCost;
    }

    public void setSetupCost(double setupCost) {
        this.setupCost = setupCost;
    }

    public double getStorageCost() {
        return storageCost;
    }

    public void setStorageCost(double storageCost) {
        this.storageCost = storageCost;
    }

    public ArrayList<Integer> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Integer> children) {
        this.children = children;
    }

    public int getProductionLimit() {
        return productionLimit;
    }

    public void setProductionLimit(int productionLimit) {
        this.productionLimit = productionLimit;
    }

    /**
     * Calculate the costs that arise because of required preproduction.
     * @param items number of items that need to be preproduced
     * @return a double value indicating the created costs by setup and storage costs of the past
     * */
    protected double getInitCosts(int items) {
        double initCosts;
        int productionDays;
        int stored_items;

        if(items == 0) {
            //preproduction is not necessary
            initCosts = 0;
        } else {
            //calculate the number of production days
            productionDays = items / this.productionLimit;
            if(items % this.productionLimit != 0)
                productionDays++;

            //sum up the setup costs
            initCosts = productionDays * setupCost;

            //calculate the costs of storing the products
            stored_items = items % this.productionLimit;
            do {
                initCosts += stored_items * this.storageCost;
                stored_items += this.productionLimit;
            } while(stored_items <= items);
        }

        return initCosts;
    }

    /**
     * Calculates the costs that are created by the created production plan and the demands
     * @param production plan that contains data when and how many items are built
     * @param demands Contains the details of how many items can be retreived in a period
     * */
    protected double getProductionCosts(Integer[] production, Integer[] demands) {
        double costs = getInitCosts(production[0]);
        int itemsInStore = production[0];

        for(int i=0;i<demands.length;i++) {

            //see if on that day there were production costs
            if(production[i+1] > 0)
                costs += this.setupCost;

            //calculate the number of items in store that day and sum up the costs
            itemsInStore += production[i+1];
            itemsInStore -= demands[i];
            costs += itemsInStore * this.storageCost;
        }

        return costs;
    }


    /**
     * Creates the production array that is ideal for the prodvided production days.
     * Assumes that storage costs far outweigh the setupcosts. Otherwise in some cases
     * it would not make sense to produce each day, but instead use the full capacity and
     * store some items.
     * @param demands This array contains the demands per days
     * @param productionDays boolean array that defines on which periods it is allowed to produce something
     * @return and int[] that contains the created mapping of production periods
     * */
    protected Integer[] getProductionArray(Integer[] demands, boolean[] productionDays) {
        Integer production_array[] = new Integer[demands.length + 1];
        Arrays.fill(production_array, new Integer(0));
        int singleDemand;
        int remainingCapacity;

        //iterate through the demands array to calculate the demand
        for (int i=0;i<demands.length;i++) {
            singleDemand = demands[i];
            //iterate through the production array and set the numbers to be produced
            for (int j=i+1;j>=0;j--) {
                //check if the last value is reached, then the remaining numbers need to be produced in advance
                if(j == 0){
                    production_array[j]+=singleDemand;
                    break;
                }

                //check if it is allowed to produce on that day
                if(productionDays[j-1] == false)
                    continue;

                //check if on the current day there is enough capacity left to produce
                remainingCapacity = this.productionLimit - production_array[j];
                if(remainingCapacity > 0) {
                    //assign as much capacity as possible or needed
                    if(singleDemand <= remainingCapacity) {
                        //there is enough capacity available
                        production_array[j]+=singleDemand;
                        break;
                    } else {
                        //there is not enough capacity available
                        production_array[j]+=remainingCapacity;
                        singleDemand-=remainingCapacity;
                    }
                }
            }
        }

        return production_array;
    }


    /**
     * Creates a websocket and connects it to the provided URI.
     * @param websocketAddr URI of the websocket
     * @return 0 if successful
     * */
    private int createWebsocket(URI websocketAddr) {
        URI url = websocketAddr;
        if(websocketAddr == null) {
            try {
                url = new URI("ws://localhost:8080/mediator/mediator");
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return -1;
            }
        }

        this.client = new WebsocketClient(url);
        return 0;
    }

    @Override
    /**
     * Provides message handling capabilities to the agent. So far these messages are supported:
     * MediatorRequest:  agent calculates answer and sends it back
     * EndOfNegotiation: agent simple prints its result for debugging purposes
     * ErrorMessage:     agent prints the error
     * All other message types also cause printing of an error
     *
     * @param message    Incoming message that needs handling
     * */
    public void onNewMessage(AbstractMessage message) {

        //depending on the messagetype, call the corresponding messagehandler
        if(message instanceof MediatorRequest) {
            AgentResponse res = handleMediatorRequest((MediatorRequest) message);
            this.client.sendMessage(res);
        } else if (message instanceof EndNegotiation) {
            handleEndNegotiation((EndNegotiation) message);
        } else if (message instanceof ErrorMessage) {
            handleErrorMessage((ErrorMessage) message);
        } else {
            System.out.println("Unknown message " + message.toString());
        }
    }

    /**
     * Handles the MedaitorRequest mesage by calculating the production arrays based on the demand
     * and the provided solution of the mediator.
     * @param req MediatorRequest containing the demands and solutions
     * @return AgentResponse object containing the production arrays, costs and selection
     * */
    protected AgentResponse handleMediatorRequest(MediatorRequest req) {
        Solution proposal;
        Integer[] productionArray;
        double cost;
        double best_solution_costs = 0;

        //create the AgentResponse object
        AgentResponse res = new AgentResponse();
        Map<Integer, Double>    costs = res.getCosts();
        Map<Integer, Integer[]> productionArrays = res.getDemands();

        //iterate all proposals and calculate the production arrays and theirs costs
        Map<Integer, Solution>  solutions = req.getSolutions();
        int i = 0;
        for(Map.Entry<Integer, Solution> entry : solutions.entrySet()) {
            proposal = entry.getValue();

            //System.out.println("solution: " + proposal.toString());

            //store the calculated array
            productionArray = getProductionArray(proposal.getDemands(), proposal.getSolution());
            productionArrays.put(i, productionArray);

            //store the calculated costs
            cost = getProductionCosts(productionArray, proposal.getDemands());
            costs.put(i, cost);

            //update the selected solution if possible
            if(best_solution_costs == 0 || cost < best_solution_costs){
                best_solution_costs = cost;
                res.setSelection(i);
            }
            i++;
        }

        return res;
    }

    protected int handleEndNegotiation(EndNegotiation msg) {
        return 0;
    }

    protected int handleErrorMessage(ErrorMessage errmsg) {
        System.out.println("Received Error Message: " + errmsg.toString());
        return 0;
    }

}

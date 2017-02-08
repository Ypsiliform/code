package cas.ypsiliform.agent;

import cas.ypsiliform.agent.websocket.MessageHandler;
import cas.ypsiliform.agent.websocket.WebsocketClient;
import cas.ypsiliform.messages.*;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Agent implements MessageHandler{

    /**
     * This factor can be used to make storage costs a lot more costly
     * This is only necessary if the mediator selects the solution that best suits
     * all agents. In order to avoid the first agent to simply retrieve all items
     * from store, the storage costs can be made a lot more costly.
     */
    private static final int COST_CORRECTION_FACTOR = 1;

    private int id;                         // ID of the agent, starting from 1
    private double setupCost;               // costs for producing in this period
    private double storageCost;             // costs per unit per period for storing
    private int productionLimit;            // maximum production capacity per period
    private Integer[] productionTarget;     // targeted outcome, only relevant for agent #1
    private ArrayList<Integer> children;    // children of this agent in the production line
    private URI websocketAddr;              // Address of the websocket
    private WebsocketClient client;         // Mediator object to send data to once connected
    private String confId;                  // ID of the configuration that is currently used

    /**
     * Minimal agent configuration for testing purposes
     * */
    public Agent(int id, double setupCost, double storageCost, int productionLimit) {
        this.id = id;
        this.storageCost = storageCost;
        this.setupCost = setupCost;
        this.productionLimit = productionLimit;
    }

    /**
     * Normal constructor to set all parameters
     * */
    public Agent(int id, double setupCost, double storageCost, int productionLimit,
                 ArrayList<Integer> children, URI websocketAddr, Integer[] productionTarget, String confId) {
        //set the parameters
        this.id = id;
        this.setupCost = setupCost;
        this.storageCost = storageCost;
        this.productionLimit = productionLimit;
        this.children = children;
        this.websocketAddr = websocketAddr;
        this.productionTarget = productionTarget;
        this.confId = confId;
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

    public WebsocketClient getClient() {
        return client;
    }

    public void setClient(WebsocketClient client) {
        this.client = client;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public URI getWebsocketAddr() {
        return websocketAddr;
    }

    public void setWebsocketAddr(URI websocketAddr) {
        this.websocketAddr = websocketAddr;
    }

    public Integer[] getProductionTarget() {
        return productionTarget;
    }

    public void setProductionTarget(Integer[] productionTarget) {
        this.productionTarget = productionTarget;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    /******************************
     * Connection setup and registering
     ******************************/

    public int startConnection() throws ConnectException {
        //open the websocket
        URI url = this.websocketAddr;
        if(url == null) {
            try {
                url = new URI("ws://localhost:8080/mediator/mediator");
            } catch (URISyntaxException e) {
                throw new ConnectException("Connection to " + this.websocketAddr.toString() + " failed");
            }
        }
        this.client = new WebsocketClient(url);

        //register the agent as message handle
        client.addMessageHandler(this);

        //register the agent with the mediator
        registerAgent();

        return 0;
    }


    /**
     * Builds an AgentRegistration message and registers itself to the mediator.
     * If no connection is possible, a ConnectionException is thrown.
     * */
    private void registerAgent() throws ConnectException {
        //build the registration
        AgentRegistration registration = new AgentRegistration();
        registration.setId(this.id);
        registration.setConfig(this.confId);
        registration.setRequires(this.children);

        //agent #1 must also add the demand
        if(this.id == 1) {
            registration.setDemand(this.productionTarget);
        }

        //send out the registration
        if(client == null)
            throw new ConnectException("No connection to " + this.websocketAddr.toString() + " established. Can't send registration.");
        client.sendMessage(registration);
    }

    /******************************
     *  production calculkation and costs
     ******************************/

    /**
     * Calculate the costs that arise because of required preproduction.
     * @param items
     *          number of items that need to be preproduced
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

        return initCosts * COST_CORRECTION_FACTOR;
    }

    /**
     * Calculates the costs that are created by the created production plan and the demands
     * @param production
     *          plan that contains data when and how many items are built
     * @param demands
     *          Contains the details of how many items can be retreived in a period
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
     * @param demands
     *          This array contains the demands per days
     * @param productionDays
     *          boolean array that defines on which periods it is allowed to produce something
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

    /******************************
     *  Message handling routines
     ******************************/

    @Override
    /**
     * Provides message handling capabilities to the agent. So far these messages are supported:
     * MediatorRequest:  agent calculates answer and sends it back
     * EndOfNegotiation: agent simple prints its result for debugging purposes
     * ErrorMessage:     agent prints the error
     * All other message types also cause printing of an error
     *
     * @param message
     *          Incoming message that needs handling
     * */
    public void onNewMessage(AbstractMessage message) {
        if(message instanceof MediatorRequest) {
            //process the message and return the AgentResponse
            AgentResponse res = handleMediatorRequest((MediatorRequest) message);
            this.client.sendMessage(res);
        } else if (message instanceof EndNegotiation) {
            //only print the contents for logging / statistic purposes
            handleEndNegotiation((EndNegotiation) message);
        } else if (message instanceof ErrorMessage) {
            //only print the error message
            handleErrorMessage((ErrorMessage) message);
        } else {
            System.out.println("Unknown message " + message.toString());
        }
    }

    /**
     * Handles the MedaitorRequest mesage by calculating the production arrays based on the demand
     * and the provided solution of the mediator.
     * @param req
     *          MediatorRequest containing the demands and solutions
     * @return AgentResponse object containing the production arrays, costs and selection
     * */
    protected AgentResponse handleMediatorRequest(MediatorRequest req) {
        Solution proposal;
        Integer[] adaptedProductionArray;
        Integer[] realProductionArray;
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

            // store the calculated array
            // The items that need to be retrieved from storage are counted in the costs, but they are
            // not sent to the mediator as new demands. In order not to lose them or to give opponents
            // a competitive advantage, the items from the storage are added to the first bucket.
            realProductionArray = getProductionArray(proposal.getDemands(), proposal.getSolution());
            adaptedProductionArray = Arrays.copyOfRange(realProductionArray, 1, realProductionArray.length);
            if(COST_CORRECTION_FACTOR == 1) {
                adaptedProductionArray[0] += realProductionArray[0];
            }
            productionArrays.put(i, adaptedProductionArray);

            //store the calculated costs
            cost = getProductionCosts(realProductionArray, proposal.getDemands());
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

    /**
     * Handles an EndNegotiation message by printing the final result to sysout.
     * @param msg
     *          EndNegotiation message from the mediator
     * @return 0 for success (currently no error defined)
     * */
    protected int handleEndNegotiation(EndNegotiation msg) {
        //just log the changes
        Solution proposal = msg.getSolution();
        Integer[] productionArray = getProductionArray(proposal.getDemands(), proposal.getSolution());
        double costs = getProductionCosts(productionArray, proposal.getDemands());

        //print the created arrays
        System.out.println("Agent " + this.id + " has ended negotioation with the following result:");
        System.out.println("solution: " + Arrays.toString(proposal.getSolution()));
        System.out.println("demand:   " + Arrays.toString(proposal.getDemands()));
        System.out.println("result:   " + Arrays.toString(productionArray));
        System.out.println("Costs:    " + costs);

        return 0;
    }

    /**
     * Handles an ErrorMessage by printing the error message. Since the agent is operating
     * stateless (agent does not remember previous states), there is nothing else to do.
     * @param errmsg
     *          ErrorMessage message from the mediator
     * @return 0 for success (currently no error defined)
     * */
    protected int handleErrorMessage(ErrorMessage errmsg) {
        System.out.println("Received Error Message: " + errmsg.toString());
        return 0;
    }

}

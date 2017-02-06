package cas.ypsiliform.agent;

import java.sql.Array;
import java.util.ArrayList;

/**
 * Created by paul on 06.02.17.
 */

public class Agent {

    private double setupCost;
    private double storageCost;
    private int productionLimit;
    private ArrayList<Integer> children;

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


    private int startWebsocket() {
        return 0;
    }


    protected int[] getProductionArray(int[] demand, boolean[] production_days) {
        int production_array[] = new int[demand.length + 1];
        int demandOnDay;
        int remainingCapacity;

        //iterate through the demand array to calculate the demand
        for (int i=0;i<demand.length;i++) {
            demandOnDay = demand[i];
            //iterate through the production array and set the numbers to be produced
            for (int j=i+1;j>=0;j--) {
                //check if the last value is reached, then the remaining numbers need to be produced in advance
                if(j == 0){
                    production_array[j]+=demandOnDay;
                    break;
                }

                //check if it is allowed to produce on that day
                if(production_days[j] == false)
                    continue;

                //check if on the current day there is enough capacity left to produce
                remainingCapacity = this.productionLimit - production_array[j];
                if(remainingCapacity > 0) {
                    //assign as much capacity as possible or needed
                    if(demandOnDay <= remainingCapacity) {
                        //there is enough capacity available
                        production_array[j]+=demandOnDay;
                        break;
                    } else {
                        //there is not enough capacity available
                        production_array[j]+=remainingCapacity;
                        demandOnDay-=remainingCapacity;
                    }
                }
            }
        }

        return production_array;
    }
}

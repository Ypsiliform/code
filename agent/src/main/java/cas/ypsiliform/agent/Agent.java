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
            if(items % productionDays != 0)
                productionDays++;

            //sum up the setup costs
            initCosts = productionDays * setupCost;

            //calculate the costs of storing the products
            stored_items = items % this.productionLimit;
            do {
                initCosts += stored_items * this.storageCost;
                stored_items += this.productionLimit;
            } while(stored_items < items);
        }

        return initCosts;
    }

    /**
     * Calculates the costs that are created by the created production plan and the demands
     * @param production plan that contains data when and how many items are built
     * @param demands Contains the details of how many items can be retreived in a period
     * */
    protected double getProductionCosts(int[] production, int[] demands) {
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
     * Creates the production array that is ideal for the prodvided production days
     * @param demands This array contains the demands per days
     * @param productionDays boolean array that defines on which periods it is allowed to produce something
     * @return and int[] that contains the created mapping of production periods
     * */
    protected int[] getProductionArray(int[] demands, boolean[] productionDays) {
        int production_array[] = new int[demands.length + 1];
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
}

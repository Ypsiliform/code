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

    private int[] getProductionArray(int[] demand) {

        for (int i: demand) {

        }
    }
}

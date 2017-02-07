package cas.ypsiliform.messages;

import java.util.HashMap;
import java.util.Map;

public class AgentResponse
    extends AbstractMessage
{

    private int selection;
    // first int is the ID of the solution, demands contains the calculated production array to the propsed solution
    private Map<Integer, Integer[]> demands = new HashMap<>();
    // first int is the ID of the solution, cost is the cost of the solution
    private Map<Integer, Double> costs = new HashMap<>();

    public int getSelection()
    {
        return selection;
    }

    public void setSelection(int selection)
    {
        this.selection = selection;
    }

    public Map<Integer, Integer[]> getDemands()
    {
        return demands;
    }

    public void setDemands(Map<Integer, Integer[]> demands)
    {
        this.demands = demands;
    }

    public Map<Integer, Double> getCosts() {
        return costs;
    }

    public void setCosts(Map<Integer, Double> costs) {
        this.costs = costs;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("AgentResponse [selection=");
        builder.append(selection);
        builder.append(", demands=");
        builder.append(demands);
        builder.append(", cost=");
        builder.append(costs);
        builder.append("]");
        return builder.toString();
    }

}

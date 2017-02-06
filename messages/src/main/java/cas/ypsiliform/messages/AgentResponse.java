package cas.ypsiliform.messages;

import java.util.HashMap;
import java.util.Map;

public class AgentResponse
    extends AbstractMessage
{

    private int selection;
    private Map<Integer, Integer[]> demands = new HashMap<>();
    private int cost;

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

    public int getCost()
    {
        return cost;
    }

    public void setCost(int cost)
    {
        this.cost = cost;
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
        builder.append(cost);
        builder.append("]");
        return builder.toString();
    }

}

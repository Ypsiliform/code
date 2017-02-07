package cas.ypsiliform.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgentRegistration
    extends AbstractMessage
{

    private int id;
    private String config;
    private List<Integer> requires = new ArrayList<>();
    private Integer[] demand = new Integer[0];

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getConfig()
    {
        return config;
    }

    public void setConfig(String config)
    {
        this.config = config;
    }

    public List<Integer> getRequires()
    {
        return requires;
    }

    public void setRequires(List<Integer> requires)
    {
        this.requires = requires;
    }

    public Integer[] getDemand()
    {
        return demand;
    }

    public void setDemand(Integer[] demand)
    {
        this.demand = demand;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("AgentRegistration [id=");
        builder.append(id);
        builder.append(", config=");
        builder.append(config);
        builder.append(", requires=");
        builder.append(requires);
        builder.append(", demand=");
        builder.append(Arrays.toString(demand));
        builder.append("]");
        return builder.toString();
    }

}

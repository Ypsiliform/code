package cas.ypsiliform.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MediatorRequest
    extends AbstractMessage
{

    private Map<Integer, Solution> solutions = new HashMap<>();

    public Map<Integer, Solution> getSolutions()
    {
        return solutions;
    }

    public void setSolutions(Map<Integer, Solution> solutions)
    {
        this.solutions = solutions;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MediatorRequest [solutions=");
        builder.append(solutions);
        builder.append("]\n");
        return builder.toString();
    }

}

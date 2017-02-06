package cas.ypsiliform.messages;

import java.util.Arrays;

public class EndNegotiation
    extends AbstractMessage
{

    private boolean[] solution;

    public boolean[] getSolution()
    {
        return solution;
    }

    public void setSolution(boolean[] solution)
    {
        this.solution = solution;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("EndNegotiation [solution=");
        builder.append(Arrays.toString(solution));
        builder.append("]");
        return builder.toString();
    }
}

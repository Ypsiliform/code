package cas.ypsiliform.messages;

public class EndNegotiation
    extends AbstractMessage
{

    private Solution solution;

    public Solution getSolution()
    {
        return solution;
    }

    public void setSolution(Solution solution)
    {
        this.solution = solution;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("EndNegotiation [solution=");
        builder.append(solution);
        builder.append("]");
        return builder.toString();
    }
}

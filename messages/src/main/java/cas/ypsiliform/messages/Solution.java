/*
 * (c) 2015 - 2017 ENisco GmbH &amp; Co. KG
 */
package cas.ypsiliform.messages;

import java.util.Arrays;

public class Solution
{

    private boolean[] solution;
    private Integer[] demands;

    public boolean[] getSolution()
    {
        return solution;
    }

    public void setSolution(boolean[] solution)
    {
        this.solution = solution;
    }

    public Integer[] getDemands()
    {
        return demands;
    }

    public void setDemands(Integer[] demands)
    {
        this.demands = demands;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Solution [solution=");
        builder.append(Arrays.toString(solution));
        builder.append(", demands=");
        builder.append(Arrays.toString(demands));
        builder.append("]");
        return builder.toString();
    }

}

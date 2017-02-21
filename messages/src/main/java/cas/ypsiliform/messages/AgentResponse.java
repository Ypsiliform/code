package cas.ypsiliform.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AgentResponse extends AbstractMessage {

	private int selection;
	// first int is the ID of the solution, demands contains the calculated
	// production array to the propsed solution
	private Map<Integer, Integer[]> demands = new HashMap<>();
	// first int is the ID of the solution, cost is the cost of the solution
	private Map<Integer, Double> costs = new HashMap<>();

	public int getSelection() {
		return selection;
	}

	public void setSelection(int selection) {
		this.selection = selection;
	}

	public Map<Integer, Integer[]> getDemands() {
		return demands;
	}

	public void setDemands(Map<Integer, Integer[]> demands) {
		int minimumKey = demands.keySet().stream().min(Integer::compare).orElse(1);
		if (minimumKey != 1)
			throw new java.lang.IndexOutOfBoundsException("Demands array must start with id 1");

		int maximumKey = demands.keySet().stream().max(Integer::compare).orElse(1);
		if (maximumKey != demands.size())
			throw new IndexOutOfBoundsException("Demands array must use consecutive keys");

		this.demands = demands;
	}

	public Map<Integer, Double> getCosts() {
		return costs;
	}

	public void setCosts(Map<Integer, Double> costs) {
		int minimumKey = costs.keySet().stream().min(Integer::compare).orElse(1);
		if (minimumKey != 1)
			throw new java.lang.IndexOutOfBoundsException("Costs array must start with id 1");

		int maximumKey = costs.keySet().stream().max(Integer::compare).orElse(1);
		if (maximumKey != costs.size())
			throw new IndexOutOfBoundsException("Costs array must use consecutive keys");

		this.costs = costs;
	}

	@Override
	public String toString() {
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		AgentResponse that = (AgentResponse) o;

		if (that.getDemands().size() != demands.size())
			return false;

		for (Map.Entry<Integer, Integer[]> entry : demands.entrySet()) {
			if (!that.getDemands().containsKey(entry.getKey())) {
				return false;
			}

			if (!Arrays.equals(that.getDemands().get(entry.getKey()), entry.getValue())) {
				return false;
			}
		}

		if (selection != that.selection)
			return false;
		return costs.equals(that.costs);
	}

	@Override
	public int hashCode() {
		int result = selection;
		result = 31 * result + demands.hashCode();
		result = 31 * result + costs.hashCode();
		return result;
	}
}

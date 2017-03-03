package cas.ypsiliform.mediator.negotiation;

import java.util.HashMap;
import java.util.Map;

import cas.ypsiliform.messages.AgentResponse;

public class CostAggregationReduction implements ReductionFunction<AgentResponse, Map<Integer, Double>> {

	@Override
	public Map<Integer, Double> reduce(AgentResponse response, Object[] otherVotes) {
		Map<Integer, Double> variantCost = new HashMap<Integer, Double>(response.getCosts());

		if (otherVotes != null)
			for (int j = 0; j < otherVotes.length; j++) {
				((Map<Integer, Double>) otherVotes[j]).forEach((key, cost) -> {
					variantCost.merge(key, cost, (prev, next) -> {
						return prev + next;
					});
				});
			}

		return variantCost;
	}
}

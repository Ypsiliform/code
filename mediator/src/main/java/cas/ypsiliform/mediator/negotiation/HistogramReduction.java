package cas.ypsiliform.mediator.negotiation;

import java.util.HashMap;
import java.util.Map;

import cas.ypsiliform.messages.AgentResponse;

public class HistogramReduction implements ReductionFunction<AgentResponse, Map<Integer, Integer>> {
	private final int numberOfBuckets;

	public HistogramReduction() {
		this(16);
	}

	public HistogramReduction(int numberOfBuckets) {
		this.numberOfBuckets = numberOfBuckets;
	}

	@Override
	public Map<Integer, Integer> reduce(AgentResponse response, Object[] otherVotes) {
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>(numberOfBuckets);
		histogram.put(response.getSelection(), 1);

		if (otherVotes != null)
			for (int j = 0; j < otherVotes.length; j++) {
				((Map<Integer, Integer>) otherVotes[j]).forEach((key, count) -> {
					histogram.merge(key, count, (prev, next) -> {
						return prev + next;
					});
				});
			}

		return histogram;
	}

}

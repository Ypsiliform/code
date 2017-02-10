package cas.ypsiliform.mediator;

import java.util.Map;

import cas.ypsiliform.mediator.negotiation.AgentProxy;
import cas.ypsiliform.mediator.negotiation.VoteBasedMediator;

public class MediatorFactory {
	public enum Strategy {
		DEFAULT, VOTING
	}

	public static Mediator getMediator(Strategy strategy, Map<Integer, AgentProxy> agents, Integer[] primaryDemands) {
		switch (strategy) {
		case DEFAULT:
		case VOTING:
			return new VoteBasedMediator(agents, primaryDemands);

		default:
			throw new IllegalArgumentException("strategy");
		}
	}
}

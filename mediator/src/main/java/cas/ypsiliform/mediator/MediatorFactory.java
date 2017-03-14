package cas.ypsiliform.mediator;

import java.util.Map;

import cas.ypsiliform.mediator.negotiation.CostBasedMediator;
import cas.ypsiliform.mediator.negotiation.VoteBasedMediator;

public class MediatorFactory {
	public enum Strategy {
		DEFAULT, VOTING, COST
	}

	public static Mediator getMediator(Strategy strategy, Map<Integer, AgentProxy> agents, Integer[] primaryDemands) {
		switch (strategy) {
		case DEFAULT:
		case VOTING:
			return new VoteBasedMediator(agents, primaryDemands);
		case COST:
			return new CostBasedMediator(agents, primaryDemands);
		default:
			throw new IllegalArgumentException("strategy");
		}
	}
}

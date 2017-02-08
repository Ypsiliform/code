package cas.ypsiliform;

public interface Constants {
	interface Encoding {
		int NUMBER_OF_PERIODS = 12;
	}
	
	interface Negotiation {
		int NUMBER_OF_ROUNDS = 1000;
		int TIMEOUT_PER_ROUND_MS = 5*1000;
	}

	interface Agent {
		/**
		 * This factor can be used to make storage costs a lot more costly.
		 * This is only necessary if the mediator selects the solution that best suits
		 * all agents. In order to avoid the first agent to simply retrieve all items
		 * from store, the storage costs can be made a lot more costly.
		 */
		int COST_CORRECTION_FACTOR = 1;
	}
}

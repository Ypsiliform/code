package cas.ypsiliform.mediator.negotiation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import cas.ypsiliform.Constants;
import cas.ypsiliform.mediator.AgentProxy;
import cas.ypsiliform.mediator.Mediator;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.messages.AgentResponse;

/**
 * Mediator which moderates a negotiation between a given list of agents. All
 * agents can vote for a solution and the solutions with the highest number of
 * votes wins.
 * 
 * @author Michael Müller
 */
public class CostBasedMediator extends AbstractMediator implements Mediator {
	static Logger log = Logger.getLogger(CostBasedMediator.class.getSimpleName());

	/**
	 * Mediator takes a list of agents that take part in the negotiation and the
	 * primary demands, which are the basis for the negotiation.
	 * 
	 * @param agents
	 * @param primaryDemands
	 */
	public CostBasedMediator(Map<Integer, AgentProxy> agents, Integer[] primaryDemands) {
		super(agents, primaryDemands);
	}

	@Override
	public void run() {
		List<SolutionProposal> solutions = new ArrayList<SolutionProposal>();
		solutions.add(new SolutionProposal(Constants.Encoding.NUMBER_OF_PERIODS));

		AtomicBoolean stop = new AtomicBoolean(false);

		for (int i = 0; i < Constants.Negotiation.NUMBER_OF_ROUNDS && !stop.get(); i++) {
			log.fine("Entering round " + i);

			SolutionProposal[] nextGen = generateOffspring(
					getNumberOfMutations(i, Constants.Negotiation.NUMBER_OF_ROUNDS),
					solutions.toArray(new SolutionProposal[0]));

			Map<Integer, Proposal> proposals = new HashMap<Integer, Proposal>();

			for (int j = 0; j < nextGen.length; j++) {
				proposals.put(j + 1, new Proposal(nextGen[j], primaryDemands));
			}

			visitor.<Map<Integer, Proposal>, AgentResponse, Map<Integer, Double>>visit(proposals,
					this::collectResponses, new CostAggregationReduction()).then(variantCost -> {
						variantCost.forEach((key, cost) -> {
							log.finer("Proposal " + key + " has total cost of " + cost);
						});

						int minCostVariant = variantCost.entrySet().stream().min((a, b) -> {
							return a.getValue().compareTo(b.getValue());
						}).get().getKey();

						solutions.clear();
						solutions.add(proposals.get(minCostVariant).solution);

						return Thenable.resolved(null);
					}).wait(Constants.Negotiation.TIMEOUT_PER_ROUND_MS, r -> {
						log.warning("Negotiation round timed out");
						stop.set(true);
					});

		}

		SolutionProposal chosenSolution = solutions.get(0);
		log.info("Chosen solution: " + chosenSolution.toString());

		visitor.visit(new Proposal(chosenSolution, primaryDemands), this::endNegotiation, null)
				.wait(Constants.Negotiation.TIMEOUT_PER_ROUND_MS, r -> {
					log.warning("Results communication timed out");
				});

		log.info("Negotiation ended");
	}
}

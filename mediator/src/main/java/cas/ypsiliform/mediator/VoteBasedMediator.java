package cas.ypsiliform.mediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import cas.ypsiliform.Constants;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.mediator.negotiation.AgentProxy;
import cas.ypsiliform.mediator.negotiation.SolutionProposal;
import cas.ypsiliform.messages.AgentResponse;

/**
 * Mediator which moderates a negotiation between a given list of agents. All
 * agents can vote for a solution and the solutions with the highest number of
 * votes wins.
 * 
 * @author Michael Müller
 */
public class VoteBasedMediator extends AbstractMediator implements Mediator {
	static Logger log = Logger.getLogger(VoteBasedMediator.class.getSimpleName());

	/**
	 * Mediator takes a list of agents that take part in the negotiation and the
	 * primary demands, which are the basis for the negotiation.
	 * 
	 * @param agents
	 * @param primaryDemands
	 */
	public VoteBasedMediator(Map<Integer, AgentProxy> agents, Integer[] primaryDemands) {
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

			visitor.<Map<Integer, Proposal>, AgentResponse, Map<Integer, Integer>>visit(proposals,
					this::collectResponses, this::reduceToFrequencyHistogram).then(histogram -> {
						int maxVotes = histogram.values().stream().max(Integer::compare).orElse(0);

						solutions.clear();

						histogram.forEach((key, votes) -> {
							if (votes >= maxVotes)
								solutions.add(proposals.get(key).solution);
						});

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

	private Map<Integer, Integer> reduceToFrequencyHistogram(AgentResponse response, Object[] otherVotes) {
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>(getNumberOfProposals());
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

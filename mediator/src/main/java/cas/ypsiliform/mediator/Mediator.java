package cas.ypsiliform.mediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import cas.ypsiliform.Constants;
import cas.ypsiliform.mediator.async.Action;
import cas.ypsiliform.mediator.async.Function;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.mediator.negotiation.AgentProxy;
import cas.ypsiliform.mediator.negotiation.SolutionProposal;
import cas.ypsiliform.messages.MediatorRequest;
import cas.ypsiliform.messages.Solution;

public class Mediator implements Runnable {
	private static Logger log = Logger.getLogger(Mediator.class.getSimpleName());

	private final Map<Integer, AgentProxy> agents;
	private final Integer[] primaryDemands;

	public Mediator(Map<Integer, AgentProxy> agents, Integer[] primaryDemands) {
		// validate consecutive IDs
		for (int i = 1; i <= agents.size(); i++) {
			if (!agents.containsKey(i))
				throw new IllegalArgumentException();
		}

		if (primaryDemands.length != Constants.Encoding.NUMBER_OF_PERIODS) {
			throw new IllegalArgumentException();
		}

		this.agents = agents;
		this.primaryDemands = primaryDemands;

		registerAgentDeadListener(agents.values());
	}

	@Override
	public void run() {
		log.info("Starting new negotiation with " + agents.size() + " agents");

		List<SolutionProposal> solutions = Collections.EMPTY_LIST;
		solutions.add(new SolutionProposal(Constants.Encoding.NUMBER_OF_PERIODS));

		for (int i = 0; i < Constants.Negotiation.NUMBER_OF_ROUNDS; i++) {
			log.fine("Entering round " + i);

			SolutionProposal[] nextGen = generateOffspring(getNumberOfProposals(),
					solutions.toArray(new SolutionProposal[0]));

			Map<Integer, Proposal> proposals = new HashMap<Integer, Proposal>();

			for (int j = 0; j < nextGen.length; j++) {
				proposals.put(j + 1, new Proposal(nextGen[j], primaryDemands));
			}

			recursiveNegotiation(agents.get(1), proposals).then((Function<Map<Integer, Integer>, Thenable>) votes -> {
				int[] histogram = new int[getNumberOfProposals()];

				votes.forEach((agentId, solution) -> {
					// assuming solutions are numbered 1 - n
					histogram[solution - 1]++;
				});

				int maxVotes = 0;
				for (int j = 1; j < histogram.length; j++) {
					maxVotes = Math.max(histogram[j - 1], histogram[j]);
				}

				solutions.clear();

				for (int j = 0; j < histogram.length; j++) {
					if (histogram[j] >= maxVotes)
						solutions.add(nextGen[j]);
				}

				Thenable stop = new Thenable();
				stop.resolve(null);
				return stop;
			}).wait(Constants.Negotiation.TIMEOUT_PER_ROUND_MS, r -> {
				log.warning("Negotiation round timed out");
				return;
			});
		}

		log.info("Negotiation ended");
		SolutionProposal chosenSolution = solutions.get(0);
		log.info("Chosen solution: " + chosenSolution.toString());

		agents.forEach((id, agent) -> {
			agent.endNegotiation(chosenSolution);
		});
	}

	private int getNumberOfProposals() {
		return agents.size() - 1;
	}

	private SolutionProposal[] generateOffspring(int numberOfSolutions, SolutionProposal... parents) {
		SolutionProposal[] result = new SolutionProposal[numberOfSolutions];

		int i = 0;
		for (; i < parents.length && i < numberOfSolutions; i++) {
			result[i] = parents[i];
		}

		for (; i < numberOfSolutions; i++) {
			result[i] = parents[i % parents.length].mutate();
		}

		return result;
	}

	private Thenable<Map<Integer, Integer>> recursiveNegotiation(AgentProxy current, Map<Integer, Proposal> proposals) {
		List<Thenable> nextIteration = new ArrayList<Thenable>();

		// simple Integer is not allowed: "Local variable currentAgentPreference
		// defined in an enclosing scope must be final or effectively final"
		// stupid Java, fooled so easily
		Integer[] currentAgentPreference = { 0 };

		// map view on bit string and primary secondary/secondary demands from
		// previous agent into message
		Map<Integer, Solution> solutionsMap = new HashMap<Integer, Solution>();

		proposals.forEach((pos, proposal) -> {
			Solution sol = new Solution();
			sol.setSolution(proposal.solution.sliceForAgent(pos));
			sol.setDemands(proposal.demands);
			solutionsMap.put(pos, sol);
		});

		MediatorRequest request = new MediatorRequest();
		request.setSolutions(solutionsMap);

		// send message to current agent asynchronously
		current.sendSolutionProposals(request).then(response -> {
			// store and validate response
			Map<Integer, Integer[]> agentDemandVariants = response.getDemands();
			currentAgentPreference[0] = response.getSelection();

			log.finer("Agent " + current.getId() + " prefers solution " + currentAgentPreference[0]);

			assert agentDemandVariants.size() == solutionsMap
					.size() : "Expected to receive as many secondary demand variants from agent as were sent to it";

			// prepare recursion: Get full solution proposal (not only view for
			// previous agent) and secondary demands
			Map<Integer, Proposal> nextIterationProposals = new HashMap<Integer, Proposal>();

			for (int j = 0; j < response.getDemands().size(); j++) {
				Integer[] demandVariant = agentDemandVariants.get(j);

				assert demandVariant.length == Constants.Encoding.NUMBER_OF_PERIODS : "Number of secondary demands should equal number of periods";

				nextIterationProposals.put(j, new Proposal(proposals.get(j).solution, demandVariant));
			}

			// send solution + secondary demands of current agent to all
			// children
			current.getChildIds().forEach(childId -> {
				log.finer("Asking agent " + childId);
				nextIteration.add(recursiveNegotiation(agents.get(childId), nextIterationProposals));
			});
		});

		Thenable<Map<Integer, Integer>> result = new Thenable<Map<Integer, Integer>>();

		// collect votes from recursive calls
		Thenable.whenAll(nextIteration).then(results -> {
			Map<Integer, Integer> pref = new HashMap<Integer, Integer>();

			for (int i = 0; i < results.length; i++) {
				pref.putAll((Map<Integer, Integer>) results[i]);
			}
			pref.put(current.getId(), currentAgentPreference[0]);

			result.resolve(pref);
		});

		return result;
	}

	private void registerAgentDeadListener(Collection<AgentProxy> agents) {
		for (AgentProxy agent : agents) {
			agent.addAgentDeadListener((AgentProxy a) -> {
				// TODO abort
			});
		}
	}

	class Proposal {
		public final SolutionProposal solution;
		public final Integer[] demands;

		public Proposal(SolutionProposal solution, Integer[] demands) {
			this.solution = solution;
			this.demands = demands;
		}
	}
}

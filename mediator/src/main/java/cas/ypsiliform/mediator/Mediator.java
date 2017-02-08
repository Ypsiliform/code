package cas.ypsiliform.mediator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import cas.ypsiliform.Constants;
import cas.ypsiliform.mediator.async.Function;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.mediator.negotiation.AgentProxy;
import cas.ypsiliform.mediator.negotiation.SolutionProposal;
import cas.ypsiliform.messages.EndNegotiation;
import cas.ypsiliform.messages.MediatorRequest;
import cas.ypsiliform.messages.Solution;

/**
 * Mediator which moderates a negotiation between a given list of agents
 * 
 * @author Michael Müller
 */
public class Mediator implements Runnable {
	private static Logger log = Logger.getLogger(Mediator.class.getSimpleName());

	private final Map<Integer, AgentProxy> agents;
	private final Integer[] primaryDemands;

	/**
	 * Mediator takes a list of agents that take part in the negotiation and the
	 * primary demands, which are the basis for the negotiation.
	 * 
	 * @param agents
	 * @param primaryDemands
	 */
	public Mediator(Map<Integer, AgentProxy> agents, Integer[] primaryDemands) {
		// validate consecutive IDs
		for (int i = 1; i <= agents.size(); i++) {
			if (!agents.containsKey(i))
				throw new IllegalArgumentException();
		}

		if (agents.size() <= 2)
			throw new IllegalArgumentException();

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

		SolutionProposal chosenSolution = solutions.get(0);
		log.info("Chosen solution: " + chosenSolution.toString());

		endNegotiationRecursive(agents.get(1), new Proposal(chosenSolution, primaryDemands))
				.wait(Constants.Negotiation.TIMEOUT_PER_ROUND_MS, r -> {
					log.warning("Results communication timed out");
					return;
				});
		log.info("Negotiation ended");
	}

	/**
	 * Determine how many solution proposals will be sent per iteration of the
	 * negotiation We have to send less proposals than the number of agents to
	 * avoid stalemate situations, but as many as possible to explore the
	 * solution space faster.
	 */
	private int getNumberOfProposals() {
		assert agents.size() > 2 : "Algorithm can only handle negotiations with more than two agents";
		return agents.size() - 1;
	}

	/**
	 * n+p strategy to generate offspring from a set of parents.
	 * 
	 * @param numberOfSolutions
	 *            Requested number of solutions to be returned
	 * @param parents
	 *            Parents from which to mutate new solutions
	 * @return parents + mutations of parents
	 */
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

	/**
	 * Recursively traverse the supply chain and send the demands of higher
	 * levels to the next levels. Uses asynchronous communication with agents
	 * and can communicate with multiple agents on the same level at the same
	 * time.
	 * 
	 * @param current
	 *            Current agent to communicate with. Will propagate the current
	 *            agent's demands to the next level
	 * @param proposals
	 *            Solution proposals to communicate to the agent
	 * @return A map of the agents' preferences (agent id => solution id)
	 */
	private Thenable<Map<Integer, Integer>> recursiveNegotiation(AgentProxy current, Map<Integer, Proposal> proposals) {
		List<Thenable> nextIteration = new ArrayList<Thenable>();

		// simple Integer is not allowed: "Local variable currentAgentPreference
		// defined in an enclosing scope must be final or effectively final"
		// stupid Java, fooled so easily
		Integer[] currentAgentPreference = { 0 };

		MediatorRequest request = getMediatorRequestMessage(current.getId(), proposals);

		// send message to current agent asynchronously
		current.sendSolutionProposals(request).then(response -> {
			// store and validate response
			Map<Integer, Integer[]> agentDemandVariants = response.getDemands();
			currentAgentPreference[0] = response.getSelection();

			log.finer("Agent " + current.getId() + " prefers solution " + currentAgentPreference[0]);

			assert agentDemandVariants.size() == request.getSolutions()
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

	private Thenable<Void> endNegotiationRecursive(AgentProxy current, Proposal chosenSolution) {
		List<Thenable> runningMessages = new ArrayList<Thenable>();

		// map view on bit string and primary secondary/secondary demands from
		// previous agent into message
		
		Map<Integer, Proposal> proposalMap = Collections.emptyMap();
		proposalMap.put(1, chosenSolution);
		MediatorRequest request = getMediatorRequestMessage(current.getId(), proposalMap);

		// send message to current agent asynchronously
		current.sendSolutionProposals(request).then(response -> {
			// store and validate response
			Map<Integer, Integer[]> agentDemandVariants = response.getDemands();

			assert agentDemandVariants
					.size() == 1 : "Expected to receive as many secondary demand variants from agent as were sent to it";

			// prepare recursion: Get full solution (not only view for previous
			// agent) and secondary demands

			Integer[] demandVariant = agentDemandVariants.get(0);
			assert demandVariant.length == Constants.Encoding.NUMBER_OF_PERIODS : "Number of secondary demands should equal number of periods";

			Proposal nextIterationSolution = new Proposal(chosenSolution.solution, demandVariant);

			// send solution + secondary demands of current agent to all
			// children
			current.getChildIds().forEach(childId -> {
				log.finer("Asking agent " + childId);
				runningMessages.add(endNegotiationRecursive(agents.get(childId), nextIterationSolution));
			});

			// confirm solution to current agent
			EndNegotiation message = new EndNegotiation();
			message.setSolution(request.getSolutions().get(1));
			runningMessages.add(current.endNegotiation(message));
		});

		Thenable<Void> result = new Thenable<Void>();
		Thenable.whenAll(runningMessages).then(r -> {
			result.resolve(null);
		});
		return result;
	}

	private MediatorRequest getMediatorRequestMessage(int agentId, Map<Integer, Proposal> proposals) {
		// map view on bit string and primary secondary/secondary demands from
		// previous agent into message
		Map<Integer, Solution> solutionsMap = new HashMap<Integer, Solution>();

		proposals.forEach((pos, proposal) -> {
			solutionsMap.put(pos, getSolutionMessage(agentId, proposal));
		});

		MediatorRequest request = new MediatorRequest();
		request.setSolutions(solutionsMap);
		return request;
	}

	private Solution getSolutionMessage(int agentId, Proposal proposal) {
		Solution sol = new Solution();
		sol.setSolution(proposal.solution.sliceForAgent(agentId - 1));
		sol.setDemands(proposal.demands);
		return sol;
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

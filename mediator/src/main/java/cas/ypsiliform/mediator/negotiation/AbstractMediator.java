package cas.ypsiliform.mediator.negotiation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import cas.ypsiliform.Constants;
import cas.ypsiliform.mediator.negotiation.AgentVisitor.VisitorResult;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.messages.AgentResponse;
import cas.ypsiliform.messages.EndNegotiation;
import cas.ypsiliform.messages.MediatorRequest;
import cas.ypsiliform.messages.Solution;

public abstract class AbstractMediator implements Runnable {
	private static final Logger log = Logger.getLogger(AbstractMediator.class.getName());

	protected final AgentVisitor visitor;
	protected final Integer[] primaryDemands;
	protected final int numberOfProposals;

	public AbstractMediator(Map<Integer, AgentProxy> agents, Integer[] primaryDemands) {
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

		this.visitor = new AgentVisitor(agents, 1);
		this.primaryDemands = primaryDemands;
		this.numberOfProposals = agents.size() - 1;

		registerAgentDeadListener(agents.values());

		log.info("Mediator initialized with " + agents.size() + " agents");
	}

	/**
	 * Simulated annealing for number of mutations in a negotiation round. Start
	 * with many mutations to make big jumps in the solution space and decrease
	 * the number of mutations towards the end in order not to lose a good
	 * solution.
	 * 
	 * @param round
	 *            Number of the current round, starting with 0
	 * @param totalRounds
	 *            Number of total rounds in the negotiation
	 * @return The number of mutations to be performed. Maximum is
	 *         <code>Constants.Negotiation.INITIAL_NUMBER_OF_MUTATION</code>.
	 */
	protected int getNumberOfMutations(int round, int totalRounds) {
		return (int) Math.ceil(
				Constants.Negotiation.INITIAL_NUMBER_OF_MUTATION * (Math.pow(Math.E, -(round / (totalRounds / 3)))));
	}

	/**
	 * Determine how many solution proposals will be sent per iteration of the
	 * negotiation We have to send less proposals than the number of agents to
	 * avoid stalemate situations, but as many as possible to explore the
	 * solution space faster.
	 */
	protected int getNumberOfProposals() {
		return numberOfProposals;
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
	protected SolutionProposal[] generateOffspring(int numberOfMutations, SolutionProposal... parents) {
		int numberOfSolutions = getNumberOfProposals();
		SolutionProposal[] result = new SolutionProposal[numberOfSolutions];

		int i = 0;
		for (; i < parents.length && i < numberOfSolutions; i++) {
			result[i] = parents[i];
		}

		for (; i < numberOfSolutions; i++) {
			result[i] = parents[i % parents.length].mutate(numberOfMutations);
		}

		return result;
	}

	/**
	 * Step for the AgentVisitor to recursively traverse the supply chain and
	 * send the demands of higher levels to the next levels. Uses asynchronous
	 * communication with agents and can communicate with multiple agents on the
	 * same level at the same time.
	 * 
	 * @param current
	 *            Current agent to communicate with. Will propagate the current
	 *            agent's demands to the next level
	 * @param proposals
	 *            Solution proposals to communicate to the agent
	 * @return A map of the agents' preferences (agent id => solution id)
	 */
	protected Thenable<AgentVisitor.VisitorResult<Map<Integer, Proposal>, AgentResponse>> collectResponses(
			AgentProxy current, Map<Integer, Proposal> proposals) {
		Thenable<AgentVisitor.VisitorResult<Map<Integer, Proposal>, AgentResponse>> result = new Thenable<AgentVisitor.VisitorResult<Map<Integer, Proposal>, AgentResponse>>();

		MediatorRequest request = getMediatorRequestMessage(current.getId(), proposals);

		// send message to current agent asynchronously
		current.sendSolutionProposals(request).then(response -> {
			Map<Integer, Integer[]> agentDemandVariants = readDependentDemands(response, request.getSolutions().size());

			int currentAgentPreference = response.getSelection();
			log.finer("Agent " + current.getId() + " prefers solution " + currentAgentPreference);

			// prepare recursion: Get full solution proposal (not only view for
			// previous agent) and secondary demands
			Map<Integer, Proposal> nextIterationProposals = new HashMap<Integer, Proposal>(proposals.size());

			agentDemandVariants.forEach((key, dependentDemand) -> {
				assert dependentDemand.length == Constants.Encoding.NUMBER_OF_PERIODS : "Number of secondary demands should equal number of periods";

				nextIterationProposals.put(key, new Proposal(proposals.get(key).solution, dependentDemand));
			});

			result.resolve(new AgentVisitor.VisitorResult<Map<Integer, Proposal>, AgentResponse>(nextIterationProposals,
					response));
		});

		return result;
	}

	/**
	 * Recursively traverse the supply chain one last time to gather the
	 * secondary demands of all agents for the next level and then end the
	 * negotiation for the current agent.
	 * 
	 * @param current
	 *            Current agent to communicate with. Will propagate the current
	 *            agent's demands to the next level
	 * @param chosenSolution
	 *            The solution that is to be communicated to all agents
	 */
	protected Thenable<AgentVisitor.VisitorResult<Proposal, Void>> endNegotiation(AgentProxy current,
			Proposal chosenSolution) {
		Thenable<AgentVisitor.VisitorResult<Proposal, Void>> result = new Thenable<AgentVisitor.VisitorResult<Proposal, Void>>();

		// map view on bit string and primary secondary/secondary demands from
		// previous agent into message

		Map<Integer, Proposal> proposalMap = new HashMap<Integer, Proposal>();
		proposalMap.put(1, chosenSolution);
		MediatorRequest request = getMediatorRequestMessage(current.getId(), proposalMap);

		// send message to current agent asynchronously
		current.sendSolutionProposals(request).then(response -> {
			Map<Integer, Integer[]> agentDemandVariants = readDependentDemands(response, 1);

			// prepare recursion: Get full solution (not only view for previous
			// agent) and secondary demands

			Integer[] demandVariant = agentDemandVariants.get(1);
			assert demandVariant.length == Constants.Encoding.NUMBER_OF_PERIODS : "Number of secondary demands should equal number of periods";

			Proposal nextIterationSolution = new Proposal(chosenSolution.solution, demandVariant);

			// confirm solution to current agent
			// fire and forget
			EndNegotiation message = new EndNegotiation();
			message.setSolution(request.getSolutions().get(1));
			current.endNegotiation(message);

			// send solution + secondary demands of current agent to all
			// children
			result.resolve(new VisitorResult<Proposal, Void>(nextIterationSolution, null));
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

	private Map<Integer, Integer[]> readDependentDemands(AgentResponse response, int numberOfVariantsExpected) {
		// store and validate response
		Map<Integer, Integer[]> agentDemandVariants = response.getDemands();

		assert agentDemandVariants
				.size() == numberOfVariantsExpected : "Expected to receive as many secondary demand variants from agent as were sent to it";
		for (int i = 1; i <= agentDemandVariants.size(); i++) {
			assert agentDemandVariants.containsKey(i) : "Expect demands to be mapped from 1 to n";
		}
		return agentDemandVariants;
	}

	protected void registerAgentDeadListener(Collection<AgentProxy> agents) {
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
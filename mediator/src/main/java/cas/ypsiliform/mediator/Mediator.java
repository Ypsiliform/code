package cas.ypsiliform.mediator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cas.ypsiliform.Constants;
import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.mediator.negotiation.AgentProxy;
import cas.ypsiliform.mediator.negotiation.SolutionProposal;
import cas.ypsiliform.messages.MediatorRequest;
import cas.ypsiliform.messages.Solution;

public class Mediator implements Runnable {
	private final Map<Integer, AgentProxy> agents;
	private final int[] primaryDemands;
	
	public Mediator(Map<Integer, AgentProxy> agents, int[] primaryDemands) {
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
		
	}

	private void registerAgentDeadListener(Collection<AgentProxy> agents) {
		for (AgentProxy agent : agents) {
			agent.addAgentDeadListener((AgentProxy a) -> {
				// TODO abort
			});
		}
	}
	
	private Thenable<Map<Integer, Integer>> recursiveNegotiation(AgentProxy current, Map<Integer, Proposal> proposals) {		
		List<Thenable> nextIteration = new ArrayList<Thenable>();
		
		// simple Integer is not allowed: "Local variable currentAgentPreference defined in an enclosing scope must be final or effectively final"
		// stupid Java, fooled so easily
		Integer[] currentAgentPreference = {0};
		
		// map view on bit string and primary secondary/secondary demands from previous agent into message
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
			
			assert agentDemandVariants.size() == solutionsMap.size() : "Expected to receive as many secondary demand variants from agent as were sent to it";
			
			
			// prepare recursion: Get full solution proposal (not only view for previous agent) and secondary demands
			Map<Integer, Proposal> nextIterationProposals = new HashMap<Integer, Proposal>();
			
			for(int j = 0; j < response.getDemands().size(); j++) {
				Integer[] demandVariant = agentDemandVariants.get(j);
				
				assert demandVariant.length == Constants.Encoding.NUMBER_OF_PERIODS : "Number of secondary demands should equal number of periods";
				
				nextIterationProposals.put(j, new Proposal(proposals.get(j).solution, demandVariant));
			}
			
			// send solution + secondary demands of current agent to all children
			current.getChildIds().forEach(childId -> {
				nextIteration.add(recursiveNegotiation(agents.get(childId), nextIterationProposals));
			});
		});
		
		
		Thenable<Map<Integer, Integer>> result = new Thenable<Map<Integer, Integer>>();

		// collect votes from recursive calls
		Thenable.whenAll(nextIteration).then(results -> {
			Map<Integer, Integer> pref = new HashMap<Integer, Integer>();
			
			for (int i = 0; i < results.length; i++) {
				pref.putAll((Map<Integer, Integer>)results[i]);
			}
			pref.put(current.getId(), currentAgentPreference[0]);
			
			result.resolve(pref);
		});
		
		return result;
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

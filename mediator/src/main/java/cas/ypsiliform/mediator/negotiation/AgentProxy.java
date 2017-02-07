package cas.ypsiliform.mediator.negotiation;

import java.util.ArrayList;

import cas.ypsiliform.mediator.async.Thenable;
import cas.ypsiliform.messages.AgentResponse;

public class AgentProxy {
	private ArrayList<AgentDeadListener> listeners = new ArrayList<AgentDeadListener>();

	public void addAgentDeadListener(AgentDeadListener listener) {
		listeners.add(listener);
	}

	public void removeAgentDeadListener(AgentDeadListener listener) {
		listeners.remove(listener);
	}

	public Thenable<AgentResponse> sendSolutionProposal(SolutionProposal solution) {
		// TODO implementation missing
		throw new UnsupportedOperationException();
	}

	public Thenable<Void> endNegotiation(SolutionProposal solution) {
		// TODO implementation missing
		throw new UnsupportedOperationException();
	}
}

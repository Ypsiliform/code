package cas.ypsiliform.mediator.negotiation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import cas.ypsiliform.mediator.AgentProxy;
import cas.ypsiliform.mediator.async.Action;
import cas.ypsiliform.mediator.async.Function2;
import cas.ypsiliform.mediator.async.Thenable;

public class AgentVisitor {
	private static Logger log = Logger.getLogger(AgentVisitor.class.getName());

	private final Map<Integer, AgentProxy> agents;
	private final int startAgentID;

	public AgentVisitor(Map<Integer, AgentProxy> agents, int startAgentID) {
		if (!agents.containsKey(startAgentID))
			throw new IllegalArgumentException("startAgentID");

		this.agents = agents;
		this.startAgentID = startAgentID;
	}

	public <Parameter, IterationResult, Result> Thenable<Result> visit(Parameter initialParameter,
			Function2<AgentProxy, Parameter, Thenable<VisitorResult<Parameter, IterationResult>>> activity,
			Function2<IterationResult, Object[], Result> reduction) {
		return visitRecursive(agents.get(startAgentID), initialParameter, activity, reduction);
	}

	private <Parameter, IterationResult, Result> Thenable<Result> visitRecursive(AgentProxy current, Parameter param,
			Function2<AgentProxy, Parameter, Thenable<VisitorResult<Parameter, IterationResult>>> activity,
			Function2<IterationResult, Object[], Result> reduction) {

		Thenable<Result> result = new Thenable<Result>();

		List<Thenable> nextIteration = new ArrayList<Thenable>();

		activity.perform(current, param).then(currentResult -> {
			current.getChildIds().forEach(childId -> {
				log.finer("Asking agent " + childId);

				assert agents.containsKey(childId) : "Invalid reference to child agent";
				AgentProxy child = agents.get(childId);

				nextIteration.add(visitRecursive(child, currentResult.NextIterationParameter, activity, reduction));
			});

			if (nextIteration.size() == 0) {
				if (reduction == null)
					result.resolve(null);
				else
					result.resolve(reduction.perform(currentResult.CurrentIterationResult, null));
			} else {
				Thenable.whenAll(nextIteration).then((Action<Object[]>) nextIterationResult -> {
					Result r = null;

					if (reduction != null) {
						r = reduction.perform(currentResult.CurrentIterationResult, nextIterationResult);
					}

					result.resolve(r);
				});
			}
		});

		return result;
	}

	public static class VisitorResult<Parameter, Result> {
		public Parameter NextIterationParameter;
		public Result CurrentIterationResult;

		public VisitorResult(Parameter nextIterationParameter, Result currentIterationResult) {
			this.NextIterationParameter = nextIterationParameter;
			this.CurrentIterationResult = currentIterationResult;
		}
	}
}

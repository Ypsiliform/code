package cas.ypsiliform.mediator.negotiation;

public interface ReductionFunction<IntermediateResult, Result> {
	public Result reduce(IntermediateResult currentResult, Object[] previousResults);
}

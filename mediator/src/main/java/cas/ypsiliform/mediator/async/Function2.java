package cas.ypsiliform.mediator.async;

public interface Function2<T1, T2, U> {
	U perform(T1 param1, T2 param2);
}
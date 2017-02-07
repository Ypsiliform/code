package cas.ypsiliform.mediator.async;

/**
 * A function which can be registered in a thenable pipeline. T is the type of input for this
 * thenable function, U is the type of its output.
 */
public interface Function<T,U> {
    U perform(T data);
}

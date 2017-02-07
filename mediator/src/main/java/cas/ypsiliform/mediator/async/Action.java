package cas.ypsiliform.mediator.async;

/**
 * An action which can be registered at the end of a thenable pipeline.
 */
public interface Action<T> {
    void perform(T data);
}

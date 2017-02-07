package cas.ypsiliform.mediator.async;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Tool for asynchronous programming. Thenables are the glue to build a pipeline of actions
 * which are processed asynchronously. Once the first action resolves the thenable, the next
 * actions are executed.
 */
public class Thenable<T> {
    private static Timer timeout = new Timer(true);
    private LinkedList<Action<T>> actions = new LinkedList<Action<T>>();
    private T resolve;
    private boolean resolved = false;
    private boolean resolving = false;
    
    public static Thenable<Object[]> whenAll(final List<Thenable> elements) {
    	final Map<Integer, Object> values = new HashMap<Integer, Object>();
        final Thenable<Object[]> asyncReturn = new Thenable<Object[]>();

        class IterativeAction<T> implements Action<T> {
            int i;

            IterativeAction(int i) {
                this.i = i;
            }

            @Override
            public void perform(T data) {
                values.put(i, data);

                if (values.size() == elements.size()) {
                    Object[] vals = new Object[values.size()];
                    for (int i = 0; i < values.size(); i++)
                        vals[i] = values.get(i);

                    asyncReturn.resolve(vals);
                }
            }
        }

        int i = 0;
        for (Thenable element : elements) {
			element.then(new IterativeAction(i));
			i++;
		}

        return asyncReturn;
    }

    public static Thenable<Object[]> whenAll(final Thenable... elements) {
        return whenAll(Arrays.asList(elements));
    }

    public boolean isResolved() {
        return resolved;
    }

    /**
     * End the operation of the current action and send <param>data</param> to the next actions in
     * the pipeline.
     * @param data
     */
    public void resolve(T data) {
        if (resolving)
            return;

        resolving = true;

        for (Action action : actions)
            action.perform(data);

        resolve = data;
        resolved = true;
    }

    /**
     * Register an action for execution when this thenable is resolved.
     * @param action
     */
    public void then(final Action<T> action) {
        this.actions.add(action);

        if (resolved)
            action.perform(resolve);
    }

    /**
     * Register an function in the pipeline which will take an argument of type <type>T</type> and
     * emit a value of type <type>U</type> for processing by the next operations in the pipeline.
     * @param function
     * @param <U>
     * @return
     */
    public <U> Thenable<U> then(final Function<T, U> function) {
        final Thenable<U> asyncReturn = new Thenable<U>();

        this.then(new Action<T>() {
            @Override
            public void perform(T data) {
                U result = function.perform(data);
                asyncReturn.resolve(result);
            }
        });

        return asyncReturn;
    }

    /**
     * Resolve nested thenables (When a thenable function emits a thenable).
     * @param <U> must match the type of the inner thenable. There is a runtime exception otherwise.
     * @return
     */
    public <U> Thenable<U> unnest() {
        final Thenable<U> asyncReturn = new Thenable<U>();

        this.then(new Action<T>() {
            @Override
            public void perform(T data) {
                // its impossible to make sure only thenables of type <U> reach this point
                // java knows the generic types at compile time, but strips all information from
                // them at this point.
                Thenable<U> inner = (Thenable<U>) data;
                inner.then(new Action<U>() {
                    @Override
                    public void perform(U data) {
                        asyncReturn.resolve(data);
                    }
                });
            }
        });

        return asyncReturn;
    }

    public Thenable<T> timeout(int timeoutMillis, final T fallback) {
        timeout.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isResolved())
                    resolve(fallback);
            }
        }, timeoutMillis);

        return this;
    }
}

package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import java.util.Collection;

public interface Selector<T> {
    Selector<T> conjunction(Selector<T> selector);

    boolean contains(Selector<T> selector);

    boolean covers(Collection<T> entities);

    boolean covers(T entity);
}

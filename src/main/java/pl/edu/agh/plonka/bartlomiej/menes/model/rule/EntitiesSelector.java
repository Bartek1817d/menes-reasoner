package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.HashSet;

public class EntitiesSelector<T> extends HashSet<T> implements Selector<T> {

    private static final long serialVersionUID = 640758287916192919L;
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public EntitiesSelector() {
        super();
    }

    public EntitiesSelector(Collection<T> collection) {
        super(collection);
    }

    @Override
    public Selector conjunction(Selector selector) {
        if (!(selector instanceof EntitiesSelector))
            return null;
        EntitiesSelector<T> resultSelector = new EntitiesSelector<>(this);
        resultSelector.addAll((EntitiesSelector<T>) selector);
        return resultSelector;
    }

    @Override
    public boolean contains(Selector selector) {
        if (selector == null)
            return false;
        if (selector instanceof EntitiesSelector) {
            EntitiesSelector<?> nominalSelector = (EntitiesSelector) selector;
            return containsAll(nominalSelector);
        }
        return false;
    }

    @Override
    public boolean covers(Collection<T> entities) {
        if (isEmpty())
            return true;
        if (entities == null || entities.isEmpty())
            return false;
        return entities.containsAll(this);
    }

    @Override
    public boolean covers(T entity) {
        throw new NotImplementedException();
    }
}

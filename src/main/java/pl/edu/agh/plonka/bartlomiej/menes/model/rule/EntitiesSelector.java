package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.HashSet;

import static org.slf4j.LoggerFactory.getLogger;

public class EntitiesSelector extends HashSet<Entity> implements Selector<Entity> {

    private static final Logger LOG = getLogger(EntitiesSelector.class);
    private static final long serialVersionUID = 640758287916192919L;

    public EntitiesSelector() {
        super();
    }

    public EntitiesSelector(Collection<Entity> collection) {
        super(collection);
    }

    @Override
    public Selector conjunction(Selector selector) {
        if (!(selector instanceof EntitiesSelector))
            return null;
        EntitiesSelector resultSelector = new EntitiesSelector(this);
        resultSelector.addAll((EntitiesSelector) selector);
        return resultSelector;
    }

    @Override
    public boolean contains(Selector selector) {
        if (selector == null)
            return false;
        if (selector instanceof EntitiesSelector) {
            EntitiesSelector nominalSelector = (EntitiesSelector) selector;
            return containsAll(nominalSelector);
        }
        return false;
    }

    @Override
    public boolean covers(Collection<Entity> entities) {
        if (isEmpty())
            return true;
        if (entities == null || entities.isEmpty())
            return false;
        return entities.containsAll(this);
    }

    @Override
    public boolean covers(Entity entity) {
        throw new NotImplementedException();
    }
}

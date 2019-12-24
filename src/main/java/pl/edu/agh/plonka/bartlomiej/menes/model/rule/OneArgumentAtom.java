package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;

public class OneArgumentAtom<T> extends AbstractAtom {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    protected T argument;

    public OneArgumentAtom() {
    }

    public OneArgumentAtom(String predicate) {
        super(predicate);
    }

    public OneArgumentAtom(String predicate, String prefix) {
        super(predicate, prefix);
    }

    public OneArgumentAtom(String predicate, T argument) {
        super(predicate);
        this.argument = argument;
    }

    public T getArgument() {
        return argument;
    }

    public void setArgument(T argument) {
        this.argument = argument;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((argument == null) ? 0 : argument.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        OneArgumentAtom other = (OneArgumentAtom) obj;
        if (argument == null) {
            if (other.argument != null)
                return false;
        } else if (!argument.equals(other.argument))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(prefix);
        str.append(':');
        str.append(predicate);
        str.append('(');
        if (argument instanceof Entity) {
            str.append(prefix);
            str.append(':');
            str.append(((Entity) argument).getID());
        } else
            str.append(argument);
        str.append(')');
        return str.toString();
    }

}

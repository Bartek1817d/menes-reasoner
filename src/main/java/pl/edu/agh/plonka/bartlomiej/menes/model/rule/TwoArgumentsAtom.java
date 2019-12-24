package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;

public class TwoArgumentsAtom<T1, T2> extends AbstractAtom {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private T1 argument1;
    private T2 argument2;

    public TwoArgumentsAtom(String predicate) {
        super(predicate);
    }

    public TwoArgumentsAtom(String predicate, String prefix) {
        super(predicate, prefix);
    }

    public TwoArgumentsAtom(String predicate, T1 argument1, T2 argument2) {
        super(predicate);
        this.argument1 = argument1;
        this.argument2 = argument2;
    }

    public TwoArgumentsAtom(String predicate, String prefix, T1 argument1, T2 argument2) {
        super(predicate, prefix);
        this.argument1 = argument1;
        this.argument2 = argument2;
    }

    public T1 getArgument1() {
        return argument1;
    }

    public void setArgument1(T1 argument1) {
        this.argument1 = argument1;
    }

    public T2 getArgument2() {
        return argument2;
    }

    public void setArgument2(T2 argument2) {
        this.argument2 = argument2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((argument1 == null) ? 0 : argument1.hashCode());
        result = prime * result + ((argument2 == null) ? 0 : argument2.hashCode());
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
        TwoArgumentsAtom other = (TwoArgumentsAtom) obj;
        if (argument1 == null) {
            if (other.argument1 != null)
                return false;
        } else if (!argument1.equals(other.argument1))
            return false;
        if (argument2 == null) {
            if (other.argument2 != null)
                return false;
        } else if (!argument2.equals(other.argument2))
            return false;
        return true;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(prefix);
        str.append(':');
        str.append(predicate);
        str.append('(');
        if (argument1 instanceof Entity) {
            str.append(prefix);
            str.append(':');
            str.append(((Entity) argument1).getID());
        } else
            str.append(argument1);
        str.append(", ");
        if (argument2 instanceof Entity) {
            str.append(prefix);
            str.append(':');
            str.append(((Entity) argument2).getID());
        } else
            str.append(argument2);
        str.append(')');
        return str.toString();
    }

}

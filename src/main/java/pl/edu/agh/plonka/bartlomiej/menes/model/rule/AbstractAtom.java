package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAtom {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    protected String prefix = "base";
    protected String predicate;

    public AbstractAtom() {
    }

    public AbstractAtom(String predicate) {
        this.predicate = predicate;
    }

    public AbstractAtom(String predicate, String prefix) {
        this.predicate = predicate;
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractAtom other = (AbstractAtom) obj;
        if (predicate == null) {
            if (other.predicate != null)
                return false;
        } else if (!predicate.equals(other.predicate))
            return false;
        if (prefix == null) {
            if (other.prefix != null)
                return false;
        } else if (!prefix.equals(other.prefix))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return prefix + ':' + predicate + "()";
    }
}

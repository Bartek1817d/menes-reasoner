package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;

public class Variable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private String name;
    private Entity parentClass;

    public Variable(String name) {
        this.name = name;
    }

    public Variable(String name, Entity parentClass) {
        this.name = name;
        this.parentClass = parentClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Entity getParentClass() {
        return parentClass;
    }

    public void setParentClass(Entity parentClass) {
        this.parentClass = parentClass;
    }

    @Override
    public String toString() {
        return '?' + name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Variable other = (Variable) obj;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

}

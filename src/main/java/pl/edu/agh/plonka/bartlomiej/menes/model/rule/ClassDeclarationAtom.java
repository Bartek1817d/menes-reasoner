package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;

import static org.slf4j.LoggerFactory.getLogger;

public class ClassDeclarationAtom<T> extends OneArgumentAtom<T> {

    private final Logger LOG = getLogger(ClassDeclarationAtom.class);

    private Entity classEntity;

    public ClassDeclarationAtom(Entity classEntity) {
        this.classEntity = classEntity;
    }

    public ClassDeclarationAtom(Entity classEntity, String prefix) {
        this.classEntity = classEntity;
        this.prefix = prefix;
    }

    public ClassDeclarationAtom(Entity classEntity, T argument) {
        super(classEntity.getID());
        this.classEntity = classEntity;
        this.argument = argument;
    }

    public Entity getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(Entity classEntity) {
        this.classEntity = classEntity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((classEntity == null) ? 0 : classEntity.hashCode());
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
        ClassDeclarationAtom other = (ClassDeclarationAtom) obj;
        if (classEntity == null) {
            if (other.classEntity != null)
                return false;
        } else if (!classEntity.equals(other.classEntity))
            return false;
        return true;
    }
}

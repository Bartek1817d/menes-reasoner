package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OntologyClass extends Entity {

    private final Set<Entity> instances = new HashSet<>();

    public OntologyClass(String id) {
        super(id);
    }

    public Set<Entity> getInstances() {
        return instances;
    }

    public void addInstance(Entity instance) {
        this.instances.add(instance);
    }

    public void addInstances(Collection<Entity> instances) {
        this.instances.addAll(instances);
    }
}

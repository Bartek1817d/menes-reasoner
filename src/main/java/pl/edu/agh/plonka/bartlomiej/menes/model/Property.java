package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.HashSet;
import java.util.Set;

public class Property<T> extends Entity {
    protected final Set<Entity> domain = new HashSet<>();
    protected final Set<T> range = new HashSet<>();
}

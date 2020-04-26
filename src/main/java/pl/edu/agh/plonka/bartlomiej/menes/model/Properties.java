package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.HashSet;
import java.util.Set;

public class Properties {
    public Set<Property> stringProperties = new HashSet<>();
    public Set<Property> booleanProperties = new HashSet<>();
    public Set<NumericProperty> numericProperties = new HashSet<>();
    public Set<ObjectProperty> entityProperties = new HashSet<>();
}

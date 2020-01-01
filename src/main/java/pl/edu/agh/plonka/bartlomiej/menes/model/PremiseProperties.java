package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.Set;

public class PremiseProperties {

    public Set<IntegerProperty> integerProperties;
    public Set<ObjectProperty> objectProperties;

    public PremiseProperties(Set<IntegerProperty> integerProperties, Set<ObjectProperty> objectProperties) {
        this.integerProperties = integerProperties;
        this.objectProperties = objectProperties;
    }

}

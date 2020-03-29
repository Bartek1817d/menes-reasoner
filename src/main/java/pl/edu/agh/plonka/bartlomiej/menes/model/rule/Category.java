package pl.edu.agh.plonka.bartlomiej.menes.model.rule;

import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

import java.util.Set;

import static java.lang.String.format;

public class Category {

    private final Entity entity;
    private final String predicate;

    public Category(Entity entity, String predicate) {
        this.entity = entity;
        this.predicate = predicate;
    }

    public boolean assertPatientInCategory(Patient patient) {
        Set<Entity> values = patient.getEntityProperties().get(predicate);
        Set<Entity> inferredValues = patient.getInferredEntityProperties().get(predicate);

        //TODO support for integers etc.
        return values != null && values.contains(entity) || inferredValues != null && inferredValues.contains(entity);
    }

    public void setPatientCategory(Patient patient) {
        //TODO support for integers etc.
        patient.setEntityProperty(predicate, entity);
    }

    public Entity getEntity() {
        return entity;
    }

    public String getPredicate() {
        return predicate;
    }

    @Override
    public String toString() {
        return format("%s=%s", predicate, entity);
    }
}

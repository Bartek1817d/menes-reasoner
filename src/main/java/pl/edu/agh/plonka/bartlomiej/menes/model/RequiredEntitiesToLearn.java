package pl.edu.agh.plonka.bartlomiej.menes.model;

import java.util.Map;
import java.util.Set;

public class RequiredEntitiesToLearn {
    private final Set<String> stringProperties;
    private final Set<String> integerProperties;
    private final Set<String> entityProperties;


    public RequiredEntitiesToLearn(Set<String> stringProperties, Set<String> integerProperties, Set<String> entityProperties) {
        this.stringProperties = stringProperties;
        this.integerProperties = integerProperties;
        this.entityProperties = entityProperties;
    }

    public boolean invalidPatient(Patient patient) {
        return invalidStringProperties(patient) || invalidIntegerProperties(patient) || invalidEntityProperties(patient);
    }

    private boolean invalidStringProperties(Patient patient) {
        return invalidProperties(stringProperties, patient.getStringProperties(), patient.getInferredStringProperties());
    }

    private boolean invalidIntegerProperties(Patient patient) {
        return invalidProperties(integerProperties, patient.getIntegerProperties(), patient.getInferredIntegerProperties());
    }

    private boolean invalidEntityProperties(Patient patient) {
        return invalidProperties(entityProperties, patient.getStringProperties(), patient.getInferredStringProperties());
    }

    private <T> boolean invalidProperties(Set<String> propertyNames, Map<String, Set<T>> properties, Map<String, Set<T>> inferredProperties) {
        if (propertyNames == null || propertyNames.isEmpty())
            return false;
        return propertyNames.stream().anyMatch(property -> invalidProperty(property, properties, inferredProperties));
    }

    private <T> boolean invalidProperty(String propertyName, Map<String, Set<T>> properties, Map<String, Set<T>> inferredProperties) {
        Set<?> values = properties.get(propertyName);
        if (values != null && !values.isEmpty())
            return false;
        else {
            Set<?> inferredValues = inferredProperties.get(propertyName);
            return inferredValues == null || inferredValues.isEmpty();
        }
    }
}

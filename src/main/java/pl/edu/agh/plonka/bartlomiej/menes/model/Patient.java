package pl.edu.agh.plonka.bartlomiej.menes.model;

import org.slf4j.Logger;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.FIRST_NAME_PROPERTY;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.LAST_NAME_PROPERTY;

/**
 * Model class for a Patient.
 *
 * @author Bartłomiej Płonka
 */
public class Patient extends Entity implements Comparable<Patient> {

    private static final Logger LOG = getLogger(Patient.class);

    private final Map<String, Set<String>> stringProperties = new HashMap<>();
    private final Map<String, Set<Integer>> integerProperties = new HashMap<>();
    private final Map<String, Set<Entity>> entityProperties = new HashMap<>();

    private final Map<String, Set<String>> inferredStringProperties = new HashMap<>();
    private final Map<String, Set<Integer>> inferredIntegerProperties = new HashMap<>();
    private final Map<String, Set<Entity>> inferredEntityProperties = new HashMap<>();

    private float evaluation;

    public Patient() {
        super();
    }

    /**
     * Default constructor.
     */
    public Patient(String id) {
        super(id);
    }

    /**
     * Constructor with some initial data.
     *
     * @param firstName
     * @param lastName
     */
    public Patient(String id, String firstName, String lastName) {
        super(id);
        setStringProperty(FIRST_NAME_PROPERTY, firstName);
        setStringProperty(LAST_NAME_PROPERTY, lastName);
    }

    public float getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(float evaluation) {
        this.evaluation = evaluation;
    }

    public String getFirstName() {
        return getStringProperty(FIRST_NAME_PROPERTY);
    }

    public void setFirstName(String firstName) {
        setStringProperty(FIRST_NAME_PROPERTY, firstName);
    }

    public String getLastName() {
        return getStringProperty(LAST_NAME_PROPERTY);
    }

    public void setLastName(String lastName) {
        setStringProperty(LAST_NAME_PROPERTY, lastName);
    }

    /**
     * if evaluation1 > evaluation2 -> patient1 > patient2
     */
    @Override
    public int compareTo(Patient patient) {
        return (int) Math.signum(evaluation - patient.evaluation);
    }

    public String getStringProperty(String propertyName) {
        return getProperty(stringProperties, propertyName);
    }

    public Integer getIntegerProperty(String propertyName) {
        return getProperty(integerProperties, propertyName);
    }

    public Entity getEntityProperty(String propertyName) {
        return getProperty(entityProperties, propertyName);
    }

    public void setStringProperty(String propertyName, String value) {
        setProperty(stringProperties, propertyName, value);
    }

    public void setIntegerProperty(String propertyName, Integer value) {
        setProperty(integerProperties, propertyName, value);
    }

    public void setEntityProperty(String propertyName, Entity value) {
        setProperty(entityProperties, propertyName, value);
    }

    public void setStringProperties(String propertyName, Collection<String> values) {
        setProperties(stringProperties, propertyName, values);
    }

    public void setIntegerProperties(String propertyName, Collection<Integer> values) {
        setProperties(integerProperties, propertyName, values);
    }

    public void setEntityProperties(String propertyName, Collection<Entity> values) {
        setProperties(entityProperties, propertyName, values);
    }

    private <T> T getProperty(Map<String, Set<T>> propertyMap, String propertyName) {
        Set<T> values = propertyMap.get(propertyName);
        if (values != null)
            return values.stream().findAny().orElse(null);
        else
            return null;
    }

    private <T> void setProperties(Map<String, Set<T>> propertyMap, String propertyName, Collection<T> values) {
        if (propertyMap.containsKey(propertyName))
            propertyMap.get(propertyName).addAll(values);
        else {
            HashSet<T> container = new HashSet<T>(values);
            propertyMap.put(propertyName, container);
        }
    }

    private <T> void setProperty(Map<String, Set<T>> propertyMap, String propertyName, T value) {
        setProperties(propertyMap, propertyName, singleton(value));
    }

    public Map<String, Set<String>> getStringProperties() {
        return stringProperties;
    }

    public Set<String> getStringProperties(String propertyName) {
        return stringProperties.get(propertyName);
    }

    public Map<String, Set<Integer>> getIntegerProperties() {
        return integerProperties;
    }

    public Set<Integer> getIntegerProperties(String propertyName) {
        return integerProperties.get(propertyName);
    }

    public Map<String, Set<Entity>> getEntityProperties() {
        return entityProperties;
    }

    public Set<Entity> getEntityProperties(String propertyName) {
        Set<Entity> properties = entityProperties.get(propertyName);
        if (properties == null)
            return emptySet();
        else
            return properties;
    }

    public Map<String, Set<String>> getInferredStringProperties() {
        return inferredStringProperties;
    }

    public Set<String> getInferredStringProperties(String propertyName) {
        return inferredStringProperties.get(propertyName);
    }

    public Map<String, Set<Integer>> getInferredIntegerProperties() {
        return inferredIntegerProperties;
    }

    public Set<Integer> getInferredIntegerProperties(String propertyName) {
        return inferredIntegerProperties.get(propertyName);
    }

    public Map<String, Set<Entity>> getInferredEntityProperties() {
        return inferredEntityProperties;
    }

    public Set<Entity> getInferredEntityProperties(String propertyName) {
        return inferredEntityProperties.get(propertyName);
    }

    public void setInferredStringProperties(String propertyName, Collection<String> values) {
        setProperties(inferredStringProperties, propertyName, values);
    }

    public void setInferredIntegerProperties(String propertyName, Collection<Integer> values) {
        setProperties(inferredIntegerProperties, propertyName, values);
    }

    public void setInferredEntityProperties(String propertyName, Collection<Entity> values) {
        setProperties(inferredEntityProperties, propertyName, values);
    }
}
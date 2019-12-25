package pl.edu.agh.plonka.bartlomiej.menes.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.FIRST_NAME_PROPERTY;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.LAST_NAME_PROPERTY;

/**
 * Model class for a Patient.
 *
 * @author Bartłomiej Płonka
 */
public class Patient extends Entity implements Comparable<Patient> {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final Map<String, Set<String>> stringProperties = new HashMap<>();
    private final Map<String, Set<Integer>> integerProperties = new HashMap<>();
    private final Map<String, Set<Entity>> entityProperties = new HashMap<>();

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
        putStringProperty(FIRST_NAME_PROPERTY, firstName);
        putStringProperty(LAST_NAME_PROPERTY, lastName);
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
        putStringProperty(FIRST_NAME_PROPERTY, firstName);
    }

    public String getLastName() {
        return getStringProperty(LAST_NAME_PROPERTY);
    }

    public void setLastName(String lastName) {
        putStringProperty(LAST_NAME_PROPERTY, lastName);
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

    public void putStringProperty(String propertyName, String value) {
        putProperty(stringProperties, propertyName, value);
    }

    public void putIntegerProperty(String propertyName, Integer value) {
        putProperty(integerProperties, propertyName, value);
    }

    public void putEntityProperty(String propertyName, Entity value) {
        putProperty(entityProperties, propertyName, value);
    }

    private <T> T getProperty(Map<String, Set<T>> propertyMap, String propertyName) {
        Set<T> values = propertyMap.get(propertyName);
        if (values != null)
            return values.stream().findAny().orElse(null);
        else
            return null;
    }

    private <T> void putProperty(Map<String, Set<T>> propertyMap, String propertyName, T value) {
        if (propertyMap.containsKey(propertyName))
            propertyMap.get(propertyName).add(value);
        else {
            HashSet<T> values = new HashSet<T>();
            values.add(value);
            propertyMap.put(propertyName, values);
        }
    }
}
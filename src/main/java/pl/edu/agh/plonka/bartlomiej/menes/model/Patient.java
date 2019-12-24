package pl.edu.agh.plonka.bartlomiej.menes.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Model class for a Patient.
 *
 * @author Bartłomiej Płonka
 */
public class Patient extends Entity implements Comparable<Patient> {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final IntegerProperty age = new SimpleIntegerProperty(-1);
    private final IntegerProperty height = new SimpleIntegerProperty(-1);
    private final IntegerProperty weight = new SimpleIntegerProperty(-1);
    private final StringProperty placeOfResidence = new SimpleStringProperty();
    private final ObservableList<Entity> symptoms = FXCollections.observableArrayList();
    private final ObservableList<Entity> inferredSymptoms = FXCollections.observableArrayList();
    private final ObservableList<Entity> diseases = FXCollections.observableArrayList();
    private final ObservableList<Entity> inferredDiseases = FXCollections.observableArrayList();
    private final ObservableList<Entity> tests = FXCollections.observableArrayList();
    private final ObservableList<Entity> inferredTests = FXCollections.observableArrayList();
    private final ObservableList<Entity> treatments = FXCollections.observableArrayList();
    private final ObservableList<Entity> inferredTreatments = FXCollections.observableArrayList();
    private final ObservableList<Entity> causes = FXCollections.observableArrayList();
    private final ObservableList<Entity> inferredCauses = FXCollections.observableArrayList();
    private final ObservableList<Entity> negativeTests = FXCollections.observableArrayList();
    private final ObservableList<Entity> inferredNegativeTests = FXCollections.observableArrayList();
    private final ObservableList<Entity> previousDiseases = FXCollections.observableArrayList();
    private final ObservableList<Entity> inferredPreviousDiseases = FXCollections.observableArrayList();


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
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }

    public float getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(float evaluation) {
        this.evaluation = evaluation;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty getObservableFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty getObservableLastName() {
        return lastName;
    }

    public int getAge() {
        return age.get();
    }

    public void setAge(Integer age) {
        this.age.set(age);
    }

    public IntegerProperty getObservableAge() {
        return age;
    }

    public int getHeight() {
        return height.get();
    }

    public void setHeight(int height) {
        this.height.set(height);
    }

    public IntegerProperty getObservableHeight() {
        return height;
    }

    public int getWeight() {
        return weight.get();
    }

    public void setWeight(int weight) {
        this.weight.set(weight);
    }

    public IntegerProperty getObservableWeight() {
        return weight;
    }

    public String getPlaceOfResidence() {
        return placeOfResidence.get();
    }

    public void setPlaceOfResidence(String placeOfResidence) {
        this.placeOfResidence.set(placeOfResidence);
    }

    public StringProperty getObservablePlaceOfResidence() {
        return placeOfResidence;
    }

    public ObservableList<Entity> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(Collection<Entity> symptoms) {
        this.symptoms.setAll(symptoms);
    }

    public void addSymptoms(Collection<Entity> symptoms) {
        this.symptoms.addAll(symptoms);
    }

    public void addSymptom(Entity symptom) {
        this.symptoms.add(symptom);
    }

    public ObservableList<Entity> getInferredSymptoms() {
        return inferredSymptoms;
    }

    public void setInferredSymptoms(Collection<Entity> inferredSymptoms) {
        this.inferredSymptoms.setAll(inferredSymptoms);
    }

    public void addInferredSymptoms(Collection<Entity> inferredSymptoms) {
        this.inferredSymptoms.addAll(inferredSymptoms);
    }

    public void addInferredSymptom(Entity inferredSymptom) {
        this.symptoms.add(inferredSymptom);
    }

    public ObservableList<Entity> getDiseases() {
        return diseases;
    }

    public void setDiseases(Collection<Entity> diseases) {
        this.diseases.setAll(diseases);
    }

    public void addDiseases(Collection<Entity> diseases) {
        this.diseases.addAll(diseases);
    }

    public void addDisease(Entity disease) {
        this.diseases.add(disease);
    }

    public void removeAllDiseases() {
        this.diseases.clear();
    }

    public void removeAllCauses() {
        this.causes.clear();
    }

    public void removeAllTests() {
        this.tests.clear();
    }

    public void removeAllTreatments() {
        this.treatments.clear();
    }

    public ObservableList<Entity> getInferredDiseases() {
        return inferredDiseases;
    }

    public void setInferredDiseases(Collection<Entity> inferredDiseases) {
        this.inferredDiseases.setAll(inferredDiseases);
    }

    public void addInferredDiseases(Collection<Entity> inferredDieseases) {
        this.diseases.addAll(diseases);
    }

    public void addInferredDisease(Entity inferredDiesease) {
        this.diseases.add(inferredDiesease);
    }

    public ObservableList<Entity> getTests() {
        return tests;
    }

    public void setTests(Collection<Entity> tests) {
        this.tests.setAll(tests);
    }

    public void addTests(Collection<Entity> tests) {
        this.tests.addAll(tests);
    }

    public void addTest(Entity test) {
        this.tests.add(test);
    }

    public ObservableList<Entity> getInferredTests() {
        return inferredTests;
    }

    public void setInferredTests(Collection<Entity> inferredTests) {
        this.inferredTests.setAll(inferredTests);
    }

    public void addInferredTests(Collection<Entity> inferredTests) {
        this.inferredTests.addAll(inferredTests);
    }

    public void addInferredTest(Entity inferredTest) {
        this.inferredTests.add(inferredTest);
    }

    public ObservableList<Entity> getTreatments() {
        return treatments;
    }

    public void setTreatments(Collection<Entity> treatments) {
        this.treatments.setAll(treatments);
    }

    public void addTreatments(Collection<Entity> treatments) {
        this.treatments.addAll(treatments);
    }

    public void addTreatment(Entity treatment) {
        this.treatments.add(treatment);
    }

    public ObservableList<Entity> getInferredTreatments() {
        return inferredTreatments;
    }

    public void setInferredTreatments(Collection<Entity> inferredTreatments) {
        this.inferredTreatments.setAll(inferredTreatments);
    }

    public void addInferredTreatments(Collection<Entity> inferredTreatments) {
        this.inferredTreatments.addAll(inferredTreatments);
    }

    public void addInferredTreatment(Entity inferredTreatment) {
        this.inferredTreatments.add(inferredTreatment);
    }

    public ObservableList<Entity> getCauses() {
        return causes;
    }

    public void setCauses(Collection<Entity> causes) {
        this.causes.setAll(causes);
    }

    public void addCauses(Collection<Entity> causes) {
        this.causes.addAll(causes);
    }

    public void addCause(Entity cause) {
        this.causes.add(cause);
    }

    public ObservableList<Entity> getInferredCauses() {
        return inferredCauses;
    }

    public void setInferredCauses(Collection<Entity> inferredCauses) {
        this.inferredCauses.setAll(inferredCauses);
    }

    public void addInferredCauses(Collection<Entity> inferredCauses) {
        this.inferredCauses.addAll(inferredCauses);
    }

    public void addInferredCause(Entity inferredCause) {
        this.inferredCauses.add(inferredCause);
    }

    public ObservableList<Entity> getNegativeTests() {
        return negativeTests;
    }

    public void setNegativeTests(Collection<Entity> negativeTests) {
        this.negativeTests.setAll(negativeTests);
    }

    public void addNegativeTests(Collection<Entity> negativeTests) {
        this.negativeTests.addAll(negativeTests);
    }

    public void addNegativeTest(Entity negativeTest) {
        this.negativeTests.add(negativeTest);
    }

    public ObservableList<Entity> getPreviousDiseases() {
        return previousDiseases;
    }

    public void setPreviousDiseases(Collection<Entity> previousDiseases) {
        this.previousDiseases.setAll(previousDiseases);
    }

    public void addPreviousDiseases(Collection<Entity> previousAndCurrentDiseases) {
        this.previousDiseases.addAll(previousAndCurrentDiseases);
    }

    public void addPreviousOrCurrentDisease(Entity previousAndCurrentDisease) {
        this.previousDiseases.add(previousAndCurrentDisease);
    }

    public ObservableList<Entity> getInferredNegativeTests() {
        return inferredNegativeTests;
    }

    public void addInferredNegativeTests(Collection<Entity> inferredNegativeTests) {
        this.inferredNegativeTests.addAll(inferredNegativeTests);
    }

    public ObservableList<Entity> getInferredPreviousDiseases() {
        return inferredPreviousDiseases;
    }

    public void addInferredPreviousDiseases(Collection<Entity> inferredPreviousDiseases) {
        this.inferredPreviousDiseases.addAll(inferredPreviousDiseases);
    }

    public String toString() {
        return firstName.get() + ' ' + lastName.get();
    }

    /**
     * if evaluation1 > evaluation2 -> patient1 > patient2
     */
    @Override
    public int compareTo(Patient patient) {
        return (int) Math.signum(evaluation - patient.evaluation);
    }
}
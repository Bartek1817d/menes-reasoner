package pl.edu.agh.plonka.bartlomiej.menes.service;

import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;

import java.util.Set;

public class MachineLearningInput<T> {
    public Set<Patient> trainingSet;
    public Set<TestPatient> testSet;

    public MachineLearningInput(Set<Patient> trainingSet, Set<TestPatient> testSet) {
        this.trainingSet = trainingSet;
        this.testSet = testSet;
    }
}

package pl.edu.agh.plonka.bartlomiej.menes.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.exception.CreateRuleException;
import pl.edu.agh.plonka.bartlomiej.menes.exception.RuleAlreadyExistsException;
import pl.edu.agh.plonka.bartlomiej.menes.model.Entity;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;
import pl.edu.agh.plonka.bartlomiej.menes.model.RequiredEntitiesToLearn;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Rule;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.GENERATED_RULE_PREFIX;

public class PatientsService {

    private static final Logger LOG = getLogger(PatientsService.class);

    private OntologyWrapper ontology;
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<Rule> rules = FXCollections.observableArrayList();

    public PatientsService(String url, Set<String> integerProperties, Set<String> stringProperties, Set<String> entityProperties) throws OWLOntologyCreationException {
        createKnowledgeBase(url, integerProperties, stringProperties, entityProperties);
    }

    public PatientsService(File file, Set<String> integerProperties, Set<String> stringProperties, Set<String> entityProperties) throws OWLOntologyCreationException {
        createKnowledgeBase(file, integerProperties, stringProperties, entityProperties);
    }

    public void createKnowledgeBase(String url, Set<String> integerProperties, Set<String> stringProperties, Set<String> entityProperties) throws OWLOntologyCreationException {
        ontology = new OntologyWrapper(url, integerProperties, stringProperties, entityProperties);
        patients.clear();
        rules.clear();
    }

    public void createKnowledgeBase(File file, Set<String> integerProperties, Set<String> stringProperties, Set<String> entityProperties) throws OWLOntologyCreationException {
        ontology = new OntologyWrapper(file, integerProperties, stringProperties, entityProperties);
        patients.setAll(ontology.getPatients());
        rules.setAll(ontology.getRules());
    }

    public void saveKnowledgeBase(File file) throws OWLOntologyStorageException {
        ontology.saveOntologyToFile(file);
    }

    public ObservableList<Patient> getPatients() {
        return patients;
    }

    public void addPatient(Patient patient) {
        ontology.addPatient(patient);
        patients.add(patient);
    }

    public void addPatients(Collection<Patient> patients) {
        patients.forEach(p -> {
            ontology.addPatient(p);
            this.patients.add(p);
        });

    }

    public void deletePatient(Patient patient) {
        patients.remove(patient);
        ontology.deleteEntity(patient);
    }

    public void deletePatients(Collection<Patient> patients) {
        this.patients.removeAll(patients);
        ontology.deletePatients(patients);
    }

    public Collection<Patient> updatePatients(Collection<Patient> patients) {
        patients.forEach(p -> ontology.updatePatient(p));
        return patients;
    }

    public ObservableList<Rule> getRules() {
        return rules;
    }

    public void addRule(Rule rule) throws RuleAlreadyExistsException, CreateRuleException {
        if (rules.contains(rule))
            throw new RuleAlreadyExistsException(rule);
        ontology.addRule(rule);
        rules.add(rule);
        updatePatients(patients);
    }

    public void addRules(Collection<Rule> rules) throws RuleAlreadyExistsException, CreateRuleException {
        for (Rule rule : rules)
            addRule(rule);
    }

    public void deleteRule(Rule rule) {
        this.ontology.deleteRule(rule);
        this.rules.remove(rule);
        updatePatients(patients);
    }

    public void deleteRules(Collection<Rule> rules) {
        this.ontology.deleteRules(rules);
        this.rules.removeAll(rules);
        updatePatients(patients);
    }

    public void editPatient(Patient patient) {
        ontology.updatePatient(patient);
    }

    public OntologyWrapper getOntology() {
        return ontology;
    }

    public void changeLanguage() {
        ontology.changeLanguage();
    }

    public void infer(RequiredEntitiesToLearn requiredEntities, MachineLearning machineLearning, Map<String, Collection<String>> predicateClassCategories) throws Throwable {
        Collection<Patient> invalidPatients = patients.stream()
                .map(ontology::getInferredPatient)
                .filter(requiredEntities::invalidPatient)
                .collect(Collectors.toSet());
        if (!invalidPatients.isEmpty()) {
            Set<Patient> trainingSet = new HashSet<>(getPatients());
            trainingSet.removeAll(invalidPatients);
            learnNewRules(machineLearning, trainingSet, predicateClassCategories);
            patients.forEach(ontology::getInferredPatient);
        }
    }

    public void learnNewRules(MachineLearning machineLearning, Map<String, Collection<String>> predicateClassCategories) throws Throwable {
        learnNewRules(machineLearning, new HashSet<>(patients), predicateClassCategories);
    }

    public void learnNewRules(MachineLearning machineLearning, Set<Patient> trainingSet, Map<String, Collection<String>> predicateClassCategories) throws Throwable {
        Map<String, Collection<Entity>> predicateEntityCategories = mapCategoryClassToEntity(predicateClassCategories);
        Collection<Rule> newGeneratedRules = machineLearning.sequentialCovering(trainingSet, predicateEntityCategories);
        Set<Rule> oldGeneratedRules = getRules()
                .stream()
                .filter(this::isGeneratedRule)
                .collect(toSet());
        deleteRules(oldGeneratedRules);
        addRules(newGeneratedRules);
    }

    private Map<String, Collection<Entity>> mapCategoryClassToEntity(Map<String, Collection<String>> predicateClassCategories) {
        HashMap<String, Collection<Entity>> predicateEntityCategories = new HashMap<>();
        predicateClassCategories.forEach((predicate, classes) -> {
            Set<Entity> entities = classes
                    .stream()
                    .map(ontology::getClassInstances)
                    .flatMap(Collection::stream)
                    .collect(toSet());
            predicateEntityCategories.put(predicate, entities);
        });
        return predicateEntityCategories;
    }

    private boolean isGeneratedRule(Rule rule) {
        return rule.getName().trim().startsWith(GENERATED_RULE_PREFIX);
    }
}

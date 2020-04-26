package pl.edu.agh.plonka.bartlomiej.menes.service;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import pl.edu.agh.plonka.bartlomiej.menes.exception.CreateRuleException;
import pl.edu.agh.plonka.bartlomiej.menes.exception.RuleAlreadyExistsException;
import pl.edu.agh.plonka.bartlomiej.menes.model.ObjectProperty;
import pl.edu.agh.plonka.bartlomiej.menes.model.Patient;
import pl.edu.agh.plonka.bartlomiej.menes.model.RequiredEntitiesToLearn;
import pl.edu.agh.plonka.bartlomiej.menes.model.rule.Rule;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;
import static pl.edu.agh.plonka.bartlomiej.menes.utils.Constants.GENERATED_RULE_PREFIX;

public class PatientsService {

    private static final Logger LOG = getLogger(PatientsService.class);

    private OntologyWrapper ontology;
    private MachineLearning machineLearning;
    private Set<Patient> patients;
    private Set<Rule> rules;

    public PatientsService(String url) throws OWLOntologyCreationException {
        createKnowledgeBase(url);
    }

    public PatientsService(File file, MachineLearning machineLearning) throws OWLOntologyCreationException {
        createKnowledgeBase(file);
        this.machineLearning = machineLearning;
    }

    public PatientsService(OntologyWrapper ontology, MachineLearning machineLearning) {
        this.ontology = ontology;
        this.machineLearning = machineLearning;
        this.patients = new HashSet<>(ontology.getPatients());
        this.rules = new HashSet<>(ontology.getRules());
    }

    public void createKnowledgeBase(String url) throws OWLOntologyCreationException {
        ontology = new OntologyWrapper(url);
        patients = new HashSet<>();
        rules = new HashSet<>();
    }

    public void createKnowledgeBase(File file) throws OWLOntologyCreationException {
        ontology = new OntologyWrapper(file);
        patients = new HashSet<>(ontology.getPatients());
        rules = new HashSet<>(ontology.getRules());
    }

    public void saveKnowledgeBase(File file) throws OWLOntologyStorageException {
        ontology.saveOntologyToFile(file);
    }

    public Set<Patient> getPatients() {
        return new HashSet<>(patients);
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

    public void deleteAllPatients() {
        patients.forEach(ontology::deleteEntity);
        patients.clear();
    }

    public void deletePatients(Collection<Patient> patients) {
        this.patients.removeAll(patients);
        ontology.deletePatients(patients);
    }

    public Collection<Patient> updatePatients(Collection<Patient> patients) {
        patients.forEach(p -> ontology.updatePatient(p));
        return patients;
    }

    public Set<Rule> getRules() {
        return new HashSet<>(rules);
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

    public void deleteAllRules() {
        rules.clear();
        ontology.deleteRules();
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

    public void infer() {
        patients.forEach(ontology::getInferredPatient);
    }

    public void infer(RequiredEntitiesToLearn requiredEntities, Set<ObjectProperty> predicateCategories) throws Throwable {
        Collection<Patient> invalidPatients = patients.stream()
                .map(ontology::getInferredPatient)
                .filter(requiredEntities::invalidPatient)
                .collect(Collectors.toSet());
        if (!invalidPatients.isEmpty()) {
            Set<Patient> trainingSet = new HashSet<>(getPatients());
            trainingSet.removeAll(invalidPatients);
            learnNewRules(trainingSet, predicateCategories);
            patients.forEach(ontology::getInferredPatient);
        }
    }

    public Set<Rule> learnNewRules(Set<ObjectProperty> predicateCategories) throws Throwable {
        return learnNewRules(new HashSet<>(patients), predicateCategories);
    }

    public Set<Rule> learnNewRules(Set<Patient> trainingSet, Set<ObjectProperty> predicateCategories) throws Throwable {
        Collection<Rule> newGeneratedRules = machineLearning.sequentialCovering(trainingSet, predicateCategories);
        Set<Rule> oldGeneratedRules = getRules()
                .stream()
                .filter(this::isGeneratedRule)
                .collect(toSet());
        deleteRules(oldGeneratedRules);
        addRules(newGeneratedRules);
        return new HashSet<>(rules);
    }


    private boolean isGeneratedRule(Rule rule) {
        return rule.getName().trim().startsWith(GENERATED_RULE_PREFIX);
    }
}
